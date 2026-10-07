package cn.iocoder.yudao.module.wujin.controller.admin.merchant;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportExcelVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportPreviewRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportResultRespVO;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "商家后台 - 五金关系模板导入")
@RestController
@RequestMapping("/wujin/merchant-import")
@Validated
public class WujinMerchantImportController {

    static final int MAX_IMPORT_ROWS = 500;

    @Resource
    private WujinMerchantImportService importService;

    @GetMapping("/template")
    @Operation(summary = "下载关系导入 Excel 模板")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-import:import')")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "五金商家关系导入模板.xls", "关系数据", WujinMerchantImportExcelVO.class,
                Arrays.asList(sampleRow("天然橡胶", "原材料"), sampleRow("硫化成型", "加工工艺")));
    }

    @PostMapping("/preview-file")
    @Operation(summary = "上传关系导入 Excel 并预校验")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @Parameter(name = "templateId", description = "行业模板编号", required = true)
    @Parameter(name = "defaultProductCategoryId", description = "默认成品分类编号")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-import:import')")
    public CommonResult<WujinMerchantImportPreviewRespVO> previewFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("templateId") Long templateId,
            @RequestParam(value = "industryCode", required = false) String industryCode,
            @RequestParam(value = "defaultProductCategoryId", required = false) Long defaultProductCategoryId)
            throws IOException {
        List<WujinMerchantImportExcelVO> excelRows = ExcelUtils.read(file, WujinMerchantImportExcelVO.class);
        if (excelRows.size() > MAX_IMPORT_ROWS) {
            throw new IllegalArgumentException("单次最多导入 " + MAX_IMPORT_ROWS + " 行");
        }
        WujinMerchantImportReqVO reqVO = new WujinMerchantImportReqVO();
        reqVO.setTemplateId(templateId);
        reqVO.setIndustryCode(industryCode);
        reqVO.setDefaultProductCategoryId(defaultProductCategoryId);
        reqVO.setRows(toImportRows(excelRows));
        return success(importService.previewImport(fillMerchant(reqVO)));
    }

    @PostMapping("/preview")
    @Operation(summary = "预校验关系导入数据")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-import:import')")
    public CommonResult<WujinMerchantImportPreviewRespVO> preview(@Valid @RequestBody WujinMerchantImportReqVO reqVO) {
        return success(importService.previewImport(fillMerchant(reqVO)));
    }

    @PostMapping("/import")
    @Operation(summary = "确认导入关系数据：模板内关系直接生效，其余进入平台审核")
    @PreAuthorize("@ss.hasPermission('wujin:merchant-import:import')")
    public CommonResult<WujinMerchantImportResultRespVO> importRows(@Valid @RequestBody WujinMerchantImportReqVO reqVO) {
        if (reqVO.getRows() != null && reqVO.getRows().size() > MAX_IMPORT_ROWS) {
            throw new IllegalArgumentException("单次最多导入 " + MAX_IMPORT_ROWS + " 行");
        }
        return success(importService.importRows(fillMerchant(reqVO)));
    }

    /**
     * 商家后台导入时，未填写商家编号的行归属当前登录商家
     */
    private WujinMerchantImportReqVO fillMerchant(WujinMerchantImportReqVO reqVO) {
        if (reqVO.getRows() == null) {
            return reqVO;
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        for (WujinMerchantImportReqVO.ImportRow row : reqVO.getRows()) {
            if (row.getMerchantId() == null) {
                row.setMerchantId(loginUserId);
            }
        }
        return reqVO;
    }

    private List<WujinMerchantImportReqVO.ImportRow> toImportRows(List<WujinMerchantImportExcelVO> excelRows) {
        List<WujinMerchantImportReqVO.ImportRow> rows = new ArrayList<>();
        for (WujinMerchantImportExcelVO excelRow : excelRows) {
            WujinMerchantImportReqVO.ImportRow row = new WujinMerchantImportReqVO.ImportRow();
            row.setProductId(excelRow.getProductId());
            row.setProductName(excelRow.getProductName());
            row.setProductCategoryId(excelRow.getProductCategoryId());
            row.setEntityId(excelRow.getEntityId());
            row.setEntityName(excelRow.getEntityName());
            row.setRelationType(excelRow.getRelationType());
            row.setStockCount(excelRow.getStockCount());
            row.setMinOrderQuantity(excelRow.getMinOrderQuantity());
            row.setDeliveryDays(excelRow.getDeliveryDays());
            row.setServiceArea(excelRow.getServiceArea());
            row.setRemark(excelRow.getRemark());
            rows.add(row);
        }
        return rows;
    }

    private WujinMerchantImportExcelVO sampleRow(String entityName, String relationType) {
        WujinMerchantImportExcelVO row = new WujinMerchantImportExcelVO();
        row.setProductId(10001L);
        row.setProductName("高耐磨乘用车轮胎 205/55R16");
        row.setEntityName(entityName);
        row.setRelationType(relationType);
        row.setStockCount(200);
        row.setMinOrderQuantity(10);
        row.setDeliveryDays(3);
        row.setServiceArea("全国");
        row.setRemark("示例行，导入前请删除");
        return row;
    }
}
