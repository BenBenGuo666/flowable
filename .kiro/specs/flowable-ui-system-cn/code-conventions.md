# 代码规范和风格指南

## 概述

本文档定义了 Flowable UI 系统的代码规范、命名约定和最佳实践，确保代码的一致性、可读性和可维护性。

## 通用编码原则

### 1. SOLID 原则
- **单一职责原则 (SRP)**: 每个类或函数只负责一个功能
- **开闭原则 (OCP)**: 对扩展开放，对修改关闭
- **里氏替换原则 (LSP)**: 子类应该能够替换父类
- **接口隔离原则 (ISP)**: 不应该强迫客户端依赖不需要的接口
- **依赖倒置原则 (DIP)**: 依赖抽象而不是具体实现

### 2. 代码质量标准
- 代码行数：函数不超过 50 行，类不超过 500 行
- 圈复杂度：单个函数不超过 10
- 嵌套层级：不超过 4 层
- 参数个数：函数参数不超过 5 个

## 前端代码规范 (Vue.js + TypeScript)

### 1. 项目结构规范

```
src/
├── assets/                 # 静态资源
│   ├── images/
│   ├── icons/
│   └── styles/
├── components/             # 通用组件
│   ├── base/              # 基础组件
│   ├── business/          # 业务组件
│   └── layout/            # 布局组件
├── composables/           # 组合式函数
├── modules/               # 功能模块
│   └── process-designer/
│       ├── components/
│       ├── composables/
│       ├── services/
│       ├── stores/
│       ├── types/
│       └── views/
├── router/                # 路由配置
├── services/              # API 服务
├── stores/                # 状态管理
├── types/                 # 类型定义
├── utils/                 # 工具函数
└── views/                 # 页面组件
```

### 2. 命名规范

#### 2.1 文件命名
```typescript
// 组件文件 - PascalCase
ProcessDesigner.vue
UserManagement.vue
TaskList.vue

// 工具文件 - camelCase
dateUtils.ts
apiClient.ts
validationRules.ts

// 类型文件 - camelCase
processTypes.ts
userTypes.ts

// 常量文件 - SCREAMING_SNAKE_CASE
API_ENDPOINTS.ts
ERROR_CODES.ts
```

#### 2.2 变量和函数命名
```typescript
// 变量 - camelCase
const userName = 'admin'
const processInstanceList = []
const isLoading = false

// 函数 - camelCase，动词开头
function fetchProcesses() {}
function validateForm() {}
function handleSubmit() {}

// 常量 - SCREAMING_SNAKE_CASE
const MAX_RETRY_COUNT = 3
const API_BASE_URL = 'https://api.example.com'

// 类型和接口 - PascalCase
interface ProcessModel {}
type TaskStatus = 'pending' | 'completed'

// 枚举 - PascalCase
enum ProcessStatus {
  DRAFT = 'draft',
  PUBLISHED = 'published',
  ARCHIVED = 'archived'
}
```

### 3. Vue 组件规范

#### 3.1 组件结构
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup lang="ts">
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import type { ProcessModel } from '@/types/process'

// 2. 类型定义
interface Props {
  processId: string
  readonly?: boolean
}

interface Emits {
  (e: 'update', process: ProcessModel): void
  (e: 'delete', id: string): void
}

// 3. Props 和 Emits
const props = withDefaults(defineProps<Props>(), {
  readonly: false
})

const emit = defineEmits<Emits>()

// 4. 响应式数据
const loading = ref(false)
const process = ref<ProcessModel | null>(null)

// 5. 计算属性
const isEditable = computed(() => !props.readonly && process.value?.status === 'draft')

// 6. 方法
const fetchProcess = async () => {
  loading.value = true
  try {
    // 获取流程数据
  } finally {
    loading.value = false
  }
}

// 7. 生命周期
onMounted(() => {
  fetchProcess()
})
</script>

<style scoped>
/* 样式 */
</style>
```

#### 3.2 组件命名和注册
```typescript
// 组件名使用 PascalCase
export default defineComponent({
  name: 'ProcessDesigner'
})

// 组件注册使用 kebab-case
<process-designer :process-id="processId" />
```

#### 3.3 Props 定义
```typescript
// 使用 TypeScript 接口定义 Props
interface Props {
  // 必需属性
  processId: string
  
  // 可选属性，提供默认值
  readonly?: boolean
  
  // 复杂类型
  process?: ProcessModel
  
  // 联合类型
  status?: 'draft' | 'published' | 'archived'
}

// 使用 withDefaults 提供默认值
const props = withDefaults(defineProps<Props>(), {
  readonly: false,
  status: 'draft'
})
```

### 4. TypeScript 规范

#### 4.1 类型定义
```typescript
// 基础类型
interface User {
  readonly id: string
  username: string
  email: string
  createdAt: Date
  updatedAt?: Date
}

