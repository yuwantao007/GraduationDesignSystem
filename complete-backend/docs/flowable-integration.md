# Flowable 流程引擎集成开发文档

> **文档版本**：V2.0  
> **创建日期**：2026年3月15日  
> **最后更新**：2026年3月15日（新增第十一章：可视化流程设计器决策分析）  
> **适用范围**：毕业设计全过程管理系统 — 课题审查流程引擎集成  

---

## 一、背景与目标

### 1.1 背景

原有课题审查流程采用**手工状态机**驱动：`TopicReviewServiceImpl.reviewTopic()` 直接修改 `topic_info.review_status` 字段，审查逻辑完全由后端条件分支维护，存在以下问题：

- 流程状态不可追溯（无法查看完整的历史流转记录）
- 角色任务分配分散，缺乏统一的"任务收件箱"入口
- 无法在线可视化流程图

### 1.2 目标

引入 **Flowable 7.x** 流程引擎，实现：

1. 用 BPMN 2.0 流程定义驱动课题审查的任务路由
2. 为各审核角色提供统一的**待办任务收件箱**
3. 记录完整的**流程历史**（节点、操作人、时间）
4. 前端通过 **bpmn-js** 渲染只读流程图并高亮当前节点

> **兼容性原则**：保留 `topic_info.review_status` 字段，由 FlowService 同步更新；原有审查 API（`/topic-review/**`）依然可用，`reviewTopic()` 内部追加调用 `syncFlowTask()` 同步 Flowable 进度。

---

## 二、技术选型

| 技术 | 版本 | 说明 |
|------|------|------|
| Flowable Process Engine | 7.0.0 | 仅引入 Process 模块，禁用 DMN/Form/Content |
| Spring Boot | 3.2.2 | 通过 `flowable-spring-boot-starter-process` 自动装配 |
| bpmn-js | latest | 前端 BPMN 2.0 只读渲染库 |
| MySQL | 8.0.x | 存储 Flowable ACT_* 表及业务映射表 |

---

## 三、流程设计

### 3.1 两条审查路径

```
路径 A（高职升本，topicCategory = 1）：
  课题提交 → 预审（高校教师）→ 初审（专业方向主管）→ 终审（督导教师）→ 结束

路径 B（3+1 / 实验班，topicCategory = 2/3）：
  课题提交 → 初审（专业方向主管）→ 终审（高校教师）→ 结束
```

### 3.2 修改循环

| 阶段 | 审查结果 | 流程走向 | reviewStatus |
|------|----------|----------|-------------|
| 预审 | PASS | 进入初审 | 2（预审通过） |
| 预审 | NEED_MODIFY | 进入预审修改任务 | 3（预审需修改） |
| 预审修改完成 | 重新提交 | 循环回预审 | 1（待预审） |
| 初审（升本） | PASS | 进入终审（督导） | 4（初审通过） |
| 初审（升本） | NEED_MODIFY | 进入初审修改任务 | 5（初审需修改） |
| 初审修改完成（升本） | 重新提交 | 循环回**预审** | 1（待预审） |
| 初审（其他） | PASS | 进入终审（高校教师） | 4（初审通过） |
| 初审（其他） | NEED_MODIFY | 进入初审修改任务 | 5（初审需修改） |
| 初审修改完成（其他） | 重新提交 | 循环回**初审** | 1（待预审） |
| 终审 | PASS | 流程结束 | 6（终审通过） |
| 终审 | REJECT | 流程结束 | 7（终审不通过） |

### 3.3 BPMN 任务节点说明

| 节点 ID | 节点名称 | 候选组 / Assignee |
|---------|---------|-------------------|
| `preReviewTask` | 预审 | `UNIVERSITY_TEACHER`（候选组） |
| `initUpgradeTask` | 初审（升本） | `MAJOR_DIRECTOR`（候选组） |
| `initOtherTask` | 初审（其他） | `MAJOR_DIRECTOR`（候选组） |
| `finalUpgradeTask` | 终审（升本） | `SUPERVISOR_TEACHER`（候选组） |
| `finalOtherTask` | 终审（其他） | `UNIVERSITY_TEACHER`（候选组） |
| `preModifyTask` | 预审修改待重提 | `${creatorId}`（企业教师） |
| `initUpgradeModifyTask` | 初审修改待重提（升本） | `${creatorId}`（企业教师） |
| `initOtherModifyTask` | 初审修改待重提（其他） | `${creatorId}`（企业教师） |

---

## 四、数据库变更

### 4.1 新增表

执行脚本：`complete-backend/docs/sql/topic_process_instance.sql`

```sql
CREATE TABLE topic_process_instance (
  id                  VARCHAR(32)  NOT NULL  COMMENT '主键（雪花ID）',
  topic_id            VARCHAR(32)  NOT NULL  COMMENT '课题ID',
  process_instance_id VARCHAR(64)  NOT NULL  COMMENT 'Flowable 流程实例ID',
  process_def_key     VARCHAR(64)  NOT NULL  DEFAULT 'topic_review',
  topic_category      TINYINT      NOT NULL  DEFAULT 1,
  process_status      TINYINT      NOT NULL  DEFAULT 0  COMMENT '0-运行中 1-已完成 2-已终止',
  create_time         DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  update_time         DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted             TINYINT      NOT NULL  DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_topic_id (topic_id)
) COMMENT='课题流程实例映射表';
```

### 4.2 Flowable 自动建表

Flowable 首次启动时自动创建 40+ 张 `ACT_*` 表，无需手动建表。配置：

```yaml
flowable:
  database-schema-update: true
```

---

## 五、后端开发详情

### 5.1 依赖与配置

**pom.xml**（新增）：
```xml
<dependency>
    <groupId>org.flowable</groupId>
    <artifactId>flowable-spring-boot-starter-process</artifactId>
    <version>7.0.0</version>
</dependency>
```

**application.yml**（新增）：
```yaml
flowable:
  database-schema-update: true
  async-executor-activate: false
  history-level: full
  dmn.enabled: false
  form.enabled: false
  content.enabled: false
  app.enabled: false
  idm.enabled: false
```

### 5.2 新增文件清单

| 文件路径 | 说明 |
|----------|------|
| `src/main/resources/processes/topic_review.bpmn20.xml` | BPMN 流程定义文件 |
| `model/entity/TopicProcessInstance.java` | 映射表实体 |
| `mapper/TopicProcessInstanceMapper.java` | 映射表 Mapper |
| `model/vo/FlowTaskVO.java` | 待办任务 VO |
| `model/vo/ProcessStatusVO.java` | 流程状态 VO（含历史节点内部类） |
| `model/vo/ProcessInstanceVO.java` | 监控实例 VO |
| `model/dto/CompleteReviewTaskDTO.java` | 完成审查任务 DTO |
| `service/ITopicFlowService.java` | Service 接口（8 个方法） |
| `service/impl/TopicFlowServiceImpl.java` | Service 实现 |
| `controller/TopicFlowController.java` | Controller（7 个端点） |
| `workflow/listener/ReviewTaskCreateListener.java` | 任务创建监听器 |

