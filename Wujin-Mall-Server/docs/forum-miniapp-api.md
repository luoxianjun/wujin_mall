# 论坛小程序端接口文档

本文档基于 `yudao-module-forum` 模块中 App 端 Controller 的实现整理而成，覆盖论坛小程序端（App 端）能够调用的帖子、评论、活动、消息、积分、用户、签到等接口，并给出了入参与出参示例，方便直接对接。

> 代码参考：帖子、评论、消息、积分等接口均位于 `yudao-module-forum/src/main/java/cn/iocoder/yudao/module/forum/controller/app/**`（如 `AppPostController`、`AppCommentController` 等）。

---

## 1. 通用说明

### 1.1 接口前缀与域名

- 线上环境通常通过 `https://{网关域名}/app-api` 对外暴露 App 端接口。
- 具体某个接口的最终 URL 由 `网关前缀 (/app-api)` + `Controller 上 @RequestMapping` + `方法上的 @GetMapping/@PostMapping` 组成。本文中列出的路径均使用 Controller 上定义的路径，实际调用时请记得在最前面拼接 `/app-api`。

### 1.2 认证方式

- 标记了 `@PreAuthenticated` 的接口必须携带登录态（通常是 `Authorization: Bearer ${token}`），否则会被拒绝。
- 未标记 `@PreAuthenticated` 的接口默认为游客可用，但如果在接口内部获取到了登录用户，则可以带上 token 以获得个性化结果（例如帖子/评论的 `liked`、`followed` 等字段）。

### 1.3 通用响应结构

所有接口都返回 `CommonResult<T>`，其结构如下：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 错误码，成功固定为 `0` |
| msg | string | 错误描述，成功时为空字符串 |
| data | T | 业务数据 |

示例：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "...": "..."
  }
}
```

### 1.4 分页参数与返回

- 继承 `PageParam` 的请求体会额外包含 `pageNo`（必填，默认 1）和 `pageSize`（必填，默认 10，最大 100）。
- 分页返回统一使用 `PageResult<T>`：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "total": 35,
    "list": [
      { "…": "…" }
    ]
  }
}
```

### 1.5 时间与经纬度

- 时间字段统一使用 `yyyy-MM-dd HH:mm:ss`（或日期字段 `yyyy-MM-dd`）的字符串。
- 经纬度均为 `double` 类型。

---

## 2. 帖子接口（`/forum/post`，参考 AppPostController）

### 2.1 创建帖子 `POST /forum/post/create`（需登录）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| title | string | 是 | 帖子标题 |
| content | string | 是 | 帖子正文 |
| category | int | 是 | 帖子分类编号 |
| imageUrls | string[] | 否 | 图片 URL 列表 |
| anonymous | bool | 是 | 是否匿名 |
| schoolOnly | bool | 否 | 是否仅本校可见 |

请求示例：

```json
{
  "title": "求助：如何学好 Java",
  "content": "我是一名大一新生，刚接触 Java…",
  "category": 2,
  "imageUrls": [
    "https://cdn.example.com/forum/post-1.png"
  ],
  "anonymous": true,
  "schoolOnly": false
}
```

返回示例（`data` 为帖子 ID）：

```json
{ "code": 0, "msg": "", "data": 128 }
```

### 2.2 获取帖子详情 `GET /forum/post/get?id=`（可游客）

- Query 参数：`id` 帖子 ID。
- 返回 `AppPostRespVO`。

