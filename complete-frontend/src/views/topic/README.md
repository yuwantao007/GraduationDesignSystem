# 课题申报模块 - 开发说明

## 模块概述

课题申报模块是毕业设计全过程管理系统的核心功能之一，提供课题的创建、编辑、提交、审批等完整流程管理。

## 技术栈

### 前端
- **框架**: Vue 3 + TypeScript + Composition API
- **UI组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由**: Vue Router 5.x
- **HTTP客户端**: Axios
- **日期处理**: dayjs

### 后端
- **框架**: Spring Boot 3.x
- **数据库**: MySQL
- **ORM**: MyBatis Plus
- **API文档**: Knife4j (Swagger)

## 功能特性

### 1. 课题列表管理 (TopicList.vue)
- ✅ 分页查询课题列表
- ✅ 多条件搜索（课题名称、课题大类、课题类型、审查状态）
- ✅ 课题状态展示（草稿/已提交、审查状态）
- ✅ 课题操作（查看、编辑、删除、提交、撤回）

### 2. 课题表单管理 (TopicForm.vue)
- ✅ 采用类似纸质报告表格式的表单布局
- ✅ 支持新建和编辑模式
- ✅ 实时字数统计
- ✅ 表单验证
- ✅ 保存草稿和提交功能

### 3. 课题详情查看 (TopicDetail.vue)
- ✅ 只读模式展示课题完整信息
- ✅ 保持与表单一致的纸质报告格式
- ✅ 支持打印功能
- ✅ 显示审批状态和签名信息

## 文件结构

```
complete-frontend/
├── src/
│   ├── api/
│   │   └── topic.ts                 # 课题API接口封装
│   ├── types/
│   │   └── topic.ts                 # 课题类型定义
│   ├── views/
│   │   └── topic/
│   │       ├── TopicList.vue        # 课题列表页面
│   │       ├── TopicForm.vue        # 课题表单页面（创建/编辑）
│   │       └── TopicDetail.vue      # 课题详情页面
│   └── router/
│       └── index.ts                 # 路由配置（已添加课题相关路由）
```

## 路由配置

```typescript
// 课题管理相关路由
{
  path: 'topic/list',
  name: 'TopicList',
  component: () => import('@/views/topic/TopicList.vue'),
  meta: { title: '课题列表', icon: 'FileTextOutlined', permission: 'topic:view' }
},
{
  path: 'topic/create',
  name: 'TopicCreate',
  component: () => import('@/views/topic/TopicForm.vue'),
  meta: { title: '创建课题', permission: 'topic:create', hideInMenu: true }
},
{
  path: 'topic/edit/:id',
  name: 'TopicEdit',
  component: () => import('@/views/topic/TopicForm.vue'),
  meta: { title: '编辑课题', permission: 'topic:edit', hideInMenu: true }
},
{
  path: 'topic/detail/:id',
  name: 'TopicDetail',
  component: () => import('@/views/topic/TopicDetail.vue'),
  meta: { title: '课题详情', permission: 'topic:view', hideInMenu: true }
}
```

## API接口说明

### 课题API (topicApi)

| 方法名 | 接口路径 | 说明 |
|--------|---------|------|
| createTopic | POST /api/topic | 创建课题 |
| updateTopic | PUT /api/topic/:id | 更新课题 |
| getTopicDetail | GET /api/topic/:id | 获取课题详情 |
| getTopicList | GET /api/topic/list | 分页查询课题列表 |
| getMyTopics | GET /api/topic/my | 获取我的课题列表 |
| deleteTopic | DELETE /api/topic/:id | 删除课题（仅草稿） |
| submitTopic | POST /api/topic/submit | 提交课题 |
| withdrawTopic | POST /api/topic/:id/withdraw | 撤回课题 |
| signTopic | POST /api/topic/sign | 课题签名 |
| countPassedTopics | GET /api/topic/count/passed | 统计通过终审的课题数 |

## 数据模型

### 课题枚举类型

```typescript
// 课题大类
export enum TopicCategory {
  UPGRADE = 1,          // 高职升本
  THREE_PLUS_ONE = 2,   // 3+1联合培养
  EXPERIMENTAL = 3      // 实验班
}

// 课题类型
export enum TopicType {
  DESIGN = 1,           // 设计
  THESIS = 2            // 论文
}

// 课题来源
export enum TopicSource {
  INTERNAL = 1,         // 校内
  EXTERNAL = 2          // 校外协同开发
}

// 审查状态
export enum TopicReviewStatus {
  PENDING_PRE_REVIEW = 1,      // 待预审
  PRE_REVIEW_PASSED = 2,       // 预审通过
  PRE_REVIEW_REJECTED = 3,     // 预审不通过
  PENDING_FINAL_REVIEW = 4,    // 待终审
  FINAL_REVIEW_PASSED = 5,     // 终审通过
  FINAL_REVIEW_REJECTED = 6    // 终审不通过
}
```

