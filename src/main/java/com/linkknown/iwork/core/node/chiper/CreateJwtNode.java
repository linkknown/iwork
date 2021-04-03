package com.linkknown.iwork.core.node.chiper;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.ParamHelper;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@AutoRegistry
public class CreateJwtNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "secretKey", "密钥"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "claimsMap", "加密参数,多个参数逗号分隔"));
        paramMetaList.add(new ParamMeta(Constants.NUMBER_PREFIX + "expireSecond", "过期秒数"));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public Param.ParamInputSchema getRuntimeParamInputSchema() throws IWorkException {
        Param.ParamInputSchema paramInputSchema = new Param.ParamInputSchema();
        String claimsMap = (String) ParamHelper.getStaticParamValueWithStep(this.getAppId(), Constants.STRING_PREFIX + "claimsMap", this.getWorkStep());
        String[] claimArr = StringUtils.splitByWholeSeparator(claimsMap, ",");
        for (String claim : claimArr) {
            paramInputSchema.getParamInputSchemaItems().add(
                    new Param.ParamInputSchemaItem().setParamName(Constants.STRING_PREFIX + StringUtils.trim(claim))
            );
        }
        return paramInputSchema;
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "tokenString", ""));
        return this.buildParamOutputSchema(paramMetaList);
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        Map<String, Object> paramMap = new HashMap<>();
        HashMap<String, String> claimsMap = new HashMap<>();

        String secretKey = ((String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "secretKey"));
        String _expireSecond = ((String) this.getTmpDataMap().get(Constants.NUMBER_PREFIX + "expireSecond"));
        int expireSecond = Integer.parseInt(_expireSecond);
        String _claimsMap = (String)this.getTmpDataMap().get(Constants.STRING_PREFIX + "claimsMap");
        String[] claimArr = StringUtils.splitByWholeSeparator(_claimsMap, ",");
        for (String claim : claimArr) {
            claimsMap.put(claim, (String)this.getTmpDataMap().get(Constants.STRING_PREFIX + claim));
        }

        String tokenString = JwtUtil.createJWT(secretKey, claimsMap, expireSecond);

        paramMap.put(Constants.STRING_PREFIX + "tokenString", tokenString);
        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
    }
}