返回示例：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "id": 128,
    "userId": 9527,
    "uid": "U123456",
    "nickname": "张三",
    "avatar": "https://cdn.example.com/avatar.png",
    "title": "求助：如何学好 Java",
    "content": "我是一名大一新生…",
    "category": 2,
    "categoryName": "求助",
    "imageUrls": [
      "https://cdn.example.com/forum/post-1.png"
    ],
    "anonymous": true,
    "schoolOnly": false,
    "status": 1,
    "isTop": false,
    "likeCount": 36,
    "commentCount": 9,
    "followCount": 4,
    "viewCount": 342,
    "latestCommentTime": "2024-04-20 18:20:30",
    "createTime": "2024-04-20 14:05:11",
    "liked": true,
    "followed": false
  }
}
```

### 2.3 帖子分页 `GET /forum/post/page`（可游客）

| 字段 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| pageNo | Query | int | 是 | 页码 |
| pageSize | Query | int | 是 | 每页条数 |
| category | Query | int | 否 | 分类 |
| status | Query | int | 否 | 审核状态 |
| userId | Query | long | 否 | 指定作者 |
| keyword | Query | string | 否 | 搜索关键词 |
| orderBy | Query | int | 否 | 1=最新,2=热度 |

返回 `PageResult<AppPostRespVO>`，结构同上。

### 2.4 删除帖子 `DELETE /forum/post/delete?id=`（需登录、只能删本人）

- Query：`id` 帖子 ID。
- 返回：`{ "code":0,"msg":"","data":true }`

### 2.5 点赞帖子 `POST /forum/post/like?id=`（需登录）

### 2.6 取消点赞 `POST /forum/post/unlike?id=`（需登录）

### 2.7 关注帖子 `POST /forum/post/follow?id=`（需登录）

### 2.8 取消关注 `POST /forum/post/unfollow?id=`（需登录）

> 上述 2.5-2.8 接口均只需要 query 参数 `id`，成功返回 `data=true`。

---

## 3. 评论接口（`/forum/comment`，参考 AppCommentController）

### 3.1 创建评论 `POST /forum/comment/create`（需登录）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| postId | long | 是 | 所属帖子 ID |
| parentId | long | 否 | 父评论 ID（回复时） |
| rootId | long | 否 | 根评论 ID（楼层） |
| content | string | 是 | 评论内容 |
| anonymous | bool | 是 | 是否匿名 |

请求示例：

```json
{
  "postId": 128,
  "parentId": 66,
  "rootId": 66,
  "content": "说得对，我也是这么学的。",
  "anonymous": false
}
```

返回：`data` 为评论 ID。

### 3.2 评论分页 `GET /forum/comment/page`

| Query 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNo | int | 是 | 页码 |
| pageSize | int | 是 | 每页条数 |
| postId | long | 是 | 帖子 ID |
| rootId | long | 否 | 指定根评论获取子楼层 |

返回 `PageResult<AppCommentRespVO>`。

### 3.3 评论树 `GET /forum/comment/tree?postId=`（可游客）

返回示例：

```json
{
  "code": 0,
  "msg": "",
  "data": [
    {
      "id": 66,
      "postId": 128,
      "userId": 9528,
      "uid": "U223344",
      "nickname": "李四",
      "avatar": "https://cdn.example.com/u2.png",
      "parentId": null,
      "rootId": 66,
      "content": "坚持刷题最重要",
      "anonymous": false,
      "likeCount": 2,
      "createTime": "2024-04-20 15:00:00",
      "liked": false,
      "children": [
        {
          "id": 67,
          "postId": 128,
          "userId": 9529,
          "uid": "U445566",
          "nickname": "匿名用户",
          "avatar": null,
          "parentId": 66,
          "rootId": 66,
          "content": "受教了！",
          "anonymous": true,
          "likeCount": 0,
          "createTime": "2024-04-20 15:12:00",
          "liked": true,
          "children": []
        }
      ]
    }
  ]
}
```

### 3.4 删除/点赞/取消点赞评论

- `DELETE /forum/comment/delete?id=`（需登录）
- `POST /forum/comment/like?id=`（需登录）
- `POST /forum/comment/unlike?id=`（需登录）

均以 `id` 为评论 ID，返回 `true`。

---

## 4. 活动接口（`/app-api/forum/activity`，参考 AppActivityController）

> 此 Controller 自身带了 `/app-api` 前缀，下文地址已是最终路径。

### 4.1 获取活动详情 `GET /app-api/forum/activity/get?id=`

返回 `AppActivityRespVO`，示例：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "id": 301,
    "userId": 9527,
    "uid": "U123456",
    "nickname": "张三",
    "avatar": "https://cdn.example.com/avatar.png",
    "title": "校园马拉松",
    "description": "欢迎所有同学参加…",
    "coverImage": "https://cdn.example.com/activity/301-cover.jpg",
    "detailImages": [
      "https://cdn.example.com/activity/301-detail-1.png"
    ],
    "category": 2,
    "categoryName": "文体活动",
    "location": "操场",
    "longitude": 113.12345,
    "latitude": 22.12345,
    "startTime": "2024-05-01 09:00:00",
    "endTime": "2024-05-01 12:00:00",
    "signUpStartTime": "2024-04-20 10:00:00",
    "signUpEndTime": "2024-04-28 18:00:00",
    "checkInStartTime": "2024-05-01 08:30:00",
    "checkInEndTime": "2024-05-01 09:15:00",
    "checkInDistance": 100,
    "maxParticipants": 200,
    "currentParticipants": 86,
    "needApproval": false,
    "schoolOnly": true,
    "status": 1,
    "statusName": "报名中",
    "viewCount": 560,
    "likeCount": 42,
    "createTime": "2024-04-18 11:33:00",
    "signedUp": true,
    "signUpStatus": 1,
    "checkedIn": false
  }
}
```

