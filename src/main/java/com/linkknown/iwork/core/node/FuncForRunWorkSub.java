package com.linkknown.iwork.core.node;

import com.linkknown.iwork.core.run.Dispatcher;
import com.linkknown.iwork.core.run.Receiver;

@FunctionalInterface
public interface FuncForRunWorkSub {

    Receiver execute(String trackingId, Dispatcher dispatcher);
}
