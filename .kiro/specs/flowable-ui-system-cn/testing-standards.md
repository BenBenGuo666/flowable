# 测试标准和方法

## 概述

本文档定义了 Flowable UI 系统的测试标准、方法和最佳实践，确保代码质量和系统可靠性。

## 测试策略

### 1. 测试金字塔

```
        /\
       /  \
      / E2E \     端到端测试 (10%)
     /______\
    /        \
   / 集成测试  \   集成测试 (20%)
  /____________\
 /              \
/    单元测试     \  单元测试 (70%)
/________________\
```

### 2. 测试覆盖率目标

| 测试类型 | 覆盖率目标 | 说明 |
|----------|------------|------|
| 单元测试 | ≥ 80% | 代码行覆盖率 |
| 集成测试 | ≥ 70% | API 端点覆盖率 |
| 端到端测试 | 100% | 关键用户路径 |
| 分支覆盖率 | ≥ 75% | 条件分支覆盖 |

## 前端测试标准

### 1. 单元测试 (Vue.js)

#### 1.1 测试框架
- **测试运行器**: Vitest
- **测试工具**: Vue Test Utils
- **断言库**: Chai/Jest
- **模拟库**: Sinon.js

#### 1.2 组件测试示例
```typescript
// ProcessList.spec.ts
import { mount } from '@vue/test-utils'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import ProcessList from '@/components/ProcessList.vue'
import { createTestingPinia } from '@pinia/testing'

describe('ProcessList', () => {
  let wrapper: any
  
  beforeEach(() => {
    wrapper = mount(ProcessList, {
      global: {
        plugins: [createTestingPinia({
          createSpy: vi.fn
        })]
      }
    })
  })

  it('应该渲染流程列表', () => {
    expect(wrapper.find('[data-testid="process-list"]').exists()).toBe(true)
  })

  it('应该显示加载状态', async () => {
    wrapper.vm.loading = true
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-testid="loading"]').exists()).toBe(true)
  })

  it('应该处理流程选择事件', async () => {
    const process = { id: '1', name: '测试流程' }
    await wrapper.vm.selectProcess(process)
    expect(wrapper.emitted('process-selected')).toBeTruthy()
    expect(wrapper.emitted('process-selected')[0]).toEqual([process])
  })
})
```

#### 1.3 Composables 测试
```typescript
// useProcesses.spec.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useProcesses } from '@/composables/useProcesses'
import { createPinia, setActivePinia } from 'pinia'

describe('useProcesses', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应该获取流程列表', async () => {
    const { processes, fetchProcesses } = useProcesses()
    
    // 模拟 API 响应
    vi.mocked(processApi.getProcesses).mockResolvedValue({
      data: [{ id: '1', name: '测试流程' }]
    })

    await fetchProcesses()
    
    expect(processes.value).toHaveLength(1)
    expect(processes.value[0].name).toBe('测试流程')
  })
})
```

### 2. 集成测试

#### 2.1 API 集成测试
```typescript
// api.integration.spec.ts
import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import { setupServer } from 'msw/node'
import { rest } from 'msw'
import { processApi } from '@/services/api'

const server = setupServer(
  rest.get('/api/processes', (req, res, ctx) => {
    return res(ctx.json({
      success: true,
      data: {
        items: [{ id: '1', name: '测试流程' }],
        pagination: { page: 1, size: 20, total: 1 }
      }
    }))
  })
)

describe('Process API Integration', () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())

  it('应该获取流程列表', async () => {
    const response = await processApi.getProcesses()
    expect(response.data.items).toHaveLength(1)
    expect(response.data.items[0].name).toBe('测试流程')
  })
})
```

### 3. 端到端测试 (E2E)

#### 3.1 测试框架
- **E2E 框架**: Playwright
- **页面对象模式**: 是
- **测试数据管理**: 独立测试数据库

#### 3.2 E2E 测试示例
```typescript
// process-management.e2e.spec.ts
import { test, expect } from '@playwright/test'
import { ProcessListPage } from '../pages/ProcessListPage'
import { ProcessDesignerPage } from '../pages/ProcessDesignerPage'

test.describe('流程管理', () => {
  let processListPage: ProcessListPage
  let processDesignerPage: ProcessDesignerPage

  test.beforeEach(async ({ page }) => {
    processListPage = new ProcessListPage(page)
    processDesignerPage = new ProcessDesignerPage(page)
    await processListPage.goto()
  })

  test('应该能够创建新流程', async () => {
    await processListPage.clickCreateProcess()
    await processDesignerPage.waitForLoad()
    
    await processDesignerPage.setProcessName('测试流程')
    await processDesignerPage.addStartEvent()
    await processDesignerPage.addUserTask('审批任务')
    await processDesignerPage.addEndEvent()
    
    await processDesignerPage.saveProcess()
    
    await expect(processDesignerPage.successMessage).toBeVisible()
  })
})
```

