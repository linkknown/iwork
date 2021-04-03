package com.linkknown.iwork.listener;

import com.linkknown.iwork.common.GlobalThreadPoolFactory;
import com.linkknown.iwork.core.Memory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

//        prepareRegistForEvent();

        // 开启线程加载 memory
        GlobalThreadPoolFactory.getCommonPool().execute(Memory::flushInitMemoryAll);
    }

//    private void prepareRegistForEvent(Register register) {
//        register.regist();
//    }
}
