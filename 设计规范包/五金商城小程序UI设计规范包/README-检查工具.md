# 小程序 UI 检查说明

当前包先提供人工检查清单，后续可按同名规则补 `scripts/check_mini_ui.py`。

## 必查项

1. 页面背景必须是白色或浅灰，不能出现论坛旧蓝绿渐变主背景。
2. 首页、详情、寻源、图谱、我的、消息等主页面顶部必须使用主题黄色头部。
3. 主文字使用黑色或深灰，辅助文字使用中灰，不能出现低对比灰。
4. 主按钮和选中态使用黑色/黄色组合，不使用蓝色作为默认主操作。
5. 三泳道页面必须保留 `keyword/lane/sourceKeyword` 上下文。
6. 登录页必须保留手机号一键授权登录和验证码兜底。
7. token 刷新链路必须保留 `refresh_token`，不能只存 access token。
8. 消息页必须使用 WukongIM 适配层，不再直接依赖 Tencent TUIKit 作为主体验。

## 建议检查命令

```powershell
npm run build:mp-weixin
```

构建后再用微信开发者工具打开 `dist/build/mp-weixin` 做真机或模拟器检查。
