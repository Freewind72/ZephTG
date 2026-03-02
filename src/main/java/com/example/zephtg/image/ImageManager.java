package com.example.zephtg.image;

import com.example.zephtg.config.ConfigManager;
import com.example.zephtg.logging.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ImageManager {
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired
    private LogManager logManager;
    
    private final Map<String, List<File>> categoryImages = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        scanImageDirectories();
    }

    public void scanImageDirectories() {
        categoryImages.clear();
        String basePath = configManager.getConfig().getImage().getStoragePath();
        
        try {
            Path baseDir = Paths.get(basePath);
            if (!Files.exists(baseDir)) {
                logManager.warn("图片基础目录不存在: " + basePath);
                return;
            }
            
            // 获取配置中的分类
            List<String> categories = configManager.getConfig().getImage().getCategories();
            if (categories == null || categories.isEmpty()) {
                logManager.warn("未配置图片分类");
                return;
            }
            
            // 扫描每个分类目录
            for (String category : categories) {
                Path categoryPath = baseDir.resolve(category);
                List<File> images = scanDirectoryForImages(categoryPath);
                categoryImages.put(category, images);
                // 只在有图片时记录日志
                if (images.size() > 0) {
                    logManager.info(String.format("分类 '%s' 发现 %d 张图片", category, images.size()));
                }
            }
            
        } catch (Exception e) {
            logManager.error("扫描图片目录失败", e);
        }
    }

    private List<File> scanDirectoryForImages(Path directory) {
        List<File> images = new ArrayList<>();
        
        if (!Files.exists(directory)) {
            return images;
        }
        
        try {
            List<Path> imagePaths = Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(this::isValidImageFile)
                .collect(Collectors.toList());
                
            for (Path imagePath : imagePaths) {
                images.add(imagePath.toFile());
            }
            
        } catch (IOException e) {
            logManager.error("扫描目录失败: " + directory.toString(), e);
        }
        
        return images;
    }

    private boolean isValidImageFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        List<String> allowedFormats = configManager.getConfig().getImage().getSupportedFormats();
        
        for (String format : allowedFormats) {
            // 确保格式以点号开头，并正确匹配文件扩展名
            String normalizedFormat = format.startsWith(".") ? format.toLowerCase() : "." + format.toLowerCase();
            if (fileName.endsWith(normalizedFormat)) {
                return true;
            }
        }
        return false;
    }

    public File getRandomImage(String category) {
        List<File> images = categoryImages.get(category);
        
        if (images == null || images.isEmpty()) {
            logManager.warn("分类 '" + category + "' 没有找到可用的图片");
            return null;
        }
        
        int randomIndex = random.nextInt(images.size());
        return images.get(randomIndex);
    }

    public Set<String> getAvailableCategories() {
        return categoryImages.keySet();
    }

    public int getImageCountByCategory(String category) {
        List<File> images = categoryImages.get(category);
        return images != null ? images.size() : 0;
    }

    public Map<String, Integer> getAllCategoryStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Map.Entry<String, List<File>> entry : categoryImages.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }

    public boolean isValidCategory(String category) {
        return categoryImages.containsKey(category);
    }

    public void refreshCategory(String category) {
        String basePath = configManager.getConfig().getImage().getStoragePath();
        Path categoryPath = Paths.get(basePath, category);
        List<File> images = scanDirectoryForImages(categoryPath);
        categoryImages.put(category, images);
        logManager.info(String.format("刷新分类 '%s' 完成，当前有 %d 张图片", category, images.size()));
    }

    public void refreshAllCategories() {
        logManager.info("开始刷新所有图片分类...");
        scanImageDirectories();
        logManager.info("所有图片分类刷新完成");
    }
}