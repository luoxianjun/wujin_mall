package cn.iocoder.yudao.module.forum.framework.sign;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * 签到周期 key 生成工具
 */
public class PeriodKeyUtils {

    public static String weekKey(LocalDate date) {
        WeekFields wf = WeekFields.ISO;
        int weekOfYear = date.get(wf.weekOfWeekBasedYear());
        int year = date.get(wf.weekBasedYear());
        return year + "W" + weekOfYear;
    }

    public static String monthKey(LocalDate date) {
        return date.getYear() + "M" + String.format("%02d", date.getMonthValue());
    }

    public static String buildKey(LocalDate date, PeriodType type) {
        if (type == PeriodType.WEEK) {
            return weekKey(date);
        }
        return date.getYear() + "M" + String.format("%02d", date.getMonthValue());
    }

    public enum PeriodType {
        WEEK(1),
        MONTH(2);

        private final int code;

        PeriodType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
