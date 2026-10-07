package cn.iocoder.yudao.module.forum.controller.app.search.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 帖子搜索 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppPostSearchReqVO extends PageParam {

    @Schema(description = "搜索关键字", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java")
    private String keyword;

}
