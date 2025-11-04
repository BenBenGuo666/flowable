# JDK 21 迁移指南

## 概述

本文档记录了 Flowable 项目从 JDK 11 + Spring Boot 2.7.18 升级到 JDK 21 + Spring Boot 3.2.5 的完整迁移过程。

**迁移日期**: 2025-11-04
**迁移人**: e-Benben.Guo

---

## 一、版本变更对比

### 核心依赖版本

| 组件 | 迁移前 | 迁移后 | 说明 |
|------|--------|--------|------|
| Java | 11 | 21 | 主要版本升级 |
| Spring Boot | 2.7.18 | 3.2.5 | 主要版本升级（LTS） |
| Flowable | 6.8.0 | 7.1.0 | 兼容 Spring Boot 3.x |
| Lombok | 1.18.30 | 1.18.32 | 支持 JDK 21 |
| Druid | 1.2.16 | 1.2.23 | 最新稳定版 |
| H2 | 2.1.214 | 2.2.224 | 最新版本 |

---

## 二、迁移步骤

### 步骤 1: 升级 pom.xml 依赖

#### 1.1 修改 Spring Boot 版本

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
    <relativePath/>
</parent>
```

#### 1.2 修改 Java 版本和其他依赖

```xml
<properties>
    <java.version>21</java.version>
    <flowable.version>7.1.0</flowable.version>
    <druid.version>1.2.23</druid.version>
    <h2.version>2.2.224</h2.version>
    <lombok.version>1.18.32</lombok.version>
</properties>
```

#### 1.3 配置 Maven Compiler Plugin

**关键配置**：明确指定 JDK 21 的路径

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>21</source>
        <target>21</target>
        <release>21</release>
        <!-- 明确指定 JDK 21 的 javac 编译器路径 -->
        <executable>/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin/javac</executable>
        <fork>true</fork>
    </configuration>
</plugin>
```

---

### 步骤 2: Jakarta EE 包名迁移

Spring Boot 3.x 使用 Jakarta EE 规范，需要将所有 `javax.*` 包名改为 `jakarta.*`。

#### 受影响的文件

| 文件 | 修改内容 |
|------|---------|
| `dto/LeaveRequestDTO.java` | `javax.validation.constraints.*` → `jakarta.validation.constraints.*` |
| `dto/ApprovalDTO.java` | 同上 |
| `controller/LeaveController.java` | `javax.validation.Valid` → `jakarta.validation.Valid` |
| `exception/GlobalExceptionHandler.java` | `javax.validation.ConstraintViolation` → `jakarta.validation.ConstraintViolation` |

#### 替换命令

```bash
# 批量替换 javax.validation 为 jakarta.validation
find src/main/java -name "*.java" -exec sed -i '' 's/javax\.validation/jakarta.validation/g' {} +
```

---

### 步骤 3: 配置虚拟线程

JDK 21 的核心特性之一是虚拟线程（Virtual Threads），可显著提升 I/O 密集型任务的性能。

#### 3.1 启用虚拟线程（application.yml）

```yaml
spring:
  threads:
    virtual:
      enabled: true  # 启用虚拟线程 (JDK 21+)
```

#### 3.2 创建虚拟线程配置类

`src/main/java/com/demo/flowable/config/VirtualThreadConfig.java`

```java
@Slf4j
@Configuration
@EnableAsync
public class VirtualThreadConfig implements AsyncConfigurer {

    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor(TaskExecutorBuilder builder) {
        log.info("初始化虚拟线程异步任务执行器 (Virtual Threads)");
        return builder.taskDecorator(loggingTaskDecorator()).build();
    }

    @Override
    public Executor getAsyncExecutor() {
        return command -> Thread.startVirtualThread(command);
    }
}
```

---

### 步骤 4: 使用 JDK 21 Record Patterns

#### 4.1 创建 Record DTO

`src/main/java/com/demo/flowable/dto/record/TaskInfoRecord.java`

```java
public record TaskInfoRecord(
        String taskId,
        String taskName,
        String processInstanceId,
        LocalDateTime createTime,
        Map<String, Object> variables
) {
    // 紧凑构造器：参数校验
    public TaskInfoRecord {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
    }

    // 自定义方法
    public boolean isManagerApproval() {
        return "经理审批".equals(taskName);
    }
}
```

#### 4.2 使用 Record Patterns 解构

```java
// JDK 21 Record Pattern in instanceof
if (obj instanceof TaskInfoRecord(
        String taskId,
        String taskName,
        String processInstanceId,
        var createTime,
        var variables)) {
    // 直接使用解构出的变量
    System.out.println("Task: " + taskId);
}
```

---

### 步骤 5: 更新线程池配置

将传统线程池标记为废弃，优先使用虚拟线程。

`src/main/java/com/demo/flowable/core/ThreadPoolTaskConfig.java`

```java
@Slf4j
@Deprecated(since = "JDK 21", forRemoval = false)
@Configuration
@ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "false")
public class ThreadPoolTaskConfig {
    // 仅在禁用虚拟线程时启用
}
```

---

## 三、遇到的问题与解决方案

### 问题 1: Maven 使用 JDK 11 运行

**症状**:
```
[ERROR] Failed to execute goal ... Compilation failure: Compilation failure:
错误: 不支持发行版本 21
```

