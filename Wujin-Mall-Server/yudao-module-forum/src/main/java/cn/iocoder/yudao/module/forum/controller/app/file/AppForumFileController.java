package cn.iocoder.yudao.module.forum.controller.app.file;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.infra.service.file.InfraImageAuditService;
import cn.iocoder.yudao.module.infra.controller.app.file.vo.AppFileUploadReqVO;
import cn.iocoder.yudao.module.infra.framework.file.core.utils.FileTypeUtils;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.IMAGE_AUDIT_FAILED;

/**
 * 论坛文件上传（带图片审核）
 *
 * @author codex
 */
@Tag(name = "用户 APP - 论坛文件")
@RestController
@RequestMapping("/forum/file")
@Validated
@Slf4j
public class AppForumFileController {

    @Resource
    private FileService fileService;

    @Resource
    private InfraImageAuditService imageAuditService;

    @PostMapping("/upload-image")
    @Operation(summary = "上传论坛图片（自动内容审核）")
    @PermitAll
    public CommonResult<String> uploadForumImage(@Valid AppFileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        byte[] content = IoUtil.readBytes(file.getInputStream());

        String mimeType = FileTypeUtils.getMineType(content, file.getOriginalFilename());
        if (StrUtil.isEmpty(mimeType)) {
            mimeType = file.getContentType();
        }
        if (StrUtil.isNotBlank(mimeType) && !StrUtil.startWithIgnoreCase(mimeType, "image")) {
            throw ServiceExceptionUtil.exception(IMAGE_AUDIT_FAILED, "仅支持上传图片文件");
        }

        imageAuditService.auditImage(content, file.getOriginalFilename(), mimeType);
        String directory = StrUtil.blankToDefault(uploadReqVO.getDirectory(), "forum");

        return success(fileService.createFile(content, file.getOriginalFilename(), directory, mimeType));
    }

}
