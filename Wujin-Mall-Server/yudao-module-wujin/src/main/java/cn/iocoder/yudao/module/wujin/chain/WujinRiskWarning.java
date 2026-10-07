package cn.iocoder.yudao.module.wujin.chain;

public class WujinRiskWarning {

    private final boolean required;
    private final String title;
    private final String message;

    public WujinRiskWarning(boolean required, String title, String message) {
        this.required = required;
        this.title = title;
        this.message = message;
    }

    public static WujinRiskWarning none() {
        return new WujinRiskWarning(false, null, null);
    }

    public boolean isRequired() {
        return required;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
