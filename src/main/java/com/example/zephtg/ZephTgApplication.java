package com.example.zephtg;

import com.example.zephtg.config.ConfigManager;
import com.example.zephtg.logging.ConsoleLoggerInterceptor;
import com.example.zephtg.logging.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
@EnableScheduling
public class ZephTgApplication {

    @Autowired
    private LogManager logManager;
    
    // 移除ConsoleLoggerInterceptor的自动装配以避免循环依赖
    
    @Autowired
    private ConfigManager configManager;

    public static void main(String[] args) {
        SpringApplication.run(ZephTgApplication.class, args);
    }
    
    @PreDestroy
    public void onShutdown() {
        logManager.shutdown();
    }
}
