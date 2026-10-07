package cn.iocoder.yudao.module.forum.service.post.listener;

import cn.iocoder.yudao.module.forum.dal.dataobject.post.ForumPostDO;
import cn.iocoder.yudao.module.forum.dal.elasticsearch.post.ForumPostESDO;
import cn.iocoder.yudao.module.forum.dal.elasticsearch.post.ForumPostRepository;
import cn.iocoder.yudao.module.forum.dal.mysql.post.ForumPostMapper;
import cn.iocoder.yudao.module.forum.event.post.ForumPostSaveEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ForumPostESListener {

    @Resource
    private ForumPostMapper forumPostMapper;

    @Resource
    private ForumPostRepository postRepository;

    @Async
    @EventListener
    public void onPostSave(ForumPostSaveEvent event) {
        log.info("[onPostSave][开始同步帖子到 ES，postId={}]", event.getId());
        ForumPostDO post = forumPostMapper.selectById(event.getId());
        if (post == null) {
            log.warn("[onPostSave][帖子不存在，跳过同步，postId={}]", event.getId());
            return;
        }

        ForumPostESDO esDO = new ForumPostESDO();
        esDO.setId(post.getId());
        esDO.setTitle(post.getTitle());
        esDO.setContent(post.getContent());
        esDO.setUserId(post.getUserId());
        esDO.setStatus(post.getStatus());
        esDO.setCreateTime(post.getCreateTime());
        esDO.setCategory(post.getCategory());
        esDO.setCategories(post.getCategories());

        postRepository.save(esDO);
        log.info("[onPostSave][同步帖子到 ES 完成，postId={}]", event.getId());
    }

}