### 4.2 活动分页 `GET /app-api/forum/activity/page`

| Query 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNo / pageSize | int | 分页必填 |
| category | int | 分类 |
| status | int | 状态 |
| keyword | string | 标题或描述关键字 |

### 4.3 报名活动 `POST /app-api/forum/activity/sign-up`（需登录）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | long | 是 | 活动 ID |
| remark | string | 否 | 报名备注 |

### 4.4 取消报名 `POST /app-api/forum/activity/cancel-sign-up?activityId=`（需登录）

返回 `true`。

### 4.5 活动签到 `POST /app-api/forum/activity/check-in`（需登录）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| activityId | long | 是 | 活动 ID |
| longitude | double | 是 | 签到经度 |
| latitude | double | 是 | 签到纬度 |

### 4.6 我的报名分页 `GET /app-api/forum/activity/my-sign-up/page`（需登录）

| Query 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNo / pageSize | int | 分页 |
| activityId | long | 可选，按活动过滤 |
| approvalStatus | int | 可选，0 待审核 /1 通过 /2 拒绝 |

返回 `PageResult<AppActivitySignUpRespVO>`，其中 `data.list[*]` 结构如下：

```json
{
  "id": 9001,
  "activityId": 301,
  "activityTitle": "校园马拉松",
  "coverImage": "https://cdn.example.com/activity-cover.png",
  "needCheckIn": true,
  "userId": 9527,
  "uid": "U123456",
  "nickname": "张三",
  "avatar": "https://cdn.example.com/avatar.png",
  "remark": "我想参加",
  "approvalStatus": 1,
  "approvalStatusName": "已通过",
  "approvalRemark": "欢迎参加",
  "checkedIn": true,
  "checkInTime": "2024-05-01 08:45:00",
  "createTime": "2024-04-20 12:00:00"
}
```

### 4.7 获取签到二维码 `GET /app-api/forum/activity/check-in/qrcode`（需登录）

- Query：`activityId` long。
- 只有已通过审核且未签到的报名用户可获取。
- 返回 `AppActivityCheckInQrRespVO`：

```json
{ "qrContent": "MTIzfDQ1Nnw3ODk=" }
```

可直接将 `qrContent` 渲染成二维码，供管理员扫描。

### 4.8 扫码签到（管理员） `POST /app-api/forum/activity/check-in/scan`（需登录）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| qrContent | string | 是 | 签到二维码内容（Base64），来自 4.7 接口 |

- 仅活动创建者可用，用于扫描报名用户的二维码完成签到，不再校验地理位置，但需处于签到时间范围内。
- 返回 `true`。

---

## 5. 消息与通知接口（`/forum/message`，参考 AppMessageController）