### 课题VO (TopicVO)

```typescript
interface TopicVO {
  topicId: string
  topicTitle: string
  topicCategory: TopicCategory
  topicCategoryDesc: string
  topicType: TopicType
  topicTypeDesc: string
  topicSource: TopicSource
  topicSourceDesc: string
  enterpriseId: string
  enterpriseName: string
  guidanceDirection: string
  backgroundSignificance: string
  contentSummary: string
  professionalTraining: string
  workloadWeeks: number
  startDate: string
  endDate: string
  creatorId: string
  creatorName: string
  reviewStatus: TopicReviewStatus
  reviewStatusDesc: string
  isSubmitted: number
  createTime: string
  updateTime: string
  remark: string
  // ... 其他字段
}
```

## 使用说明

### 1. 创建课题

1. 点击"新建课题"按钮
2. 填写课题基本信息（姓名、专业、班级、学号）
3. 填写课题题目和指导教师
4. 填写三个主要内容区域（每个区域有最小字数要求）
5. 填写附加信息（课题大类、课题类型、课题来源等）
6. 点击"保存"保存为草稿，或点击"保存并提交"直接提交

### 2. 编辑课题

1. 在课题列表中找到需要编辑的课题
2. 点击"编辑"按钮（仅草稿状态可编辑）
3. 修改课题信息
4. 保存或提交

### 3. 查看课题

1. 在课题列表中点击课题名称或"查看"按钮
2. 查看课题详细信息
3. 可以打印课题报告

### 4. 提交课题

1. 确保课题信息填写完整
2. 在列表中点击"提交"按钮
3. 提交后课题状态变为"待预审"

### 5. 撤回课题

1. 仅在"待预审"状态下可以撤回
2. 点击"撤回"按钮
3. 撤回后可以继续编辑

## 样式说明

### 表单页面样式特点

- 采用类似纸质报告表格式的布局
- 顶部显示标题"毕业设计（论文）开题报告表"
- 使用表格单元格布局展示基本信息
- 使用带边框的章节区域展示大文本内容
- 每个文本区域都有实时字数统计

### 响应式设计

- 表单内容区最大宽度1200px，居中显示
- 支持打印样式（@media print）
- 移动端适配

## 开发规范遵循

本模块严格遵循 [rule.md](../../../rule.md) 中定义的前端开发规范：

### 命名规范
- ✅ 组件文件名：大驼峰命名法（TopicList.vue）
- ✅ 变量和函数：小驼峰命名法（getTopicList）
- ✅ 类型定义：大驼峰命名法（TopicVO）
- ✅ 常量：全大写+下划线（MAX_RETRY_COUNT）

### 组件结构规范
- ✅ 使用 Composition API (setup)
- ✅ 代码组织顺序：导入 → 组件选项 → Props → Emits → 响应式数据 → 计算属性 → 方法 → 生命周期
- ✅ TypeScript 类型定义完整

### 样式规范
- ✅ 使用 BEM 命名规范
- ✅ Scoped CSS
- ✅ SCSS 变量管理

## 待完成功能

- [ ] 企业选择下拉框数据加载（需要企业管理模块API）
- [ ] 工作量明细的动态表格编辑
- [ ] 任务与进度要求的动态表格编辑
- [ ] 参考文献的动态列表编辑
- [ ] 开发环境的动态字段编辑
- [ ] 课题签名功能的电子签名组件
- [ ] 课题审批流程页面

## 注意事项

1. **字数要求**：
   - 选题背景与意义：不少于150字
   - 课题内容简述：不少于150字
   - 专业知识综合训练情况：不少于100字

2. **权限控制**：
   - 仅企业教师和系统管理员可以创建课题
   - 只有课题创建人可以编辑和删除自己的课题
   - 草稿状态下可以编辑和删除
   - 提交后只能查看，待预审状态下可以撤回

3. **数据验证**：
   - 必填字段：课题名称、课题大类、课题类型、课题来源、归属企业
   - 字数要求的文本框有前端和后端双重验证

## 更新记录

### 2026-02-22
- ✅ 创建课题类型定义文件（types/topic.ts）
- ✅ 创建课题API接口封装（api/topic.ts）
- ✅ 创建课题列表页面（TopicList.vue）
- ✅ 创建课题表单页面（TopicForm.vue）
- ✅ 创建课题详情页面（TopicDetail.vue）
- ✅ 添加路由配置
- ✅ 修复API响应数据访问错误

## 联系方式

如有问题，请联系系统架构师。
