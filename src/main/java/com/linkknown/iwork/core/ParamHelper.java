package com.linkknown.iwork.core;

import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.util.ApplicationContextUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ParamHelper {

    @Data
    @Accessors(chain = true)
    public static class ParamNameHelper {
        private String paramName;
        private WorkStep workStep;

        // 根据 ParamName 获取相对值,真值可能需要 ParamVauleHelper 处理一下
        public String parseAndGetRelativeParamValue() {
            Param.ParamInputSchema paramInputSchema = Param.ParamInputSchema.parseToParamInputSchema(this.workStep.getWorkStepInput());
            List<Param.ParamInputSchemaItem> paramInputSchemaItems = paramInputSchema.getParamInputSchemaItems();
            for (Param.ParamInputSchemaItem item : paramInputSchemaItems) {
                if (StringUtils.equals(item.getParamName(), paramName)) {
                    // 非必须参数不得为空
                    if (!StringUtils.endsWith(item.getParamName(), "?") && StringUtils.isEmpty(StringUtils.trim(item.getParamValue()))) {
                        return null;
                    }
                    return item.getParamValue();
                }
            }
            return null;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class ParamValueHelper {
        private int appId;
        private String paramValue;

        // 获取静态字段值,即无需运行流程(Runtime 运行时)的数据
        public Object getStaticParamValue() throws IWorkException {
            // sql 语句中含有 \n 不需要替换成空
            //this.removeUnsupportChars()

            if (StringUtils.startsWith(this.getParamValue(),"$RESOURCE.")) {
                String resourceName = StringUtils.trim(this.getParamValue());
                resourceName = StringUtils.replace(resourceName, "$RESOURCE.", "");
                resourceName = StringUtils.replace(resourceName, ";", "");
                resourceName = StringUtils.trim(resourceName);

                Resource resource = Memory.resourceMap.get(appId + "_" + resourceName);
                if (StringUtils.equals(resource.getResourceType(), "db")) {
                    if (StringUtils.startsWith(resource.getResourceDsn(), "$Global.")) {
                        ParamValueHelper paramValueHelper = new ParamValueHelper()
                                .setAppId(this.getAppId())
                                .setParamValue(resource.getResourceDsn());
                        return paramValueHelper.getStaticParamValue();
                    }
                    return resource.getResourceDsn();
                } else if (StringUtils.equals(resource.getResourceType(), "sftp") || StringUtils.equals(resource.getResourceType(), "ssh")) {
                    return resource;
                }
                return null;
            } else if (StringUtils.startsWith(this.getParamValue(), "$Global.")) {
                String globalVarName = StringUtils.replace(this.getParamValue(), "$Global.", "");
                globalVarName = StringUtils.replace(globalVarName, ";", "");

                IworkConfig iworkConfig = ApplicationContextUtil.getBean(IworkConfig.class);
                GlobalVar globalVar = Memory.globalVarMap.get(String.format("%d_%s_%s", appId, globalVarName, iworkConfig.getEnvOnUse()));
                GlobalVar globalVarForDev = Memory.globalVarMap.get(String.format("%d_%s_%s", appId, globalVarName, "dev"));
                if (globalVar != null) {
                    return globalVar.getValue();
                } else if (globalVarForDev != null) {
                    return globalVarForDev.getValue();
                }
                throw new IWorkException(String.format("can't find globalVar for %s", globalVarName));
            }
            return this.getParamValue();
        }

        // 去除不合理的字符
        public void removeUnsupportChars() {
            this.setParamValue(StringUtils.trim(this.getParamValue()));
            this.setParamValue(StringUtils.replace(this.getParamValue(), "\n", ""));
        }
    }

    //根据步骤和参数名称获取静态参数值
    public static Object getStaticParamValueWithStep(int appId, String paramName, WorkStep workStep) throws IWorkException {
        ParamNameHelper paramNameHelper = new ParamNameHelper()
                .setParamName(paramName)
                .setWorkStep(workStep);
        return getStaticParamValue(appId, paramNameHelper.parseAndGetRelativeParamValue());
    }

    private static Object getStaticParamValue(int appId, String paramValue) throws IWorkException {
        ParamValueHelper paramValueHelper = new ParamValueHelper()
                .setAppId(appId)
                .setParamValue(paramValue);
        return paramValueHelper.getStaticParamValue();
    }

}
