# 部署流程和工作流

## 概述

本文档定义了 Flowable UI 系统的部署流程、环境管理策略和 CI/CD 工作流，确保系统的可靠部署和运维。

## 环境架构

### 1. 环境分层

```
┌─────────────────────────────────────────┐
│              生产环境 (PROD)              │
│  - 高可用部署                            │
│  - 负载均衡                              │
│  - 数据备份                              │
│  - 监控告警                              │
└─────────────────────────────────────────┘
                    ▲
                    │ 发布
┌─────────────────────────────────────────┐
│            预生产环境 (STAGING)           │
│  - 生产环境镜像                          │
│  - 性能测试                              │
│  - 用户验收测试                          │
└─────────────────────────────────────────┘
                    ▲
                    │ 集成测试
┌─────────────────────────────────────────┐
│              测试环境 (TEST)              │
│  - 自动化测试                            │
│  - 集成测试                              │
│  - API 测试                             │
└─────────────────────────────────────────┘
                    ▲
                    │ 构建部署
┌─────────────────────────────────────────┐
│            开发环境 (DEVELOP)             │
│  - 功能开发                              │
│  - 单元测试                              │
│  - 代码集成                              │
└─────────────────────────────────────────┘
```

### 2. 环境配置

#### 2.1 开发环境 (DEVELOP)
```yaml
# application-dev.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:devdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  redis:
    host: localhost
    port: 6379
    database: 0

flowable:
  rest:
    app:
      authentication-mode: verify-privilege
  
logging:
  level:
    com.flowable.ui: DEBUG
    org.springframework.security: DEBUG
```

#### 2.2 测试环境 (TEST)
```yaml
# application-test.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://test-db:3306/flowable_ui_test
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 5
  
  redis:
    host: test-redis
    port: 6379
    database: 1

flowable:
  database-schema-update: true
  
logging:
  level:
    com.flowable.ui: INFO
```

#### 2.3 预生产环境 (STAGING)
```yaml
# application-staging.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://staging-db:3306/flowable_ui_staging
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  
  redis:
    cluster:
      nodes: staging-redis-1:6379,staging-redis-2:6379,staging-redis-3:6379
    database: 0

flowable:
  database-schema-update: false
  
logging:
  level:
    com.flowable.ui: WARN
```

#### 2.4 生产环境 (PROD)
```yaml
# application-prod.yml
server:
  port: 8080
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE_PATH}
    key-store-password: ${SSL_KEYSTORE_PASSWORD}

spring:
  datasource:
    url: jdbc:mysql://prod-db-cluster:3306/flowable_ui_prod
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
  
  redis:
    cluster:
      nodes: ${REDIS_CLUSTER_NODES}
    password: ${REDIS_PASSWORD}
    database: 0

flowable:
  database-schema-update: false
  
logging:
  level:
    com.flowable.ui: ERROR
    org.springframework.security: WARN
```

## CI/CD 流水线

### 1. GitHub Actions 工作流

#### 1.1 主工作流配置
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: flowable_ui_test
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3
      
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: --health-cmd="redis-cli ping" --health-interval=10s --health-timeout=5s --health-retries=3

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'

      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2

      - name: Install frontend dependencies
        run: npm ci
        working-directory: ./frontend

      - name: Run frontend tests
        run: |
          npm run test:unit
          npm run test:e2e
        working-directory: ./frontend

      - name: Build frontend
        run: npm run build
        working-directory: ./frontend

      - name: Run backend tests
        run: ./mvnw test
        working-directory: ./backend

      - name: Run integration tests
        run: ./mvnw verify -P integration-test
        working-directory: ./backend
        env:
          SPRING_PROFILES_ACTIVE: test

      - name: Generate test report
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Test Results
          path: '**/target/surefire-reports/*.xml'
          reporter: java-junit

      - name: Upload coverage reports
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml,./frontend/coverage/lcov.info

  security-scan:
    runs-on: ubuntu-latest
    needs: test
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'

      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'

      - name: Run OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          project: 'flowable-ui-system'
          path: '.'
          format: 'ALL'

  build-and-push:
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    if: github.event_name == 'push'
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=ref,event=pr
            type=sha,prefix={{branch}}-
            type=raw,value=latest,enable={{is_default_branch}}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          platforms: linux/amd64,linux/arm64
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  deploy-staging:
    runs-on: ubuntu-latest
    needs: build-and-push
    if: github.ref == 'refs/heads/develop'
    environment: staging
    
    steps:
      - name: Deploy to Staging
        uses: azure/k8s-deploy@v1
        with:
          manifests: |
            k8s/staging/deployment.yaml
            k8s/staging/service.yaml
            k8s/staging/ingress.yaml
          images: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:develop-${{ github.sha }}

  deploy-production:
    runs-on: ubuntu-latest
    needs: build-and-push
    if: github.ref == 'refs/heads/main'
    environment: production
    
    steps:
      - name: Deploy to Production
        uses: azure/k8s-deploy@v1
        with:
          manifests: |
            k8s/production/deployment.yaml
            k8s/production/service.yaml
            k8s/production/ingress.yaml
          images: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