// 泛型类型
interface ApiResponse<T> {
  success: boolean
  data: T
  message: string
  timestamp: Date
}

// 联合类型
type TaskStatus = 'pending' | 'in-progress' | 'completed' | 'cancelled'

// 工具类型
type CreateUserRequest = Omit<User, 'id' | 'createdAt' | 'updatedAt'>
type UpdateUserRequest = Partial<Pick<User, 'username' | 'email'>>
```

#### 4.2 函数类型
```typescript
// 函数签名
type EventHandler<T> = (event: T) => void
type AsyncFunction<T, R> = (params: T) => Promise<R>

// 函数实现
const handleProcessSelect: EventHandler<ProcessModel> = (process) => {
  console.log('Selected process:', process.name)
}

const fetchProcess: AsyncFunction<string, ProcessModel> = async (id) => {
  const response = await api.get(`/processes/${id}`)
  return response.data
}
```

### 5. 样式规范

#### 5.1 CSS 类命名 (BEM)
```scss
// Block
.process-designer {
  // Element
  &__toolbar {
    // Modifier
    &--collapsed {
      display: none;
    }
  }
  
  &__canvas {
    &--loading {
      opacity: 0.5;
    }
  }
}
```

#### 5.2 Tailwind CSS 使用
```vue
<template>
  <!-- 使用语义化的 CSS 类 -->
  <div class="process-card">
    <h3 class="process-card__title">{{ process.name }}</h3>
    <p class="process-card__description">{{ process.description }}</p>
  </div>
</template>

<style scoped>
.process-card {
  @apply bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow;
}

.process-card__title {
  @apply text-xl font-semibold text-gray-900 mb-2;
}

.process-card__description {
  @apply text-gray-600 text-sm;
}
</style>
```

## 后端代码规范 (Spring Boot + Java)

### 1. 项目结构规范

```
src/main/java/com/flowable/ui/
├── config/                 # 配置类
├── controller/             # 控制器
│   ├── api/               # API 控制器
│   └── web/               # Web 控制器
├── service/               # 服务层
│   ├── impl/              # 服务实现
│   └── dto/               # 数据传输对象
├── repository/            # 数据访问层
├── entity/                # 实体类
├── exception/             # 异常类
├── security/              # 安全配置
├── util/                  # 工具类
└── Application.java       # 启动类
```

### 2. 命名规范

#### 2.1 包命名
```java
// 使用小写字母和点分隔
com.flowable.ui.controller
com.flowable.ui.service.impl
com.flowable.ui.repository
```

#### 2.2 类命名
```java
// 控制器 - Controller 后缀
@RestController
public class ProcessController {}

// 服务接口 - Service 后缀
public interface ProcessService {}

// 服务实现 - ServiceImpl 后缀
@Service
public class ProcessServiceImpl implements ProcessService {}

// 实体类 - 名词
@Entity
public class ProcessModel {}

// DTO - Request/Response 后缀
public class ProcessCreateRequest {}
public class ProcessResponse {}

// 异常类 - Exception 后缀
public class ProcessNotFoundException extends RuntimeException {}
```

#### 2.3 方法命名
```java
// 查询方法 - find/get 开头
public ProcessModel findById(String id) {}
public List<ProcessModel> findByCategory(String category) {}

// 保存方法 - save/create 开头
public ProcessModel saveProcess(ProcessModel process) {}
public ProcessModel createProcess(ProcessCreateRequest request) {}

// 更新方法 - update 开头
public ProcessModel updateProcess(String id, ProcessUpdateRequest request) {}

// 删除方法 - delete/remove 开头
public void deleteProcess(String id) {}

