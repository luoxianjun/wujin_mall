# 论坛后台运营端接口文档

本文档基于 `yudao-module-forum` 模块 Admin 端 Controller 的实现整理而成，覆盖后台运营/管理中台可以调用的论坛活动、帖子相关接口，便于运营人员、系统对接方或测试快速理解调用方式。

> 代码参考：`yudao-module-forum/src/main/java/cn/iocoder/yudao/module/forum/controller/admin/**`（如 `AdminActivityController`、`AdminPostController`）。

---

## 1. 通用说明

### 1.1 接口前缀与域名

- 后台接口统一由网关在 `https://{网关域名}/admin-api` 暴露。
- 实际调用时，最终的 URL 由 `/admin-api` + Controller 上的 `@RequestMapping`（当前为 `/forum/activity`）+ 方法上的 `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping` 构成，本文中列出的路径只包含 Controller+方法上的部分。

### 1.2 认证与权限

- 所有接口都需要携带 `Authorization: Bearer ${token}`；权限判断由 `@PreAuthorize("@ss.hasPermission('…')")` 控制。
- 每个接口说明中都会给出权限标识（如 `forum:activity:create`、`forum:activity:query`），确保当前管理员拥有对应权限再发起请求。

### 1.3 通用响应

所有接口均返回 `CommonResult<T>`，结构如下：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 业务错误码，成功返 `0` |
| msg | string | 失败原因，成功时为空 |
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

### 1.4 分页规范

- 继承 `PageParam` 的请求体会自动包含 `pageNo`（默认 1）和 `pageSize`（默认 10，最大 100）。
- 分页返回使用 `PageResult<T>`，包含 `total` 和 `list`。

### 1.5 时间与经纬度

- 时间字段统一采用 `yyyy-MM-dd HH:mm:ss` 格式的字符串。
- 经度/纬度字段为 `double`。

---

## 2. 论坛活动管理（`/forum/activity`）

接口均位于 `AdminActivityController`，涉及活动的创建、编辑、分页查询、详情、删除、报名列表以及审核流程。

### 2.1 创建活动 `POST /forum/activity/create`（权限 `forum:activity:create`）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| title | string | 是 | 活动标题 |
| description | string | 是 | 活动描述 |
| coverImage | string | 是 | 活动封面图 |
| detailImages | string[] | 否 | 详情图列表（JSON） |
| category | int | 是 | 活动分类（1-5） |
| location | string | 是 | 活动地点 |
| longitude | double | 否 | 经度 |
| latitude | double | 否 | 纬度 |
| startTime | string | 是 | 活动开始时间 |
| endTime | string | 是 | 活动结束时间 |
| signUpStartTime | string | 否 | 报名开始时间 |
| signUpEndTime | string | 否 | 报名结束时间 |
| checkInStartTime | string | 否 | 签到开始时间 |
| checkInEndTime | string | 否 | 签到结束时间 |
| checkInDistance | int | 否 | 签到距离限制（米） |
| maxParticipants | int | 否 | 报名人数上限（0 不限） |
| needApproval | bool | 否 | 是否需要审核报名 |
| schoolOnly | bool | 否 | 是否只允许本校可见 |

请求示例：

```json
{
  "title": "校园马拉松",
  "description": "欢迎报名…",
  "coverImage": "https://cdn.example.com/activity-cover.png",
  "category": 2,
  "location": "体育馆",
  "startTime": "2024-05-01 10:00:00",
  "endTime": "2024-05-01 12:00:00",
  "needApproval": true
}
```

返回示例（`data` 为创建成功的 `activityId`）：

```json
{ "code": 0, "msg": "", "data": 123 }
```

### 2.2 编辑活动 `PUT /forum/activity/update`（权限 `forum:activity:update`）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 活动 ID |
| title | string | 否 | 活动标题 |
| description | string | 否 | 活动描述 |
| coverImage | string | 否 | 活动封面图 |
| detailImages | string[] | 否 | 详情图列表，传空数组可清空 |
| category | int | 否 | 活动分类 |
| location | string | 否 | 活动地点 |
| longitude | double | 否 | 经度 |
| latitude | double | 否 | 纬度 |
| startTime | string | 否 | 活动开始 |
| endTime | string | 否 | 活动结束 |
| signUpStartTime | string | 否 | 报名开始 |
| signUpEndTime | string | 否 | 报名结束 |
| checkInStartTime | string | 否 | 签到开始 |
| checkInEndTime | string | 否 | 签到结束 |
| checkInDistance | int | 否 | 签到距离（米） |
| maxParticipants | int | 否 | 报名人数上限 |
| needApproval | bool | 否 | 是否审核报名 |
| schoolOnly | bool | 否 | 是否仅本校可见 |