```

### 2. 构建配置

#### 2.1 多阶段 Dockerfile
```dockerfile
# Dockerfile
# 前端构建阶段
FROM node:18-alpine AS frontend-builder

WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci --only=production

COPY frontend/ ./
RUN npm run build

# 后端构建阶段
FROM maven:3.9-openjdk-17-slim AS backend-builder

WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B

COPY backend/src ./src
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

RUN mvn clean package -DskipTests

# 运行时镜像
FROM openjdk:17-jre-slim

# 创建应用用户
RUN groupadd -r flowable && useradd -r -g flowable flowable

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制应用文件
COPY --from=backend-builder /app/backend/target/flowable-ui-*.jar app.jar
COPY docker/entrypoint.sh ./
RUN chmod +x entrypoint.sh

# 创建日志目录
RUN mkdir -p /app/logs && chown -R flowable:flowable /app

# 切换到应用用户
USER flowable

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["./entrypoint.sh"]
```

#### 2.2 启动脚本
```bash
#!/bin/bash
# docker/entrypoint.sh

set -e

# 等待数据库启动
echo "等待数据库连接..."
while ! nc -z ${DB_HOST:-localhost} ${DB_PORT:-3306}; do
  sleep 1
done
echo "数据库连接成功"

# 等待 Redis 启动
echo "等待 Redis 连接..."
while ! nc -z ${REDIS_HOST:-localhost} ${REDIS_PORT:-6379}; do
  sleep 1
done
echo "Redis 连接成功"

# 设置 JVM 参数
JAVA_OPTS="${JAVA_OPTS} -Xms${HEAP_SIZE:-512m} -Xmx${HEAP_SIZE:-512m}"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/"
JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"

echo "启动应用..."
exec java ${JAVA_OPTS} -jar app.jar
```

## Kubernetes 部署

### 1. 部署配置

#### 1.1 生产环境部署
```yaml
# k8s/production/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flowable-ui
  namespace: flowable-prod
  labels:
    app: flowable-ui
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: flowable-ui
  template:
    metadata:
      labels:
        app: flowable-ui
        version: v1.0.0
    spec:
      serviceAccountName: flowable-ui
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: flowable-ui
        image: ghcr.io/company/flowable-ui-system:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: host
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: password
        - name: REDIS_CLUSTER_NODES
          valueFrom:
            configMapKeyRef:
              name: redis-config
              key: cluster-nodes
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        volumeMounts:
        - name: logs
          mountPath: /app/logs
        - name: config
          mountPath: /app/config
          readOnly: true
      volumes:
      - name: logs
        emptyDir: {}
      - name: config
        configMap:
          name: flowable-ui-config
      imagePullSecrets:
      - name: ghcr-secret
```

#### 1.2 服务配置
```yaml
# k8s/production/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: flowable-ui-service
  namespace: flowable-prod
  labels:
    app: flowable-ui
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: flowable-ui

---
apiVersion: v1
kind: Service
metadata:
  name: flowable-ui-headless
  namespace: flowable-prod
  labels:
    app: flowable-ui
spec:
  type: ClusterIP
  clusterIP: None
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: flowable-ui
```

#### 1.3 Ingress 配置
```yaml
# k8s/production/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: flowable-ui-ingress
  namespace: flowable-prod
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - flowable.example.com
    secretName: flowable-ui-tls
  rules:
  - host: flowable.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: flowable-ui-service
            port:
              number: 80
