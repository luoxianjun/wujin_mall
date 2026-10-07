<template>
  <div :class="['message-input', !isPC && 'message-input-h5']">
    <div class="audio-main-content-line">
      <MessageInputAudio
        v-if="(isWeChat || isApp) && isRenderVoice"
        :class="{
          'message-input-wx-audio-open': displayType === 'audio',
        }"
        :isEnableAudio="displayType === 'audio'"
        @changeDisplayType="changeDisplayType"
      />
      <MessageInputEditor
        v-show="displayType === 'editor'"
        ref="editor"
        class="message-input-editor"
        :placeholder="props.placeholder"
        :isMuted="props.isMuted"
        :muteText="props.muteText"
        :enableInput="props.enableInput"
        :enableAt="props.enableAt"
        :enableTyping="props.enableTyping"
        :isGroup="isGroup"
        :inputToolbarDisplayType="inputToolbarDisplayType"
        @onTyping="onTyping"
        @onAt="onAt"
        @onFocus="onFocus"
      />
      <MessageInputAt
        v-if="props.enableAt"
        ref="messageInputAtRef"
        @insertAt="insertAt"
        @onAtListOpen="onAtListOpen"
      />
      <Icon
        v-if="isRenderEmojiPicker"
        class="icon icon-face"
        :file="faceIcon"
        :size="'23px'"
        :hotAreaSize="'3px'"
        @onClick="changeToolbarDisplayType('emojiPicker')"
      />
      <view
        v-if="isWeChat && !inputHasContent"
        :class="['speech-btn', isRecording && 'speech-btn-recording']"
        @click="toggleSpeechToText"
      >
        <text class="speech-btn-icon">🎙</text>
      </view>
      <Icon
        v-if="isRenderMore && !inputHasContent"
        class="icon icon-more"
        :file="moreIcon"
        :size="'23px'"
        :hotAreaSize="'3px'"
        @onClick="changeToolbarDisplayType('tools')"
      />
      <view
        v-if="inputHasContent"
        class="send-btn"
        @click="handleSendMessage"
      >
        <text class="send-btn-text">发送</text>
      </view>
    </div>
    <div>
      <MessageQuote
        :style="{minWidth: 0}"
        :displayType="displayType"
      />
    </div>
  </div>
</template>
<script setup lang="ts">
import TUIChatEngine, {
  TUIStore,
  StoreName,
  IMessageModel,
  IConversationModel,
} from '@tencentcloud/chat-uikit-engine-lite';
import { ref, watch, onMounted, onUnmounted } from '../../../adapter-vue';
import MessageInputEditor from './message-input-editor.vue';
import MessageInputAt from './message-input-at/index.vue';
import MessageInputAudio from './message-input-audio.vue';
import MessageQuote from './message-input-quote/index.vue';
import Icon from '../../common/Icon.vue';
import faceIcon from '../../../assets/icon/face-uni.png';
import moreIcon from '../../../assets/icon/more-uni.png';
import { isPC, isH5, isWeChat, isApp } from '../../../utils/env';
import { sendTyping } from '../utils/sendMessage';
import { ToolbarDisplayType, InputDisplayType } from '../../../interface';
import TUIChatConfig from '../config';

interface IProps {
  placeholder: string;
  isMuted?: boolean;
  muteText?: string;
  enableInput?: boolean;
  enableAt?: boolean;
  enableTyping?: boolean;
  replyOrReference?: Record<string, any>;
  inputToolbarDisplayType: ToolbarDisplayType;
}
interface IEmits {
  (e: 'changeToolbarDisplayType', displayType: ToolbarDisplayType): void;
}

const emits = defineEmits<IEmits>();
const props = withDefaults(defineProps<IProps>(), {
  placeholder: 'this is placeholder',
  replyOrReference: () => ({}),
  isMuted: true,
  muteText: '',
  enableInput: true,
  enableAt: true,
  enableTyping: true,
  inputToolbarDisplayType: 'none',
});

const editor = ref();
const messageInputAtRef = ref();
const currentConversation = ref<IConversationModel>();
const isGroup = ref<boolean>(false);
const displayType = ref<InputDisplayType>('editor');
const inputHasContent = ref<boolean>(false);
const isRecording = ref<boolean>(false);
let speechRecognitionManager: any = null;
const featureConfig = TUIChatConfig.getFeatureConfig();
const isRenderVoice = ref<boolean>(featureConfig.InputVoice);
const isRenderEmojiPicker = ref<boolean>(featureConfig.InputEmoji || featureConfig.InputStickers);
const isRenderMore = ref<boolean>(featureConfig.InputImage || featureConfig.InputVideo || featureConfig.InputEvaluation || featureConfig.InputQuickReplies);

onMounted(() => {
  TUIStore.watch(StoreName.CONV, {
    currentConversation: onCurrentConversationUpdated,
  });

  TUIStore.watch(StoreName.CHAT, {
    quoteMessage: onQuoteMessageUpdated,
  });

  // Listen for editor content changes via event bus (reliable in WeChat mini programs)
  uni.$on('editor-content-change', (data: any) => {
    inputHasContent.value = !!data?.hasContent;
  });
});

