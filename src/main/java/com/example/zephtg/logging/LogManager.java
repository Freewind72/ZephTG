package com.example.zephtg.logging;

import com.example.zephtg.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LogManager {
    
    private static LogManager instance;
    private static final Logger logger = LoggerFactory.getLogger(LogManager.class);
    
    @Autowired
    private ConfigManager configManager;
    
    private PrintWriter logWriter;
    private File currentLogFile;
    private final ReentrantLock logLock = new ReentrantLock();
    private LocalDateTime lastLogTime;

    @PostConstruct
    public void init() {
        instance = this;
        createNewLogFile();
    }
    
    public static LogManager getInstance() {
        return instance;
    }

    private void createNewLogFile() {
        try {
            logLock.lock();
            
            // 关闭之前的日志文件
            closeCurrentLogFile();
            
            // 生成新的日志文件名 Zeph-YYYY-MM-DD-HH-mm
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
            String logFileName = "Zeph-" + timestamp + ".log";
            File logDir = new File("./logs"); // 使用外部logs文件夹
            currentLogFile = new File(logDir, logFileName);
            
            // 创建日志文件
            if (!currentLogFile.getParentFile().exists()) {
                currentLogFile.getParentFile().mkdirs();
            }
            
            logWriter = new PrintWriter(new FileWriter(currentLogFile, true), true);
            lastLogTime = LocalDateTime.now();
            
            info("日志系统初始化完成");
            info("当前日志文件: " + currentLogFile.getAbsolutePath());
            
        } catch (IOException e) {
            logger.error("创建日志文件失败", e);
        } finally {
            logLock.unlock();
        }
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void warn(String message) {
        log("WARN", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    public void error(String message, Throwable throwable) {
        log("ERROR", message + " - " + throwable.getMessage());
        // 记录堆栈跟踪
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        log("ERROR", "堆栈跟踪:\n" + sw.toString());
    }

    public void debug(String message) {
        if ("DEBUG".equals(configManager.getConfig().getLogging().getLogLevel())) {
            log("DEBUG", message);
        }
    }

    private void log(String level, String message) {
        try {
            logLock.lock();
            
            String logEntry = String.format("[%s] %s %s", 
                level, 
                getCurrentTimestamp(), 
                message);
            
            // 写入日志文件
            if (logWriter != null) {
                logWriter.println(logEntry);
                logWriter.flush();
            }
            
            // 输出到控制台
            if (level.equals("ERROR")) {
                System.err.println(logEntry);
            } else {
                System.out.println(logEntry);
            }
            
        } finally {
            logLock.unlock();
        }
    }
    
    // 直接写入日志文件的方法，避免递归调用
    public void writeDirectLog(String level, String message) {
        try {
            logLock.lock();
            
            String logEntry = String.format("[%s] %s %s", 
                level, 
                getCurrentTimestamp(), 
                message);
            
            // 只写入日志文件，不输出到控制台
            if (logWriter != null) {
                logWriter.println(logEntry);
                logWriter.flush();
            }
            
        } finally {
            logLock.unlock();
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void closeCurrentLogFile() {
        if (logWriter != null) {
            logWriter.close();
            logWriter = null;
        }
    }

    public void rotateLogIfNeeded() {
        // 检查是否需要轮转日志文件（基于时间或其他条件）
        LocalDateTime now = LocalDateTime.now();
        if (lastLogTime != null) {
            // 这里可以根据配置决定何时轮转日志
            // 目前简单实现：每天轮转一次
            if (!lastLogTime.toLocalDate().equals(now.toLocalDate())) {
                info("执行日志轮转");
                createNewLogFile();
            }
        }
    }

    public File getCurrentLogFile() {
        return currentLogFile;
    }

    public void shutdown() {
        info("应用程序正在关闭...");
        closeCurrentLogFile();
    }
}