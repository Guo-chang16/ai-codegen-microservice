<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <div class="search-section">
      <a-card :bordered="false" class="search-card">
        <a-form layout="vertical" :model="searchParams" @finish="doSearch">
          <a-row :gutter="24">
            <a-col :xl="6" :md="12" :sm="24">
              <a-form-item label="应用名称">
                <a-input
                  v-model:value="searchParams.appName"
                  placeholder="请输入应用名称"
                  allow-clear
                >
                  <template #prefix>
                    <SearchOutlined />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :md="12" :sm="24">
              <a-form-item label="创建者ID">
                <a-input v-model:value="searchParams.userId" placeholder="请输入用户ID" allow-clear>
                  <template #prefix>
                    <UserOutlined />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :md="12" :sm="24">
              <a-form-item label="生成类型">
                <a-select
                  v-model:value="searchParams.codeGenType"
                  placeholder="请选择生成类型"
                  allow-clear
                >
                  <a-select-option value="">全部类型</a-select-option>
                  <a-select-option
                    v-for="option in CODE_GEN_TYPE_OPTIONS"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :md="12" :sm="24">
              <a-form-item label="&nbsp;">
                <a-space>
                  <a-button type="primary" html-type="submit">
                    <template #icon><SearchOutlined /></template>
                    搜索
                  </a-button>
                  <a-button @click="resetSearch">
                    <template #icon><RedoOutlined /></template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-card>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <a-card :bordered="false">
        <template #title>
          <div class="table-header">
            <span>应用管理列表</span>
          </div>
        </template>

        <a-table
          :columns="columns"
          :data-source="data"
          :pagination="pagination"
          @change="doTableChange"
          :scroll="{ x: 1400 }"
          :loading="loading"
          row-key="id"
          class="app-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'id'">
              <span class="app-id">#{{ record.id }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'cover'">
              <div class="cover-cell">
                <a-image
                  v-if="record.cover"
                  :src="record.cover"
                  :width="60"
                  :height="45"
                  class="app-cover"
                />
                <div v-else class="no-cover">
                  <FileImageOutlined />
                </div>
              </div>
            </template>
            <template v-else-if="column.dataIndex === 'appName'">
              <div class="app-name-cell">
                <span class="app-name">{{ record.appName }}</span>
                <a-tag v-if="record.priority === 99" color="gold" class="featured-tag">精选</a-tag>
              </div>
            </template>
            <template v-else-if="column.dataIndex === 'initPrompt'">
              <a-tooltip :title="record.initPrompt">
                <div class="prompt-text">{{ record.initPrompt }}</div>
              </a-tooltip>
            </template>
            <template v-else-if="column.dataIndex === 'codeGenType'">
              <a-tag :color="getCodegenTypeColor(record.codeGenType)">
                {{ formatCodeGenType(record.codeGenType) }}
              </a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'priority'">
              <a-tag v-if="record.priority === 99" color="gold">精选</a-tag>
              <span v-else>{{ record.priority || 0 }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'deployedTime'">
              <span v-if="record.deployedTime" class="deployed-time">
                {{ formatTime(record.deployedTime) }}
              </span>
            </template>
            <template v-else-if="column.dataIndex === 'user'">
              <UserInfo :user="record.user" size="small" />
            </template>
            <template v-else-if="column.dataIndex === 'createTime'">
              <span class="create-time">{{ formatTime(record.createTime) }}</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <div class="action-buttons">
                <a-button type="link" size="small" @click="editApp(record)">
                  <EditOutlined /> 编辑
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  @click="toggleFeatured(record)"
                  :class="{ 'featured-btn': record.priority === 99 }"
                >
                  <StarOutlined v-if="record.priority !== 99" />
                  <StarFilled v-else />
                  {{ record.priority === 99 ? '取消精选' : '精选' }}
                </a-button>
                <a-popconfirm
                  title="确定要删除这个应用吗？"
                  @confirm="deleteApp(record.id)"
                  ok-text="确定"
                  cancel-text="取消"
                >
                  <a-button type="link" size="small" danger> <DeleteOutlined /> 删除 </a-button>
                </a-popconfirm>
              </div>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  UserOutlined,
  RedoOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  StarOutlined,
  StarFilled,
  ClockCircleOutlined,
  FileImageOutlined,
} from '@ant-design/icons-vue'
import { listAppVoByPageByAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 100,
    fixed: 'left',
  },
  {
    title: '应用信息',
    dataIndex: 'appName',
    width: 200,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    width: 200,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    width: 120,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 100,
  },

  {
    title: '创建者',
    dataIndex: 'user',
    width: 150,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 220,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取精选应用数量
const featuredCount = computed(() => {
  return data.value.filter((item) => item.priority === 99).length
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchParams.appName = undefined
  searchParams.userId = undefined
  searchParams.codeGenType = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 获取生成类型颜色
const getCodegenTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    '1': 'blue',
    '2': 'green',
    '3': 'purple',
    '4': 'orange',
  }
  return colorMap[type] || 'default'
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条记录`,
    showQuickJumper: true,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 切换精选状态
const toggleFeatured = async (app: API.AppVO) => {
  if (!app.id) return

  const newPriority = app.priority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({
      id: app.id,
      priority: newPriority,
    })

    if (res.data.code === 0) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      // 刷新数据
      fetchData()
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  }
}

// 删除应用
const deleteApp = async (id: number | undefined) => {
  if (!id) return

  try {
    const res = await deleteAppByAdmin({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
#appManagePage {
  padding: 24px;
  background: #f5f6f7;
  min-height: calc(100vh - 64px);
}

.search-section {
  margin-bottom: 24px;
}

.search-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  border-radius: 8px;
}

.stats-section {
  margin-bottom: 24px;
}

.stats-section :deep(.ant-statistic) {
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stats-section :deep(.ant-statistic-title) {
  font-size: 14px;
  color: #666;
}

.stats-section :deep(.ant-statistic-content) {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
}

.table-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  overflow: hidden;
}

:deep(.table-section .ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-table {
  border: none;
}

.app-table :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

.app-table :deep(.ant-table-tbody > tr:hover) {
  background: #f9f9f9;
}

.app-id {
  font-family: 'Monaco', 'Consolas', monospace;
  color: #1890ff;
  font-weight: 500;
}

.cover-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-cover {
  border-radius: 4px;
  object-fit: cover;
}

.no-cover {
  width: 60px;
  height: 45px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
  font-size: 16px;
  border-radius: 4px;
  border: 1px dashed #d9d9d9;
}

.app-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-name {
  font-weight: 500;
  color: #1a1a1a;
}

.featured-tag {
  font-size: 12px;
}

.prompt-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

.text-gray {
  color: #999;
  font-size: 12px;
}

.deployed-time {
  color: #52c41a;
  font-weight: 500;
}

.create-time {
  color: #8c8c8c;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.featured-btn {
  color: #faad14 !important;
}

.featured-btn:hover {
  color: #d48806 !important;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
  padding: 16px 8px;
}

:deep(.ant-table-thead > tr > th) {
  padding: 16px 8px;
}

@media (max-width: 768px) {
  #appManagePage {
    padding: 12px;
  }

  .stats-section :deep(.ant-col) {
    margin-bottom: 12px;
  }

  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