#### 3.3 页面对象模式
```typescript
// ProcessListPage.ts
import { Page, Locator } from '@playwright/test'

export class ProcessListPage {
  readonly page: Page
  readonly createButton: Locator
  readonly processTable: Locator
  readonly searchInput: Locator

  constructor(page: Page) {
    this.page = page
    this.createButton = page.locator('[data-testid="create-process"]')
    this.processTable = page.locator('[data-testid="process-table"]')
    this.searchInput = page.locator('[data-testid="search-input"]')
  }

  async goto() {
    await this.page.goto('/processes')
  }

  async clickCreateProcess() {
    await this.createButton.click()
  }

  async searchProcess(name: string) {
    await this.searchInput.fill(name)
    await this.searchInput.press('Enter')
  }
}
```

## 后端测试标准

### 1. 单元测试 (Spring Boot)

#### 1.1 测试框架
- **测试框架**: JUnit 5
- **模拟框架**: Mockito
- **断言库**: AssertJ
- **测试切片**: @WebMvcTest, @DataJpaTest

#### 1.2 Service 层测试
```java
// ProcessServiceTest.java
@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

    @Mock
    private ProcessRepository processRepository;
    
    @Mock
    private FlowableService flowableService;
    
    @InjectMocks
    private ProcessService processService;

    @Test
    @DisplayName("应该成功创建流程模型")
    void shouldCreateProcessModel() {
        // Given
        ProcessCreateRequest request = ProcessCreateRequest.builder()
            .name("测试流程")
            .key("test_process")
            .category("TEST")
            .build();
            
        ProcessModel savedModel = ProcessModel.builder()
            .id("1")
            .name("测试流程")
            .key("test_process")
            .build();
            
        when(processRepository.save(any(ProcessModel.class)))
            .thenReturn(savedModel);

        // When
        ProcessModel result = processService.createProcess(request);

        // Then
        assertThat(result.getName()).isEqualTo("测试流程");
        assertThat(result.getKey()).isEqualTo("test_process");
        verify(processRepository).save(any(ProcessModel.class));
    }

    @Test
    @DisplayName("当流程键已存在时应该抛出异常")
    void shouldThrowExceptionWhenProcessKeyExists() {
        // Given
        ProcessCreateRequest request = ProcessCreateRequest.builder()
            .key("existing_key")
            .build();
            
        when(processRepository.existsByKey("existing_key"))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> processService.createProcess(request))
            .isInstanceOf(ProcessKeyExistsException.class)
            .hasMessage("流程键已存在: existing_key");
    }
}
```

#### 1.3 Repository 层测试
```java
// ProcessRepositoryTest.java
@DataJpaTest
class ProcessRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProcessRepository processRepository;

    @Test
    @DisplayName("应该根据分类查找流程")
    void shouldFindProcessesByCategory() {
        // Given
        ProcessModel process1 = ProcessModel.builder()
            .name("流程1")
            .category("HR")
            .build();
        ProcessModel process2 = ProcessModel.builder()
            .name("流程2")
            .category("FINANCE")
            .build();
            
        entityManager.persistAndFlush(process1);
        entityManager.persistAndFlush(process2);

        // When
        List<ProcessModel> hrProcesses = processRepository.findByCategory("HR");

        // Then
        assertThat(hrProcesses).hasSize(1);
        assertThat(hrProcesses.get(0).getName()).isEqualTo("流程1");
    }
}
```

### 2. 集成测试

#### 2.1 Web 层集成测试
```java
// ProcessControllerIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class ProcessControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private FlowableService flowableService;

    @Test
    @DisplayName("应该成功创建流程")
    void shouldCreateProcess() throws Exception {
        // Given
        ProcessCreateRequest request = ProcessCreateRequest.builder()
            .name("测试流程")
            .key("test_process")
            .category("TEST")
            .build();

        // When & Then
        mockMvc.perform(post("/api/processes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("测试流程"))
                .andExpect(jsonPath("$.data.key").value("test_process"));
    }

    @Test
    @DisplayName("当请求参数无效时应该返回400错误")
    void shouldReturn400WhenRequestInvalid() throws Exception {
        // Given
        ProcessCreateRequest request = ProcessCreateRequest.builder()
            .name("") // 空名称
            .build();

        // When & Then
        mockMvc.perform(post("/api/processes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
```

### 3. Flowable 集成测试