onUnmounted(() => {
  TUIStore.unwatch(StoreName.CONV, {
    currentConversation: onCurrentConversationUpdated,
  });

  TUIStore.unwatch(StoreName.CHAT, {
    quoteMessage: onQuoteMessageUpdated,
  });

  uni.$off('editor-content-change');
});

watch(() => props.inputToolbarDisplayType, (newVal: ToolbarDisplayType) => {
  if (newVal !== 'none') {
    changeDisplayType('editor');
  }
});

function changeDisplayType(display: InputDisplayType) {
  displayType.value = display;
  if (display === 'audio') {
    emits('changeToolbarDisplayType', 'none');
  }
}

function changeToolbarDisplayType(displayType: ToolbarDisplayType) {
  emits('changeToolbarDisplayType', displayType);
}

const onTyping = (inputContentEmpty: boolean, inputBlur: boolean) => {
  sendTyping(inputContentEmpty, inputBlur);
  inputHasContent.value = !inputContentEmpty;
};

const handleSendMessage = () => {
  editor?.value?.handleSendMessage && editor.value.handleSendMessage();
  inputHasContent.value = false;
};

// ========== 语音转文字 (Speech-to-Text) ==========
const initSpeechRecognition = () => {
  if (!isWeChat) return;
  try {
    // @ts-ignore - WeChat mini program plugin API
    const plugin = requirePlugin('WechatSI');
    speechRecognitionManager = plugin.getRecordRecognitionManager();
    speechRecognitionManager.onRecognize = (res: any) => {
      // Real-time partial results — can optionally show live preview
    };
    speechRecognitionManager.onStop = (res: any) => {
      isRecording.value = false;
      if (res.result) {
        // Insert recognized text into input field
        editor?.value?.setEditorContent(
          (editor?.value?.getEditorContent?.()?.[0]?.payload?.text || '') + res.result
        );
        // Trigger content detection so send button appears
        inputHasContent.value = true;
      }
    };
    speechRecognitionManager.onError = (res: any) => {
      isRecording.value = false;
      console.error('[SpeechToText] Error:', res);
    };
  } catch (e) {
    console.warn('[SpeechToText] WechatSI plugin not available:', e);
  }
};

const toggleSpeechToText = () => {
  if (!speechRecognitionManager) {
    initSpeechRecognition();
  }
  if (!speechRecognitionManager) return;

  if (isRecording.value) {
    speechRecognitionManager.stop();
    isRecording.value = false;
  } else {
    speechRecognitionManager.start({
      lang: 'zh_CN',
    });
    isRecording.value = true;
    // Close toolbar if open
    emits('changeToolbarDisplayType', 'none');
  }
};

const onAt = (show: boolean) => {
  messageInputAtRef?.value?.toggleAtList(show);
};

const onFocus = () => {
  emits('changeToolbarDisplayType', 'none');
};

const insertEmoji = (emoji: any) => {
  editor?.value?.addEmoji && editor?.value?.addEmoji(emoji);
};

const insertAt = (atInfo: any) => {
  editor?.value?.insertAt && editor?.value?.insertAt(atInfo);
};

const onAtListOpen = () => {
  editor?.value?.blur && editor?.value?.blur();
};

const reEdit = (content: any) => {
  editor?.value?.resetEditor();
  editor?.value?.setEditorContent(content);
};

function onCurrentConversationUpdated(conversation: IConversationModel) {
  currentConversation.value = conversation;
  isGroup.value = currentConversation.value?.type === TUIChatEngine.TYPES.CONV_GROUP;
}

function onQuoteMessageUpdated(options?: { message: IMessageModel; type: string }) {
  // switch text input mode when there is a quote message
  if (options?.message && options?.type === 'quote') {
    changeDisplayType('editor');
  }
}

defineExpose({
  insertEmoji,
  reEdit,
});
</script>

<style scoped lang="scss">
@import "../../../assets/styles/common";

:not(not) {
  display: flex;
  flex-direction: column;
  min-width: 0;
  box-sizing: border-box;
}

.message-input {
  position: relative;
  display: flex;
  flex-direction: column;
  border: none;
  overflow: hidden;
  background: #ebf0f6;

  &-h5 {
    padding: 10px 10px 15px;
  }

  &-editor {
    flex: 1;
    display: flex;
  }

  .icon {
    margin-left: 3px;
  }

  &-wx-audio-open {
    flex: 1;
  }
}

.audio-main-content-line {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.send-btn {
  width: 64px;
  height: 32px;
  border-radius: 6px;
  background: linear-gradient(135deg, #10b981, #059669);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  margin-left: 6px;
  flex-shrink: 0;

  .send-btn-text {
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    line-height: 1;
  }
}

.speech-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  margin-left: 4px;
  flex-shrink: 0;
  transition: all 0.2s ease;

  .speech-btn-icon {
    font-size: 20px;
    line-height: 1;
  }

  &-recording {
    background: rgba(16, 185, 129, 0.15);
    animation: speech-pulse 1.2s ease-in-out infinite;

    .speech-btn-icon {
      color: #059669;
    }
  }
}

@keyframes speech-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
  }
}
</style>
