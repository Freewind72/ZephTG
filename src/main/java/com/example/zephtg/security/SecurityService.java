package com.example.zephtg.security;

import com.example.zephtg.config.ConfigManager;
import com.example.zephtg.logging.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SecurityService {
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired
    private LogManager logManager;
    
    // 存储IP地址的请求计数 (分钟级别)
    private final ConcurrentHashMap<String, RequestCounter> minuteCounters = new ConcurrentHashMap<>();
    // 存储IP地址的请求计数 (小时级别)
    private final ConcurrentHashMap<String, RequestCounter> hourCounters = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 启动清理过期计数器的任务
        Thread cleanupThread = new Thread(this::cleanupExpiredCounters);
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public boolean isRequestAllowed(String clientIp, String loginKey) {
        // 如果提供了有效的管理员密钥，则允许无限制访问
        if (loginKey != null && configManager.isAdminKeyValid(loginKey)) {
            return true;
        }
        
        return checkRateLimits(clientIp);
    }

    private boolean checkRateLimits(String clientIp) {
        int maxPerHour = configManager.getConfig().getSecurity().getRateLimitPerHour();
        
        long currentTime = System.currentTimeMillis();
        
        // 使用compute方法确保线程安全
        RequestCounter hourCounter = hourCounters.compute(clientIp, (k, existingCounter) -> {
            if (existingCounter == null || existingCounter.isExpired(currentTime)) {
                return new RequestCounter(currentTime, 3600000); // 3600秒
            }
            return existingCounter;
        });
        
        // 使用原子操作确保线程安全
        int currentCount = hourCounter.incrementAndGet();
        if (currentCount > maxPerHour) {
            logManager.warn(String.format("IP %s 超过小时请求限制 (%d/%d)", 
                clientIp, currentCount, maxPerHour));
            return false;
        }
        
        return true;
    }

    public void logRequest(String clientIp, String category, boolean success) {
        String status = success ? "SUCCESS" : "BLOCKED";
        logManager.info(String.format("API请求 [%s] IP: %s, 分类: %s", status, clientIp, category));
    }

    private void cleanupExpiredCounters() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(60000); // 每分钟清理一次
                
                long currentTime = System.currentTimeMillis();
                
                // 清理过期的分钟计数器
                minuteCounters.entrySet().removeIf(entry -> 
                    entry.getValue().isExpired(currentTime));
                
                // 清理过期的小时计数器
                hourCounters.entrySet().removeIf(entry -> 
                    entry.getValue().isExpired(currentTime));
                    
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logManager.error("清理过期计数器时出错", e);
            }
        }
    }

    // 内部类用于跟踪请求计数
    private static class RequestCounter {
        private final long startTime;
        private final long expireTime;
        private final AtomicInteger count = new AtomicInteger(0);
        
        public RequestCounter(long startTime, long durationMs) {
            this.startTime = startTime;
            this.expireTime = startTime + durationMs;
        }
        
        public int incrementAndGet() {
            return count.incrementAndGet();
        }
        
        public int getCount() {
            return count.get();
        }
        
        public boolean isExpired(long currentTime) {
            return currentTime > expireTime;
        }
    }
    
    public int getRemainingRequestsPerHour(String clientIp) {
        RequestCounter counter = hourCounters.get(clientIp);
        if (counter == null) {
            return configManager.getConfig().getSecurity().getRateLimitPerHour();
        }
        
        int used = counter.getCount();
        int max = configManager.getConfig().getSecurity().getRateLimitPerHour();
        return Math.max(0, max - used);
    }
    
    public boolean isAdminRequest(String loginKey) {
        return loginKey != null && configManager.isAdminKeyValid(loginKey);
    }
}