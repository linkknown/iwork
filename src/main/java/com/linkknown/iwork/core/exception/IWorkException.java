package com.linkknown.iwork.core.exception;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IWorkException extends Exception {

    private String workStepName;

    public IWorkException(String msg) {
        super(msg);
    }


}