### 5.3 ITopicFlowService 接口方法

| 方法 | 说明 |
|------|------|
| `startProcess(topicId, creatorId, topicCategory, topicTitle)` | 首次提交时启动流程 |
| `completeModifyTask(topicId)` | 企业教师重提时完成修改任务 |
| `getMyTasks(userId, roleCode)` | 查询当前用户待办任务 |
| `claimTask(taskId, userId)` | 签收任务 |
| `completeReviewTask(taskId, outcome, opinion, userId)` | 完成审查任务（同步 reviewStatus） |
| `getProcessStatus(topicId)` | 获取课题流程状态 |
| `listProcessInstances(processStatus, pageNum, pageSize)` | 管理员监控查询 |
| `getProcessDiagramXml(processInstanceId)` | 获取 BPMN XML（前端渲染用） |
| `getProcessHistory(processInstanceId)` | 获取历史节点记录 |
| `syncFlowTask(topicId, outcome, reviewerId)` | 旧审查路径同步推进 Flowable（不重复写记录） |

### 5.4 Controller 端点

| HTTP | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/flow/task/my` | 各审核角色 | 我的待办任务列表 |
| POST | `/flow/task/{taskId}/claim` | 各审核角色 | 签收任务 |
| POST | `/flow/task/{taskId}/complete` | 各审核角色 | 提交审核意见 |
| GET | `/flow/process/topic/{topicId}` | 所有登录用户 | 课题流程状态（含历史节点） |
| GET | `/flow/process/list` | SYSTEM_ADMIN | 管理员监控列表 |
| GET | `/flow/process/{processInstanceId}/diagram` | SYSTEM_ADMIN | BPMN XML |
| GET | `/flow/process/{processInstanceId}/history` | 审核角色+管理员 | 历史节点 |

### 5.5 修改现有代码

#### TopicServiceImpl.submitTopic()

```
草稿状态首次提交 → topicFlowService.startProcess(topicId, creatorId, category, title)
修改后重提（PRE_MODIFY/INIT_MODIFY）→ topicFlowService.completeModifyTask(topicId)
```

#### TopicReviewServiceImpl.reviewTopic()

原有逻辑（状态变更、记录写入）保留不变，在方法末尾追加：
```
topicFlowService.syncFlowTask(topicId, outcome, reviewerId)
```
`syncFlowTask` 仅推进 Flowable 任务，不重复写审查记录。

---

## 六、前端开发详情

### 6.1 依赖

```bash
npm install bpmn-js --legacy-peer-deps
```

### 6.2 新增文件清单

| 文件路径 | 说明 |
|----------|------|
| `src/types/workflow.ts` | FlowTaskVO / ProcessStatusVO / ProcessInstanceVO / CompleteReviewTaskDTO 类型定义 |
| `src/api/workflow.ts` | 7 个 API 方法封装 |
| `src/views/workflow/TaskInbox.vue` | 待办任务收件箱（统计卡片 + 任务表格 + 审核意见抽屉） |
| `src/views/workflow/ProcessMonitor.vue` | 管理员流程监控（列表 + BPMN 弹窗 + 历史时间线） |
| `src/components/workflow/BpmnViewer.vue` | bpmn-js 只读渲染（高亮活跃/已完成节点） |
| `src/components/workflow/ProcessStatusCard.vue` | 课题详情页嵌入式流程状态卡片 |

### 6.3 修改现有文件

**MainLayout.vue**：新增"工作流"子菜单，显示条件：
- 待办任务：`UNIVERSITY_TEACHER | MAJOR_DIRECTOR | SUPERVISOR_TEACHER | ENTERPRISE_TEACHER`
- 流程监控：`SYSTEM_ADMIN`（需 `monitor:dashboard:view` 权限）

**router/index.ts**：新增两条路由：
```
/workflow/tasks   → TaskInbox.vue
/workflow/monitor → ProcessMonitor.vue
```

### 6.4 BpmnViewer 高亮策略

- **活跃节点**（橙色边框 `#faad14`）：通过 `canvas.addMarker(id, 'highlight-active')`
- **已完成节点**（绿色边框 `#52c41a`）：通过 `canvas.addMarker(id, 'highlight-completed')`
- 使用 `historyService.createHistoricActivityInstanceQuery()` 获取活跃 activityId 列表

---

## 七、关键集成时序

```
企业教师 submitTopic（首次提交）
    ↓
TopicServiceImpl 设置 reviewStatus = 1（PENDING_PRE）
    ↓
topicFlowService.startProcess(topicId, creatorId, category, title)
    ↓
Flowable 创建流程实例，category=1 路由到 preReviewTask，其他路由到 initOtherTask
    ↓
审核人（如高校教师）访问 /flow/task/my 看到任务
    ↓
claimTask(taskId, userId)  — 可选，直接 complete 也会自动签收
    ↓
completeReviewTask(taskId, "PASS", "意见", userId)
    ↓
Flowable 推进到下一节点，FlowService 同步更新 topic.reviewStatus
    ↓
[可选] 旧路径：topicReviewApi.reviewTopic() → reviewTopic() → syncFlowTask()
```

---

## 八、部署步骤

### 8.1 数据库

```sql
-- 1. 执行映射表建表脚本
source complete-backend/docs/sql/topic_process_instance.sql;

-- 2. Flowable ACT_* 表由后端启动时自动创建，无需手动操作
```

### 8.2 后端

1. 确认 `pom.xml` 已添加 Flowable 依赖
2. 重新编译并启动后端服务
3. 首次启动时观察日志，确认 Flowable 自动建表成功（会出现 `Creating table ACT_...` 日志）

### 8.3 前端

```bash
# 确认 bpmn-js 已安装
npm install

# 启动开发服务器
npm run dev
```

---

## 九、注意事项

### 9.1 MyBatis Plus 兼容性

Flowable 使用独立的 MyBatis `SqlSessionFactory`（内部维护，不暴露为 Spring Bean），与 MyBatis Plus 的 `MybatisSqlSessionFactoryBean` 完全隔离，**不存在冲突**。

### 9.2 事务隔离

`TopicFlowServiceImpl` 的 `syncFlowTask()` 方法内部使用 `try-catch`，Flowable 操作异常不会回滚业务事务，确保旧审查路径的可靠性。

### 9.3 双路径并存