// 验证方法 - validate/check 开头
public boolean validateProcess(ProcessModel process) {}
public void checkPermission(String userId, String processId) {}
```

### 3. 控制器规范

#### 3.1 REST 控制器
```java
@RestController
@RequestMapping("/api/processes")
@Validated
@Slf4j
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<ProcessResponse>>> getProcesses(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Range(min = 1, max = 100) int size,
            @RequestParam(required = false) String category) {
        
        PageResult<ProcessResponse> result = processService.getProcesses(page, size, category);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProcessResponse>> createProcess(
            @Valid @RequestBody ProcessCreateRequest request) {
        
        ProcessResponse response = processService.createProcess(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "流程创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessResponse>> updateProcess(
            @PathVariable String id,
            @Valid @RequestBody ProcessUpdateRequest request) {
        
        ProcessResponse response = processService.updateProcess(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "流程更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProcess(@PathVariable String id) {
        processService.deleteProcess(id);
        return ResponseEntity.ok(ApiResponse.success(null, "流程删除成功"));
    }
}
```

### 4. 服务层规范

#### 4.1 服务接口
```java
public interface ProcessService {
    
    /**
     * 获取流程列表
     * @param page 页码
     * @param size 页面大小
     * @param category 分类
     * @return 流程列表
     */
    PageResult<ProcessResponse> getProcesses(int page, int size, String category);
    
    /**
     * 创建流程
     * @param request 创建请求
     * @return 流程响应
     */
    ProcessResponse createProcess(ProcessCreateRequest request);
    
    /**
     * 更新流程
     * @param id 流程ID
     * @param request 更新请求
     * @return 流程响应
     */
    ProcessResponse updateProcess(String id, ProcessUpdateRequest request);
    
    /**
     * 删除流程
     * @param id 流程ID
     */
    void deleteProcess(String id);
}
```

#### 4.2 服务实现
```java
@Service
@Transactional
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;
    private final ProcessMapper processMapper;
    private final FlowableService flowableService;

    public ProcessServiceImpl(
            ProcessRepository processRepository,
            ProcessMapper processMapper,
            FlowableService flowableService) {
        this.processRepository = processRepository;
        this.processMapper = processMapper;
        this.flowableService = flowableService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProcessResponse> getProcesses(int page, int size, String category) {
        log.debug("获取流程列表: page={}, size={}, category={}", page, size, category);
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ProcessModel> processPage;
        
        if (StringUtils.hasText(category)) {
            processPage = processRepository.findByCategory(category, pageable);
        } else {
            processPage = processRepository.findAll(pageable);
        }
        
        List<ProcessResponse> responses = processPage.getContent().stream()
                .map(processMapper::toResponse)
                .collect(Collectors.toList());
        
        return PageResult.<ProcessResponse>builder()
                .items(responses)
                .page(page)
                .size(size)
                .total(processPage.getTotalElements())
                .totalPages(processPage.getTotalPages())
                .build();
    }

    @Override
    public ProcessResponse createProcess(ProcessCreateRequest request) {
        log.info("创建流程: {}", request.getName());
        
        // 验证流程键唯一性
        if (processRepository.existsByKey(request.getKey())) {
            throw new ProcessKeyExistsException("流程键已存在: " + request.getKey());
        }
        
        // 创建流程模型
        ProcessModel process = processMapper.toEntity(request);
        process.setId(UUID.randomUUID().toString());
        process.setCreatedTime(LocalDateTime.now());
        process.setStatus(ProcessStatus.DRAFT);
        
        ProcessModel savedProcess = processRepository.save(process);
        
        log.info("流程创建成功: id={}, name={}", savedProcess.getId(), savedProcess.getName());
        return processMapper.toResponse(savedProcess);
    }
}
```

### 5. 数据访问层规范

#### 5.1 Repository 接口
```java
@Repository
public interface ProcessRepository extends JpaRepository<ProcessModel, String> {
    
    /**
     * 根据分类查找流程
     */
    Page<ProcessModel> findByCategory(String category, Pageable pageable);
    
    /**
     * 根据键查找流程
     */
    Optional<ProcessModel> findByKey(String key);
    
    /**
     * 检查键是否存在
     */
    boolean existsByKey(String key);
    
    /**
     * 根据状态查找流程
     */
    List<ProcessModel> findByStatus(ProcessStatus status);
    
    /**
     * 自定义查询
     */
    @Query("SELECT p FROM ProcessModel p WHERE p.name LIKE %:name% AND p.category = :category")
    List<ProcessModel> findByNameContainingAndCategory(
            @Param("name") String name, 
            @Param("category") String category);
}
```

### 6. 实体类规范

#### 6.1 JPA 实体
```java
@Entity
@Table(name = "process_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ProcessModel {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "process_key", nullable = false, unique = true, length = 50)
    private String key;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessStatus status;

    @Lob
    @Column(name = "model_json")
    private String modelJson;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
```

### 7. 异常处理规范

#### 7.1 自定义异常
```java
// 业务异常基类
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    
    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

// 具体业务异常
public class ProcessNotFoundException extends BusinessException {
    public ProcessNotFoundException(String processId) {
        super("PROCESS_NOT_FOUND", "流程不存在: " + processId);
    }
}

public class ProcessKeyExistsException extends BusinessException {
    public ProcessKeyExistsException(String key) {
        super("PROCESS_KEY_EXISTS", "流程键已存在: " + key);
    }
}
```

#### 7.2 全局异常处理
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage()));
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.validationError("输入验证失败", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "系统内部错误"));
    }
}
```

## 代码质量工具配置

### 1. ESLint 配置 (前端)

```javascript
// .eslintrc.js
module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es2022: true
  },
  extends: [
    'eslint:recommended',
    '@typescript-eslint/recommended',
    '@vue/eslint-config-typescript',
    '@vue/eslint-config-prettier'
  ],
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@typescript-eslint/parser',
    ecmaVersion: 2022,
    sourceType: 'module'
  },
  rules: {
    // 代码质量
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-unused-vars': 'off',
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    
    // Vue 特定规则
    'vue/multi-word-component-names': 'error',
    'vue/component-definition-name-casing': ['error', 'PascalCase'],
    'vue/component-name-in-template-casing': ['error', 'kebab-case'],
    'vue/prop-name-casing': ['error', 'camelCase'],
    
    // TypeScript 规则
    '@typescript-eslint/explicit-function-return-type': 'off',
    '@typescript-eslint/no-explicit-any': 'warn',
    '@typescript-eslint/prefer-const': 'error',
    
    // 代码风格
    'prefer-const': 'error',
    'no-var': 'error',
    'object-shorthand': 'error',
    'prefer-template': 'error'
  }
}
```

### 2. Prettier 配置

```javascript
// .prettierrc.js
module.exports = {
  semi: false,
  singleQuote: true,
  quoteProps: 'as-needed',
  trailingComma: 'none',
  bracketSpacing: true,
  bracketSameLine: false,
  arrowParens: 'avoid',
  printWidth: 100,
  tabWidth: 2,
  useTabs: false,
  endOfLine: 'lf',
  vueIndentScriptAndStyle: false
}
```

### 3. Checkstyle 配置 (后端)

```xml
<!-- checkstyle.xml -->
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="warning"/>
    <property name="fileExtensions" value="java, properties, xml"/>

    <!-- 文件长度检查 -->
    <module name="FileLength">
        <property name="max" value="2000"/>
    </module>

    <!-- 行长度检查 -->
    <module name="LineLength">
        <property name="max" value="120"/>
        <property name="ignorePattern" value="^package.*|^import.*|a href|href|http://|https://|ftp://"/>
    </module>

    <module name="TreeWalker">
        <!-- 命名约定 -->
        <module name="ConstantName"/>
        <module name="LocalFinalVariableName"/>
        <module name="LocalVariableName"/>
        <module name="MemberName"/>
        <module name="MethodName"/>
        <module name="PackageName"/>
        <module name="ParameterName"/>
        <module name="StaticVariableName"/>
        <module name="TypeName"/>

        <!-- 导入检查 -->
        <module name="AvoidStarImport"/>
        <module name="IllegalImport"/>
        <module name="RedundantImport"/>
        <module name="UnusedImports"/>

        <!-- 方法长度 -->
        <module name="MethodLength">
            <property name="max" value="150"/>
        </module>

        <!-- 参数个数 -->
        <module name="ParameterNumber">
            <property name="max" value="7"/>
        </module>

        <!-- 空白检查 -->
        <module name="EmptyForIteratorPad"/>
        <module name="GenericWhitespace"/>
        <module name="MethodParamPad"/>
        <module name="NoWhitespaceAfter"/>
        <module name="NoWhitespaceBefore"/>
        <module name="OperatorWrap"/>
        <module name="ParenPad"/>
        <module name="TypecastParenPad"/>
        <module name="WhitespaceAfter"/>
        <module name="WhitespaceAround"/>
    </module>
</module>
```

## Git 提交规范

### 1. 提交消息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 2. 类型说明

- `feat`: 新功能
- `fix`: 错误修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

### 3. 示例

```
feat(process-designer): 添加流程节点拖拽功能

- 实现 BPMN 元素的拖拽操作
- 添加画布自动对齐功能
- 支持撤销/重做操作

Closes #123
```

## 最佳实践

### 1. 代码审查清单

#### 1.1 功能性
- [ ] 代码实现了需求规格
- [ ] 边界条件处理正确
- [ ] 错误处理完善
- [ ] 性能满足要求

#### 1.2 可读性
- [ ] 命名清晰有意义
- [ ] 代码结构清晰
- [ ] 注释适当且有用
- [ ] 复杂逻辑有说明

#### 1.3 可维护性
- [ ] 遵循 SOLID 原则
- [ ] 没有重复代码
- [ ] 模块职责单一
- [ ] 依赖关系清晰

### 2. 重构指导原则

#### 2.1 何时重构
- 添加新功能前
- 修复错误时
- 代码审查发现问题时
- 性能优化时

#### 2.2 重构步骤
1. 确保有足够的测试覆盖
2. 小步骤进行重构
3. 每步都运行测试
4. 提交每个重构步骤

### 3. 性能优化指南

#### 3.1 前端优化
- 使用 `v-memo` 缓存复杂计算
- 合理使用 `shallowRef` 和 `shallowReactive`
- 避免在模板中使用复杂表达式
- 使用虚拟滚动处理大列表

#### 3.2 后端优化
- 使用合适的数据库索引
- 避免 N+1 查询问题
- 使用缓存减少数据库访问
- 合理使用事务边界