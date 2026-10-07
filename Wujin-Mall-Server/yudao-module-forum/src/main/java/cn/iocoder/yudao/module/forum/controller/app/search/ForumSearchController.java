package cn.iocoder.yudao.module.forum.controller.app.search;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.forum.controller.app.post.vo.AppPostRespVO;
import cn.iocoder.yudao.module.forum.controller.app.search.vo.AppPostSearchReqVO;
import cn.iocoder.yudao.module.forum.service.search.ForumSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 搜索")
@RestController
@RequestMapping("/forum/search")
@Validated
public class ForumSearchController {

    @Resource
    private ForumSearchService searchService;

    @GetMapping("/posts")
    @Operation(summary = "搜索帖子")
    public CommonResult<PageResult<AppPostRespVO>> searchPosts(@Valid AppPostSearchReqVO reqVO) {
        return success(searchService.searchPosts(reqVO, SecurityFrameworkUtils.getLoginUserId()));
    }

    @PostMapping("/reindex")
    @Operation(summary = "重建搜索索引", description = "删除旧的 ES 索引，重新创建 mapping 并同步所有帖子数据")
    public CommonResult<String> reindexAllPosts() {
        int count = searchService.reindexAllPosts();
        return success("重建索引完成，共同步 " + count + " 条帖子");
    }

}
