-- 修复论坛帖子多选分类：缺少 categories 列导致查询报错
ALTER TABLE `forum_post`
    ADD COLUMN `categories` text DEFAULT NULL COMMENT '帖子分类列表，JSON 数组' AFTER `category`;
