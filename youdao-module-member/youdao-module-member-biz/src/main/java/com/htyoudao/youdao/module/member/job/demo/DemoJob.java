package com.htyoudao.youdao.module.member.job.demo;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class DemoJob {

    @XxlJob("demoJob")
//    @TenantJob
    public void execute() {
        System.out.println("美滋滋");
    }

}
