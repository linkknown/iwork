package com.linkknown.iwork.util;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

public class IworkUtil {

    // 去除不合理的字符
    public static String trimParamValue(String paramValue) {
        // 先进行初次的 trim
        paramValue = StringUtils.trim(paramValue);
        // 去除前后的 \n
        paramValue = StringUtils.removeStart(paramValue, "\n");
        paramValue = StringUtils.removeEnd(paramValue, "\n");
        // 再进行二次 trim
        paramValue = StringUtils.trim(paramValue);
        return paramValue;
    }

    public static Object getWorkSubNameFromParamValue(String paramVaule) {
        paramVaule = StringUtils.trim(paramVaule);
        paramVaule = StringUtils.replace(paramVaule, "$WORK.", "", -1);
        paramVaule = StringUtils.replace(paramVaule, ";", "", -1);
        paramVaule = StringUtils.replace(paramVaule, "\n", "", -1);
        paramVaule = StringUtils.trim(paramVaule);
        return paramVaule;
    }


    public static String getWorkSubNameForWorkSubNode(Param.ParamInputSchema paramInputSchema) {
        for (Param.ParamInputSchemaItem item : paramInputSchema.getParamInputSchemaItems()) {
            if (StringUtils.equals(item.getParamName(), Constants.STRING_PREFIX + "work_sub")
                    && StringUtils.startsWith(StringUtils.trim(item.getParamValue()), "$WORK.")) {
                // 找到 work_sub 字段值
                return (String)getWorkSubNameFromParamValue(StringUtils.trim(item.getParamValue()));
            }
        }
        return null;
    }

    public static Object getParamValueForEntity(String appId, String paramVaule) {
        //paramValue = strings.TrimSpace(paramValue)
        //paramValue = strings.Replace(paramValue, ";", "", -1)
        //if !strings.HasPrefix(paramValue, "$Entity.") {
        //	return paramValue
        //}
        //entity_name := strings.Replace(paramValue, "$Entity.", "", -1)
        //if entity, err := models.QueryEntityByEntityName(entity_name); err == nil {
        //	return entity.EntityFieldStr
        //}
        return "";
    }

    public static String printStackTraceToHtml(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return StringUtils.replace(sw.toString(), "\n", "<br/>");
    }

}
