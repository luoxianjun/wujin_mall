package cn.iocoder.yudao.module.forum.service.config;

import cn.iocoder.yudao.module.forum.dal.dataobject.config.ForumConfigDO;

import java.util.List;

/**
 * 论坛配置 Service 接口
 *
 * @author forum
 */
public interface ForumConfigService {

    /**
     * 根据配置键获取配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    String getValueByKey(String key);

    /**
     * 根据配置键获取配置
     *
     * @param key 配置键
     * @return 配置对象
     */
    ForumConfigDO getByKey(String key);

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<ForumConfigDO> getAll();

    /**
     * 创建或更新配置
     *
     * @param key   配置键
     * @param value 配置值
     * @param name  配置名称
     * @param type  配置类型
     * @param remark 配置描述
     * @return 配置ID
     */
    Long saveConfig(String key, String value, String name, String type, String remark);

    /**
     * 删除配置
     *
     * @param key 配置键
     */
    void deleteByKey(String key);

}
