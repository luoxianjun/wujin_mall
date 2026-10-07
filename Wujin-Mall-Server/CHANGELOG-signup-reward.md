# 活动报名奖励积分功能 - 修复说明

## 问题描述
Web端活动管理，创建活动时设置"报名奖励积分"为500，但用户报名后没有获得积分。

## 问题原因
报名奖励积分功能被注释禁用了。代码中有两处注释：
```java
// 注：活动报名不再给积分，只有签到和参加完活动才给积分
// pointService.addActivitySignUpPoint(userId, reqVO.getActivityId().toString());
```

## 解决方案

### 1. 恢复报名奖励功能
取消注释，恢复报名时发放积分的逻辑。

### 2. 修改积分服务
将固定积分值改为动态值，使用活动设置的 `pointAmount`。

**修改前：**
```java
void addActivitySignUpPoint(Long userId, String bizId);
// 实现中使用固定值 ACTIVITY_SIGN_UP_POINT = 5
```

**修改后：**
```java
void addActivitySignUpPoint(Long userId, Integer point, String bizId);
// 实现中使用活动设置的积分值
```

### 3. 两种报名流程

#### 流程1：不需要审核
1. 用户提交报名
2. 立即增加报名人数
3. **立即发放报名奖励积分**（如500积分）
4. 增加用户活动数

#### 流程2：需要审核
1. 用户提交报名（状态：待审核）
2. 管理员审核通过
3. 增加报名人数
4. **发放报名奖励积分**（如500积分）

## 修改文件清单

### 后端 (4个文件)
1. `ForumPointService.java` - 修改方法签名，添加 `point` 参数
2. `ForumPointServiceImpl.java` - 实现动态积分发放
3. `ForumActivityServiceImpl.java` - 恢复报名奖励逻辑（两处）
   - `signUpActivity()` - 不需要审核时发放
   - `approveSignUp()` - 审核通过时发放

### 前端 (3个文件)
1. `data.ts` - 添加表单字段提示信息
2. 无需其他修改（标签已正确）

## 积分发放条件
```java
if (Boolean.TRUE.equals(activity.getNeedPoint())
    && activity.getPointAmount() != null
    && activity.getPointAmount() > 0) {
    pointService.addActivitySignUpPoint(userId, activity.getPointAmount(), activityId);
}
```

## 测试步骤

### 场景1：不需要审核
1. 创建活动，设置"报名奖励积分"开关为"是"，奖励积分为500
2. 用户报名活动
3. 查看用户积分记录，应该立即获得500积分

### 场景2：需要审核
1. 创建活动，设置"需要审核"为"是"，"报名奖励积分"为500
2. 用户报名活动（此时不发放积分）
3. 管理员审核通过
4. 查看用户积分记录，应该获得500积分

## 字段说明
- `needPoint` (Boolean) - 是否开启报名奖励积分
- `pointAmount` (Integer) - 报名奖励的积分数量

## 注意事项
1. 只有 `needPoint=true` 且 `pointAmount>0` 时才发放积分
2. 审核拒绝不发放积分
3. 取消报名不退还积分（已发放的积分保留）
