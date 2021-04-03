package com.linkknown.iwork.core.node.http;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.ParamHelper;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.util.HttpUtil;
import com.linkknown.iwork.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@AutoRegistry
public class HttpRequestParserNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "headers?", "解析的请求头参数,多个参数使用逗号分隔"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "cookies?", "解析的 cookies 参数,多个参数使用逗号分隔"));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta("ip", ""));
        return this.buildParamOutputSchema(paramMetaList);
    }

    @Override
    public Param.ParamOutputSchema getRuntimeParamOutputSchema() throws IWorkException {
        String headers = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "headers?", this.getWorkStep());
        String cookies = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "headers?", this.getWorkStep());

        Param.ParamOutputSchema paramOutputSchema = new Param.ParamOutputSchema();

        quickBuildParamOutputSchema(headers, paramOutputSchema, "header_");
        quickBuildParamOutputSchema(cookies, paramOutputSchema, "cookie_");

        return paramOutputSchema;
    }

    private void quickBuildParamOutputSchema(String strs, Param.ParamOutputSchema paramOutputSchema, String prefix) {
        String[] strArr = StringUtils.split(strs, ",");
        for (String str : strArr) {
            Param.ParamOutputSchemaItem item = new Param.ParamOutputSchemaItem();
            item.setParamName(prefix + str);
            paramOutputSchema.getParamOutputSchemaItems().add(item);
        }
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        Map<String, Object> paramMap = new HashMap<>();
        HttpServletRequest request = (HttpServletRequest) this.getDispatcher().getTmpDataMap().get(Constants.HTTP_REQUEST_OBJECT);
        String headers = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "headers?", this.getWorkStep());
        String cookies = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "headers?", this.getWorkStep());

        String[] headerArr = StringUtils.split(headers, ",");
        String[] cookieArr = StringUtils.split(cookies, ",");
        for (String header : headerArr) {
            paramMap.put("header_" + header, request.getHeader(header));
        }
        for (String cookie : cookieArr) {
            paramMap.put("cookie_" + cookie, HttpUtil.getCookieFromRequest(request, cookie));
        }
        paramMap.put("ip", HttpUtil.getIpAddress(request));
        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
    }
}
