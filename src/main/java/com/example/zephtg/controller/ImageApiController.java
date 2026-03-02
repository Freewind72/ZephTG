package com.example.zephtg.controller;

import com.example.zephtg.config.ConfigManager;
import com.example.zephtg.image.ImageManager;
import com.example.zephtg.logging.LogManager;
import com.example.zephtg.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageApiController {
    
    @Autowired
    private ImageManager imageManager;
    
    @Autowired
    private SecurityService securityService;
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired
    private LogManager logManager;

    @GetMapping("/{category}")
    public ResponseEntity<Resource> getRandomImage(
            @PathVariable String category,
            @RequestParam(required = false) String login,
            HttpServletRequest request) {
        
        String clientIp = getClientIpAddress(request);
        
        try {
            // 验证请求权限
            if (!securityService.isRequestAllowed(clientIp, login)) {
                securityService.logRequest(clientIp, category, false);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(null);
            }
            
            // 验证分类是否存在
            if (!imageManager.isValidCategory(category)) {
                logManager.warn(String.format("请求不存在的分类: %s, IP: %s", category, clientIp));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
            }
            
            // 获取随机图片
            File imageFile = imageManager.getRandomImage(category);
            if (imageFile == null || !imageFile.exists()) {
                logManager.warn(String.format("分类 %s 中没有可用的图片", category));
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
            }
            
            // 记录成功的请求
            securityService.logRequest(clientIp, category, true);
            
            // 返回图片文件
            Resource resource = new FileSystemResource(imageFile);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(imageFile.getName()));
            headers.setContentLength(imageFile.length());
            headers.setCacheControl("no-cache");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
                
        } catch (Exception e) {
            logManager.error(String.format("处理图片请求时出错: 分类=%s, IP=%s", category, clientIp), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(required = false) String login) {
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基本统计信息
            stats.put("availableCategories", imageManager.getAvailableCategories());
            stats.put("categoryStats", imageManager.getAllCategoryStats());
            stats.put("totalImages", imageManager.getAllCategoryStats().values().stream()
                .mapToInt(Integer::intValue).sum());
            
            // 如果是管理员请求，提供更多信息
            if (login != null && configManager.isAdminKeyValid(login)) {
                stats.put("isAdmin", true);
                stats.put("adminKey", configManager.getAdminKey());
                stats.put("rateLimitPerHour", configManager.getConfig().getSecurity().getRateLimitPerHour());
                stats.put("monitorEnabled", configManager.getConfig().getMonitor().isEnabled());
                stats.put("monitorInterval", configManager.getConfig().getMonitor().getInterval() + 
                         configManager.getConfig().getMonitor().getUnit());
            } else {
                stats.put("isAdmin", false);
                // 普通用户只显示剩余请求次数
                String clientIp = "unknown"; // 在实际使用中应该从请求中获取
                stats.put("remainingRequests", securityService.getRemainingRequestsPerHour(clientIp));
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logManager.error("获取统计信息时出错", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private MediaType getMediaType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "webp":
                return MediaType.valueOf("image/webp");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}