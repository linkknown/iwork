package com.linkknown.iwork.core.exception;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IWorkException extends Exception {

    private String workStepName;        // 记录步骤名称
    private boolean recordedFlag;       // 是否已记录日志

    public IWorkException(String msg) {
        super(msg);
    }

    public static IWorkException wrapException (String msg, String workStepName, Exception cause) {
        IWorkException exception = new IWorkException(msg).setWorkStepName(workStepName);
        exception.initCause(cause);
        return exception;
    }

}
