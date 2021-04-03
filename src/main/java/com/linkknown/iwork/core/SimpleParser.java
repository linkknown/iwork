package com.linkknown.iwork.core;

import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.run.DataStore;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.util.ApplicationContextUtil;
import com.linkknown.iwork.util.IworkUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SimpleParser {
    private String paramName;
    private String paramVaule;
    private DataStore dataStore;

    // 是直接参数,不需要函数进行特殊处理
    public Object parseParamValue(Map<String, Object>... replaceMap) throws IWorkException {
        String appId = this.getDataStore().getWorkCache().getWork().getAppId();
        this.setParamVaule(IworkFunc.dncodeSpecialForParamVaule(this.getParamVaule()));

        // 变量
        if (StringUtils.startsWith(StringUtils.upperCase(this.getParamVaule()), "$GLOBAL.")) {
            return this.parseParamVauleFromGlobalVar(appId);
        } else if (StringUtils.startsWith(StringUtils.upperCase(this.getParamVaule()), "$RESOURCE.")) {
            return this.parseParamVauleFromResource(appId);
        } else if (StringUtils.startsWith(StringUtils.upperCase(this.getParamVaule()), "$WORK.")) {
            return IworkUtil.getWorkSubNameFromParamValue(this.paramVaule);
        } else if (StringUtils.startsWith(StringUtils.upperCase(this.getParamVaule()), "$ENTITY.")) {
            return IworkUtil.getParamValueForEntity(appId, this.paramVaule);
        } else if (StringUtils.startsWith(StringUtils.upperCase(this.getParamVaule()), "$")) {
            if (replaceMap.length > 0) {
                Object paramVaule = this.parseParamVauleWithReplaceProviderNode(Integer.parseInt(appId), replaceMap);
                if (paramVaule != null) {
                    return paramVaule;
                }
            }
            return this.parseParamVauleWithPrefixNode(appId);
        } else if (StringUtils.startsWith(this.getParamVaule(), "`") && StringUtils.endsWith(this.getParamVaule(), "`")) {
            // 字符串
            return StringUtils.substring(this.getParamVaule(), 1, this.getParamVaule().length() - 1);
        } else {
            // 数字, 默认转换成 int64
            return Integer.parseInt(this.getParamVaule());
        }
    }

    private Object parseParamVauleWithReplaceProviderNode(int appId, Map<String, Object>... replaceMap) {
        Iterator<Map.Entry<String, Object>> iterator = replaceMap[0].entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String replaceProviderNodeName = entry.getKey();
            Object replaceProviderData = entry.getValue();

            replaceProviderNodeName = StringUtils.replace(replaceProviderNodeName, ";", "");
            if (StringUtils.startsWith(this.getParamName(), replaceProviderNodeName)) {
                String attr = StringUtils.replace(this.getParamVaule(), replaceProviderNodeName + ".", "", 1);
                attr = StringUtils.replace(attr, ";", "");
                return ((Map)replaceProviderData).get(attr);
            }
        }
        return null;
    }

    /**
     * paramValue 来源于前置节点
     * TODO: paramVaule 需要支持更复杂的场景,egg $prefix_node.aa.bb.cc.dd;
     * @param appId
     * @return
     * @throws IWorkException
     */
    private Object parseParamVauleWithPrefixNode(String appId) throws IWorkException {
        // 格式校验
        if (!StringUtils.startsWith(this.paramVaule, "$")) {
            throw new IWorkException(String.format("%s ~ %s is not start with $", this.paramName, this.paramVaule));
        }
        ParamVauleParser paramVauleParser = new ParamVauleParser()
                .setParamVaule(this.getParamVaule());

        String nodeName = paramVauleParser.getNodeNameFromParamValue();
        this.paramName = paramVauleParser.getParamNameFromParamValue();
        Object paramValue = this.getDataStore().getData(nodeName, paramName); // 作为直接对象, dataStore 里面可以直接获取
        if (paramValue != null) {
            return paramValue;
        }
        if (StringUtils.contains(this.paramName, ".")) {
            String _paramName = StringUtils.substring(this.paramName, 0, StringUtils.lastIndexOf(this.paramName, "."));
            Object datas = this.getDataStore().getData(nodeName, _paramName); // 作为对象属性
            if (datas == null) {
                return null; // 对象直接不存在，后续不必执行
            }
            String attr = StringUtils.substring(this.paramName, StringUtils.lastIndexOf(this.paramName, ".") + 1, this.paramName.length());
            if (datas instanceof List) {
                return ((Map)((List) datas).get(0)).get(attr);
            }
            return ((Map)datas).get(attr);
        } else {
            return paramValue;
        }
    }

    // 尽量从缓存中获取
    private Object parseParamVauleFromResource(String appId) throws IWorkException {
        String resource_name = StringUtils.removeStart(this.getParamVaule(), "$RESOURCE.");
        Resource resource = Memory.resourceMap.get(appId + "_" + resource_name);
        if (resource != null) {
            if (StringUtils.equals(resource.getResourceType(), "db")) {
                String[] resourceLinkArr = StringUtils.splitByWholeSeparator(resource.getResourceLink(), "|||");
                String resourceUrl = resourceLinkArr[0];
                String resourceUserName = resourceLinkArr[1];
                String resourcePasswd = resourceLinkArr[2];

                if (StringUtils.startsWith(resourceUrl, "$Global.")) {
                    // 重新去查询全局变量
                    this.paramVaule = resourceUrl;
                    resourceUrl = (String)this.parseParamVauleFromGlobalVar(appId);
                }
                if (StringUtils.startsWith(resourceUserName, "$Global.")) {
                    // 重新去查询全局变量
                    this.paramVaule = resourceUserName;
                    resourceUserName = (String)this.parseParamVauleFromGlobalVar(appId);
                }
                if (StringUtils.startsWith(resourcePasswd, "$Global.")) {
                    // 重新去查询全局变量
                    this.paramVaule = resourcePasswd;
                    resourcePasswd = (String)this.parseParamVauleFromGlobalVar(appId);
                }
                resource.setResourceLink(String.format("%s|||%s|||%s", resourceUrl, resourceUserName, resourcePasswd));
                return resource;
            } else if (StringUtils.equals(resource.getResourceType(), "sftp") || StringUtils.equals(resource.getResourceType(), "ssh")) {
                return resource;
            }
            return null;
        }
        throw new IWorkException(String.format("can't find resource for %s", resource_name));
    }

    // 尽量从缓存中获取
    private Object parseParamVauleFromGlobalVar(String appId) throws IWorkException {
        String gvName = StringUtils.removeStart(this.getParamVaule(), "$Global.");
        gvName = StringUtils.removeEnd(gvName, ";");
        IworkConfig iworkConfig = ApplicationContextUtil.getBean(IworkConfig.class);

        GlobalVar globalVar = Memory.globalVarMap.get(String.format("%s_%s_%s", appId, gvName, iworkConfig.getEnvOnUse()));
        if (globalVar != null) {
            return StringUtils.trim(globalVar.getValue());
        }
        globalVar = Memory.globalVarMap.get(String.format("%s_%s_%s", appId, gvName, "dev"));
        if (globalVar != null) {
            return StringUtils.trim(globalVar.getValue());
        }
        throw new IWorkException(String.format("can't find globalVar for %s", gvName));
    }
}
