package com.example.zephtg.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AppConfig {
    @JsonProperty("app")
    private AppInfo app = new AppInfo();
    
    @JsonProperty("api")
    private ApiConfig api = new ApiConfig();
    
    @JsonProperty("image")
    private ImageConfig image = new ImageConfig();
    
    @JsonProperty("security")
    private SecurityConfig security = new SecurityConfig();
    
    @JsonProperty("logging")
    private LoggingConfig logging = new LoggingConfig();
    
    @JsonProperty("monitor")
    private MonitorConfig monitor = new MonitorConfig();

    // Getters and Setters
    public AppInfo getApp() { return app; }
    public void setApp(AppInfo app) { this.app = app; }
    
    public ApiConfig getApi() { return api; }
    public void setApi(ApiConfig api) { this.api = api; }
    
    public ImageConfig getImage() { return image; }
    public void setImage(ImageConfig image) { this.image = image; }
    
    public SecurityConfig getSecurity() { return security; }
    public void setSecurity(SecurityConfig security) { this.security = security; }
    
    public LoggingConfig getLogging() { return logging; }
    public void setLogging(LoggingConfig logging) { this.logging = logging; }
    
    public MonitorConfig getMonitor() { return monitor; }
    public void setMonitor(MonitorConfig monitor) { this.monitor = monitor; }

    public static class AppInfo {
        @JsonProperty("name")
        private String name = "ZephTG Random Image API";
        
        @JsonProperty("version")
        private String version = "1.0.0";
        
        @JsonProperty("description")
        private String description = "高性能随机图片API服务";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ApiConfig {
        @JsonProperty("port")
        private int port = 8080;
        
        @JsonProperty("cors_enabled")
        private boolean corsEnabled = true;
        
        @JsonProperty("allowed_origins")
        private List<String> allowedOrigins = List.of("*");
        
        @JsonProperty("timeout")
        private int timeout = 30;

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public boolean isCorsEnabled() { return corsEnabled; }
        public void setCorsEnabled(boolean corsEnabled) { this.corsEnabled = corsEnabled; }
        
        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }

    public static class ImageConfig {
        @JsonProperty("storage_path")
        private String storagePath = "./images";
        
        @JsonProperty("categories")
        private List<String> categories;
        
        @JsonProperty("supported_formats")
        private List<String> supportedFormats = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

        public String getStoragePath() { return storagePath; }
        public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
        
        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }
        
        public List<String> getSupportedFormats() { return supportedFormats; }
        public void setSupportedFormats(List<String> supportedFormats) { this.supportedFormats = supportedFormats; }
    }

    public static class SecurityConfig {
        @JsonProperty("admin_key")
        private String adminKey;
        
        @JsonProperty("rate_limit_per_hour")
        private int rateLimitPerHour = 100;
        
        @JsonProperty("enable_ip_whitelist")
        private boolean enableIpWhitelist = false;
        
        @JsonProperty("ip_whitelist")
        private List<String> ipWhitelist = List.of();
        
        @JsonProperty("log_access")
        private boolean logAccess = true;

        public String getAdminKey() { return adminKey; }
        public void setAdminKey(String adminKey) { this.adminKey = adminKey; }
        
        public int getRateLimitPerHour() { return rateLimitPerHour; }
        public void setRateLimitPerHour(int rateLimitPerHour) { this.rateLimitPerHour = rateLimitPerHour; }
        
        public boolean isEnableIpWhitelist() { return enableIpWhitelist; }
        public void setEnableIpWhitelist(boolean enableIpWhitelist) { this.enableIpWhitelist = enableIpWhitelist; }
        
        public List<String> getIpWhitelist() { return ipWhitelist; }
        public void setIpWhitelist(List<String> ipWhitelist) { this.ipWhitelist = ipWhitelist; }
        
        public boolean isLogAccess() { return logAccess; }
        public void setLogAccess(boolean logAccess) { this.logAccess = logAccess; }
    }

    public static class LoggingConfig {
        @JsonProperty("log_path")
        private String logPath = "./logs";
        
        @JsonProperty("level")
        private String level = "INFO";
        
        @JsonProperty("console_output")
        private boolean consoleOutput = true;
        
        @JsonProperty("max_file_size")
        private int maxFileSize = 10;
        
        @JsonProperty("retention_days")
        private int retentionDays = 7;

        public String getLogPath() { return logPath; }
        public void setLogPath(String logPath) { this.logPath = logPath; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        
        public boolean isConsoleOutput() { return consoleOutput; }
        public void setConsoleOutput(boolean consoleOutput) { this.consoleOutput = consoleOutput; }
        
        public int getMaxFileSize() { return maxFileSize; }
        public void setMaxFileSize(int maxFileSize) { this.maxFileSize = maxFileSize; }
        
        public int getRetentionDays() { return retentionDays; }
        public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
        
        public String getLogLevel() { return level; }
        public void setLogLevel(String logLevel) { this.level = logLevel; }
    }

    public static class MonitorConfig {
        @JsonProperty("interval")
        private int interval = 30;
        
        @JsonProperty("unit")
        private String unit = "m";
        
        @JsonProperty("enabled")
        private boolean enabled = true;

        public int getInterval() { return interval; }
        public void setInterval(int interval) { this.interval = interval; }
        
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}