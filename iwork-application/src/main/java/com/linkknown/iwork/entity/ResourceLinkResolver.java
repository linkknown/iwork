package com.linkknown.iwork.entity;

import com.linkknown.iwork.service.GlobalVarService;
import com.linkknown.iwork.util.ApplicationContextUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ResourceLinkResolver {

    private GlobalVarService globalVarService = ApplicationContextUtil.getBean(GlobalVarService.class);

    private int appId;
    private String resourceLink;

    // resourceLink 专用分隔符
    public static final String SEP = "|||";

    public static ResourceLinkResolver getResolver (int appId, String resourceLink) {
        ResourceLinkResolver resolver = new ResourceLinkResolver();
        resolver.setResourceLink(resourceLink);
        resolver.setAppId(appId);
        return resolver;
    }

    // 检查 resourceLink 中的全局变量并进行替换，再按照分隔符进行分割
    public String[] resolveAsPartFromGlobalVar () {
        String[] resourceLinkArr = StringUtils.splitByWholeSeparator(resourceLink, SEP);
        for (int i=0; i< resourceLinkArr.length; i++) {
            resourceLinkArr[i] = globalVarService.getGlobalValueForGlobalVariable(appId, resourceLinkArr[i]);
        }
        return resourceLinkArr;
    }
}
