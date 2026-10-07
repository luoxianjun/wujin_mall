package cn.iocoder.yudao.module.forum.controller.admin.activity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理后台 - 活动报名动态导出数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivitySignUpExportData {

    private List<List<String>> head;

    private List<List<Object>> rows;

}
