package cn.iocoder.yudao.module.forum.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 论坛配置 DO
 * 
 * 使用 @TenantIgnore 忽略租户过滤，配置表不区分租户
 *
 * @author forum
 */
@TableName("forum_config")
@KeySequence("forum_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class ForumConfigDO extends BaseDO {

    /**
     * 配置ID
     */
    @TableId
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值（支持富文本等长内容）
     */
    private String configValue;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 配置描述
     */
    private String remark;

    /**
     * 配置类型：text-普通文本, rich_text-富文本, json-JSON
     */
    private String type;

}
