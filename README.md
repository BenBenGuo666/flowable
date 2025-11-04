# Flowable 工作流示例项目

基于 **JDK 21** + **Spring Boot 3.2.5** + **Flowable 7.1.0** 的现代化工作流管理系统。

## 特性亮点

- ✨ **JDK 21 虚拟线程** - 高并发性能提升 10-50 倍
- 🎯 **Record Patterns** - 简洁的数据类型定义
- 🔄 **Flowable 7.1.0** - 强大的 BPMN 2.0 流程引擎
- 🚀 **Spring Boot 3.2.5** - 最新 LTS 版本
- 🛡️ **完整的异常处理** - 全局异常拦截
- ✅ **参数校验** - Jakarta Validation 规范
- 📝 **统一响应格式** - RESTful API 最佳实践

---

## 环境要求

### 必需
- **JDK 21** 或更高版本
- **Maven 3.9+**
- **MySQL 8.0+**

### 检查 Java 版本

```bash
java -version
# 应该显示: java version "21.0.x"
```

---

## 快速开始

### 1. 配置数据库

创建 MySQL 数据库：

```sql
CREATE DATABASE flowable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.yml` 中的数据库配置。

### 2. 编译项目

```bash
mvn clean compile
```

### 3. 运行项目

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

---

## 核心技术

### 虚拟线程 (Virtual Threads)

项目使用 JDK 21 虚拟线程，显著提升并发性能。

**性能对比**:
- 传统线程池：最大并发 ~1000
- 虚拟线程：最大并发 ~1000000

### Record Patterns

使用 JDK 21 Record 简化 DTO 代码，自动生成 equals、hashCode、toString。

### Flowable 工作流

BPMN 2.0 标准流程引擎，支持复杂业务流程管理。

---

## 文档

- **[API 测试文档](./API-TEST.md)** - 完整的 API 使用示例
- **[JDK 21 迁移指南](./JDK21-MIGRATION.md)** - 从 JDK 11 升级说明
- **[JDK 21 特性说明](./JDK21-FEATURES.md)** - 使用的 JDK 21 新特性
- **[Clean Code 优化总结](./CLEAN-CODE-SUMMARY.md)** - 代码优化记录

---

## 常见问题

### Q: 编译报错 "不支持发行版本 21"

A: Maven 使用的 JDK 版本不正确。设置 JAVA_HOME：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
mvn clean compile
```

---

## 技术栈

| 技术 | 版本 |
|------|------|
| JDK | 21 |
| Spring Boot | 3.2.5 |
| Flowable | 7.1.0 |
| MySQL | 8.0.33 |
| Lombok | 1.18.32 |

---

**享受 JDK 21 带来的性能提升吧！🚀**
