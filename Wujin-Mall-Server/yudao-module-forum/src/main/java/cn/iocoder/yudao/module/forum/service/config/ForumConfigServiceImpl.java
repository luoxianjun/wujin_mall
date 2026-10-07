package cn.iocoder.yudao.module.forum.service.config;

import cn.iocoder.yudao.module.forum.dal.dataobject.config.ForumConfigDO;
import cn.iocoder.yudao.module.forum.dal.mysql.config.ForumConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 论坛配置 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumConfigServiceImpl implements ForumConfigService {

    @Resource
    private ForumConfigMapper forumConfigMapper;

    @Override
    public String getValueByKey(String key) {
        ForumConfigDO config = forumConfigMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public ForumConfigDO getByKey(String key) {
        return forumConfigMapper.selectByKey(key);
    }

    @Override
    public List<ForumConfigDO> getAll() {
        log.info("[getAll][获取所有配置]");
        List<ForumConfigDO> list = forumConfigMapper.selectAll();
        log.info("[getAll][获取所有配置成功，数量={}]", list.size());
        return list;
    }

    @Override
    public Long saveConfig(String key, String value, String name, String type, String remark) {
        ForumConfigDO existConfig = forumConfigMapper.selectByKey(key);
        if (existConfig != null) {
            // 更新
            ForumConfigDO updateObj = ForumConfigDO.builder()
                    .id(existConfig.getId())
                    .configValue(value)
                    .name(name != null ? name : existConfig.getName())
                    .type(type != null ? type : existConfig.getType())
                    .remark(remark != null ? remark : existConfig.getRemark())
                    .build();
            forumConfigMapper.updateById(updateObj);
            log.info("[saveConfig][更新配置成功，key={}]", key);
            return existConfig.getId();
        } else {
            // 创建
            ForumConfigDO config = ForumConfigDO.builder()
                    .configKey(key)
                    .configValue(value)
                    .name(name)
                    .type(type != null ? type : "text")
                    .remark(remark)
                    .build();
            forumConfigMapper.insert(config);
            log.info("[saveConfig][创建配置成功，key={}, id={}]", key, config.getId());
            return config.getId();
        }
    }

    @Override
    public void deleteByKey(String key) {
        ForumConfigDO config = forumConfigMapper.selectByKey(key);
        if (config != null) {
            forumConfigMapper.deleteById(config.getId());
            log.info("[deleteByKey][删除配置成功，key={}]", key);
        }
    }

}
