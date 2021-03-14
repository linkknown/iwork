package com.linkknown.iwork.listener;

import com.linkknown.iwork.core.Memory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Thread thread = new Thread(Memory::flushInitMemoryAll);
        thread.start();
    }
}
