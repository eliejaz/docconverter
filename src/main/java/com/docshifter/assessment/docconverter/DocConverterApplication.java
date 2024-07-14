package com.docshifter.assessment.docconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableAspectJAutoProxy
@EnableCaching
public class DocConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocConverterApplication.class, args);
    }

}
