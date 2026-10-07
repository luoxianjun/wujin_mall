# yudao-module-forum

学生论坛业务模块，围绕“发帖 / 活动 / 消息 / 积分”等能力，为微信小程序提供后端接口。

主要结构规划：

- controller
  - admin  // 后台接口（活动发布、内容审核等）
  - app    // 微信小程序端接口（帖子、活动、消息、个人中心等）
- service  // 业务服务（帖子、评论、活动、积分、签到等）
- dal
  - dataobject  // 数据对象定义
  - mysql       // Mapper
- enums    // 模块枚举 & 错误码
- vo       // 接口层 VO
- sql/mysql/yudao-module-forum.sql  // 建表脚本