```

### 2. 配置管理

#### 2.1 ConfigMap
```yaml
# k8s/production/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: flowable-ui-config
  namespace: flowable-prod
data:
  application.yml: |
    server:
      port: 8080
      servlet:
        context-path: /
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: when-authorized
    
    logging:
      level:
        com.flowable.ui: INFO
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
        file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
      file:
        name: /app/logs/application.log

  redis-config: |
    cluster-nodes: redis-cluster-0.redis-cluster:6379,redis-cluster-1.redis-cluster:6379,redis-cluster-2.redis-cluster:6379
```

#### 2.2 Secret 管理
```yaml
# k8s/production/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: database-secret
  namespace: flowable-prod
type: Opaque
data:
  host: <base64-encoded-host>
  username: <base64-encoded-username>
  password: <base64-encoded-password>

---
apiVersion: v1
kind: Secret
metadata:
  name: jwt-secret
  namespace: flowable-prod
type: Opaque
data:
  secret: <base64-encoded-jwt-secret>

---
apiVersion: v1
kind: Secret
metadata:
  name: ghcr-secret
  namespace: flowable-prod
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: <base64-encoded-docker-config>
```

## 数据库管理

### 1. 数据库迁移

#### 1.1 Flyway 配置
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>9.22.0</version>
    <configuration>
        <url>${flyway.url}</url>
        <user>${flyway.user}</user>
        <password>${flyway.password}</password>
        <locations>
            <location>classpath:db/migration</location>
        </locations>
        <baselineOnMigrate>true</baselineOnMigrate>
        <validateOnMigrate>true</validateOnMigrate>
    </configuration>
</plugin>
```

#### 1.2 迁移脚本示例
```sql
-- src/main/resources/db/migration/V1__Initial_schema.sql
CREATE TABLE IF NOT EXISTS process_models (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    process_key VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50),
    description TEXT,
    version INT NOT NULL DEFAULT 1,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    model_json LONGTEXT,
    tenant_id VARCHAR(50),
    created_by VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_created_by (created_by)
);

-- src/main/resources/db/migration/V2__Add_audit_tables.sql
CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource VARCHAR(50) NOT NULL,
    resource_id VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    result ENUM('SUCCESS', 'FAILURE') NOT NULL,
    details TEXT,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_resource (resource),
    INDEX idx_timestamp (timestamp)
);
```

### 2. 数据备份策略

#### 2.1 自动备份脚本
```bash
#!/bin/bash
# scripts/backup-database.sh

set -e

# 配置
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-flowable_ui_prod}
DB_USER=${DB_USER:-backup_user}
DB_PASSWORD=${DB_PASSWORD}
BACKUP_DIR=${BACKUP_DIR:-/backups}
RETENTION_DAYS=${RETENTION_DAYS:-30}

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 生成备份文件名
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/flowable_ui_${TIMESTAMP}.sql.gz"

echo "开始数据库备份: ${DB_NAME}"

# 执行备份
mysqldump \
  --host=${DB_HOST} \
  --port=${DB_PORT} \
  --user=${DB_USER} \
  --password=${DB_PASSWORD} \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --hex-blob \
  --opt \
  ${DB_NAME} | gzip > ${BACKUP_FILE}

if [ $? -eq 0 ]; then
    echo "备份成功: ${BACKUP_FILE}"
    
    # 验证备份文件
    if [ -s ${BACKUP_FILE} ]; then
        echo "备份文件验证成功"
    else
        echo "错误: 备份文件为空"
        exit 1
    fi
else
    echo "错误: 备份失败"
    exit 1
fi

# 清理旧备份
echo "清理 ${RETENTION_DAYS} 天前的备份文件"
find ${BACKUP_DIR} -name "flowable_ui_*.sql.gz" -mtime +${RETENTION_DAYS} -delete

echo "备份完成"
```

