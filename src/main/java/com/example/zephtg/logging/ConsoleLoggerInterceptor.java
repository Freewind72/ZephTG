package com.example.zephtg.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.PrintStream;

@Component
public class ConsoleLoggerInterceptor {
    
    @Autowired
    private LogManager logManager;
    
    private static final Logger logger = LoggerFactory.getLogger(ConsoleLoggerInterceptor.class);
    
    private PrintStream originalOut;
    private PrintStream originalErr;
    private volatile boolean isLogging = false;

    @PostConstruct
    public void init() {
        try {
            // 保存原始的System.out和System.err
            originalOut = System.out;
            originalErr = System.err;
            
            logger.info("控制台日志拦截器初始化完成");
            
        } catch (Exception e) {
            logger.error("初始化控制台日志拦截器失败", e);
        }
    }

    // 提供直接写入日志的方法，避免递归调用
    public void writeConsoleOutput(String level, String message) {
        if (isLogging) return;
        
        try {
            isLogging = true;
            // 直接写入日志文件，避免通过LogManager造成递归
            LogManager.getInstance().writeDirectLog(level, message);
        } finally {
            isLogging = false;
        }
    }

    @PreDestroy
    public void restoreOriginalStreams() {
        try {
            if (originalOut != null) {
                System.setOut(originalOut);
            }
            if (originalErr != null) {
                System.setErr(originalErr);
            }
            logger.info("控制台日志拦截器已恢复原始流");
        } catch (Exception e) {
            logger.error("恢复原始控制台流失败", e);
        }
    }
}