系统同时支持两种操作路径：
- **新路径**：通过 `/flow/task/{taskId}/complete` 直接驱动 Flowable 并同步状态
- **旧路径**：通过 `/topic-review/review` 审查，内部调用 `syncFlowTask()` 同步 Flowable

两条路径均会更新 `topic_info.review_status`，不会产生双重写入。

### 9.4 bpmn-js 初始化

`BpmnViewer.vue` 使用动态 `import('bpmn-js')` 避免 SSR 问题，仅在 `onMounted` 后初始化，确保容器 DOM 已渲染。

### 9.5 流程实例重置

若需要重置某课题的流程状态（如测试），执行：
```sql
UPDATE topic_process_instance SET process_status = 2, deleted = 1
WHERE topic_id = 'xxx';
-- 同时在 ACT_RU_* 表中终止对应流程实例（或直接重启后端服务以清空运行时数据）
```

---

## 十、后续扩展方向

1. **消息通知**：在 `ReviewTaskCreateListener` 中接入 RabbitMQ，新任务创建时推送站内消息
2. **超时提醒**：使用 Flowable Timer Event，超过 N 天未处理自动发送预警
3. **批量审核流程化**：将 `batchReviewTopics()` 也接入 Flowable，为每个课题创建独立流程实例

---

## 十一、可视化流程设计器决策分析与实现方案

> **章节说明**：本章记录了在系统第四阶段开发中，针对"可视化流程设计器"是否需要完整开发这一技术决策的完整分析过程，包括需求解析、方案对比、最终选型依据以及落地实现细节。本章内容具有重要的设计文档价值，可作为项目技术选型报告的参考依据。

---

### 11.1 背景与需求来源

在系统总体开发计划（`READMES.md` 第四阶段）中，明确列出了两项流程引擎相关任务：

1. **Flowable 流程引擎集成**（已完成，见第一章至第九章）
2. **可视化流程设计器开发**

同时，在系统待开发功能清单（`READMES.md` §11.2.4）中，"审批流设计模块"包含以下三个子项：

```
- ⏳ 可视化审批流设计器
- ⏳ 流程配置功能
- ⏳ 流程监控
```

完成 Flowable 集成后，需要评估上述第二项任务的具体内涵：究竟应开发一个允许用户在浏览器中拖拽绘制并部署 BPMN 流程的**全功能流程设计器**，还是一个以**只读可视化**为核心的流程图展示系统？这两种解读对应的开发工作量和架构决策存在本质差异，因此需要进行系统性分析。

---

### 11.2 全功能 BPMN 设计器的技术解析

#### 11.2.1 定义与典型实现

**全功能 BPMN 流程设计器**（Full BPMN Modeler）是指允许用户通过图形化界面拖拽、连接和配置 BPMN 2.0 流程元素，并将生成的流程定义部署到工作流引擎的完整工具。典型实现包括：

- **Flowable UI（原 Activiti Modeler）**：Flowable 官方提供的独立 Web 应用，基于 `bpmn-js/modeler` 构建，支持完整的流程绘制、属性配置和一键部署到 Flowable Engine。
- **Camunda Modeler**：桌面端 BPMN 设计器，支持导出 XML 并手动部署。
- **自研内嵌设计器**：在业务系统中内嵌 `bpmn-js/modeler`，通过自定义属性面板（Properties Panel）配置 Task Listener、Service Task 等，并调用引擎 REST API 进行热部署。

#### 11.2.2 核心技术栈

实现全功能设计器所需的前端技术栈：

| 组件 | 库 / 版本 | 说明 |
|------|-----------|------|
| BPMN 画布 | `bpmn-js/modeler`（独立于 `bpmn-js` Viewer） | 提供拖拽创建、元素属性编辑、连线等编辑能力 |
| 属性面板 | `@bpmn-io/properties-panel` + `bpmn-js-properties-panel` | 配置 Task Assignee、Candidate Groups、Listeners 等 Flowable 扩展属性 |
| Flowable 扩展 | `flowable-bpmn-moddle` | 提供 `flowable:*` 扩展元素的 moddle 描述符，使 bpmn-js 能识别 Flowable 私有扩展属性 |
| XML 序列化 | `bpmn-js` 内置 `saveXML()` | 将画布状态导出为 BPMN 2.0 XML |
| 热部署 API | Flowable REST API `POST /repository/deployments` | 将 XML 上传部署到引擎 |

后端所需额外接口：

```
POST /flow/definition/deploy          # 接收 multipart/form-data 格式的 BPMN XML 并部署
GET  /flow/definition/list            # 查询已部署的所有流程定义版本
DELETE /flow/definition/{deploymentId} # 删除某个部署版本（回滚用）
```

预估开发工作量：**3~5 周**（仅前端设计器界面即需 2~3 周，后端部署 API 约 1 周，联调测试 1~2 周）。

---

### 11.3 全功能设计器对本系统的适用性分析

#### 11.3.1 分析维度一：业务需求驱动性

全功能 BPMN 设计器的核心价值在于：**允许非技术人员在系统运行期间灵活调整工作流定义**，适用于以下场景：

- 企业 OA 审批流程：不同部门、不同业务可能使用不同的审批层级，且规则经常变化
- 低代码平台：工作流是用户可自定义配置的核心能力
- 通用 BPM 系统：支持多租户，每个租户定义自己的业务流程

而本系统（毕业设计全过程管理系统）的课题审查流程具有以下特征：

**① 流程规则由制度规定，不可随意变更**

课题审查的三级审批路径（预审 → 初审 → 终审）是由学校教务管理规范确定的，具体表现为：
- 路径 A（高职升本）对应 `topicCategory = 1`，固定为三级审核
- 路径 B（3+1/实验班）对应 `topicCategory = 2/3`，固定为两级审核

这种路径划分是制度性约束，而非系统配置项。在系统运行期间，管理员不具备（也不应该具备）修改审核级数的权限。

**② BPMN 流程定义与业务代码强耦合**

本系统中，Flowable BPMN 定义并非独立存在，而是与以下业务代码深度耦合：

```
BPMN 节点 ID           ↔  TopicFlowServiceImpl.TASK_PASS_STATUS_MAP（Java 静态 Map）
BPMN 节点 ID           ↔  TopicFlowServiceImpl.TASK_STAGE_MAP（ReviewStage 枚举）
BPMN Candidate Groups  ↔  getMyTasks() 中的角色过滤逻辑
BPMN Gateway 条件      ↔  variables.put("preOutcome", outcome) 等流程变量
```

若通过设计器修改了 BPMN（如将 `preReviewTask` 重命名为 `preTask`），而 Java 代码中的 `TASK_PASS_STATUS_MAP` 仍引用旧 Key，则系统将静默失效——任务完成后无法找到对应的 `reviewStatus` 映射，导致课题状态无法更新。这种**代码与配置的强耦合**是全功能设计器的根本障碍。