#### 2.2 Kubernetes CronJob 备份
```yaml
# k8s/production/backup-cronjob.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: database-backup
  namespace: flowable-prod
spec:
  schedule: "0 2 * * *"  # 每天凌晨2点
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: mysql:8.0
            command:
            - /bin/bash
            - -c
            - |
              mysqldump \
                --host=${DB_HOST} \
                --user=${DB_USER} \
                --password=${DB_PASSWORD} \
                --single-transaction \
                --routines \
                --triggers \
                ${DB_NAME} | gzip > /backup/flowable_ui_$(date +%Y%m%d_%H%M%S).sql.gz
            env:
            - name: DB_HOST
              valueFrom:
                secretKeyRef:
                  name: database-secret
                  key: host
            - name: DB_USER
              valueFrom:
                secretKeyRef:
                  name: database-secret
                  key: username
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: database-secret
                  key: password
            - name: DB_NAME
              value: "flowable_ui_prod"
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
```

## 监控和日志

### 1. 应用监控

#### 1.1 Prometheus 配置
```yaml
# k8s/monitoring/servicemonitor.yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: flowable-ui-monitor
  namespace: flowable-prod
  labels:
    app: flowable-ui
spec:
  selector:
    matchLabels:
      app: flowable-ui
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
```

#### 1.2 Grafana 仪表盘
```json
{
  "dashboard": {
    "title": "Flowable UI System",
    "panels": [
      {
        "title": "应用状态",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"flowable-ui\"}",
            "legendFormat": "实例状态"
          }
        ]
      },
      {
        "title": "HTTP 请求率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{job=\"flowable-ui\"}[5m])",
            "legendFormat": "{{method}} {{uri}}"
          }
        ]
      },
      {
        "title": "响应时间",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job=\"flowable-ui\"}[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      },
      {
        "title": "JVM 内存使用",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{job=\"flowable-ui\"}",
            "legendFormat": "{{area}}"
          }
        ]
      }
    ]
  }
}
```

### 2. 日志管理

#### 2.1 ELK Stack 配置
```yaml
# k8s/logging/filebeat-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
  namespace: flowable-prod
data:
  filebeat.yml: |
    filebeat.inputs:
    - type: container
      paths:
        - /var/log/containers/flowable-ui-*.log
      processors:
      - add_kubernetes_metadata:
          host: ${NODE_NAME}
          matchers:
          - logs_path:
              logs_path: "/var/log/containers/"
    
    output.elasticsearch:
      hosts: ["elasticsearch:9200"]
      index: "flowable-ui-logs-%{+yyyy.MM.dd}"
    
    setup.template.name: "flowable-ui"
    setup.template.pattern: "flowable-ui-logs-*"
```

#### 2.2 日志聚合查询
```json
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "kubernetes.labels.app": "flowable-ui"
          }
        },
        {
          "range": {
            "@timestamp": {
              "gte": "now-1h"
            }
          }
        }
      ]
    }
  },
  "aggs": {
    "log_levels": {
      "terms": {
        "field": "level.keyword"
      }
    }
  }
}
```

## 发布管理

### 1. 发布策略

#### 1.1 蓝绿部署
```yaml
# k8s/production/blue-green-deployment.yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: flowable-ui-rollout
  namespace: flowable-prod
spec:
  replicas: 3
  strategy:
    blueGreen:
      activeService: flowable-ui-active
      previewService: flowable-ui-preview
      autoPromotionEnabled: false
      scaleDownDelaySeconds: 30
      prePromotionAnalysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: flowable-ui-preview
      postPromotionAnalysis:
        templates:
        - templateName: success-rate
        args:
        - name: service-name
          value: flowable-ui-active
  selector:
    matchLabels:
      app: flowable-ui
  template:
    metadata:
      labels:
        app: flowable-ui
    spec:
      containers:
      - name: flowable-ui
        image: ghcr.io/company/flowable-ui-system:latest
        ports:
        - containerPort: 8080
```

#### 1.2 金丝雀发布
```yaml
# k8s/production/canary-deployment.yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: flowable-ui-canary
  namespace: flowable-prod
spec:
  replicas: 5
  strategy:
    canary:
      steps:
      - setWeight: 20
      - pause: {duration: 10m}
      - setWeight: 40
      - pause: {duration: 10m}
      - setWeight: 60
      - pause: {duration: 10m}
      - setWeight: 80
      - pause: {duration: 10m}
      canaryService: flowable-ui-canary
      stableService: flowable-ui-stable
      trafficRouting:
        nginx:
          stableIngress: flowable-ui-stable
          annotationPrefix: nginx.ingress.kubernetes.io
          additionalIngressAnnotations:
            canary-by-header: X-Canary
```

