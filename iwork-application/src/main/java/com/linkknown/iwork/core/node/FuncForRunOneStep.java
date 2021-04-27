package com.linkknown.iwork.core.node;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.run.BlockStepOrdersRunner;
import com.linkknown.iwork.core.run.Receiver;

@FunctionalInterface
public interface FuncForRunOneStep {

    Receiver execute(BlockStepOrdersRunner.RunOneStepArgs args) throws IWorkException;
}
