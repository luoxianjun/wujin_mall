package cn.iocoder.yudao.module.forum.enums.user;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 学校邮箱后缀枚举
 *
 * @author forum
 */
@Getter
@AllArgsConstructor
public enum SchoolEmailSuffixEnum {

    UM_CONNECT("@connect.um.edu.mo", "澳门大学"),
    UM("@um.edu.mo", "澳门大学"),
    MUST("@must.edu.mo", "澳门科技大学"),
    MPU("@mpu.edu.mo", "澳门理工大学"),
    CITYU("@cityu.edu.mo", "澳门城市大学"),
    UTM("@utm.edu.mo", "澳门旅游学院"),
    KWNC("@kwnc.edu.mo", "澳门镜湖护理学院"),
    STUDENT_KWNC("@stud.kwnc.edu.mo", "澳门镜湖护理学院学生"),
    USJ("@usj.edu.mo", "澳门圣若瑟大学"),
    STUDENT_MUST("@student.must.edu.mo", "澳门科技大学学生"),
    ;

    /**
     * 邮箱后缀
     */
    private final String suffix;

    /**
     * 学校名称
     */
    private final String schoolName;

    /**
     * 判断邮箱后缀是否支持
     *
     * @param email 邮箱地址
     * @return 是否支持
     */
    public static boolean isSupported(String email) {
        if (StrUtil.isBlank(email)) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(e -> email.toLowerCase().endsWith(e.getSuffix()));
    }

    /**
     * 根据邮箱获取学校名称
     *
     * @param email 邮箱地址
     * @return 学校名称
     */
    public static String getSchoolName(String email) {
        if (StrUtil.isBlank(email)) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> email.toLowerCase().endsWith(e.getSuffix()))
                .map(SchoolEmailSuffixEnum::getSchoolName)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取邮箱前缀（@之前的部分）
     *
     * @param email 邮箱地址
     * @return 邮箱前缀
     */
    public static String getEmailPrefix(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return null;
        }
        return email.substring(0, email.indexOf("@"));
    }

}
