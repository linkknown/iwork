package com.linkknown.iwork.core;

import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.expression.function.FuncCaller;
import com.linkknown.iwork.expression.function.FuncExecutor;
import com.linkknown.iwork.core.run.DataStore;
import com.linkknown.iwork.util.DatatypeUtil;
import com.linkknown.iwork.util.IworkUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// iworkmodels.ParamInputSchemaItem 解析类
@Data
@Accessors(chain = true)
public class PisItemDataParser {
    DataStore dataStore;
    Param.ParamInputSchemaItem item;
    Map<String, Object> tmpDataMap;
    Map<String, String> pureTextTmpDataMap;

    public void fillPisItemDataToTmp() throws IWorkException {
        // 对必要的参数进行非空判断
        this.checkEmpty();
        this.fillPisItemDataToPureTmp();
        this.fillPisItemDataToNPureTmp();
    }

    private void fillPisItemDataToNPureTmp() throws IWorkException {
        // tmpDataMap 存储解析值
        if (this.getItem().isPureText()) {
            // 表示为纯文本则放入字符串,不再解析
            this.tmpDataMap.put(this.getItem().getParamName(), this.getItem().getParamValue());
            return;
        }
        // 判断当前参数是否是 repeat 参数
        if (!this.getItem().isRepeatable()) {
            // 输入数据存临时
            this.getTmpDataMap().put(this.getItem().getParamName(), this.parseAndGetParamVaule(this.getItem().getParamName(), this.getItem().getParamValue()));
        } else {
            this.foreachFillPisItemDataToTmp();
        }
    }

    private void foreachFillPisItemDataToTmp() throws IWorkException {
        List<Object> repeatDatas = new ArrayList<>();
        Object foreachRefer = this.getTmpDataMap().get(this.getItem().getForeachRefer());
        if (foreachRefer != null) {
            // 获取 item.ForeachRefer 对应的 repeat 切片数据,作为迭代参数,而不再从前置节点输出获取
            repeatDatas = DatatypeUtil.objectConvertToSlice(foreachRefer);
        }
        if (repeatDatas.size() > 0) {
            List<Object> paramValues = new ArrayList<>();
            for (Object repeatData : repeatDatas) {
                // 替代的节点名称、替代的对象
                String replaceProviderNodeName = RegExUtils.replaceAll(StringUtils.trim(this.pureTextTmpDataMap.get(this.getItem().getForeachRefer())), ";", "");
                Map<String, Object> replaceMap = new HashMap<>();
                replaceMap.put(replaceProviderNodeName, repeatData);
                Object paramValue = this.parseAndGetParamVaule(this.getItem().getParamName(), this.getItem().getParamValue(), replaceMap); // 输入数据存临时
                paramValues.add(paramValue);
            }
            this.getTmpDataMap().put(this.getItem().getParamName(), paramValues); // 所得值则是个切片
        } else {
            this.getTmpDataMap().put(this.getItem().getParamName(), this.parseAndGetParamVaule(this.getItem().getParamName(), this.getItem().getParamValue())); // 输入数据存临时
        }
    }

    // 属性对象
    @Data
    @Accessors(chain = true)
    public static class ObjectAttr {
        private int index;
        private String attrName;                // 对象属性名
        private String attrPureValue;           // 对象属性纯文本值
        private Object attrParseValue;          // 对象属性解析值
    }

    // 解析 paramVaule 并赋值,数据来源于前置节点(从 dataStore 中获取实际值)
    // 解析结果可能的情况有多种：单值 interface{}, 多值 []interface{}, 对象值 map[string]interface{}
    private Object parseAndGetParamVaule(String paramName, String paramValue, Map<String, Object>... replaceMap) throws IWorkException {
        // 将 paramValue 解析成对象值 []*OobjectAttrs
        List<ObjectAttr> objectAttrs = this.parseToObjectAttrs(paramValue);
        // 存储 []*OobjectAttrs 转换后的 map[string]interface{}
        Map<String, Object> objectMap = new HashMap<>();
        // 存储 []*AttrObjects 转换后的 []interface{}
        List<Object> parseValues = new ArrayList<>();
        for (ObjectAttr objectAttr : objectAttrs) {
            objectAttr.attrParseValue = this.parseParamVaule(paramName, objectAttr.attrPureValue, replaceMap);
            // 此处禁止使用 datatypeutil.InterfaceConvertToSlice(), 因为 parseValues 中的元素可以是个 interface{} 也可以是个 []interface{}
            parseValues.add(objectAttr.attrParseValue);
            objectMap.put(objectAttr.attrName, objectAttr.attrParseValue);
        }
        // 单值
        if (parseValues.size() == 1) {
            return parseValues.get(0);
        }
        // 空值
        if (parseValues.size() == 0) {
            return null;
        }
        return parseValues;
    }