- 如果同时传 `startTime` 与 `endTime`，需保证 `endTime >= startTime`，否则会返回错误 `ACTIVITY_TIME_INVALID`；
- 未传字段保持原值，`detailImages` 若传空则会清空旧值；
- 只有活动创建者本人可编辑，否则返回 `ACTIVITY_DELETE_FAIL_NOT_OWNER`。

请求示例：

```json
{
  "id": 123,
  "title": "校园马拉松·新版",
  "startTime": "2024-05-01 09:30:00",
  "endTime": "2024-05-01 12:30:00"
}
```

返回示例：

```json
{ "code": 0, "msg": "", "data": true }
```

### 2.3 获取活动详情 `GET /forum/activity/get?id=`（权限 `forum:activity:query`）

- Query 参数：`id` 活动 ID。
- 返回 `AppActivityRespVO`，包含活动各字段（`title/description/coverImage/category/categoryName/location` 等）与 `status/statusName`、`viewCount/likeCount` 及当前管理员（若作为发布者）是否报名/签到 `signedUp/checkedIn` 等。

返回示例：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "id": 128,
    "title": "校园马拉松",
    "description": "欢迎参加…",
    "coverImage": "https://cdn.example.com/activity-cover.png",
    "category": 2,
    "categoryName": "文体活动",
    "location": "体育馆",
    "startTime": "2024-05-01 10:00:00",
    "endTime": "2024-05-01 12:00:00",
    "signUpStartTime": "2024-04-20 08:00:00",
    "signUpEndTime": "2024-04-30 20:00:00",
    "checkInStartTime": "2024-05-01 09:30:00",
    "checkInEndTime": "2024-05-01 11:30:00",
    "checkInDistance": 100,
    "maxParticipants": 200,
    "currentParticipants": 56,
    "needApproval": true,
    "schoolOnly": false,
    "status": 1,
    "statusName": "报名中",
    "viewCount": 342,
    "likeCount": 12,
    "signedUp": true,
    "checkedIn": false,
    "createTime": "2024-04-20 14:05:11"
  }
}
```

### 2.4 活动分页 `GET /forum/activity/page`（权限 `forum:activity:query`）

| 字段 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| pageNo | Query | int | 是 | 页码 |
| pageSize | Query | int | 是 | 每页条数 |
| category | Query | int | 否 | 活动分类 |
| status | Query | int | 否 | 活动状态 |
| keyword | Query | string | 否 | 标题/描述模糊匹配 |

- 返回 `PageResult<AppActivityRespVO>`，每条记录与详情接口字段一致。

---

## 3. 帖子管理（`/admin/forum/post`）

接口位于 `AdminPostController`，包含帖子分页、详情和后台删除。

> 说明：路径拼接规则同 1.1，小节中的路径均省略 `/admin-api` 网关前缀。

### 3.1 帖子分页 `GET /admin/forum/post/page`（权限 `forum:post:query`）

| 字段 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| pageNo | Query | int | 是 | 页码 |
| pageSize | Query | int | 是 | 每页条数 |
| category | Query | int | 否 | 帖子分类 |
| status | Query | int | 否 | 审核状态：0-待审核，1-已通过，2-已驳回 |
| userId | Query | long | 否 | 指定用户的帖子 |
| keyword | Query | string | 否 | 标题/内容模糊搜索 |
| orderBy | Query | int | 否 | 排序：1-最新，2-热度 |

- 返回 `PageResult<AppPostRespVO>`，单条字段见 3.2。

### 3.2 帖子详情 `GET /admin/forum/post/get?id=`（权限 `forum:post:query`）

- Query 参数：`id` 帖子 ID；
- 返回字段参考 `AppPostRespVO`，主要包括：`id/userId/uid/nickname/avatar/title/content/category/categoryName/imageUrls/anonymous/schoolOnly/status/isTop/likeCount/commentCount/followCount/viewCount/latestCommentTime/createTime/liked/followed`。

返回示例：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "id": 501,
    "userId": 1001,
    "uid": "U202401",
    "nickname": "张三",
    "title": "求助：Java 复习资料",
    "content": "有没有期末重点？",
    "category": 2,
    "categoryName": "求助",
    "imageUrls": ["https://cdn.example.com/p1.jpg"],
    "anonymous": false,
    "schoolOnly": false,
    "status": 1,
    "isTop": false,
    "likeCount": 12,
    "commentCount": 3,
    "followCount": 1,
    "viewCount": 128,
    "latestCommentTime": "2024-04-18 19:20:00",
    "createTime": "2024-04-18 18:00:00",
    "liked": false,
    "followed": false
  }
}
```

