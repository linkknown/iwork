package com.linkknown.iwork.core.node.chiper;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.ParamHelper;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ParseJwtNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "tokenString", "密文"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "secretKey", "密钥"));
        paramMetaList.add(new ParamMeta(Constants.NUMBER_PREFIX + "claimsMap", "解密参数,多个参数逗号分隔"));
        paramMetaList.add(new ParamMeta(Constants.BOOL_PREFIX + "handleExpiredError?",
                "处理 jwt 数据过期错误,true 返回过期标识,false 则抛出异常,默认 false",
                new String[]{"`true`", "`false`"}));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public Param.ParamOutputSchema getRuntimeParamOutputSchema() throws IWorkException {
        Param.ParamOutputSchema paramOutputSchema = new Param.ParamOutputSchema();
        String claimsMap = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "claimsMap", this.getWorkStep());
        String[] claimArr = StringUtils.splitByWholeSeparator(claimsMap, ",");
        for (String claim : claimArr) {
            paramOutputSchema.getParamOutputSchemaItems().add(
                    new Param.ParamOutputSchemaItem().setParamName(StringUtils.trim(claim))
            );
        }
        String handleExpiredError = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.BOOL_PREFIX + "handleExpiredError?", this.getWorkStep());
        if (StringUtils.equals(handleExpiredError, "`true`") || StringUtils.equals(handleExpiredError, "true")) {
            paramOutputSchema.getParamOutputSchemaItems().add(
                    new Param.ParamOutputSchemaItem().setParamName(Constants.BOOL_PREFIX + "Expired")
            );
        }
        return paramOutputSchema;
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        Map<String, Object> paramMap = new HashMap<>();

        String tokenString = ((String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "tokenString"));
        String secretKey = ((String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "secretKey"));
        String handleExpiredError = (String)this.getTmpDataMap().get(Constants.BOOL_PREFIX + "handleExpiredError?");

        boolean isExpired = false;
        try {
            Map<String, Object> claimsMap = JwtUtil.verifyToken(secretKey, tokenString);
            paramMap.putAll(claimsMap);
        } catch (Exception e) {
            isExpired = true;
        }

        if (StringUtils.equals(handleExpiredError, "`true`") || StringUtils.equals(handleExpiredError, "true")) {
            paramMap.put(Constants.BOOL_PREFIX+"Expired", isExpired);
        }

        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
    }
}
