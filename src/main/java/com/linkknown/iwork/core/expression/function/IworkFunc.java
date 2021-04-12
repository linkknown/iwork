package com.linkknown.iwork.core.expression.function;

import com.linkknown.iwork.core.exception.IWorkException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class IworkFunc {
    public static Object executeFuncCaller(FuncCaller funcCaller, List<Object> args) throws IWorkException {
        try {
            // 将 funcName 首字母变成大写
            // 执行函数
            return invoke(funcCaller.getFuncName(), args);
        } catch (Exception e) {
            String errorMsg = String.format("caller.FuncName is %s, caller.FuncArgs %s, %s",
                    funcCaller.getFuncName(), funcCaller.getFuncArgs(), e.getMessage());
            throw IWorkException.wrapException(errorMsg, null, e);
        }
    }

    // 执行函数
    private static Object invoke(String funcName, List<Object> args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        IWorkFuncProxy proxy = new IWorkFuncProxy();
            funcName = adjust(funcName);
            // 调用可变参数方法
            Method method = proxy.getClass().getDeclaredMethod(funcName, Object[].class);
            method.setAccessible(true);
            //可变参数必须这样封装,因为java反射内部实现做了参数个数为1的判断,如果参数长度不为 1,则会抛出异常
        return method.invoke(proxy, new Object[]{args.toArray()});
    }

    private static String adjust(String funcName) {
        if (StringUtils.equals(funcName, "true")) {
            return "_true";
        } else if (StringUtils.equals(funcName, "false")) {
            return "_false";
        }
        return funcName;
    }


    // 编码特殊字符, // 对转义字符 \, \; \( \) 等进行编码
    public static String encodeSpecialForParamVaule(String paramVaule) {
        //paramVaule = strings.Replace(paramVaule, "\\\\n", "__newline__", -1)
        //paramVaule = strings.Replace(paramVaule, "\\(", "__leftBracket__", -1)
        //paramVaule = strings.Replace(paramVaule, "\\)", "__rightBracket__", -1)
        //paramVaule = strings.Replace(paramVaule, "\\,", "__comma__", -1)
        //paramVaule = strings.Replace(paramVaule, "\\;", "__semicolon__", -1)
        paramVaule = StringUtils.replace(paramVaule, "\\`", "__ENCODE_1__", -1);
        return paramVaule;
    }

    // 解码特殊字符
    public static String dncodeSpecialForParamVaule(String paramVaule) {
        //paramVaule = strings.Replace(paramVaule, "__newline__", "\n", -1)
        //paramVaule = strings.Replace(paramVaule, "__leftBracket__", "(", -1)
        //paramVaule = strings.Replace(paramVaule, "__rightBracket__", ")", -1)
        //paramVaule = strings.Replace(paramVaule, "__comma__", ",", -1)
        //paramVaule = strings.Replace(paramVaule, "__semicolon__", ";", -1)
        paramVaule = StringUtils.replace(paramVaule, "\\`", "__ENCODE_1__", -1);
        return paramVaule;
    }


}
