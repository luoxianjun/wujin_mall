# 用户认证状态修改指南

## 认证相关字段

用户认证信息存储在 `forum_user_profile` 表中：

| 字段 | 类型 | 说明 |
|------|------|------|
| `school_email_verified` | bit(1) | **是否完成学校邮箱认证**（核心字段）<br>- `b'0'` = 未认证<br>- `b'1'` = 已认证 |
| `school_email_verify_time` | datetime | 学校邮箱认证通过时间 |
| `school_name` | varchar(64) | 学校名称 |
| `school_email` | varchar(128) | 认证的学校邮箱 |
| `school_email_prefix` | varchar(64) | 学校邮箱前缀（@之前的部分）|
| `real_name` | varchar(64) | 真实姓名（认证后填写）|

## 快速修改方法

### 方法1：通过用户ID修改（推荐）

```sql
UPDATE `forum_user_profile`
SET `school_email_verified` = b'1',
    `school_email_verify_time` = NOW()
WHERE `user_id` = 1;  -- 替换为实际的用户ID
```

### 方法2：通过UID修改

```sql
UPDATE `forum_user_profile`
SET `school_email_verified` = b'1',
    `school_email_verify_time` = NOW()
WHERE `uid` = 'U123456';  -- 替换为实际的UID
```

### 方法3：通过昵称修改

```sql
UPDATE `forum_user_profile`
SET `school_email_verified` = b'1',
    `school_email_verify_time` = NOW()
WHERE `nickname` = '测试用户';  -- 替换为实际的昵称
```

## 完整认证信息设置

如果需要设置完整的认证信息：

```sql
UPDATE `forum_user_profile`
SET
    `school_email_verified` = b'1',
    `school_email_verify_time` = NOW(),
    `school_name` = '清华大学',
    `school_email` = 'zhangsan@tsinghua.edu.cn',
    `school_email_prefix` = 'zhangsan',
    `real_name` = '张三'
WHERE `user_id` = 1;
```

## 查询用户认证状态

### 查看所有用户

```sql
SELECT
    `user_id`,
    `uid`,
    `nickname`,
    `school_email_verified`,
    `school_email_verify_time`,
    `school_name`,
    `real_name`
FROM `forum_user_profile`
WHERE `deleted` = b'0';
```

### 查看特定用户

```sql
SELECT * FROM `forum_user_profile`
WHERE `user_id` = 1;
```

## 取消认证

```sql
UPDATE `forum_user_profile`
SET
    `school_email_verified` = b'0',
    `school_email_verify_time` = NULL
WHERE `user_id` = 1;
```

## 批量操作

### 批量设置所有用户为已认证（测试环境）

```sql
UPDATE `forum_user_profile`
SET
    `school_email_verified` = b'1',
    `school_email_verify_time` = NOW()
WHERE `deleted` = b'0';
```

⚠️ **警告**：生产环境请谨慎使用批量操作！

## 执行步骤

1. **连接数据库**
   ```bash
   mysql -u root -p
   ```

2. **选择数据库**
   ```sql
   USE your_database_name;
   ```

3. **查询用户ID**（如果不知道）
   ```sql
   SELECT user_id, uid, nickname FROM forum_user_profile WHERE nickname LIKE '%关键词%';
   ```

4. **执行更新SQL**
   ```sql
   UPDATE forum_user_profile SET school_email_verified = b'1' WHERE user_id = 1;
   ```

5. **验证结果**
   ```sql
   SELECT user_id, nickname, school_email_verified FROM forum_user_profile WHERE user_id = 1;
   ```

## 注意事项

1. **核心字段**：`school_email_verified` 是判断用户是否认证的核心字段
2. **时间字段**：建议同时设置 `school_email_verify_time` 为当前时间
3. **可选字段**：`school_name`、`school_email`、`real_name` 等字段可选填
4. **备份数据**：修改前建议备份数据库
5. **测试验证**：修改后在应用中验证用户认证状态是否生效

## 相关SQL脚本

完整的SQL脚本文件：`sql/mysql/manual/update-user-verification.sql`