### 5.1 发送私信 `POST /forum/message/send`（需登录）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| receiverId | long | 是 | 接收方用户 ID |
| messageType | int | 是 | 1 文本 /2 图片 /3 语音 /4 视频 |
| content | string | 是 | 文本或素材地址 |

返回：`data` 为消息 ID。

### 5.2 会话列表 `GET /forum/message/conversation/page`（需登录）

仅需分页参数。返回 `PageResult<AppConversationRespVO>`，列表元素示例：

```json
{
  "id": 6001,
  "otherUserId": 9528,
  "otherUserUid": "U223344",
  "otherUserNickname": "李四",
  "otherUserAvatar": "https://cdn.example.com/u2.png",
  "lastMessageContent": "晚上一起自习？",
  "lastMessageTime": "2024-04-20 21:00:00",
  "unreadCount": 2
}
```

### 5.3 会话消息 `GET /forum/message/page?conversationId=`（需登录）

返回 `PageResult<AppMessageRespVO>`。示例：

```json
{
  "id": 88001,
  "conversationId": 6001,
  "senderId": 9527,
  "senderUid": "U123456",
  "senderNickname": "张三",
  "senderAvatar": "https://cdn.example.com/avatar.png",
  "receiverId": 9528,
  "messageType": 1,
  "messageTypeName": "文本",
  "content": "晚上一起自习？",
  "readStatus": false,
  "createTime": "2024-04-20 21:00:00"
}
```

### 5.4 会话标记已读 `POST /forum/message/mark-read?conversationId=`（需登录）

返回 `true`。

### 5.5 未读统计 `GET /forum/message/unread-count`（需登录）

