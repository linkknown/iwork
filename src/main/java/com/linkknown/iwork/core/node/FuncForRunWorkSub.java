package com.linkknown.iwork.core.node;

import com.linkknown.iwork.core.run.Dispatcher;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.entity.Work;

@FunctionalInterface
public interface FuncForRunWorkSub {

    Receiver execute(Work work, Dispatcher dispatcher);
}
