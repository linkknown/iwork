package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.BaseNode;

import java.util.LinkedList;
import java.util.List;

public class PanicErrorNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.BOOL_PREFIX + "panic_expression", "抛出异常的条件,值为 bool 类型!"));
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "panic_errorMsg?", "抛出异常的信息,值为字符串类型!"));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        Boolean expression = (Boolean) this.getTmpDataMap().get(Constants.BOOL_PREFIX + "panic_expression");
        if (expression) {
            String errorMsg = ((String) this.getTmpDataMap().get(Constants.STRING_PREFIX + "panic_errorMsg?"));
            throw new IWorkException(errorMsg);
        }
    }
}