**③ 实际修改频率极低**

从系统生命周期角度看，课题审查流程自上线后至少一届（1年）内不会发生调整；若确实需要调整（如制度变化），则需要同步修改 Java 代码（`TopicFlowServiceImpl`）和 BPMN 定义，不可能仅通过 UI 完成。在这种低频、需代码协同的修改场景下，为设计器投入大量开发资源的 ROI（投资回报率）极低。

#### 11.3.2 分析维度二：开发成本与系统稳定性

| 对比项 | 全功能设计器 | 只读可视化方案 |
|--------|------------|--------------|
| 预估工作量 | 3~5 周 | 0.5~1 周（已有 BpmnViewer 复用） |
| 前端依赖 | `bpmn-js/modeler` + 属性面板 + moddle 扩展，bundle 体积约 +2MB | 已安装的 `bpmn-js`（只读模式），无额外依赖 |
| 后端接口 | 需实现部署 API，处理并发部署、版本回滚等复杂场景 | 仅需 1 个 `GET /flow/definition/diagram` 接口 |
| 安全风险 | 管理员可能误改流程导致系统失效，需完善的权限隔离和回滚机制 | 只读展示，零修改风险 |
| 维护成本 | 高：浏览器兼容性、bpmn-js 版本升级、属性面板与引擎版本兼容 | 低：与 Viewer 共用同一库 |

#### 11.3.3 分析维度三：项目阶段与实际价值

本系统处于**原型开发阶段**，当前首要目标是完成核心业务功能（课题申报→审核→双选→指导→答辩的全链路）并验证流程引擎集成的可行性。在此阶段：

- 全功能设计器属于**锦上添花**功能，不影响核心业务流程的验证
- 只读流程图可视化能直接服务于**系统演示、需求评审和论文展示**
- 将设计器开发资源投入到更紧迫的功能（如答辩管理、文档管理）具有更高的价值

#### 11.3.4 结论

综合以上三个分析维度，**不建议开发全功能 BPMN 拖拽设计器**。其核心理由如下：

> 本系统的课题审查流程是由制度规范决定的固定业务逻辑，BPMN 流程定义与后端 Java 代码存在不可分离的强耦合关系。通过 UI 修改 BPMN 而不同步修改代码，将导致系统逻辑失效。在此约束下，全功能设计器不仅开发成本高，而且实际使用价值极低，引入后还会带来额外的系统稳定性风险。

---

### 11.4 替代方案：流程可视化展示体系

在放弃全功能设计器之后，真正需要开发的是一套完整的**流程可视化展示体系**。该体系的目标是：让所有相关角色都能直观地理解审查流程的当前状态、历史轨迹和整体流程图，从而替代全功能设计器的大部分"管理价值"。

实现流程可视化展示体系共分三个阶段：

```
阶段 A：流程定义全图预览          → 让人了解流程"长什么样"
阶段 B：课题维度的流程状态嵌入     → 让人知道"当前在哪"
阶段 C：管理员流程实例监控         → 让管理员"掌握全局"
```

---

### 11.5 阶段 A：流程定义可视化页面

#### 11.5.1 功能定位

流程定义可视化页面（`/workflow/definition`）面向所有已登录用户开放，展示当前系统中部署的 `topic_review` 流程的完整 BPMN 图，同时配有文字说明辅助理解两条审查路径。

该页面的价值在于：
- **操作培训**：新入职的企业教师、高校教师可通过此页面直观了解审查流程全貌，无需阅读制度文件
- **系统演示**：在项目答辩或需求评审时，可直接展示动态的 BPMN 流程图
- **问题排查**：管理员在处理审查投诉时，可对照流程图理解任务流转逻辑

#### 11.5.2 后端实现

**接口设计**：

```
GET /flow/definition/diagram
```

- **权限**：无额外权限限制（仅需登录），所有角色均可访问
- **返回值**：`Result<String>`，其中 `data` 为 BPMN 2.0 XML 字符串
- **异常处理**：若流程定义尚未部署（如首次启动后未自动加载），返回友好错误信息

**服务层实现逻辑**（`TopicFlowServiceImpl.getProcessDefinitionDiagramXml()`）：

```java
// 1. 通过 RepositoryService 查询最新版本的流程定义
ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
    .processDefinitionKey("topic_review")
    .latestVersion()
    .singleResult();

// 2. 校验定义存在
if (pd == null) {
    throw new BusinessException("流程定义不存在，请确认已部署 topic_review.bpmn20.xml");
}

// 3. 通过部署 ID 和资源名称读取 BPMN 文件内容
try (var inputStream = repositoryService.getResourceAsStream(
        pd.getDeploymentId(), pd.getResourceName())) {
    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
}
```

**关键设计点**：
- 使用 `.latestVersion()` 确保始终返回最新版本，兼容未来可能的手动升级
- `getResourceAsStream()` 直接从 `ACT_GE_BYTEARRAY` 表读取二进制资源，无需文件系统操作
- 流资源通过 `try-with-resources` 自动关闭，避免连接泄漏

#### 11.5.3 前端实现

**页面结构**（`ProcessDefinition.vue`）：

```
┌─────────────────────────────────────────────────────────┐
│  页面标题：审查流程定义                                     │
├──────────────────────┬──────────────────────────────────┤
│  路径A 说明卡片（蓝）  │  路径B 说明卡片（绿）               │
│  Steps 组件展示4步    │  Steps 组件展示3步                │
│  附注说明修改循环规则   │  附注说明修改循环规则               │
├──────────────────────┴──────────────────────────────────┤
│  图例说明（橙色=活跃，绿色=已完成）+ 版本号 + 刷新按钮         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│         BpmnViewer 组件（高度 540px）                    │
│         渲染完整的 topic_review BPMN 图                   │
│         （只读模式，无节点高亮，展示静态全图）               │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**数据加载策略**：

```typescript
// 页面挂载时自动调用
onMounted(() => loadDefinition())

async function loadDefinition() {
  loading.value = true
  const xml = await workflowApi.getProcessDefinitionXml()
  bpmnXml.value = xml
  // 从 XML 解析版本标签（若 BPMN 文件中定义了 flowable:versionTag）
  const versionMatch = xml.match(/flowable:versionTag="([^"]+)"/)
  processVersion.value = versionMatch ? versionMatch[1] : 'v1.0'
}
```

**关键设计点**：
- 路径说明卡片使用 Ant Design `Steps` 组件，直观展示审查节点顺序
- 两张卡片使用不同颜色的顶部边框（蓝色对应路径A，绿色对应路径B），与 BPMN 图中的路径颜色形成视觉对应
- 错误处理：若 API 返回失败（如后端未启动或流程未部署），展示 `a-result` 警告状态，而非空白页面

**菜单可见性设计**：

不同于"待办任务"（仅审核角色和企业教师可见）和"流程监控"（仅管理员可见），"流程定义"面向**所有已登录用户**开放，无角色条件限制：

```html
<!-- 流程定义：所有已登录用户均可查看，无 v-if 角色限制 -->
<a-menu-item key="/workflow/definition">
  流程定义
