package com.guochang.aicodegenmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class AiCodegenMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodegenMicroserviceApplication.class, args);
    }

}