### 3.3 删除帖子 `DELETE /admin/forum/post/delete?id=`（权限 `forum:post:delete`）

- Query 参数：`id` 帖子 ID；
- 直接删除目标帖子（不校验归属），并减少对应发布者的发帖数。

返回示例：

```json
{ "code": 0, "msg": "", "data": true }
```

### 3.4 帖子复审 `POST /admin/forum/post/review`（权限 `forum:post:review`）

- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 帖子 ID |
| approve | bool | 是 | 是否通过（true 通过，false 驳回） |

- 作用：将帖子状态置为通过或驳回，不校验归属。
- 返回示例：`{ "code": 0, "msg": "", "data": true }`

分页示例：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "total": 1,
    "list": [
      {
        "id": 123,
        "title": "校园马拉松",
        "category": 2,
        "categoryName": "文体活动",
        "status": 1,
        "statusName": "报名中",
        "needApproval": true,
        "schoolOnly": false,
        "viewCount": 342,
        "likeCount": 12,
        "createTime": "2024-04-20 14:05:11",
        "signedUp": false,
        "checkedIn": false
      }
    ]
  }
}
```

### 2.5 删除活动 `DELETE /forum/activity/delete?id=`（权限 `forum:activity:delete`）

- Query 参数：`id` 活动 ID。
- 仅允许活动创建者本人删除，否则返回 `ACTIVITY_DELETE_FAIL_NOT_OWNER`。
- 成功返回：`{ "code": 0, "msg": "", "data": true }`

### 2.6 活动报名列表 `GET /forum/activity/sign-up/page`（权限 `forum:activity-sign-up:query`）

| 字段 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| pageNo | Query | int | 是 | 页码 |
| pageSize | Query | int | 是 | 每页条数 |
| activityId | Query | long | 否 | 活动 ID |
| approvalStatus | Query | int | 否 | 审核状态：0=待审核、1=通过、2=拒绝 |

- `activityId` 若为空则会抛出 `ACTIVITY_NOT_EXISTS`。
- 只有活动创建者可查询该活动报名列表（否则 `ACTIVITY_DELETE_FAIL_NOT_OWNER`）。
- 返回 `PageResult<AppActivitySignUpRespVO>`。

`AppActivitySignUpRespVO` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | long | 报名 ID |
| activityId | long | 活动 ID |
| activityTitle | string | 活动标题 |
| coverImage | string | 活动封面图 |
| needCheckIn | bool | 是否需要签到 |
| userId | long | 报名用户 ID |
| uid | string | 报名用户 UID |
| nickname | string | 昵称 |
| avatar | string | 头像 |
| remark | string | 报名备注 |
| approvalStatus | int | 审核编码 |
| approvalStatusName | string | 审核名称 |
| approvalRemark | string | 审核备注 |
| checkedIn | bool | 是否已签到 |
| checkInTime | string | 签到时间 |
| createTime | string | 报名时间 |

分页返回示例：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "total": 1,
    "list": [
      {
        "id": 1,
        "activityId": 123,
        "activityTitle": "校园马拉松",
        "coverImage": "https://cdn.example.com/activity-cover.png",
        "needCheckIn": true,
        "userId": 42,
        "uid": "U10001",
        "nickname": "李四",
        "avatar": "https://cdn.example.com/avatar.png",
        "remark": "希望参加",
        "approvalStatus": 0,
        "approvalStatusName": "待审核",
        "approvalRemark": null,
        "checkedIn": false,
        "checkInTime": null,
        "createTime": "2024-04-25 09:20:00"
      }
    ]
  }
}
```

### 2.7 审核报名 `POST /forum/activity/approve-sign-up`（权限 `forum:activity-sign-up:approve`）

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| signUpId | long | 是 | 报名记录 ID |
| approvalStatus | int | 是 | 审核状态：1=通过、2=拒绝 |
| approvalRemark | string | 否 | 审核备注 |

- 只有活动创建者可以审核，传入不合法 `approvalStatus` 会报 `ACTIVITY_TIME_INVALID`（即不在 `1/2` 范围）。
- 审核通过时会自动累加报名人数与积分；拒绝且之前是已通过状态会扣减人数。

请求示例：

```json
{
  "signUpId": 1,
  "approvalStatus": 1,
  "approvalRemark": "通过"
}
```

返回示例：

```json
{ "code": 0, "msg": "", "data": true }
```
