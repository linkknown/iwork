package com.linkknown.iwork.util;

import org.apache.commons.lang3.StringUtils;

public class HightLightUtil {

    /**
     * 高亮显示信息
     * @param detail
     * @return
     */
    public static String hightLight(String detail) {
        String exceptionStr = StringUtil.getNoRepeatSubStringWithRegexp(detail, "IWorkException\\(.*\\)").stream().findFirst().orElse(null);
        if (StringUtils.isNotEmpty(exceptionStr)) {
            detail = StringUtils.replace(detail, exceptionStr, String.format("<span style='color:red;'>%s</span>", exceptionStr));
        }
        String causedBy = StringUtil.getNoRepeatSubStringWithRegexp(detail, "Caused by:.*").stream().findFirst().orElse(null);
        if (StringUtils.isNotEmpty(causedBy)) {
            detail = StringUtils.replace(detail, causedBy, String.format("<span style='color:red;'>%s</span>", causedBy));
        }
        return detail;
    }
}
