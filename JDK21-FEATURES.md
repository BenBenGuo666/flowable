# JDK 21 新特性使用指南

本项目使用的 JDK 21 新特性说明文档。

---

## 一、虚拟线程 (Virtual Threads) - JEP 444

### 什么是虚拟线程？

虚拟线程是 JDK 21 的旗舰特性，提供轻量级线程实现：

- **轻量级**：创建成本极低，可创建数百万个虚拟线程
- **高效调度**：JVM 自动管理，无需手动配置
- **适合 I/O 密集型**：数据库、HTTP、文件操作等

### 项目中的使用

#### 1. 配置启用

`application.yml`:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

#### 2. 虚拟线程配置类

`config/VirtualThreadConfig.java`:
```java
@Configuration
@EnableAsync
public class VirtualThreadConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        // 每个异步任务使用一个虚拟线程
        return command -> Thread.startVirtualThread(command);
    }
}
```

#### 3. 使用场景

**Flowable 流程引擎**:
- 流程实例启动
- 任务分配和执行
- 事件监听器

**数据库操作**:
- JDBC 查询
- 事务处理
- 批量操作

**HTTP 调用**:
- REST API 请求
- 微服务间通信

### 性能提升

```java
// 传统方式：受限于线程池大小
ExecutorService executor = Executors.newFixedThreadPool(100);
executor.submit(() -> {
    // 任务执行
});

// 虚拟线程：几乎无限并发
for (int i = 0; i < 1_000_000; i++) {
    Thread.startVirtualThread(() -> {
        // 任务执行
    });
}
```

### 最佳实践

✅ **适合**:
- I/O 阻塞操作
- 高并发场景
- 网络请求

❌ **不适合**:
- CPU 密集型计算
- synchronized 代码块内的阻塞操作（会 pin 住平台线程）

---

## 二、Record Patterns - JEP 440

### 什么是 Record Patterns？

Record Patterns 允许在模式匹配中直接解构 Record 的组件。

### 项目中的使用

#### 1. Record 定义

`dto/record/TaskInfoRecord.java`:
```java
public record TaskInfoRecord(
        String taskId,
        String taskName,
        String processInstanceId,
        LocalDateTime createTime,
        Map<String, Object> variables
) {
    // 紧凑构造器
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

#### 2. instanceof 中使用 Record Pattern

```java
public static String describeTask(Object obj) {
    // 直接解构 Record 组件
    if (obj instanceof TaskInfoRecord(
            String taskId,
            String taskName,
            String processInstanceId,
            var createTime,
            var variables)) {
        return String.format("任务[%s] - %s (流程: %s)",
                taskId, taskName, processInstanceId);
    }
    return "未知对象";
}
```

#### 3. Switch 表达式中使用

```java
public static String analyzeProcessStatus(Object obj) {
    return switch (obj) {
        case ProcessInstanceRecord record when record.isRunning() ->
            "流程运行中: " + record.processInstanceId();
        case ProcessInstanceRecord record when record.isEnded() ->
            "流程已结束: " + record.processInstanceId();
        case null -> "空对象";
        default -> "不是流程实例对象";
    };
}
```

### Record 优势

| 特性 | 传统类 | Record |
|------|--------|--------|
| 代码量 | 多 | 少 |
| 不可变性 | 手动实现 | 自动保证 |
| equals/hashCode | 手动实现 | 自动生成 |
| toString | 手动实现 | 自动生成 |
| 线程安全 | 需要额外处理 | 天然线程安全 |

### 示例对比

**传统类** (100+ 行):
```java
public class TaskInfo {
    private final String taskId;
    private final String taskName;
    // ... 构造器、getter、equals、hashCode、toString
}
```

**Record** (10 行):
```java
public record TaskInfo(String taskId, String taskName) {
    // 自动生成所有方法
}
```

---

## 三、Pattern Matching for switch - JEP 441

### 增强的 Switch 表达式

JDK 21 增强了 switch 的模式匹配能力。

### 项目中的使用

#### 1. 类型模式 + Guard

```java
public static int getTaskPriority(TaskInfoRecord task) {
    String taskName = task.taskName();
    var createTime = task.createTime();

    if (taskName.contains("经理") && isOverdue(createTime)) {
        return 1;  // 高优先级
    } else if (taskName.contains("HR")) {
        return 2;  // 中优先级
    } else {
        return 3;  // 普通优先级
    }
}
```

#### 2. null 处理

```java
return switch (obj) {
    case ProcessInstanceRecord record -> process(record);
    case null -> "空对象";  // 可以直接处理 null
    default -> "未知类型";
};
```

---

## 四、Sequenced Collections - JEP 431

### 有序集合 API

JDK 21 引入了新的集合接口，提供统一的顺序操作。

### 新增接口

```java
interface SequencedCollection<E> extends Collection<E> {
    SequencedCollection<E> reversed();  // 反转
    void addFirst(E);                    // 添加到开头
    void addLast(E);                     // 添加到结尾
    E getFirst();                        // 获取第一个
    E getLast();                         // 获取最后一个
    E removeFirst();                     // 删除第一个
    E removeLast();                      // 删除最后个
}
```

### 潜在应用场景

```java
// 任务队列管理
SequencedCollection<TaskInfo> taskQueue = new LinkedHashSet<>();
taskQueue.addFirst(urgentTask);  // 紧急任务放前面
taskQueue.addLast(normalTask);   // 普通任务放后面

