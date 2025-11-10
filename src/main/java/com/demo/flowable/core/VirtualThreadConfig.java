package com.demo.flowable.core;

import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc:
 */
public class VirtualThreadConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        // 每个异步任务使用一个虚拟线程
        return command -> Thread.startVirtualThread(command);
    }
}