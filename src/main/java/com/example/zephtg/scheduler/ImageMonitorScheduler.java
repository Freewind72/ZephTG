package com.example.zephtg.scheduler;

import com.example.zephtg.config.ConfigManager;
import com.example.zephtg.image.ImageManager;
import com.example.zephtg.logging.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class ImageMonitorScheduler {
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired
    private ImageManager imageManager;
    
    @Autowired
    private LogManager logManager;
    
    private static long intervalMillis = 0;

    @PostConstruct
    public void init() {
        calculateInterval();
    }

    private void calculateInterval() {
        int intervalValue = configManager.getConfig().getMonitor().getInterval();
        String unit = configManager.getConfig().getMonitor().getUnit();
        
        try {
            switch (unit.toLowerCase()) {
                case "s":
                    intervalMillis = TimeUnit.SECONDS.toMillis(intervalValue);
                    break;
                case "m":
                    intervalMillis = TimeUnit.MINUTES.toMillis(intervalValue);
                    break;
                case "h":
                    intervalMillis = TimeUnit.HOURS.toMillis(intervalValue);
                    break;
                case "d":
                case "t":
                    intervalMillis = TimeUnit.DAYS.toMillis(intervalValue);
                    break;
                default:
                    intervalMillis = TimeUnit.MINUTES.toMillis(30); // 默认30分钟
                    logManager.warn("无效的时间单位，使用默认值30分钟");
            }
            
            logManager.info(String.format("设置图片监控间隔: %d%s (%d 毫秒)", intervalValue, unit, intervalMillis));
            
        } catch (Exception e) {
            intervalMillis = TimeUnit.MINUTES.toMillis(30);
            logManager.error("解析监控间隔失败，使用默认值30分钟", e);
        }
    }

    @Scheduled(fixedDelay = 1800000) // 默认30分钟
    public void monitorImageDirectories() {
        if (!configManager.getConfig().getMonitor().isEnabled()) {
            return;
        }
        
        try {
            logManager.info("开始执行图片目录监控任务...");
            imageManager.refreshAllCategories();
            logManager.info("图片目录监控任务完成");
        } catch (Exception e) {
            logManager.error("执行图片监控任务时出错", e);
        }
    }

    @Scheduled(fixedRate = 300000) // 每5分钟检查一次配置变化
    public void checkConfigurationChanges() {
        try {
            // 重新计算间隔（如果配置发生变化）
            calculateInterval();
        } catch (Exception e) {
            logManager.error("检查配置变化时出错", e);
        }
    }
}