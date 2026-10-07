import TUICore, { TUIConstants } from '@tencentcloud/tui-core-lite';
import { TUITranslateService } from '@tencentcloud/chat-uikit-engine-lite';
import { isPC } from '../../../utils/env';
import createGroup from '../../../assets/icon/start-group.svg';
import C2C from '../../../assets/icon/icon-c2c.svg';
import { CONV_CREATE_TYPE } from '../../../constant';

export interface IMenuItem {
  icon?: string;
  text: string;
  data: {
    name: string;
    children?: any[];
  };
  listener?: {
    onClicked: (...args: any[]) => void;
  };
}

export default class ConversationHeaderServer {
  static instance: ConversationHeaderServer;

  static getInstance(): ConversationHeaderServer {
    if (!ConversationHeaderServer.instance) {
      ConversationHeaderServer.instance = new ConversationHeaderServer();
    }
    return ConversationHeaderServer.instance;
  }

  public getMenu(): any[] {
    const list = this.generateMenuList();
    // if (!isPC && list.length > 0) {
    //   return [{
    //     text: TUITranslateService.t('TUIConversation.发起会话'),
    //     data: {
    //       name: 'all',
    //       children: list,
    //     },
    //   }];
    // }
    return list;
  }

  private isUserAdmin(): boolean {
    try {
      // 与 @/utils/session 的 user_profile 统一
      const profile = uni.getStorageSync('user_profile');
      return !!(profile && profile.isAdmin === true);
    } catch {
      return false;
    }
  }

  private generateMenuList(): any[] {
    // 只有管理员才能发起群聊
    if (!this.isUserAdmin()) {
      return [];
    }

    const list = [
      // {
      //   icon: C2C,
      //   text: TUITranslateService.t('TUIConversation.发起单聊'),
      //   data: {
      //     name: CONV_CREATE_TYPE.TYPEC2C,
      //   },
      //   listener: {
      //     onClicked: this.createConversation.bind(this),
      //   },
      // },
      {
        icon: createGroup,
        text: TUITranslateService.t('TUIConversation.发起群聊'),
        data: {
          name: CONV_CREATE_TYPE.TYPEGROUP,
        },
        listener: {
          onClicked: this.createConversation.bind(this),
        },
      },
    ];
    return list;
  }

  private createConversation(item: IMenuItem) {
    // Navigate to user selection page
    uni.navigateTo({
      url: '/pages/messages/user-select',
    });
  }
}

