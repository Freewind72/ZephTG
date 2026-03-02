package com.example.zephtg.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE_PATH = "./config/config.yml";
    
    private AppConfig config;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @PostConstruct
    public void init() {
        loadOrCreateConfig();
        validateAndFixConfig();
        saveConfig();
    }

    private void loadOrCreateConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        
        if (!configFile.exists()) {
            logger.info("配置文件不存在，创建默认配置...");
            createDefaultConfig();
        } else {
            try {
                config = yamlMapper.readValue(configFile, AppConfig.class);
                logger.info("成功加载配置文件");
            } catch (IOException e) {
                logger.error("读取配置文件失败，使用默认配置", e);
                createDefaultConfig();
            }
        }
    }

    private void createDefaultConfig() {
        config = new AppConfig();
        
        // 设置默认图片分类
        config.getImage().setCategories(java.util.Arrays.asList("anime", "landscape", "starry", "nature", "art"));
        
        // 生成管理员密钥
        config.getSecurity().setAdminKey(generateSecureKey());
        
        // 创建配置目录
        try {
            Path configDir = Paths.get("./config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            logger.error("创建配置目录失败", e);
        }
        
        saveConfig();
    }

    private void validateAndFixConfig() {
        boolean configChanged = false;
        
        // 检查并补全缺失的配置项
        if (config.getApp() == null) {
            config.setApp(new AppConfig.AppInfo());
            configChanged = true;
        }
        
        if (config.getApi() == null) {
            config.setApi(new AppConfig.ApiConfig());
            configChanged = true;
        }
        
        if (config.getImage() == null) {
            config.setImage(new AppConfig.ImageConfig());
            configChanged = true;
        }
        
        if (config.getSecurity() == null) {
            config.setSecurity(new AppConfig.SecurityConfig());
            configChanged = true;
        }
        
        if (config.getLogging() == null) {
            config.setLogging(new AppConfig.LoggingConfig());
            configChanged = true;
        }
        
        if (config.getMonitor() == null) {
            config.setMonitor(new AppConfig.MonitorConfig());
            configChanged = true;
        }
        
        // 检查图片配置
        if (config.getImage().getStoragePath() == null || config.getImage().getStoragePath().isEmpty()) {
            config.getImage().setStoragePath("./images");
            configChanged = true;
        }
        
        if (config.getImage().getCategories() == null || config.getImage().getCategories().isEmpty()) {
            config.getImage().setCategories(java.util.Arrays.asList("anime", "landscape", "starry", "nature", "art"));
            configChanged = true;
        }
        
        if (config.getImage().getSupportedFormats() == null || config.getImage().getSupportedFormats().isEmpty()) {
            config.getImage().setSupportedFormats(java.util.Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));
            configChanged = true;
        }
        
        // 检查监控配置
        if (config.getMonitor().getInterval() <= 0) {
            config.getMonitor().setInterval(30);
            configChanged = true;
        }
        
        if (config.getMonitor().getUnit() == null || config.getMonitor().getUnit().isEmpty()) {
            config.getMonitor().setUnit("m");
            configChanged = true;
        }
        
        // 检查安全配置
        if (config.getSecurity().getAdminKey() == null || config.getSecurity().getAdminKey().isEmpty()) {
            config.getSecurity().setAdminKey(generateSecureKey());
            configChanged = true;
        }
        
        if (config.getSecurity().getRateLimitPerHour() <= 0) {
            config.getSecurity().setRateLimitPerHour(100);
            configChanged = true;
        }
        
        // 检查日志配置
        if (config.getLogging().getLogPath() == null || config.getLogging().getLogPath().isEmpty()) {
            config.getLogging().setLogPath("./logs");
            configChanged = true;
        }
        
        if (config.getLogging().getLevel() == null || config.getLogging().getLevel().isEmpty()) {
            config.getLogging().setLevel("INFO");
            configChanged = true;
        }
        
        // 确保必要的目录存在
        createNecessaryDirectories();
        
        if (configChanged) {
            logger.info("配置文件已更新，补充了缺失的配置项");
        }
    }

    private void createNecessaryDirectories() {
        try {
            // 创建图片存储目录
            Path imageDir = Paths.get(config.getImage().getStoragePath());
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
                logger.info("创建图片存储目录: {}", imageDir.toAbsolutePath());
            }
            
            // 为每个分类创建子目录
            if (config.getImage().getCategories() != null) {
                for (String category : config.getImage().getCategories()) {
                    Path categoryDir = imageDir.resolve(category);
                    if (!Files.exists(categoryDir)) {
                        Files.createDirectories(categoryDir);
                        logger.info("创建分类目录: {}", categoryDir.toAbsolutePath());
                    }
                }
            }
            
            // 创建日志目录
            Path logDir = Paths.get("./logs"); // 固定使用外部logs目录
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("创建日志目录: {}", logDir.toAbsolutePath());
            }
            
            // 创建配置目录
            Path configDir = Paths.get("./config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                logger.info("创建配置目录: {}", configDir.toAbsolutePath());
            }
            
        } catch (IOException e) {
            logger.error("创建必要目录失败", e);
        }
    }

    public void saveConfig() {
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            yamlMapper.writeValue(configFile, config);
            logger.info("配置文件已保存: {}", configFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("保存配置文件失败", e);
        }
    }

    private String generateSecureKey() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public AppConfig getConfig() {
        return config;
    }
    
    public String getAdminKey() {
        return config.getSecurity().getAdminKey();
    }
    
    public boolean isAdminKeyValid(String key) {
        return key != null && key.equals(config.getSecurity().getAdminKey());
    }
}