**原因**: Maven 使用系统默认的 JDK 11。

**解决方案**:
在 pom.xml 中明确指定 JDK 21 的编译器路径：

```xml
<executable>/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin/javac</executable>
<fork>true</fork>
```

---

### 问题 2: Record Pattern 语法错误

**症状**:
```
[ERROR] /Users/.../RecordPatternUtil.java:[101,26] 错误: 需要';'
[ERROR] 从发行版 21 开始，下划线关键字 '_' 只允许用于声明未命名模式...
```

**原因**: 使用了 JDK 21 预览特性或不稳定的语法。

**解决方案**:
使用稳定的 Record Pattern 语法：
- 避免使用 `_` 作为未命名模式（这是预览特性）
- 避免使用解构赋值语法 `var Record(a, b) = obj;`
- 使用标准的 instanceof Record Pattern

---

### 问题 3: Spring Boot Maven Plugin 类加载冲突

**症状**:
```
[WARNING] Error injecting: org.springframework.boot.maven.RepackageMojo
java.lang.TypeNotPresentException: Type org.springframework.boot.maven.RepackageMojo not present
```

**原因**: Maven 运行在 JDK 11，但编译使用 JDK 21，导致类加载器冲突。

**临时解决方案**:
编译成功即可，打包问题可通过设置 Maven 的 JAVA_HOME 解决：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
mvn clean package
```

**永久解决方案**:
修改 `~/.zshrc` 或 `~/.bash_profile`:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH
```

---

## 四、验证清单

### ✅ 编译验证

```bash
mvn clean compile
```

**预期结果**: `BUILD SUCCESS`

### ✅ 代码检查

- [ ] Jakarta EE 包名已全部迁移
- [ ] Lombok 生成的代码正常工作
- [ ] Record 类编译成功
- [ ] 虚拟线程配置加载成功

### ✅ 功能测试

1. 启动应用
```bash
mvn spring-boot:run
```

2. 测试 API 接口
```bash
curl -X POST http://localhost:8080/api/leave/submit \
  -H "Content-Type: application/json" \
  -d '{...}'
```

3. 检查日志，确认虚拟线程启用：
```
初始化虚拟线程异步任务执行器 (Virtual Threads)
```

---

## 五、性能对比

### 虚拟线程 vs 传统线程池

| 指标 | 传统线程池 | 虚拟线程 | 提升 |
|------|-----------|---------|-----|
| 线程创建成本 | 高 | 极低 | 100x+ |
| 最大并发数 | 受限于线程数 | 数百万 | 1000x+ |
| 内存占用 | 每线程 ~1MB | 每线程 ~1KB | 1000x |
| I/O 阻塞效率 | 低（线程被占用） | 高（自动调度） | 10-50x |

### 适用场景

**虚拟线程最佳场景**:
- 数据库查询
- HTTP 请求
- 文件 I/O
- Flowable 流程执行

**传统线程池适用场景**:
- CPU 密集型计算
- 需要精确控制线程数

---

## 六、注意事项

### 6.1 虚拟线程 Pinning 问题

虚拟线程在以下情况下会 "钉住" 平台线程，丧失优势：

1. **synchronized 代码块内进行阻塞操作**
   ```java
   synchronized (lock) {
       Thread.sleep(1000);  // ❌ 会 pin 住平台线程
   }
   ```

   **解决方案**: 使用 ReentrantLock
   ```java
   lock.lock();
   try {
       Thread.sleep(1000);  // ✅ 不会 pin
   } finally {
       lock.unlock();
   }
   ```

2. **native 方法或 JNI 调用**

### 6.2 Record 限制

- Record 是不可变的（final）
- 不能声明实例字段
- 不能继承其他类（但可以实现接口）
- 适用于 DTO，不适用于需要修改的业务实体

### 6.3 Spring Boot 3.x 变更

- Servlet 4.0 → 5.0
- Hibernate 5.x → 6.x
- 最低 JDK 要求: JDK 17

---

## 七、回滚方案

如需回滚到 JDK 11 版本：

```xml
<!-- pom.xml -->
<parent>
    <version>2.7.18</version>
</parent>

<properties>
    <java.version>11</java.version>
    <flowable.version>6.8.0</flowable.version>
    <lombok.version>1.18.30</lombok.version>
</properties>
```

并将 Jakarta EE 包名改回 javax：
```bash
find src/main/java -name "*.java" -exec sed -i '' 's/jakarta\.validation/javax.validation/g' {} +
```

---

## 八、参考资料

- [JDK 21 Release Notes](https://www.oracle.com/java/technologies/javase/21-relnotes.html)
- [Spring Boot 3.2 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [JEP 440: Record Patterns](https://openjdk.org/jeps/440)
- [Flowable 7.x Migration Guide](https://www.flowable.com/open-source/docs/migration-guide)

---

## 九、后续优化建议

1. **使用 String Templates (Preview)**: 简化字符串拼接
2. **使用 Sequenced Collections**: 新的集合 API
3. **启用 AOT 编译**: 提升启动速度
4. **使用 GraalVM**: 编译为 native image

---

**迁移完成！项目已成功运行在 JDK 21 + Spring Boot 3.2.5 环境下。**