### 2. 回滚策略

#### 2.1 自动回滚配置
```yaml
# .github/workflows/rollback.yml
name: Rollback

on:
  workflow_dispatch:
    inputs:
      environment:
        description: 'Environment to rollback'
        required: true
        default: 'staging'
        type: choice
        options:
        - staging
        - production
      version:
        description: 'Version to rollback to'
        required: true
        type: string

jobs:
  rollback:
    runs-on: ubuntu-latest
    environment: ${{ github.event.inputs.environment }}
    
    steps:
      - name: Rollback deployment
        run: |
          kubectl rollout undo deployment/flowable-ui \
            --namespace=flowable-${{ github.event.inputs.environment }} \
            --to-revision=${{ github.event.inputs.version }}
          
          kubectl rollout status deployment/flowable-ui \
            --namespace=flowable-${{ github.event.inputs.environment }} \
            --timeout=300s
```

## 灾难恢复

### 1. 备份恢复流程

#### 1.1 数据库恢复脚本
```bash
#!/bin/bash
# scripts/restore-database.sh

set -e

BACKUP_FILE=$1
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-flowable_ui_prod}
DB_USER=${DB_USER:-root}
DB_PASSWORD=${DB_PASSWORD}

if [ -z "$BACKUP_FILE" ]; then
    echo "用法: $0 <backup_file>"
    exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
    echo "错误: 备份文件不存在: $BACKUP_FILE"
    exit 1
fi

echo "开始恢复数据库: $DB_NAME"
echo "备份文件: $BACKUP_FILE"

# 创建数据库（如果不存在）
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} \
  -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME};"

# 恢复数据
if [[ $BACKUP_FILE == *.gz ]]; then
    gunzip -c $BACKUP_FILE | mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME}
else
    mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME} < $BACKUP_FILE
fi

echo "数据库恢复完成"
```

### 2. 应急响应计划

#### 2.1 故障分级
| 级别 | 描述 | 响应时间 | 恢复时间目标 |
|------|------|----------|--------------|
| P0 | 系统完全不可用 | 15分钟 | 1小时 |
| P1 | 核心功能不可用 | 30分钟 | 4小时 |
| P2 | 部分功能异常 | 2小时 | 24小时 |
| P3 | 性能问题 | 1天 | 3天 |

#### 2.2 应急联系人
```yaml
# 应急响应联系人
contacts:
  - role: "技术负责人"
    name: "张三"
    phone: "+86-138-0000-0000"
    email: "zhangsan@company.com"
  
  - role: "运维负责人"
    name: "李四"
    phone: "+86-139-0000-0000"
    email: "lisi@company.com"
  
  - role: "产品负责人"
    name: "王五"
    phone: "+86-137-0000-0000"
    email: "wangwu@company.com"
```

## 最佳实践

### 1. 部署检查清单

#### 1.1 部署前检查
- [ ] 代码审查通过
- [ ] 所有测试通过
- [ ] 安全扫描通过
- [ ] 性能测试通过
- [ ] 数据库迁移脚本验证
- [ ] 配置文件检查
- [ ] 依赖版本确认

#### 1.2 部署后验证
- [ ] 应用启动成功
- [ ] 健康检查通过
- [ ] 关键功能验证
- [ ] 性能指标正常
- [ ] 日志输出正常
- [ ] 监控告警配置
- [ ] 备份任务运行

### 2. 运维自动化

#### 2.1 自动扩缩容
```yaml
# k8s/production/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: flowable-ui-hpa
  namespace: flowable-prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: flowable-ui
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

### 3. 成本优化

#### 3.1 资源优化建议
- 使用多阶段构建减少镜像大小
- 合理设置资源请求和限制
- 使用节点亲和性优化调度
- 定期清理未使用的资源
- 监控资源使用情况并调整

#### 3.2 存储优化
- 使用适当的存储类
- 定期清理日志和临时文件
- 压缩备份文件
- 使用生命周期策略管理对象存储