返回示例：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "unreadMessageCount": 2,
    "unreadNoticeCount": 5,
    "totalUnreadCount": 7
  }
}
```

### 5.6 系统通知分页 `GET /forum/message/notice/page`（需登录）

| Query 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNo / pageSize | int | 分页 |
| noticeType | int | 可选，1 点赞/2 评论/3 关注/4 系统/5 活动 |
| readStatus | bool | 可选，过滤已读状态 |

列表元素示例：

```json
{
  "id": 7001,
  "noticeType": 1,
  "noticeTypeName": "点赞通知",
  "title": "有人点赞了你的帖子",
  "content": "用户李四点赞了《求助：如何学好 Java》",
  "relatedId": 128,
  "relatedType": 1,
  "readStatus": false,
  "createTime": "2024-04-20 16:00:00"
}
```

### 5.7 通知标记已读 `POST /forum/message/notice/mark-read?noticeId=`（需登录）

### 5.8 通知全部已读 `POST /forum/message/notice/mark-all-read`（需登录）

均返回 `true`。

---

## 6. 积分接口（`/forum/point`，参考 AppPointController）

### 6.1 获取积分余额 `GET /forum/point/balance`（需登录）

返回 `data` 为当前积分值：

```json
{ "code": 0, "msg": "", "data": 520 }
```

### 6.2 积分记录分页 `GET /forum/point/record/page`（需登录）

| Query 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNo / pageSize | int | 分页 |
| bizType | int | 可选，积分业务类型 |

返回 `PageResult<ForumPointRecordDO>`，单条示例：

```json
{
  "id": 10001,
  "userId": 9527,
  "bizId": "post:128",
  "bizType": 1,
  "title": "发布帖子奖励",
  "description": "首帖奖励 +10",
  "point": 10,
  "totalPoint": 530,
  "createTime": "2024-04-20 14:05:11",
  "updateTime": "2024-04-20 14:05:11"
}
```

---

## 7. 用户资料接口（`/forum/user/profile`，参考 AppUserProfileController）

### 7.1 获取当前用户资料 `GET /forum/user/profile/get`（需登录）

返回 `AppUserProfileRespVO`，示例：

```json
{
  "id": 9527,
  "uid": "U123456",
  "nickname": "张三",
  "avatar": "https://cdn.example.com/avatar.png",
  "schoolName": "澳门大学",
  "schoolEmail": "student@um.edu.mo",
  "schoolEmailVerified": true,
  "schoolEmailVerifyTime": "2024-04-10 12:00:00",
  "realName": "张三",
  "gender": 1,
  "majorAndGrade": "计算机科学 2021级",
  "schoolInfoPublic": true,
  "birthday": "2003-06-01",
  "constellation": "双子座",
  "mbti": "INTJ",
  "introduction": "热爱编程的学生",
  "point": 520,
  "totalPoint": 1024,
  "continuousSignDays": 5,
  "totalSignDays": 48,
  "lastSignDate": "2024-04-20",
  "postCount": 18,
  "activityCount": 6,
  "likeCount": 230,
  "favoriteCount": 42
}
```

### 7.2 根据 UID 获取资料 `GET /forum/user/profile/get-by-uid?uid=`

可用于查看他人资料，无需登录。

### 7.3 更新资料 `PUT /forum/user/profile/update`（需登录）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| nickname | string | 论坛昵称，<=64 |
| avatar | string | 头像 URL |
| birthday | string | `yyyy-MM-dd` |
| mbti | string | 4 位大写字母 |
| introduction | string | <=500 |

### 7.4 提交学校认证 `POST /forum/user/profile/school/submit`（需登录）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| schoolEmail | string | 是 | 学校邮箱 |
| realName | string | 是 | 真实姓名 |
| gender | int | 是 | 1 男 2 女 |
| majorAndGrade | string | 是 | 专业年级 |

### 7.5 发送学校邮箱验证码 `POST /forum/user/profile/school/send-code`（需登录）

请求体：`{ "email": "student@um.edu.mo" }`

### 7.6 校验学校邮箱验证码 `POST /forum/user/profile/school/verify-code`（需登录）

请求体：`{ "email": "student@um.edu.mo", "code": "123456" }`

### 7.7 更新学校信息公开设置 `PUT /forum/user/profile/school/update-public?schoolInfoPublic=`

参数为 `true/false`。

### 7.8 获取用户统计 `GET /forum/user/profile/statistics`（需登录）

返回 `AppUserStatisticsRespVO`（字段同资料中的积分/签到/帖子统计）。

### 7.9 获取个人中心 `GET /forum/user/profile/center`（需登录）

返回 `AppUserCenterRespVO`，在统计基础上增加 `todaySigned` 等字段：

```json
{
  "userId": 9527,
  "uid": "U123456",
  "nickname": "张三",
  "avatar": "https://cdn.example.com/avatar.png",
  "schoolName": "澳门大学",
  "schoolEmailVerified": true,
  "constellation": "摩羯座",
  "mbti": "INTJ",
  "introduction": "这是我的个人介绍",
  "point": 520,
  "totalPoint": 1024,
  "continuousSignDays": 5,
  "totalSignDays": 48,
  "lastSignDate": "2024-04-20",
  "todaySigned": true,
  "postCount": 18,
  "activityCount": 6,
  "likeCount": 230,
  "favoriteCount": 42
}
```

---

## 8. 签到接口（`/forum/sign`，参考 AppSignController）

### 8.1 签到 `POST /forum/sign/do`（需登录）

返回 `AppSignRespVO`：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "id": 5001,
    "signDate": "2024-04-20",
    "continuousDays": 5,
    "point": 10,
    "remark": "连续签到 5 天，额外奖励 5 积分"
  }
}
```

### 8.2 获取签到状态 `GET /forum/sign/status`（需登录）

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "todaySigned": true,
    "continuousDays": 5,
    "totalDays": 48,
    "lastSignDate": "2024-04-20"
  }
}
```

### 8.3 签到记录分页 `GET /forum/sign/record/page`（需登录）

| Query 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNo / pageSize | int | 分页 |
| startDate | string | 开始日期 `yyyy-MM-dd` |
| endDate | string | 结束日期 |

返回 `PageResult<AppSignRespVO>`。

---

如需扩展新的小程序端接口，可按照现有 Controller 编写，并补充到本文档对应章节。