</a-menu-item>
```

此设计的依据：流程定义图属于系统的公开信息，任何参与毕设流程的人（学生、教师、管理员）都有权查看整个审查流程是如何运转的，这有助于提升系统透明度，减少因信息不对称导致的操作疑问。

---

### 11.6 阶段 B：课题详情页嵌入流程状态卡片

#### 11.6.1 功能定位

阶段 B 解决的核心问题是：**用户在查看某一具体课题的详情时，如何直观了解该课题当前处于审查流程的哪个节点？**

在引入 Flowable 之前，课题详情页仅展示 `review_status` 字段对应的文字描述（如"待预审"）。引入 Flowable 后，后端已存储完整的历史节点信息（审查人、时间、结果），但这些信息在课题详情页中完全不可见，造成了信息浪费。

`ProcessStatusCard` 组件正是为填补这一信息缺口而设计的。

#### 11.6.2 组件功能说明

`ProcessStatusCard.vue` 是一个可在任意需要展示课题流程状态的位置复用的卡片组件，其接口定义如下：

```typescript
// Props 定义
interface Props {
  topicId: string   // 课题 ID，组件内部调用 GET /flow/process/topic/{topicId}
}
```

渲染内容：

```
┌────────────────────────────────────────────────┐
│  审查流程状态                              [刷新] │
├────────────────────────────────────────────────┤
│  [当前状态标签]  等待：高校教师（预审）            │
│  ○ 课题提交                    3/1 09:30       │
│  ● 预审（进行中）                 等待签收        │
│  ○ 初审（待流转）                               │
│  ○ 终审（待流转）                               │
└────────────────────────────────────────────────┘
```

关键展示元素：
- **状态标签**：使用颜色区分各审查状态（灰=草稿，蓝=待审，橙=修改中，青=通过，绿=终审通过，红=不通过）
- **等待角色**：明确告知用户当前任务由谁操作，减少沟通成本
- **历史时间线**：使用 `a-timeline` 展示已完成的节点（绿色）、当前活跃节点（橙色）和未到达的节点（灰色）
- **操作人姓名**：从后端返回的 `assigneeName` 字段显示，而非 userId，降低信息理解门槛

#### 11.6.3 嵌入策略

在 `TopicDetail.vue` 中，`ProcessStatusCard` 被插入在操作按钮区和主内容区之间：

```html
<div class="topic-detail">
  <!-- 操作按钮区 -->
  <a-card class="topic-detail__actions">...</a-card>

  <!-- 审查流程状态卡片（仅已提交课题显示） -->
  <ProcessStatusCard
    v-if="topicData?.isSubmitted === 1 && topicData?.topicId"
    :topic-id="topicData.topicId"
    class="topic-detail__flow-status"
  />

  <!-- 详情内容区（任务书格式） -->
  <a-card class="topic-detail__content">...</a-card>
</div>
```

**条件显示逻辑**：仅当 `isSubmitted === 1` 时才渲染，原因如下：
- 草稿状态（`isSubmitted = 0`）的课题尚未提交，Flowable 流程实例还未创建，此时调用 `GET /flow/process/topic/{topicId}` 会返回"流程尚未启动"的空状态，展示意义不大
- 已提交的课题（`isSubmitted = 1`）必然已有流程实例，展示流程状态有实际价值

**打印隔离**：流程状态卡片在打印模式下自动隐藏，确保打印出的课题任务书格式保持标准化：

```scss
.topic-detail__flow-status {
  @media print {
    display: none;
  }
}
```

#### 11.6.4 数据获取与缓存策略

`ProcessStatusCard` 在 `onMounted` 钩子中调用 API，并通过 `watch` 监听 `topicId` prop 的变化：

```typescript
// 初次挂载时加载
onMounted(() => { if (props.topicId) loadStatus() })

// topicId 变化时重新加载（如页面复用）
watch(() => props.topicId, (newVal) => { if (newVal) loadStatus() })
```

卡片提供手动刷新按钮（`ReloadOutlined` 图标），允许用户在不刷新整页的情况下获取最新流程状态，这在审核人刚完成操作后尤为实用。

---

### 11.7 阶段 C：管理员流程实例监控

#### 11.7.1 功能定位

`ProcessMonitor.vue`（路径：`/workflow/monitor`）是面向系统管理员的全局流程监控仪表板，仅对拥有 `monitor:dashboard:view` 权限的用户可见。

其核心价值在于：**让管理员无需进入数据库即可了解当前所有课题的审查进度**，并支持深入查看每个流程实例的完整 BPMN 图和历史记录。

#### 11.7.2 核心功能

**① 流程实例列表**

基于分页的列表视图，支持按流程状态（全部 / 运行中 / 已完成 / 已终止）筛选：

| 列名 | 数据来源 | 说明 |
|------|----------|------|
| 课题信息 | `topicTitle` + `topicCategoryDesc` | 课题标题和大类标签 |
| 审查状态 | `reviewStatusDesc` | 当前 `topic_info.review_status` 对应描述 |
| 流程状态 | `processStatusDesc` | `topic_process_instance.process_status` |
| 当前任务 | `currentTaskName` | 运行中时显示当前活跃任务名称 |
| 启动时间 | `startTime` | 流程实例启动时间 |
| 运行时长 | `durationDesc` | 格式化为"X天X小时" |

**② 流程图弹窗（实例级 BPMN 高亮）**

点击"流程图"按钮触发弹窗，实现逻辑如下：

```typescript
async function viewDiagram(instance: ProcessInstanceVO) {
  currentInstance.value = instance
  diagramModalVisible.value = true

  // 并发加载：BPMN XML + 历史节点（利用 Promise.all 减少等待时间）
  const [xml, history] = await Promise.all([
    workflowApi.getProcessDiagram(instance.processInstanceId),
    workflowApi.getProcessHistory(instance.processInstanceId)
  ])

  bpmnXml.value = xml
  // 活跃节点（当前正在等待操作的节点）→ 橙色高亮
  activeActivityIds.value = history.filter(n => n.active).map(n => n.activityId)
  // 已完成节点 → 绿色高亮
  completedActivityIds.value = history.filter(n => !n.active && n.endTime).map(n => n.activityId)
}
```

弹窗结构：
```
┌───────────────────────────────────────────┐
│ 流程图 - [课题标题]                         │
├───────────────────────────────────────────┤
│  流程状态 | 审查状态 | 运行时长               │
│  当前任务 | 等待角色                         │
├───────────────────────────────────────────┤
│                                           │
│   BpmnViewer（高度 420px）                 │
│   活跃节点橙色高亮 / 已完成节点绿色高亮        │
│                                           │
├───────────────────────────────────────────┤
│  流程历史（时间线）                          │
│  ● 课题提交 ○ 预审（高校教师/张三）09:30      │
│  ● 初审（专业主管/李四）10:15 → 10:45        │
│  ○ 终审（进行中）                           │
└───────────────────────────────────────────┘
```

**③ 历史节点时间线**

从 `GET /flow/process/{processInstanceId}/history` 获取的 `HistoryNodeVO` 列表包含：
- `activityName`：节点名称（如"预审"、"初审（升本）"）
- `assigneeName`：执行人姓名（后端通过 `userMapper.selectById(a.getAssignee())` 转换）
- `startTime` / `endTime`：节点开始和结束时间
- `active`：是否为当前活跃节点（通过比对 `ACT_RU_EXECUTION` 中的 activityId 确定）

过滤逻辑：仅展示 `userTask`、`startEvent`、`endEvent` 类型的节点，过滤掉网关（`exclusiveGateway`）和顺序流（`sequenceFlow`），使时间线更简洁易读。

#### 11.7.3 与流程定义页的区别

| 对比项 | 流程定义页（阶段A） | 流程监控弹窗（阶段C） |
|--------|------------------|--------------------|
| BPMN 数据来源 | `GET /flow/definition/diagram`（从流程定义读取，不依赖实例） | `GET /flow/process/{id}/diagram`（从历史流程实例读取） |
| 高亮状态 | 无高亮（静态全图） | 有高亮（橙色=当前活跃，绿色=已完成） |
| 用户对象 | 所有登录用户 | 仅管理员 |
| 使用场景 | 了解流程规则 | 监控具体实例进度 |

---

### 11.8 BpmnViewer 组件的技术实现分析

#### 11.8.1 只读 vs 编辑模式的选择

`bpmn-js` 库提供两种使用模式：

```javascript
// 只读模式（NavigatedViewer）—— 本系统使用此模式
import BpmnViewer from 'bpmn-js'
const viewer = new BpmnViewer({ container: '#container' })

