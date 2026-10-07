import { TUILogin } from '@tencentcloud/tui-core-lite';
import { TUIConversationService, TUIChatEngine } from '@tencentcloud/chat-uikit-engine-lite';
// #ifdef MP-WEIXIN
import { TUIChatKit } from '../../index';
// #endif

export const initChat = (options: Record<string, string>) => {
  // #ifdef MP-WEIXIN
  // uni-app packages the mini program.
  // If you call TUIChatKit.init() directly during import, an error will be reported.
  // You need to init during the page onLoad.
  TUIChatKit.init();
  // #endif

  // When opening TUIChat, the options and options.conversationID parameters carried in the url,
  // determine whether to enter the Chat from the [Conversation List] or [Online Communication].
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

  if (chat?.isReady()) {
      trySwitchConversation();
  } else {
      const onSdkReady = () => {
          trySwitchConversation();
          TUIChatKit.off(TUIChatEngine.EVENT.SDK_READY, onSdkReady);
      };
      TUIChatKit.on(TUIChatEngine.EVENT.SDK_READY, onSdkReady);
  }
};

export const logout = (flag: boolean) => {
  if (flag) {
    return TUILogin.logout();
  }
  return Promise.resolve();
};
