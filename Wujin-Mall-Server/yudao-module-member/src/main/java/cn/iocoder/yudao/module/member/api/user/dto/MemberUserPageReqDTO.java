package cn.iocoder.yudao.module.member.api.user.dto;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberUserPageReqDTO extends PageParam {

    /**
     * 用户昵称，模糊匹配
     */
    private String nickname;

}
