<template>
  <div class="teacher-relation">
    <a-card class="teacher-relation__card">
      <div class="teacher-relation__header">
        <h3 class="teacher-relation__title">教师配对管理</h3>
      </div>

      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- ==================== Tab1: 方向级分配 ==================== -->
        <a-tab-pane key="major" tab="方向级分配">
          <div class="teacher-relation__search">
            <a-form layout="inline" :model="majorSearch">
              <a-form-item label="企业">
                <a-select
                  v-model:value="majorSearch.enterpriseId"
                  placeholder="全部企业"
                  allow-clear
                  style="width: 200px"
                  :options="enterpriseOptions"
                  @change="loadMajorAssignments"
                />
              </a-form-item>
              <a-form-item label="届别">
                <a-input
                  v-model:value="majorSearch.cohort"
                  placeholder="如 2026届"
                  allow-clear
                  style="width: 140px"
                  @press-enter="loadMajorAssignments"
                />
              </a-form-item>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="loadMajorAssignments">
                    <template #icon><SearchOutlined /></template>
                    搜索
                  </a-button>
                  <a-button @click="handleMajorReset">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                  <a-button type="primary" @click="handleAddMajorAssignment">
                    <template #icon><PlusOutlined /></template>
                    新增分配
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>

          <a-table
            :columns="majorColumns"
            :data-source="majorDataSource"
            :loading="majorLoading"
            row-key="id"
            :scroll="{ x: 1100 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'isEnabled'">
                <a-tag :color="record.isEnabled === 1 ? 'green' : 'red'">
                  {{ record.isEnabled === 1 ? '启用' : '停用' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="handleEditMajorAssignment(record)">
                    <template #icon><EditOutlined /></template>
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确定要删除该分配记录吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleDeleteMajorAssignment(record)"
                  >
                    <a-button type="link" size="small" danger>
                      <template #icon><DeleteOutlined /></template>
                      删除
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- ==================== Tab2: 精确配对 ==================== -->
        <a-tab-pane key="pair" tab="精确配对">
          <div class="teacher-relation__search">
            <a-form layout="inline" :model="pairSearch">
              <a-form-item label="企业">
                <a-select
                  v-model:value="pairSearch.enterpriseId"
                  placeholder="全部企业"
                  allow-clear
                  style="width: 200px"
                  :options="enterpriseOptions"
                  @change="loadTeacherPairs"
                />
              </a-form-item>
              <a-form-item label="届别">
                <a-input
                  v-model:value="pairSearch.cohort"
                  placeholder="如 2026届"
                  allow-clear
                  style="width: 140px"
                  @press-enter="loadTeacherPairs"
                />
              </a-form-item>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="loadTeacherPairs">
                    <template #icon><SearchOutlined /></template>
                    搜索
                  </a-button>
                  <a-button @click="handlePairReset">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                  <a-button type="primary" @click="handleAddTeacherPair">
                    <template #icon><PlusOutlined /></template>
                    新增配对
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>

          <a-table
            :columns="pairColumns"
            :data-source="pairDataSource"
            :loading="pairLoading"
            row-key="relationId"
            :scroll="{ x: 1400 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'relationType'">
                <a-tag :color="RELATION_TYPE_COLORS[record.relationType] || 'default'">
                  {{ RELATION_TYPE_LABELS[record.relationType] || record.relationType }}
                </a-tag>
              </template>
              <template v-if="column.key === 'isEnabled'">
                <a-tag :color="record.isEnabled === 1 ? 'green' : 'red'">
                  {{ record.isEnabled === 1 ? '启用' : '停用' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="handleEditTeacherPair(record)">
                    <template #icon><EditOutlined /></template>
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确定要删除该配对记录吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleDeleteTeacherPair(record)"
                  >
                    <a-button type="link" size="small" danger>
                      <template #icon><DeleteOutlined /></template>
                      删除
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- ==================== Tab3: 覆盖检查 ==================== -->
        <a-tab-pane key="coverage" tab="覆盖检查">
          <div class="teacher-relation__search">
            <a-form layout="inline" :model="coverageSearch">
              <a-form-item label="企业">
                <a-select
                  v-model:value="coverageSearch.enterpriseId"
                  placeholder="全部企业"
                  allow-clear
                  style="width: 200px"
                  :options="enterpriseOptions"
                  @change="loadCoverageData"
                />
              </a-form-item>
              <a-form-item label="届别">
                <a-input
                  v-model:value="coverageSearch.cohort"
                  placeholder="如 2026届"
                  allow-clear
                  style="width: 140px"
                  @press-enter="loadCoverageData"
                />
              </a-form-item>
              <a-form-item>
                <a-space>
                  <a-button type="primary" @click="loadCoverageData">
                    <template #icon><SearchOutlined /></template>
                    搜索
                  </a-button>
                  <a-button @click="handleCoverageReset">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>

          <!-- 覆盖率统计卡片 -->
          <div class="teacher-relation__stats" v-if="coverageStats">
            <a-row :gutter="16" style="margin-bottom: 16px">
              <a-col :span="8">
                <a-statistic title="企业教师总数" :value="coverageStats.totalCount">
                  <template #prefix><TeamOutlined /></template>
                </a-statistic>
              </a-col>
              <a-col :span="8">
                <a-statistic
                  title="已覆盖"
                  :value="coverageStats.coveredCount"
                  :value-style="{ color: '#3f8600' }"
                >
                  <template #prefix><CheckCircleOutlined /></template>
                </a-statistic>
              </a-col>
              <a-col :span="8">
                <a-statistic
                  title="未覆盖"
                  :value="coverageStats.uncoveredCount"
                  :value-style="{ color: coverageStats.uncoveredCount > 0 ? '#cf1322' : '#3f8600' }"
                >
                  <template #prefix><ExclamationCircleOutlined /></template>
                </a-statistic>
              </a-col>
            </a-row>
          </div>

          <a-table
            :columns="coverageColumns"
            :data-source="coverageDataSource"
            :loading="coverageLoading"
            row-key="enterpriseTeacherId"
            :scroll="{ x: 1200 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'covered'">
                <a-tag :color="record.covered ? 'green' : 'red'">
                  {{ record.covered ? '已覆盖' : '未覆盖' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'coverageSource'">
                <a-tag
                  v-if="record.coverageSource"
                  :color="COVERAGE_SOURCE_COLORS[record.coverageSource] || 'default'"
                >
                  {{ COVERAGE_SOURCE_LABELS[record.coverageSource] || '-' }}
                </a-tag>
                <span v-else>-</span>
              </template>
              <template v-if="column.key === 'univTeacherName'">
                {{ record.univTeacherName || '-' }}
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- ==================== 方向级分配弹窗 ==================== -->
    <a-modal
      v-model:open="majorModalVisible"
      :title="majorModalTitle"
      :confirm-loading="majorModalLoading"
      @ok="handleMajorModalOk"
      @cancel="handleMajorModalCancel"
    >
      <a-form
        ref="majorFormRef"
        :model="majorFormState"
        :rules="majorFormRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="高校教师" name="univTeacherId">
          <a-select
            v-model:value="majorFormState.univTeacherId"
            placeholder="请选择高校教师"
            show-search
            :filter-option="filterTeacherOption"
            :options="univTeacherOptions"
          />
        </a-form-item>
        <a-form-item label="企业" name="enterpriseId">
          <a-select
            v-model:value="majorFormState.enterpriseId"
            placeholder="请选择企业"
            :options="enterpriseOptions"
            @change="handleMajorEnterpriseChange"
          />
        </a-form-item>
        <a-form-item label="专业方向" name="directionId">
          <a-select
            v-model:value="majorFormState.directionId"
            placeholder="请先选择企业"
            :options="directionOptions"
            :disabled="!majorFormState.enterpriseId"
          />
        </a-form-item>
        <a-form-item label="届别" name="cohort">
          <a-input v-model:value="majorFormState.cohort" placeholder="如 2026届" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="majorFormState.remark" placeholder="备注信息（选填）" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- ==================== 精确配对弹窗 ==================== -->
    <a-modal
      v-model:open="pairModalVisible"
      :title="pairModalTitle"
      :confirm-loading="pairModalLoading"
      @ok="handlePairModalOk"
      @cancel="handlePairModalCancel"
    >
      <a-form
        ref="pairFormRef"
        :model="pairFormState"
        :rules="pairFormRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="高校教师" name="univTeacherId">
          <a-select
            v-model:value="pairFormState.univTeacherId"
            placeholder="请选择高校教师"
            show-search
            :filter-option="filterTeacherOption"
            :options="univTeacherOptions"
          />
        </a-form-item>
        <a-form-item label="企业" name="enterpriseId">
          <a-select
            v-model:value="pairFormState.enterpriseId"
            placeholder="请选择企业"
            :options="enterpriseOptions"
            @change="handlePairEnterpriseChange"
          />
        </a-form-item>
        <a-form-item label="企业教师" name="enterpriseTeacherId">
          <a-select
            v-model:value="pairFormState.enterpriseTeacherId"
            placeholder="请选择企业教师"
            show-search
            :filter-option="filterTeacherOption"
            :options="entTeacherOptions"
          />
        </a-form-item>
        <a-form-item label="专业方向" name="directionId">
          <a-select
            v-model:value="pairFormState.directionId"
            placeholder="可选"
            allow-clear
            :options="directionOptions"
            :disabled="!pairFormState.enterpriseId"
          />
        </a-form-item>
        <a-form-item label="届别" name="cohort">
          <a-input v-model:value="pairFormState.cohort" placeholder="如 2026届" />
        </a-form-item>
        <a-form-item label="配对类型" name="relationType">
          <a-select v-model:value="pairFormState.relationType" placeholder="请选择">
            <a-select-option value="DIRECT">直接配对</a-select-option>
            <a-select-option value="ASSIST">辅助支持</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="pairFormState.remark" placeholder="备注信息（选填）" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 教师配对管理页面
 * @description 高校教师与企业教师/专业方向的双层配对管理
 * @author YuWan
 * @date 2026-03-08
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, TableColumnsType } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { teacherRelationApi } from '@/api/teacherRelation'
import { enterpriseApi } from '@/api/enterprise'
import { majorApi } from '@/api/major'
import { userApi } from '@/api/user'
import type {
  UnivTeacherMajorVO,
  UnivTeacherMajorDTO,
  TeacherRelationVO,
  TeacherRelationDTO,
  TeacherCoverageVO,
  CoverageStats
} from '@/types/teacherRelation'
import {
  RELATION_TYPE_LABELS,
  RELATION_TYPE_COLORS,
  COVERAGE_SOURCE_LABELS,
  COVERAGE_SOURCE_COLORS
} from '@/types/teacherRelation'

defineOptions({
  name: 'TeacherRelationManagement'
})

// ==================== 通用数据 ====================

const activeTab = ref('major')

/** 企业下拉选项 */
const enterpriseOptions = ref<Array<{ label: string; value: string }>>([])

/** 专业方向下拉选项 */
const directionOptions = ref<Array<{ label: string; value: string }>>([])

/** 高校教师下拉选项 */
const univTeacherOptions = ref<Array<{ label: string; value: string }>>([])

/** 企业教师下拉选项 */
const entTeacherOptions = ref<Array<{ label: string; value: string }>>([])

/**
 * 加载企业下拉数据
 */
const loadEnterpriseOptions = async () => {
  try {
    const res = await enterpriseApi.getAllEnterprises()
    enterpriseOptions.value = res.data.map((e) => ({
      label: e.enterpriseName,
      value: e.enterpriseId
    }))
  } catch (error) {
    console.error('加载企业列表失败:', error)
  }
}

/**
 * 加载高校教师下拉数据
 */
const loadUnivTeacherOptions = async () => {
  try {
    const res = await userApi.getUserList({
      roleCode: 'UNIVERSITY_TEACHER',
      pageNum: 1,
      pageSize: 500
    })
    univTeacherOptions.value = res.data.records.map((u) => ({
      label: `${u.realName}（${u.userCode || u.username}）`,
      value: u.userId
    }))
  } catch (error) {
    console.error('加载高校教师列表失败:', error)
  }
}

/**
 * 加载企业教师下拉数据
 */
const loadEntTeacherOptions = async () => {
  try {
    const res = await userApi.getUserList({
      roleCode: 'ENTERPRISE_TEACHER',
      pageNum: 1,
      pageSize: 500
    })
    entTeacherOptions.value = res.data.records.map((u) => ({
      label: `${u.realName}（${u.userCode || u.username}）`,
      value: u.userId
    }))
  } catch (error) {
    console.error('加载企业教师列表失败:', error)
  }
}

/**
 * 根据企业ID加载方向列表
 */
const loadDirectionOptions = async (enterpriseId: string) => {
  if (!enterpriseId) {
    directionOptions.value = []
    return
  }
  try {
    const res = await majorApi.getDirectionList(enterpriseId)
    directionOptions.value = res.data.map((d) => ({
      label: d.directionName,
      value: d.directionId
    }))
  } catch (error) {
    console.error('加载专业方向列表失败:', error)
  }
}

/**
 * 教师搜索过滤
 */
const filterTeacherOption = (input: string, option: { label: string }) => {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

// ==================== Tab1: 方向级分配 ====================

const majorSearch = reactive({ enterpriseId: undefined as string | undefined, cohort: undefined as string | undefined })
const majorDataSource = ref<UnivTeacherMajorVO[]>([])
const majorLoading = ref(false)

const majorColumns: TableColumnsType = [
  { title: '高校教师', dataIndex: 'univTeacherName', key: 'univTeacherName', width: 120 },
  { title: '工号', dataIndex: 'univTeacherEmployeeNo', key: 'univTeacherEmployeeNo', width: 100 },
  { title: '企业', dataIndex: 'enterpriseName', key: 'enterpriseName', width: 160 },
  { title: '专业方向', dataIndex: 'directionName', key: 'directionName', width: 160 },
  { title: '届别', dataIndex: 'cohort', key: 'cohort', width: 100 },
  { title: '状态', dataIndex: 'isEnabled', key: 'isEnabled', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right' as const, width: 160 }
]

const loadMajorAssignments = async () => {
  majorLoading.value = true
  try {
    const res = await teacherRelationApi.listMajorAssignments(majorSearch.enterpriseId, majorSearch.cohort)
    majorDataSource.value = res.data
  } catch (error) {
    message.error('加载方向级分配列表失败')
    console.error('加载方向级分配列表失败:', error)
  } finally {
    majorLoading.value = false
  }
}

const handleMajorReset = () => {
  majorSearch.enterpriseId = undefined
  majorSearch.cohort = undefined
  loadMajorAssignments()
}

// 方向级分配弹窗
const majorModalVisible = ref(false)
const majorModalLoading = ref(false)
const majorFormRef = ref<FormInstance>()
const editingMajorId = ref<string | null>(null)

const majorModalTitle = computed(() => (editingMajorId.value ? '编辑方向级分配' : '新增方向级分配'))

const majorFormState = reactive<UnivTeacherMajorDTO>({
  univTeacherId: '',
  directionId: '',
  enterpriseId: '',
  cohort: '',
  remark: ''
})

const majorFormRules = {
  univTeacherId: [{ required: true, message: '请选择高校教师' }],
  enterpriseId: [{ required: true, message: '请选择企业' }],
  directionId: [{ required: true, message: '请选择专业方向' }],
  cohort: [{ required: true, message: '请输入届别' }]
}

const resetMajorForm = () => {
  majorFormState.univTeacherId = ''
  majorFormState.directionId = ''
  majorFormState.enterpriseId = ''
  majorFormState.cohort = ''
  majorFormState.remark = ''
  editingMajorId.value = null
  directionOptions.value = []
  majorFormRef.value?.clearValidate()
}

const handleAddMajorAssignment = () => {
  resetMajorForm()
  majorModalVisible.value = true
}

const handleEditMajorAssignment = (record: UnivTeacherMajorVO) => {
  resetMajorForm()
  editingMajorId.value = record.id
  majorFormState.univTeacherId = record.univTeacherId
  majorFormState.enterpriseId = record.enterpriseId
  majorFormState.directionId = record.directionId
  majorFormState.cohort = record.cohort
  majorFormState.remark = record.remark || ''
  loadDirectionOptions(record.enterpriseId)
  majorModalVisible.value = true
}

const handleDeleteMajorAssignment = async (record: UnivTeacherMajorVO) => {
  try {
    await teacherRelationApi.deleteMajorAssignment(record.id)
    message.success('删除成功')
    loadMajorAssignments()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handleMajorEnterpriseChange = (val: string) => {
  majorFormState.directionId = ''
  loadDirectionOptions(val)
}

const handleMajorModalOk = async () => {
  try {
    await majorFormRef.value?.validate()
  } catch {
    return
  }
  majorModalLoading.value = true
  try {
    if (editingMajorId.value) {
      await teacherRelationApi.updateMajorAssignment(editingMajorId.value, { ...majorFormState })
      message.success('编辑成功')
    } else {
      await teacherRelationApi.addMajorAssignment({ ...majorFormState })
      message.success('新增成功')
    }
    majorModalVisible.value = false
    loadMajorAssignments()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    majorModalLoading.value = false
  }
}

const handleMajorModalCancel = () => {
  majorModalVisible.value = false
}

// ==================== Tab2: 精确配对 ====================

const pairSearch = reactive({ enterpriseId: undefined as string | undefined, cohort: undefined as string | undefined })
const pairDataSource = ref<TeacherRelationVO[]>([])
const pairLoading = ref(false)

const pairColumns: TableColumnsType = [
  { title: '高校教师', dataIndex: 'univTeacherName', key: 'univTeacherName', width: 120 },
  { title: '高校教师工号', dataIndex: 'univTeacherEmployeeNo', key: 'univTeacherEmployeeNo', width: 110 },
  { title: '企业教师', dataIndex: 'enterpriseTeacherName', key: 'enterpriseTeacherName', width: 120 },
  { title: '企业教师工号', dataIndex: 'enterpriseTeacherEmployeeNo', key: 'enterpriseTeacherEmployeeNo', width: 110 },
  { title: '企业', dataIndex: 'enterpriseName', key: 'enterpriseName', width: 150 },
  { title: '专业方向', dataIndex: 'directionName', key: 'directionName', width: 140 },
  { title: '届别', dataIndex: 'cohort', key: 'cohort', width: 90 },
  { title: '配对类型', dataIndex: 'relationType', key: 'relationType', width: 100 },
  { title: '状态', dataIndex: 'isEnabled', key: 'isEnabled', width: 80 },
  { title: '操作', key: 'action', fixed: 'right' as const, width: 160 }
]

const loadTeacherPairs = async () => {
  pairLoading.value = true
  try {
    const res = await teacherRelationApi.listTeacherPairs(pairSearch.enterpriseId, pairSearch.cohort)
    pairDataSource.value = res.data
  } catch (error) {
    message.error('加载精确配对列表失败')
    console.error('加载精确配对列表失败:', error)
  } finally {
    pairLoading.value = false
  }
}

const handlePairReset = () => {
  pairSearch.enterpriseId = undefined
  pairSearch.cohort = undefined
  loadTeacherPairs()
}

// 精确配对弹窗
const pairModalVisible = ref(false)
const pairModalLoading = ref(false)
const pairFormRef = ref<FormInstance>()
const editingPairId = ref<string | null>(null)

const pairModalTitle = computed(() => (editingPairId.value ? '编辑精确配对' : '新增精确配对'))

const pairFormState = reactive<TeacherRelationDTO>({
  univTeacherId: '',
  enterpriseTeacherId: '',
  enterpriseId: '',
  directionId: '',
  cohort: '',
  relationType: 'DIRECT',
  remark: ''
})

const pairFormRules = {
  univTeacherId: [{ required: true, message: '请选择高校教师' }],
  enterpriseTeacherId: [{ required: true, message: '请选择企业教师' }],
  enterpriseId: [{ required: true, message: '请选择企业' }],
  cohort: [{ required: true, message: '请输入届别' }],
  relationType: [{ required: true, message: '请选择配对类型' }]
}

const resetPairForm = () => {
  pairFormState.univTeacherId = ''
  pairFormState.enterpriseTeacherId = ''
  pairFormState.enterpriseId = ''
  pairFormState.directionId = ''
  pairFormState.cohort = ''
  pairFormState.relationType = 'DIRECT'
  pairFormState.remark = ''
  editingPairId.value = null
  directionOptions.value = []
  pairFormRef.value?.clearValidate()
}

const handleAddTeacherPair = () => {
  resetPairForm()
  pairModalVisible.value = true
}

const handleEditTeacherPair = (record: TeacherRelationVO) => {
  resetPairForm()
  editingPairId.value = record.relationId
  pairFormState.univTeacherId = record.univTeacherId
  pairFormState.enterpriseTeacherId = record.enterpriseTeacherId
  pairFormState.enterpriseId = record.enterpriseId
  pairFormState.directionId = record.directionId || ''
  pairFormState.cohort = record.cohort
  pairFormState.relationType = record.relationType || 'DIRECT'
  pairFormState.remark = record.remark || ''
  loadDirectionOptions(record.enterpriseId)
  pairModalVisible.value = true
}

const handleDeleteTeacherPair = async (record: TeacherRelationVO) => {
  try {
    await teacherRelationApi.deleteTeacherPair(record.relationId)
    message.success('删除成功')
    loadTeacherPairs()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handlePairEnterpriseChange = (val: string) => {
  pairFormState.directionId = ''
  loadDirectionOptions(val)
}

const handlePairModalOk = async () => {
  try {
    await pairFormRef.value?.validate()
  } catch {
    return
  }
  pairModalLoading.value = true
  try {
    if (editingPairId.value) {
      await teacherRelationApi.updateTeacherPair(editingPairId.value, { ...pairFormState })
      message.success('编辑成功')
    } else {
      await teacherRelationApi.addTeacherPair({ ...pairFormState })
      message.success('新增成功')
    }
    pairModalVisible.value = false
    loadTeacherPairs()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    pairModalLoading.value = false
  }
}

const handlePairModalCancel = () => {
  pairModalVisible.value = false
}

// ==================== Tab3: 覆盖检查 ====================

const coverageSearch = reactive({ enterpriseId: undefined as string | undefined, cohort: undefined as string | undefined })
const coverageDataSource = ref<TeacherCoverageVO[]>([])
const coverageLoading = ref(false)
const coverageStats = ref<CoverageStats | null>(null)

const coverageColumns: TableColumnsType = [
  { title: '企业教师', dataIndex: 'enterpriseTeacherName', key: 'enterpriseTeacherName', width: 120 },
  { title: '工号', dataIndex: 'enterpriseTeacherEmployeeNo', key: 'enterpriseTeacherEmployeeNo', width: 100 },
  { title: '企业', dataIndex: 'enterpriseName', key: 'enterpriseName', width: 160 },
  { title: '专业方向', dataIndex: 'directionName', key: 'directionName', width: 140 },
  { title: '覆盖状态', dataIndex: 'covered', key: 'covered', width: 100 },
  { title: '配对来源', dataIndex: 'coverageSource', key: 'coverageSource', width: 110 },
  { title: '高校教师', dataIndex: 'univTeacherName', key: 'univTeacherName', width: 120 }
]

const loadCoverageData = async () => {
  coverageLoading.value = true
  try {
    const [listRes, statsRes] = await Promise.all([
      teacherRelationApi.getCoverageList(coverageSearch.enterpriseId, coverageSearch.cohort),
      teacherRelationApi.getCoverageStats(coverageSearch.cohort)
    ])
    coverageDataSource.value = listRes.data
    coverageStats.value = statsRes.data
  } catch (error) {
    message.error('加载覆盖数据失败')
    console.error('加载覆盖数据失败:', error)
  } finally {
    coverageLoading.value = false
  }
}

const handleCoverageReset = () => {
  coverageSearch.enterpriseId = undefined
  coverageSearch.cohort = undefined
  loadCoverageData()
}

// ==================== Tab切换 ====================

const handleTabChange = (key: string) => {
  if (key === 'major') loadMajorAssignments()
  else if (key === 'pair') loadTeacherPairs()
  else if (key === 'coverage') loadCoverageData()
}

// ==================== 初始化 ====================

onMounted(() => {
  loadEnterpriseOptions()
  loadUnivTeacherOptions()
  loadEntTeacherOptions()
  loadMajorAssignments()
})
</script>

<style scoped lang="scss">
.teacher-relation {
  &__card {
    min-height: calc(100vh - 200px);
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  &__title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }

  &__search {
    margin-bottom: 16px;
  }

  &__stats {
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
    margin-bottom: 16px;
  }
}
</style>
