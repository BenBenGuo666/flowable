# Flowable 请假流程 API 测试文档

## 项目说明
这是一个基于 Flowable 工作流引擎的请假审批流程示例项目。

## 流程说明
1. 员工提交请假申请
2. 经理审批（批准/拒绝）
3. 如果批准：HR备案 → 流程结束
4. 如果拒绝：直接结束流程

## 启动项目
```bash
mvn clean install
mvn spring-boot:run
```

## API 接口测试

### 1. 提交请假申请
**接口**: `POST /api/leave/submit`

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/leave/submit \
  -H "Content-Type: application/json" \
  -d '{
    "applicant": "张三",
    "leaveType": "年假",
    "startDate": "2025-11-10",
    "endDate": "2025-11-12",
    "days": 3,
    "reason": "家庭旅游"
  }'
```

**响应示例**:
```json
{
  "success": true,
  "message": "请假申请提交成功",
  "processInstanceId": "12501"
}
```

### 2. 查询待办任务
**接口**: `GET /api/leave/tasks?assignee={处理人}`

**请求示例**:
```bash
# 查询经理的待办任务
curl -X GET "http://localhost:8080/api/leave/tasks?assignee=manager"
```

**响应示例**:
```json
{
  "success": true,
  "count": 1,
  "data": [
    {
      "taskId": "12505",
      "taskName": "经理审批",
      "processInstanceId": "12501",
      "createTime": "2025-11-04T10:30:00",
      "variables": {
        "applicant": "张三",
        "leaveType": "年假",
        "startDate": "2025-11-10",
        "endDate": "2025-11-12",
        "days": 3,
        "reason": "家庭旅游"
      }
    }
  ]
}
```

### 3. 审批任务（批准）
**接口**: `POST /api/leave/approve/{taskId}`

**请求示例**:
```bash
# 经理批准请假
curl -X POST http://localhost:8080/api/leave/approve/12505 \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "comment": "同意请假，注意工作交接"
  }'
```

**响应示例**:
```json
{
  "success": true,
  "message": "审批完成"
}
```

### 4. 审批任务（拒绝）
**请求示例**:
```bash
# 经理拒绝请假
curl -X POST http://localhost:8080/api/leave/approve/12505 \
  -H "Content-Type: application/json" \
  -d '{
    "approved": false,
    "comment": "当前项目紧张，暂时无法批准"
  }'
```

### 5. HR备案
如果经理批准，流程会流转到HR备案环节：

```bash
# 查询HR的待办任务
curl -X GET "http://localhost:8080/api/leave/tasks?assignee=hr"

# HR完成备案
curl -X POST http://localhost:8080/api/leave/approve/{hrTaskId} \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "comment": "已完成备案"
  }'
```

### 6. 查询流程实例状态
**接口**: `GET /api/leave/process/{processInstanceId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/leave/process/12501"
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "processInstanceId": "12501",
    "processDefinitionId": "leaveProcess:1:10001",
    "isEnded": false,
    "isActive": true
  }
}
```

## 完整流程测试示例

```bash
# 1. 提交请假申请
RESPONSE=$(curl -s -X POST http://localhost:8080/api/leave/submit \
  -H "Content-Type: application/json" \
  -d '{
    "applicant": "李四",
    "leaveType": "病假",
    "startDate": "2025-11-05",
    "endDate": "2025-11-06",
    "days": 2,
    "reason": "身体不适需要休息"
  }')

echo "提交结果: $RESPONSE"

# 2. 查询经理待办
curl -X GET "http://localhost:8080/api/leave/tasks?assignee=manager"

# 3. 经理审批（假设任务ID为12510）
curl -X POST http://localhost:8080/api/leave/approve/12510 \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "comment": "同意病假申请，注意休息"
  }'

# 4. 查询HR待办
curl -X GET "http://localhost:8080/api/leave/tasks?assignee=hr"

# 5. HR备案（假设任务ID为12515）
curl -X POST http://localhost:8080/api/leave/approve/12515 \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "comment": "已完成考勤备案"
  }'
```

## 注意事项

1. **数据库准备**: 确保MySQL数据库 `flowable` 已创建
   ```sql
   CREATE DATABASE flowable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **首次启动**: Flowable会自动创建所需的数据库表（约30张表）

3. **流程部署**: BPMN文件位于 `src/main/resources/processes/` 目录，启动时自动部署

4. **任务处理人**:
   - 经理审批: assignee = "manager"
   - HR备案: assignee = "hr"

5. **端口配置**: 默认端口8080，可在 `application.yml` 中修改