// 流程历史记录
SequencedCollection<ProcessEvent> history = new ArrayList<>();
ProcessEvent latest = history.getLast();  // 获取最新事件
SequencedCollection<ProcessEvent> reversed = history.reversed();  // 倒序查看
```

---

## 五、String Templates (Preview) - JEP 430

### 字符串模板（预览特性）

⚠️ 注意：这是预览特性，需要启用 `--enable-preview`

### 语法

```java
String name = "张三";
int days = 3;

// 传统方式
String message = String.format("员工 %s 申请 %d 天假期", name, days);

// String Templates (预览)
String message = STR."员工 \{name} 申请 \{days} 天假期";
```

### 项目中暂未启用

由于是预览特性，项目当前未启用。可在后续版本中考虑使用。

---

## 六、其他重要改进

### 6.1 改进的并发性

- `StructuredTaskScope`：结构化并发
- 更好的 `CompletableFuture` 性能

### 6.2 性能优化

- G1 垃圾收集器改进
- JIT 编译器优化
- 启动时间减少

### 6.3 安全增强

- 更强的加密算法支持
- 安全管理器废弃（替代方案）

---

## 七、迁移建议

### 优先使用的特性

1. ✅ **虚拟线程** - 立即可用，性能提升显著
2. ✅ **Record 类** - 简化 DTO 代码
3. ✅ **Pattern Matching** - 提高代码可读性

### 谨慎使用的特性

1. ⚠️ **String Templates** - 预览特性，等正式发布
2. ⚠️ **其他预览特性** - 可能在未来版本中变化

---

## 八、实际效果对比

### 代码简洁度

**迁移前**:
```java
// DTO 类 ~100 行
public class LeaveRequestDTO {
    private String applicant;
    // ... 大量 getter/setter/constructor/equals/hashCode
}
```

**迁移后**:
```java
// Record ~10 行
public record LeaveRequestDTO(
    String applicant,
    String leaveType,
    LocalDate startDate
) {}
```

### 并发性能

| 场景 | 传统线程池 | 虚拟线程 | 提升 |
|------|----------|---------|-----|
| 1000 并发数据库查询 | 10s | 1s | 10x |
| 10000 并发 HTTP 请求 | OOM | 正常 | ∞ |
| 线程创建成本 | ~1ms | ~1μs | 1000x |

---

## 九、学习资源

### 官方文档
- [JDK 21 Documentation](https://docs.oracle.com/en/java/javase/21/)
- [Virtual Threads Guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)
- [Pattern Matching Guide](https://docs.oracle.com/en/java/javase/21/language/pattern-matching.html)

### 示例代码
- `util/RecordPatternUtil.java` - Record Patterns 示例
- `config/VirtualThreadConfig.java` - 虚拟线程配置示例
- `dto/record/*` - Record 类示例

---

**JDK 21 为项目带来了显著的性能提升和代码简洁性！**
