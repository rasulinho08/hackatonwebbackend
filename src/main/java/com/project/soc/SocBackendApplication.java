package com.project.soc;

import com.project.soc.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
@EnableAsync
public class SocBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocBackendApplication.class, args);
    }
}
