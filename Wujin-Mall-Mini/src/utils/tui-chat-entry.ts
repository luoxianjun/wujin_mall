import { TUILogin } from '@tencentcloud/tui-core-lite';
import { TUIConversationService, TUIChatEngine } from '@tencentcloud/chat-uikit-engine-lite';

export function initChat(options: Record<string, string>) {
  const { chat } = TUILogin.getContext();
  const trySwitchConversation = () => {
    if (options && options.conversationID) {
      const { conversationID } = options;
      if (!conversationID.startsWith('C2C') && !conversationID.startsWith('GROUP')) {
        console.warn('conversationID from options is invalid.');
        return;
      }
      TUIConversationService.switchConversation(conversationID);
    }
  };

  if (!chat) {
    console.warn('Chat SDK instance is not ready yet.');
    return;
  }

  if (chat?.isReady()) {
    trySwitchConversation();
  } else {
    const onSdkReady = () => {
      trySwitchConversation();
      chat.off(TUIChatEngine.EVENT.SDK_READY, onSdkReady);
    };
    chat.on(TUIChatEngine.EVENT.SDK_READY, onSdkReady);
  }
}

export function logout(flag: boolean) {
  if (flag) {
    return TUILogin.logout();
  }
  return Promise.resolve();
}

export default {
  initChat,
  logout,
};
