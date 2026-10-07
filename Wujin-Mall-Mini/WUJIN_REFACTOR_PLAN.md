# 五金商城小程序改造计划（步骤 1–5）

## 背景
`Wujin-Mall-Mini/src` 现状：主体是论坛 App「荟星Planet」，五金商城只占 `pages/wujin/*` 7 个页面。
目标：剥离论坛/养老残留，让五金独立跑通，并修复导航、断头路、配色、安全区问题。

## 已确认决策
- **联系供应商**：保留悟空 IM（messages 模块 + wukong-im + 登录链路）给五金用。
- **我的 tab**：新建五金版「我的」页，登录暂用游客态。
- **删除方式**：物理删除文件（注意：本项目非 git 仓库，删除不可逆）。

## 保留集合（不能删）
- `pages/wujin/*`、`components/BottomNavBar.vue`、`styles/wujin-theme.scss`、`api/wujin/*`
- IM 链路：`pages/messages/*`、`utils/wukong-im.js`、`utils/im-helper.js`、`utils/tui-chat-entry.ts`、`TUIKit/`、`api/message.js`、`api/user.js`、`api/auth.js`
- 登录：`pages/users/login.vue`（其余 users 子页视情况）
- 工具/基建：`utils/{request,session,toast,appIcons}.js`、`mixins/{statusBar,defaultShare}.js`、`components/AppIcon.vue`、`api/file.js`、`api/system.js`
- `App.vue`、`main.js`、`uni.scss`

## 删除集合
- `miniprogram/`（养老原生残留，整目录）
- 论坛页面：`pages/{index,tab,events,events-sub,hot,welfare,posts,vote,lottery,invitation,search}`、`pages/profile`、`pages/profile-sub`、`pages/users`(除 login)
- 论坛组件：`components/{PublishModal,ShareModal,post}`、按需 `BackButton/EmptyState/PageTransition`
- 论坛 api：`api/{activity,banner,comment,event,lottery,postVote,posts,quiz,vote,address}.js`、`api/gamification`
- 论坛工具：`utils/{activityCheckIn,activityDict,quiz,topicDict,emoji}.js`、`weapp.qrcode.min.js`(确认五金未用)

---

## 步骤 1：接通 IA（启动入口 + 底部导航）— 最高优先
1. `pages.json`：移除所有论坛路由，仅保留 `pages/wujin/*`、新建的 `pages/wujin/profile`、保留 messages/users-login/TUIKit 子包。五金搜索页 `pages/wujin/search` 置为首页（数组第一项）。
2. `BottomNavBar.vue`：`handleNav` 由 `uni.navigateTo` 改为 `uni.reLaunch`（tab 级避免压栈）；「我的」path 改为 `/pages/wujin/profile`。
3. `App.vue`：移除 `messages-page` 等论坛样式残留；确认 onLaunch IM 逻辑不报错。
4. `main.js`：`defaultShareMixin` 分享文案/路径由「荟星Planet / tab/index」改为五金。

## 步骤 2：补全断头路
1. 新建 `pages/wujin/profile.vue`（五金版「我的」）：我的寻源线索入口、联系方式、设置占位；用 `statusBar` mixin 处理安全区；底部挂 `BottomNavBar active="profile"`。
2. `detail.vue`、`supplier-capability.vue`、`trace-map.vue`、`lead-progress.vue`：补自绘返回按钮（复用 BackButton 或简单胶囊），确保非 tab 页可返回。
3. 线索进度常驻入口：在「我的」页加「我的寻源线索」入口指向 `lead-progress`。

## 步骤 3：删除残留
- 按"删除集合"物理删除。每删一批后用 `grep` 校验无 `@/pages/...`、`@/api/...` 悬空引用。
- 重点校验：messages 模块引用的 `api/user`、`api/message`、`mixins/statusBar` 均在保留集合。

## 步骤 4：体验优化
1. 五金各页 hero 顶部接入 `statusBar` mixin，替换写死的 `padding-top:64rpx/68rpx`，消除刘海屏遮挡。
2. `search.vue` 未搜索空状态：加「三泳道是什么」一句话引导。
3. `detail.vue`：预留价格/规格/MOQ/认证展示位（前端坑位，待后端）。

## 步骤 5：配色微调
1. `wujin-theme.scss`：`wujin-hero__subtitle` 由 `rgba(17,17,17,.76)` 提到 `#111`（黄底对比度）。
2. 字重分级：正文 400 / 次级 600 / 标题 800，收敛满屏 900。
3. 原材料绿 `#2f8f46` 归位：要么纳入正式色板多处呼应，要么统一进黑黄体系。

## 验证
- 每步后 `npm run dev:mp-weixin`（或项目脚本）确认编译通过、无悬空引用。
- 微信开发者工具走通：启动→搜索→结果→溯源→寻源→提交线索→进度→我的，无死链、无压栈失败。