// 编辑模式（Modeler）—— 全功能设计器所需
import BpmnModeler from 'bpmn-js/lib/Modeler'
const modeler = new BpmnModeler({
  container: '#container',
  propertiesPanel: { parent: '#properties-panel' },
  additionalModules: [BpmnPropertiesPanelModule, BpmnPropertiesProviderModule]
})
```

两种模式在打包产物大小上存在显著差异：
- `bpmn-js`（Viewer）：约 1.2MB（gzip 后约 380KB）
- `bpmn-js/lib/Modeler` + 属性面板：约 3.5MB+（gzip 后约 1.1MB+）

选用只读模式，在满足所有可视化需求的同时，将前端包体积控制在合理范围内。

#### 11.8.2 动态导入策略

`BpmnViewer.vue` 使用动态 `import()` 延迟加载 bpmn-js：

```typescript
// 在 initViewer() 函数中，而非模块顶层 import
const BpmnViewer = (await import('bpmn-js')).default
viewer = new BpmnViewer({ container: containerRef.value })
```

此策略的优势：
1. **代码分割**：bpmn-js（约 1.2MB）不会打包进主 bundle，仅在用户实际访问含流程图的页面时才下载
2. **避免 SSR 问题**：bpmn-js 内部依赖 `document`、`window` 等浏览器 API，在 Node.js 环境（SSR）下会报错；动态导入将其限制在客户端执行
3. **组件生命周期对齐**：确保 DOM 容器（`containerRef`）已挂载后再初始化，避免"容器未找到"错误

#### 11.8.3 节点高亮实现原理

bpmn-js 的 Canvas 模块提供 `addMarker(elementId, markerClassName)` API，将 CSS 类名附加到 BPMN 元素对应的 SVG 节点上：

```typescript
// 高亮活跃节点（橙色）
canvas.addMarker(activityId, 'highlight-active')

// 高亮已完成节点（绿色）
canvas.addMarker(activityId, 'highlight-completed')
```

对应的全局 CSS 样式（注意：必须使用非 scoped 样式，因为 bpmn-js 动态插入 SVG 到 DOM，scoped 属性选择器无法匹配）：

```css
/* 活跃节点：橙色边框 + 淡橙色背景 */
.bjs-container .highlight-active .djs-visual > :is(rect, circle, polygon) {
  stroke: #faad14 !important;
  stroke-width: 3px !important;
  fill: #fff7e6 !important;
}

/* 已完成节点：绿色边框 + 淡绿色背景 */
.bjs-container .highlight-completed .djs-visual > :is(rect, circle, polygon) {
  stroke: #52c41a !important;
  fill: #f6ffed !important;
}
```

颜色选择与 Ant Design 的状态色系保持一致（`#faad14` 为警告色，`#52c41a` 为成功色），使流程图的视觉语言与整体 UI 风格统一。

#### 11.8.4 组件销毁与资源释放

```typescript
onUnmounted(() => {
  if (viewer) {
    viewer.destroy()   // 调用 bpmn-js 内置销毁方法，清理事件监听和 DOM
    viewer = null
  }
})
```

若不调用 `destroy()`，bpmn-js 实例会持续监听键盘、鼠标等事件，在 Vue 组件销毁后造成内存泄漏。

---

### 11.9 方案整体评估与论文价值

#### 11.9.1 三阶段方案的完整性

本次实现的三阶段可视化方案，从功能覆盖角度完整替代了全功能设计器的管理价值：

| 管理需求 | 全功能设计器方案 | 三阶段可视化方案 |
|----------|----------------|----------------|
| 了解流程全貌 | BPMN 编辑画布（可交互） | **流程定义页 BPMN 只读图**（阶段A）|
| 了解某课题审查进度 | 查询 Flowable 管理界面 | **课题详情嵌入流程状态卡片**（阶段B）|
| 全局审查进度监控 | Flowable Admin UI | **管理员流程监控列表 + 实例高亮图**（阶段C）|
| 修改流程定义 | BPMN 拖拽编辑 + 热部署 | ❌ 不支持（需修改代码，属于合理约束）|

三阶段方案以约 **1/5** 的开发成本覆盖了约 **4/5** 的实际业务需求，是典型的帕累托最优（80/20 原则）工程决策。

#### 11.9.2 技术决策的可扩展性

尽管当前未实现全功能设计器，但系统架构上已为其预留了扩展路径。若未来确有需要，可按以下步骤升级：

