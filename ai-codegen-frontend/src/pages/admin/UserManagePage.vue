<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <div class="search-section">
      <a-card :bordered="false" class="search-card">
        <a-form layout="vertical" :model="searchParams" @finish="doSearch">
          <a-row :gutter="24">
            <a-col :xl="6" :md="12" :sm="24">
              <a-form-item label="账号">
                <a-input
                  v-model:value="searchParams.userAccount"
                  placeholder="请输入账号"
                  allow-clear
                >
                  <template #prefix>
                    <UserOutlined />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :md="12" :sm="24">
              <a-form-item label="用户名">
                <a-input
                  v-model:value="searchParams.userName"
                  placeholder="请输入用户名"
                  allow-clear
                >
                  <template #prefix>
                    <UserSwitchOutlined />
                  </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :md="12" :sm="24">
              <a-form-item label="用户角色">
                <a-select
                  v-model:value="searchParams.userRole"
                  placeholder="请选择用户角色"
                  allow-clear
                >
                  <a-select-option value="">全部角色</a-select-option>
                  <a-select-option value="admin">管理员</a-select-option>
                  <a-select-option value="user">普通用户</a-select-option>
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
            <span>用户管理列表</span>
          </div>
        </template>

        <a-table
          :columns="columns"
          :data-source="data"
          :pagination="pagination"
          @change="doTableChange"
          :scroll="{ x: 1200 }"
          :loading="loading"
          row-key="id"
          class="user-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'id'">
              <span class="user-id">#{{ record.id }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'userAvatar'">
              <div class="avatar-cell">
                <a-avatar
                  v-if="record.userAvatar"
                  :src="record.userAvatar"
                  :size="40"
                  shape="square"
                />
                <a-avatar v-else :size="40" shape="square" style="background-color: #87d068">
                  {{ record.userName?.substring(0, 1) || 'U' }}
                </a-avatar>
              </div>
            </template>
            <template v-else-if="column.dataIndex === 'userAccount'">
              <span class="user-account">{{ record.userAccount }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'userName'">
              <span class="user-name">{{ record.userName }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'userProfile'">
              <a-tooltip :title="record.userProfile">
                <div class="profile-text">
                  {{ record.userProfile || '-' }}
                </div>
              </a-tooltip>
            </template>
            <template v-else-if="column.dataIndex === 'userRole'">
              <a-tag v-if="record.userRole === 'admin'" color="green">管理员</a-tag>
              <a-tag v-else color="blue">普通用户</a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'createTime'">
              <span class="create-time">
                {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
              </span>
            </template>
            <template v-else-if="column.key === 'action'">
              <div class="action-buttons">
                <a-popconfirm
                  title="确定要删除这个用户吗？"
                  @confirm="doDelete(record.id)"
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
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  UserOutlined,
  UserSwitchOutlined,
  RedoOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { deleteUser, listUserVoByPage } from '@/api/userController.ts'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 100,
    fixed: 'left',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    width: 80,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    width: 150,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    width: 150,
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
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
  searchParams.userAccount = undefined
  searchParams.userName = undefined
  searchParams.userRole = undefined
  searchParams.pageNum = 1
  fetchData()
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

// 删除数据
const doDelete = async (id: number) => {
  if (!id) return

  try {
    const res = await deleteUser({ id })
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
#userManagePage {
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

.user-table {
  border: none;
}

.user-table :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

.user-table :deep(.ant-table-tbody > tr:hover) {
  background: #f9f9f9;
}

.user-id {
  font-family: 'Monaco', 'Consolas', monospace;
  color: #1890ff;
  font-weight: 500;
}

.avatar-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-account {
  font-weight: 500;
  color: #1a1a1a;
}

.user-name {
  font-weight: 500;
  color: #1a1a1a;
}

.profile-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

.create-time {
  color: #8c8c8c;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
  padding: 16px 8px;
}

:deep(.ant-table-thead > tr > th) {
  padding: 16px 8px;
}

@media (max-width: 768px) {
  #userManagePage {
    padding: 12px;
  }

  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