    private Object parseParamVaule(String paramName, String paramValue, Map<String, Object>... replaceMap) throws IWorkException {
        try {
            List<FuncCaller> callers = this.parseToFuncCallersWithCache(paramValue);
            if (callers == null || callers.size() == 0) {
                // 是直接参数,不需要函数进行特殊处理
                SimpleParser parser = new SimpleParser()
                        .setParamName(paramName)
                        .setParamVaule(paramValue)
                        .setDataStore(this.getDataStore());

                return parser.parseParamValue(replaceMap);
            } else {
                return this.parseParamVauleWithCallers(callers, paramName, replaceMap);
            }
        } catch (IWorkException e) {
            String errorMsg = String.format("<span style='color:red;'>execute expression with expression is %s, error msg is :%s</span>",
                    paramValue, e.getMessage());
            throw IWorkException.wrapException(errorMsg, "", e);
        }


    }

    private Object parseParamVauleWithCallers(List<FuncCaller> funcCallers, String paramName, Map<String,Object>... replaceMap) throws IWorkException {
        Map<String, Object> historyFuncResultMap = new HashMap<>();
        Object lastFuncResult = null;
        // 按照顺序依次执行函数
        for (FuncCaller funcCaller : funcCallers) {
            List<Object> args = this.getCallerArgs(funcCaller, historyFuncResultMap, paramName, replaceMap);
            // 执行函数并记录结果,供下一个函数执行使用
            Object result = FuncExecutor.executeFuncCaller(funcCaller, args);
            historyFuncResultMap.put("$expression."+funcCaller.getFuncUUID(), result);
            lastFuncResult = result;
        }
        return lastFuncResult;
    }

    // 函数参数替换成实际意义上的值
    private List<Object> getCallerArgs(FuncCaller funcCaller, Map<String,Object> historyFuncResultMap,
                                       String paramName, Map<String,Object>... replaceMap) throws IWorkException {
        List<Object> args = new ArrayList<>();
        for (String arg : funcCaller.getFuncArgs()) {
            if (historyFuncResultMap.containsKey(arg)) {
                args.add(historyFuncResultMap.get(arg));
            } else {
                // 是直接参数,不需要函数进行特殊处理
                SimpleParser parser = new SimpleParser()
                        .setParamName(paramName)
                        .setParamVaule(arg)
                        .setDataStore(this.getDataStore());
                Object _arg = parser.parseParamValue(replaceMap);
                args.add(_arg);
            }
        }
        return args;
    }

    // 从缓存中获取 FuncCallers
    private List<FuncCaller> parseToFuncCallersWithCache(String paramValue) {
        List<FuncCaller> funcCallers = this.getDataStore().getWorkCache().getFuncCallerMap().get(paramValue);
        Exception exception = this.getDataStore().getWorkCache().getFuncCallerErrMap().get(paramValue);
        // 不出错就返回
        if (exception == null && funcCallers != null) {
            return funcCallers;
        }
        this.getDataStore().getWorkCache().parseToFuncCallers(paramValue);
        return this.getDataStore().getWorkCache().getFuncCallerMap().get(paramValue);
    }

    // 将 paramVaule 转行成 []*ObjectAttr
    private List<ObjectAttr> parseToObjectAttrs(String paramValue) {
        List<ObjectAttr> objectAttrs = new ArrayList<>();
        List<String> multiVals = this.getMultiValsWithCache(paramValue);
        for (int index = 0; index < multiVals.size(); index++) {
            String value = multiVals.get(index);
            String _value = IworkUtil.trimParamValue(value);
            if (StringUtils.isNotEmpty(StringUtils.trim(_value))) {
                ObjectAttr objectAttr = this.parseToObjectAttr(index, StringUtils.trim(_value));
                objectAttrs.add(objectAttr);
            }
        }
        return objectAttrs;
    }

    private ObjectAttr parseToObjectAttr(int index, String paramValue) {
        String attrName = null;
        String attrPureValue = null;
        if (StringUtils.contains(paramValue, "::")) {
            attrName = StringUtils.substring(paramValue, 0, StringUtils.indexOf(paramValue, "::"));
            attrPureValue = StringUtils.substring(paramValue, StringUtils.indexOf(paramValue, "::") + 2);
        } else if (StringUtils.contains(paramValue, "$")) {
            attrName = StringUtils.replace(StringUtils.substring(paramValue, StringUtils.lastIndexOf(paramValue, ".") + 1), ";", "");
            attrPureValue = paramValue;
        } else {
            attrName = String.valueOf(index);
            attrPureValue = paramValue;
        }
        return new ObjectAttr()
                .setIndex(index)
                .setAttrName(attrName)
                .setAttrPureValue(attrPureValue);
    }

    // 从缓存中获取 FuncCallers
    public List<String> getMultiValsWithCache(String paramValue) {
         List<String> multiVals = this.getDataStore().getWorkCache().getMultiVals().get(paramValue);
        Exception exception = this.getDataStore().getWorkCache().getMultiValError().get(paramValue);
        if (exception == null && multiVals != null) {
            return multiVals;
        }
        this.getDataStore().getWorkCache().parseToMultiVals(paramValue);
        return this.getMultiValsWithCache(paramValue);
    }

    private void fillPisItemDataToPureTmp() {
        this.getPureTextTmpDataMap().put(this.getItem().getParamName(), this.getItem().getParamValue());
    }

    private void checkEmpty() {
        // 对参数进行非空校验
        // TODO
//
//        if ok, checkResults := iworkvalid.CheckEmptyForItem(this.Item); !ok {
//            panic(strings.Join(checkResults, ";"))
//        }
    }
}