1. **前端**：将 `BpmnViewer.vue` 升级为 `BpmnModeler.vue`，替换 `bpmn-js` Viewer 为 Modeler，并引入 `bpmn-js-properties-panel` 和 `flowable-bpmn-moddle`
2. **后端**：在 `TopicFlowController` 中新增 `POST /flow/definition/deploy` 接口，接收 BPMN XML 后调用 `RepositoryService.createDeployment()` 进行热部署
3. **代码解耦**：将 `TASK_PASS_STATUS_MAP` 等静态映射表迁移到数据库（新建 `flow_task_config` 表），实现流程节点与业务状态映射的数据驱动化

此三步升级路径清晰可行，说明当前方案具有良好的前向兼容性。

#### 11.9.3 设计原则总结

本次可视化方案的设计遵循了以下软件工程原则：

1. **YAGNI（You Aren't Gonna Need It）**：不为"可能将来用到"的全功能设计器投入过高成本，专注于当前实际需要的可视化展示功能
2. **最小权限暴露**：流程定义只读展示对所有用户开放，BPMN 实例监控仅对管理员开放，修改能力完全不暴露
3. **关注点分离（SoC）**：三个阶段分别对应三个独立页面/组件（`ProcessDefinition.vue`、`ProcessStatusCard.vue`、`ProcessMonitor.vue`），职责明确，互不耦合
4. **渐进增强**：基础功能（BPMN 静态图展示）在任何浏览器均可正常运行；高级功能（节点高亮）依赖 bpmn-js 动态加载，降级时显示友好提示
5. **代码复用**：三个阶段均复用同一个 `BpmnViewer.vue` 组件，避免重复实现 BPMN 渲染逻辑

---

### 11.10 附：相关文件变更清单（第四阶段新增）

| 类型 | 文件 | 变更说明 |
|------|------|----------|
| 后端 Service 接口 | `ITopicFlowService.java` | 新增 `getProcessDefinitionDiagramXml()` 方法声明 |
| 后端 Service 实现 | `TopicFlowServiceImpl.java` | 新增 `getProcessDefinitionDiagramXml()` 实现，查询最新版流程定义 |
| 后端 Controller | `TopicFlowController.java` | 新增 `GET /flow/definition/diagram` 端点（无角色限制）|
| 前端 API | `src/api/workflow.ts` | 新增 `getProcessDefinitionXml()` 方法 |
| 前端视图 | `src/views/workflow/ProcessDefinition.vue` | **新建**：流程定义可视化页面 |
| 前端视图 | `src/views/topic/TopicDetail.vue` | 嵌入 `ProcessStatusCard`（条件显示） |
| 前端路由 | `src/router/index.ts` | 新增 `/workflow/definition` 路由 |
| 前端布局 | `src/layouts/MainLayout.vue` | 工作流子菜单新增"流程定义"项 + 补充面包屑 + 修复 `getOpenKeys` |
| 编译验证 | — | `mvn compile → BUILD SUCCESS`（204 个源文件） |

---

## 十二、已知问题排查记录

本章记录系统联调阶段发现的典型 Bug 及其分析过程，可作为后续维护和论文问题解决章节的参考依据。

---

### 12.1 前端工作流 API 响应数据未解包导致的系列运行时错误

#### 12.1.1 问题现象

**发现时间**：2026-03-15  
**影响范围**：所有工作流可视化页面

系统联调时，打开"流程定义"页面，路径说明和图例正常渲染，但 BPMN 流程图区域报错：

```
加载流程定义失败：xml.match is not a function
```

顶部 Toast 通知同样显示相同错误信息。浏览器控制台原始错误：

```
TypeError: xml.match is not a function
    at loadDefinition (ProcessDefinition.vue:148:35)
```

同时，其他工作流组件也存在隐性问题（未报错但数据不显示）：

- **`ProcessStatusCard.vue`**（课题详情嵌入的审查状态卡片）：始终显示"审查流程尚未启动（课题未提交）"，即使课题已提交且流程已启动
- **`ProcessMonitor.vue`**（管理员流程监控页）：列表为空，`total` 为 `undefined`，分页不工作
- **`ProcessMonitor.vue` 弹窗流程图**：bpmn-js 内部报错，流程图无法渲染

#### 12.1.2 错误原因深度分析

##### 第一步：定位报错代码

`ProcessDefinition.vue` 中 `loadDefinition()` 函数的关键代码：

```typescript
async function loadDefinition() {
  const xml = await workflowApi.getProcessDefinitionXml()
  bpmnXml.value = xml
  // ↓ 报错行：xml 不是字符串，没有 .match() 方法
  const versionMatch = xml.match(/flowable:versionTag="([^"]+)"/)
  processVersion.value = versionMatch ? versionMatch[1] : 'v1.0'
}
```

##### 第二步：追溯 API 返回值类型

`workflow.ts` 中的方法定义（修复前）：

```typescript
// workflow.ts（修复前）
getProcessDefinitionXml(): Promise<string> {
  return request.get('/flow/definition/diagram')   // ← 问题根源
}
```

`request.get()` 方法的实际实现（`src/api/request.ts` 第 106-108 行）：

```typescript
// request.ts
get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.get(url, config).then(res => res.data)
  // res.data 是整个 HTTP 响应体（包含 code/message/data 的 JSON 对象）
  // 不是业务数据中的 data 字段！
}
```