#### 3.1 流程引擎测试
```java
// FlowableIntegrationTest.java
@SpringBootTest
@Deployment(resources = {"processes/test-process.bpmn20.xml"})
class FlowableIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private HistoryService historyService;

    @Test
    @DisplayName("应该成功启动和完成流程")
    void shouldStartAndCompleteProcess() {
        // Given
        Map<String, Object> variables = Map.of(
            "applicant", "张三",
            "amount", 1000
        );

        // When - 启动流程
        ProcessInstance processInstance = runtimeService
            .startProcessInstanceByKey("test-process", variables);

        // Then - 验证流程已启动
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.isEnded()).isFalse();

        // When - 完成第一个任务
        Task task = taskService.createTaskQuery()
            .processInstanceId(processInstance.getId())
            .singleResult();
        taskService.complete(task.getId(), Map.of("approved", true));

        // Then - 验证流程已完成
        HistoricProcessInstance historicProcess = historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceId(processInstance.getId())
            .singleResult();
        assertThat(historicProcess.getEndTime()).isNotNull();
    }
}
```

## 测试数据管理

### 1. 测试数据策略

#### 1.1 数据隔离
- 每个测试使用独立的数据集
- 测试后自动清理数据
- 使用事务回滚保证数据隔离

#### 1.2 测试数据工厂
```typescript
// testDataFactory.ts
export class TestDataFactory {
  static createProcess(overrides: Partial<ProcessModel> = {}): ProcessModel {
    return {
      id: faker.datatype.uuid(),
      name: faker.company.name() + '流程',
      key: faker.random.alphaNumeric(10),
      category: 'TEST',
      version: 1,
      createdTime: new Date(),
      ...overrides
    }
  }

  static createUser(overrides: Partial<User> = {}): User {
    return {
      id: faker.datatype.uuid(),
      username: faker.internet.userName(),
      email: faker.internet.email(),
      firstName: faker.name.firstName(),
      lastName: faker.name.lastName(),
      enabled: true,
      ...overrides
    }
  }
}
```

#### 1.3 数据库测试配置
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  sql:
    init:
      mode: always
      data-locations: classpath:test-data.sql
```

## 性能测试

### 1. 前端性能测试

#### 1.1 Lighthouse CI
```javascript
// lighthouse.config.js
module.exports = {
  ci: {
    collect: {
      url: ['http://localhost:3000/processes', 'http://localhost:3000/tasks'],
      numberOfRuns: 3
    },
    assert: {
      assertions: {
        'categories:performance': ['warn', { minScore: 0.8 }],
        'categories:accessibility': ['error', { minScore: 0.9 }],
        'categories:best-practices': ['warn', { minScore: 0.8 }],
        'categories:seo': ['warn', { minScore: 0.8 }]
      }
    }
  }
}
```

### 2. 后端性能测试

#### 2.1 JMeter 测试计划
```xml
<!-- process-api-load-test.jmx -->
<TestPlan>
  <ThreadGroup>
    <stringProp name="ThreadGroup.num_threads">100</stringProp>
    <stringProp name="ThreadGroup.ramp_time">60</stringProp>
    <stringProp name="ThreadGroup.duration">300</stringProp>
  </ThreadGroup>
  
  <HTTPSamplerProxy>
    <stringProp name="HTTPSampler.path">/api/processes</stringProp>
    <stringProp name="HTTPSampler.method">GET</stringProp>
  </HTTPSamplerProxy>
  
  <ResponseAssertion>
    <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
    <stringProp name="Assertion.test_type">Assertion.test_type_equals</stringProp>
    <stringProp name="Assertion.test_string">200</stringProp>
  </ResponseAssertion>
</TestPlan>
```

## 测试环境配置

### 1. CI/CD 集成

#### 1.1 GitHub Actions 配置
```yaml
# .github/workflows/test.yml
name: Test Suite

on: [push, pull_request]

jobs:
  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: npm ci
      - run: npm run test:unit
      - run: npm run test:e2e
      - run: npm run test:coverage

  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: ./mvnw test
      - run: ./mvnw verify
```

### 2. 测试报告

#### 2.1 覆盖率报告
- 使用 Istanbul (前端) 和 JaCoCo (后端)
- 生成 HTML 和 XML 格式报告
- 集成到 CI/CD 流水线

#### 2.2 测试结果通知
- 测试失败时发送通知
- 覆盖率下降时告警
- 性能回归检测

## 最佳实践

### 1. 测试编写原则

#### 1.1 AAA 模式
- **Arrange**: 准备测试数据和环境
- **Act**: 执行被测试的操作
- **Assert**: 验证结果

#### 1.2 测试命名规范
```typescript
// 好的命名
it('应该在用户名为空时返回验证错误')
it('应该在流程部署成功后更新状态')

// 避免的命名
it('测试用户验证')
it('流程测试')
```

### 2. 测试维护

#### 2.1 定期审查
- 每月审查测试覆盖率
- 清理过时的测试用例
- 更新测试数据和模拟

#### 2.2 测试重构
- 提取公共测试工具
- 减少测试间的重复代码
- 保持测试的可读性和可维护性