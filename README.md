# ZephTG 随机图片API系统

## 系统概述

这是一个高性能的随机图片API服务系统，支持多分类图片管理、安全访问控制、实时日志记录等功能。

## 主要特性

### 🖼️ 图片管理功能
- **多分类支持**：动漫(anime)、风景(landscape)、星空(starry)、自然(nature)、艺术(art)
- **本地存储**：所有图片存储在本地文件系统
- **动态扫描**：自动检测图片目录变化
- **随机获取**：每个分类随机返回一张图片

### 🔐 安全控制
- **管理员密钥**：32位随机生成的安全密钥，无限制访问
- **请求限流**：普通用户每小时最多100次请求
- **访问日志**：详细记录所有API访问情况

### 📊 系统监控
- **配置热加载**：支持运行时配置更新
- **智能补全**：自动检测并补全缺失配置项
- **定时检测**：可配置的图库变化检测间隔

### 📝 日志系统
- **外部存储**：日志文件存储在独立的logs目录
- **规范命名**：Zeph-YYYY-MM-DD-HH-mm.log格式
- **实时记录**：终端输出同步写入日志文件
- **防丢失设计**：服务器断电也能保证日志完整性

## 目录结构

```
ZephTG/
├── config/              # 外部配置目录
│   └── config.yml      # 主配置文件
├── images/             # 图片存储目录
│   ├── anime/         # 动漫分类
│   ├── landscape/     # 风景分类
│   ├── starry/        # 星空分类
│   ├── nature/        # 自然分类
│   └── art/           # 艺术分类
├── logs/              # 运行日志目录
├── src/               # 源代码目录
└── test-functionality.bat/sh  # 测试脚本
```

## API接口说明

### 获取随机图片
```
GET http://localhost:8080/api/{分类ID}?login={管理员密钥}
```

**示例：**
- `http://localhost:8080/api/anime` - 获取随机动漫图片
- `http://localhost:8080/api/landscape` - 获取随机风景图片
- `http://localhost:8080/api/starry?login=your_admin_key` - 管理员无限制访问星空图片

### 获取系统状态
```
GET http://localhost:8080/api/stats?login={管理员密钥}
```

### 健康检查
```
GET http://localhost:8080/api/health
```

## 配置文件详解

### config.yml 核心配置项

```yaml
# 图库配置
image:
  storage_path: "./images"           # 图片存储路径
  categories:                       # 支持的图片分类
    - anime
    - landscape
    - starry
    - nature
    - art
  supported_formats:                # 支持的图片格式
    - ".jpg"
    - ".jpeg"
    - ".png"
    - ".gif"
    - ".webp"

# 图库检测配置
monitor:
  interval: 30                      # 检测间隔数值
  unit: "m"                         # 时间单位(s/m/h/d)
  enabled: true                     # 是否启用检测

# 安全配置
security:
  admin_key: ""                     # 管理员密钥(自动生成)
  rate_limit_per_hour: 100          # 普通用户每小时请求限制
  enable_ip_whitelist: false        # 是否启用IP白名单
  log_access: true                  # 是否记录访问日志

# 日志配置
logging:
  log_path: "./logs"                # 日志存储路径
  level: "INFO"                     # 日志级别
  console_output: true              # 是否输出到控制台
  max_file_size: 10                 # 单个日志文件最大大小(MB)
  retention_days: 7                 # 日志保留天数
```

## 快速开始

### 1. 系统准备
```bash
# 确保Java 21+环境已安装
java -version

# 克隆项目到本地
git clone <repository-url>
cd ZephTG
```

### 2. 添加图片文件
```bash
# 在对应分类目录下放入图片文件
cp your-anime-images/* ./images/anime/
cp your-landscape-images/* ./images/landscape/
# ... 其他分类同理
```

### 3. 启动服务
```bash
# Windows
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 4. 测试API
```bash
# 获取健康状态
curl http://localhost:8080/api/health

# 获取统计信息
curl http://localhost:8080/api/stats

# 获取随机图片(需要先添加图片文件)
curl http://localhost:8080/api/anime
```

## 管理员功能

### 查看管理员密钥
首次启动后，系统会自动生成32位管理员密钥并保存在config.yml中：
```yaml
security:
  admin_key: "自动生成的32位密钥"
```

### 管理员特权
- 无请求频率限制
- 可获取详细的系统统计信息
- 可通过API监控系统状态

### 使用管理员权限
```bash
# 管理员无限制访问
curl "http://localhost:8080/api/anime?login=你的管理员密钥"

# 获取详细统计信息
curl "http://localhost:8080/api/stats?login=你的管理员密钥"
```

## 系统维护

### 日志管理
- 日志文件按时间自动分割：`Zeph-2026-02-07-14-30.log`
- 支持日志文件轮转和自动清理
- 实时记录系统运行状态和错误信息

### 配置更新
- 修改config.yml后重启服务生效
- 系统会自动检测并补全缺失的配置项
- 不会覆盖用户已有的配置设置

### 性能监控
- 系统自动监控各分类图片数量
- 定时检测图片目录变化
- 记录API访问统计和性能指标

## 故障排除

### 常见问题

1. **图片无法显示**
   - 检查图片格式是否在supported_formats列表中
   - 确认图片文件存在于正确的分类目录
   - 查看logs目录中的错误日志

2. **API访问被拒绝**
   - 检查请求频率是否超过限制
   - 确认分类ID是否正确
   - 验证管理员密钥是否正确

3. **服务启动失败**
   - 检查端口8080是否被占用
   - 确认Java版本是否符合要求
   - 查看控制台输出的具体错误信息

### 日志查看
```bash
# 查看最新日志
tail -f ./logs/Zeph-$(date +%Y-%m-%d-%H-%M).log

# 查找错误信息
grep "ERROR" ./logs/*.log
```

## 技术架构

### 核心组件
- **Spring Boot**：应用框架
- **YAML配置**：灵活的配置管理
- **并发安全**：线程安全的图片管理和访问控制
- **定时任务**：自动化的图库检测和维护

### 设计特点
- **模块化设计**：各功能组件独立，易于维护
- **配置驱动**：通过外部配置文件控制行为
- **安全优先**：多层次的安全防护机制
- **可观测性**：完善的日志和监控体系

## 许可证

本项目采用MIT许可证，详情请查看LICENSE文件。