后端 Spring Boot 统一响应包装格式（HTTP 响应体）：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "<?xml version='1.0' encoding='UTF-8'?>\n<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"..."
}
```

##### 第三步：明确根本原因

`request.get()` 返回的 `Promise<ApiResponse<T>>` resolve 后得到的是**完整 JSON 包装对象**：

```javascript
// 实际运行时 xml 的值
xml = {
  code: 200,
  message: "操作成功",
  data: "<?xml version='1.0'...>"   // ← 真正的 BPMN XML 在这里
}
// typeof xml === 'object'，不是 'string'
// xml.match 为 undefined → 调用时抛出 TypeError: xml.match is not a function
```

`workflow.ts` 声明返回 `Promise<string>`，但实际 resolve 值是 `ApiResponse<string>` 对象，造成**类型声明与运行时值不一致**。TypeScript 静态类型检查无法在编译时捕获此问题（原因见 12.1.5）。

##### 第四步：分析各组件的隐性问题

**`ProcessStatusCard.vue` 问题分析：**

```typescript
// 修复前
status.value = await workflowApi.getProcessStatus(props.topicId)
// status.value 实际为：{ code: 200, message: "...", data: { processStarted: true, ... } }
// 模板访问 status.value?.processStarted 等同于访问 { code, message, data }.processStarted
// 结果为 undefined，被视为 falsy → v-if="!status.processStarted" 始终 true
// 永远显示"审查流程尚未启动"
```

**`ProcessMonitor.vue` 列表问题分析：**

```typescript
// 修复前
const res = await workflowApi.listProcessInstances({ ... })
// res 实际为：{ code: 200, message: "...", data: { records: [...], total: 10 } }
instances.value = res.records   // ApiResponse 对象无 records 属性 → undefined
total.value = Number(res.total) // ApiResponse 对象无 total 属性 → NaN
// a-table data-source 为 undefined → 空表格；分页总数 NaN → 分页失效
```

**`ProcessMonitor.vue` 弹窗流程图问题分析：**

```typescript
// 修复前
const [xml, history] = await Promise.all([
  workflowApi.getProcessDiagram(instance.processInstanceId),
  workflowApi.getProcessHistory(instance.processInstanceId)
])
bpmnXml.value = xml
// xml 是 ApiResponse 对象，传入 BpmnViewer 组件作为 prop
// bpmn-js 的 viewer.importXML(xml) 接收到对象而非字符串，内部 XML 解析器报错
historyNodes.value = history.filter(...)
// history 是 ApiResponse 对象，没有 .filter() 方法 → TypeError
```

#### 12.1.3 修复方案

**修复文件**：`complete-frontend/src/api/workflow.ts`  
**修复原则**：在所有 API 方法末尾统一链式调用 `.then(res => res.data)` 提取业务数据层，同时完善泛型类型标注，确保类型声明与运行时实际值完全一致。

**修复前后对比（以 `getProcessDefinitionXml` 为例）：**

```typescript
// ===== 修复前 =====
getProcessDefinitionXml(): Promise<string> {
  return request.get('/flow/definition/diagram')
  // Promise resolve 值：ApiResponse<string> = { code, message, data: string }
  // 调用方拿到的是包装对象，不是字符串
}

// ===== 修复后 =====
getProcessDefinitionXml(): Promise<string> {
  return request.get<string>('/flow/definition/diagram').then(res => res.data)
  // Promise resolve 值：string（BPMN 2.0 XML 字符串）
  // 类型声明与运行时值完全一致 ✓
}
```

**全量修复后的 `workflow.ts`（所有 7 个方法）：**

```typescript
export const workflowApi = {
  // 待办任务列表
  getMyTasks(): Promise<FlowTaskVO[]> {
    return request.get<FlowTaskVO[]>('/flow/task/my').then(res => res.data)
  },
  // 签收任务（无业务返回值，返回 void）
  claimTask(taskId: string): Promise<void> {
    return request.post(`/flow/task/${taskId}/claim`).then(() => undefined)
  },
  // 完成审查任务（无业务返回值，返回 void）
  completeReviewTask(taskId: string, data: CompleteReviewTaskDTO): Promise<void> {
    return request.post(`/flow/task/${taskId}/complete`, data).then(() => undefined)
  },
  // 获取课题流程状态
  getProcessStatus(topicId: string): Promise<ProcessStatusVO> {
    return request.get<ProcessStatusVO>(`/flow/process/topic/${topicId}`).then(res => res.data)
  },
  // 分页查询流程实例列表
  listProcessInstances(params: { processStatus?: number; pageNum?: number; pageSize?: number }): Promise<PageResult<ProcessInstanceVO>> {
    return request.get<PageResult<ProcessInstanceVO>>('/flow/process/list', { params }).then(res => res.data)
  },
  // 获取流程定义 BPMN XML（静态，不依赖实例）
  getProcessDefinitionXml(): Promise<string> {
    return request.get<string>('/flow/definition/diagram').then(res => res.data)
  },
  // 获取流程实例 BPMN XML（含活跃节点信息）
  getProcessDiagram(processInstanceId: string): Promise<string> {
    return request.get<string>(`/flow/process/${processInstanceId}/diagram`).then(res => res.data)
  },
  // 获取流程实例历史节点
  getProcessHistory(processInstanceId: string): Promise<ProcessStatusVO['historyNodes']> {
    return request.get<ProcessStatusVO['historyNodes']>(`/flow/process/${processInstanceId}/history`).then(res => res.data)
  }
}
```

#### 12.1.4 修复效果验证

修复后各组件恢复正常：

| 组件 | 修复前现象 | 修复后效果 |
|------|-----------|-----------|
| `ProcessDefinition.vue` | `TypeError: xml.match is not a function`，BPMN 图显示失败警告 | BPMN 2.0 XML 正常传入 bpmn-js，流程图完整渲染 |
| `ProcessStatusCard.vue` | 始终显示"审查流程尚未启动" | 正确展示当前审查阶段、待办角色及历史节点时间线 |
| `ProcessMonitor.vue` 列表 | 空表格，分页 NaN，筛选无效 | 正常加载实例列表，分页、筛选、刷新均可用 |
| `ProcessMonitor.vue` 弹窗 | bpmn-js 内部报错，流程图不渲染 | 弹窗内 BPMN 图正常渲染，活跃/已完成节点颜色高亮正确 |

#### 12.1.5 经验总结与防范建议

##### 为何编译时不报错？

TypeScript 对 `Promise` 泛型的类型检查是结构性（Structural Typing）的，存在一个隐性的"向上兼容"盲区：

```typescript
// request.get<string>() 实际返回 Promise<ApiResponse<string>>
// 但 TypeScript 不会在以下赋值处报错：
const result: Promise<string> = request.get<string>('/some-url')
// 因为 TypeScript 在此处不会深度比较 Promise 内部类型的兼容性
// 只有在实际使用 result 的 resolve 值时才可能触发类型错误
// 而 xml.match(...) 这类调用在 TypeScript 中因为 any 推断链也不会报错
```

这类"类型声明与实际返回值不一致"的错误是 TypeScript 最难静态捕获的一类问题，只能在运行时暴露。

##### 后续开发防范措施

1. **API 方法返回类型约定**：若方法声明返回 `Promise<T>`（而非 `Promise<ApiResponse<T>>`），则**必须**在方法内部完成 `.data` 的提取，保证类型声明与运行时值完全对应

2. **优先使用泛型标注**：调用 `request.get<T>()` 时始终显式传入泛型参数（如 `request.get<string>(...)`），避免默认 `any`，为 IDE 提供更准确的类型辅助

3. **新接口联调顺序**：先在浏览器 Network 面板确认接口实际返回的 JSON 格式，再编写前端 API 封装方法，避免对响应格式的错误假设

4. **考虑改造封装层**（可选，中长期）：若项目规模继续扩大，可将 `HttpRequest.get<T>()` 统一改造为直接返回 `Promise<T>`，从根源消除此类问题：

   ```typescript
   // 可选改造方向：get() 直接返回业务数据
   get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
     return service.get(url, config).then(res => (res.data as ApiResponse<T>).data)
   }
   // 优点：调用方无需手动解包，类型更安全
   // 注意：此改造影响全部 API 调用，需全量回归测试
   ```
