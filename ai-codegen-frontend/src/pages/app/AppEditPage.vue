<template>
  <div id="appEditPage">
    <div class="page-header">
      <div class="header-content">
        <a-breadcrumb separator=">">
          <a-breadcrumb-item>
            <router-link to="/">首页</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>编辑应用</a-breadcrumb-item>
        </a-breadcrumb>
        <h1 class="page-title">编辑应用信息</h1>
      </div>
      <div class="header-actions">
        <a-button type="link" @click="goToChat" size="large">
          <template #icon>
            <MessageOutlined />
          </template>
          进入对话
        </a-button>
      </div>
    </div>

    <div class="edit-container">
      <a-row :gutter="24">
        <a-col :xl="16" :lg="16" :md="24" :sm="24">
          <a-card title="基本信息" :loading="loading" class="form-card">
            <a-form
              :model="formData"
              :rules="rules"
              layout="vertical"
              @finish="handleSubmit"
              ref="formRef"
            >
              <a-form-item label="应用名称" name="appName">
                <a-input
                  v-model:value="formData.appName"
                  placeholder="请输入应用名称"
                  :maxlength="50"
                  show-count
                  class="form-input"
                >
                  <template #prefix>
                    <AppstoreOutlined />
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item
                v-if="isAdmin"
                label="应用封面"
                name="cover"
                extra="支持图片链接，建议尺寸：400x300"
              >
                <a-input
                  v-model:value="formData.cover"
                  placeholder="请输入封面图片链接"
                  class="form-input"
                >
                  <template #prefix>
                    <PictureOutlined />
                  </template>
                </a-input>
                <div v-if="formData.cover" class="cover-preview">
                  <a-image
                    :src="formData.cover"
                    :width="200"
                    :height="150"
                    fallback="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjE1MCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiIGZpbGw9IiM5OTkiPlByZXZpZXcgbm90IGF2YWlsYWJsZTwvdGV4dD48L3N2Zz4="
                    class="cover-image"
                  />
                </div>
              </a-form-item>

              <a-form-item
                v-if="isAdmin"
                label="优先级"
                name="priority"
                extra="设置为99表示精选应用"
              >
                <a-slider
                  v-model:value="formData.priority"
                  :min="0"
                  :max="99"
                  :marks="{ 0: '普通', 50: '推荐', 99: '精选' }"
                  class="priority-slider"
                />
                <div class="priority-display">
                  当前优先级：<a-tag :color="getPriorityColor(formData.priority)">{{
                    formData.priority
                  }}</a-tag>
                </div>
              </a-form-item>

              <a-form-item label="初始提示词" name="initPrompt">
                <a-textarea
                  v-model:value="formData.initPrompt"
                  placeholder="请输入初始提示词"
                  :rows="4"
                  :maxlength="1000"
                  show-count
                  disabled
                  class="form-textarea"
                />
                <div class="form-tip">初始提示词不可修改</div>
              </a-form-item>

              <a-form-item label="生成类型" name="codeGenType">
                <a-input
                  :value="formatCodeGenType(formData.codeGenType)"
                  placeholder="生成类型"
                  disabled
                  class="form-input"
                >
                  <template #prefix>
                    <CodeOutlined />
                  </template>
                </a-input>
                <div class="form-tip">生成类型不可修改</div>
              </a-form-item>

              <a-form-item v-if="formData.deployKey" label="部署密钥" name="deployKey">
                <a-input
                  v-model:value="formData.deployKey"
                  placeholder="部署密钥"
                  disabled
                  class="form-input"
                >
                  <template #prefix>
                    <KeyOutlined />
                  </template>
                </a-input>
                <div class="form-tip">部署密钥不可修改</div>
              </a-form-item>

              <a-form-item class="form-actions">
                <a-space size="large">
                  <a-button
                    type="primary"
                    html-type="submit"
                    :loading="submitting"
                    size="large"
                    class="submit-button"
                  >
                    <template #icon>
                      <SaveOutlined />
                    </template>
                    保存修改
                  </a-button>
                  <a-button @click="resetForm" size="large">
                    <template #icon>
                      <RedoOutlined />
                    </template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>

        <a-col :xl="8" :lg="8" :md="24" :sm="24">
          <a-card title="应用信息" class="info-card">
            <div class="app-info-header">
              <div class="app-icon">
                <AppstoreOutlined />
              </div>
              <div class="app-name">{{ appInfo?.appName || '未命名应用' }}</div>
            </div>

            <a-descriptions :column="1" bordered size="small" class="info-descriptions">
              <a-descriptions-item label="应用ID">
                <span class="info-value">#{{ appInfo?.id }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="创建者">
                <UserInfo :user="appInfo?.user" size="small" />
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                <span class="info-value">{{ formatTime(appInfo?.createTime) }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="更新时间">
                <span class="info-value">{{ formatTime(appInfo?.updateTime) }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="访问链接">
                <div class="preview-actions">
                  <a-button
                    v-if="appInfo?.deployKey"
                    type="primary"
                    @click="openPreview"
                    size="small"
                    ghost
                  >
                    <template #icon>
                      <EyeOutlined />
                    </template>
                    查看预览
                  </a-button>
                </div>
              </a-descriptions-item>
            </a-descriptions>

            <div class="app-stats">
              <div class="stat-item">
                <div class="stat-label">生成类型</div>
                <div class="stat-value">
                  <a-tag color="blue">{{ formatCodeGenType(appInfo?.codeGenType) }}</a-tag>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'
import { getStaticPreviewUrl } from '@/config/env'
import type { FormInstance } from 'ant-design-vue'

import {
  AppstoreOutlined,
  PictureOutlined,
  CodeOutlined,
  KeyOutlined,
  SaveOutlined,
  RedoOutlined,
  MessageOutlined,
  EyeOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  appName: '',
  cover: '',
  priority: 0,
  initPrompt: '',
  codeGenType: '',
  deployKey: '',
})

// 是否为管理员
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度在1-50个字符', trigger: 'blur' },
  ],
  cover: [{ type: 'url', message: '请输入有效的URL', trigger: 'blur' }],
  priority: [{ type: 'number', min: 0, max: 99, message: '优先级范围0-99', trigger: 'blur' }],
}

// 获取优先级颜色
const getPriorityColor = (priority: number) => {
  if (priority >= 90) return 'gold'
  if (priority >= 50) return 'green'
  return 'default'
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  loading.value = true
  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

      // 检查权限
      if (!isAdmin.value && appInfo.value.userId !== loginUserStore.loginUser.id) {
        message.error('您没有权限编辑此应用')
        router.push('/')
        return
      }

      // 填充表单数据
      formData.appName = appInfo.value.appName || ''
      formData.cover = appInfo.value.cover || ''
      formData.priority = appInfo.value.priority || 0
      formData.initPrompt = appInfo.value.initPrompt || ''
      formData.codeGenType = appInfo.value.codeGenType || ''
      formData.deployKey = appInfo.value.deployKey || ''
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!appInfo.value?.id) return

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员可以修改更多字段
      res = await updateAppByAdmin({
        id: appInfo.value.id,
        appName: formData.appName,
        cover: formData.cover,
        priority: formData.priority,
      })
    } else {
      // 普通用户只能修改应用名称
      res = await updateApp({
        id: appInfo.value.id,
        appName: formData.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('修改成功')
      // 重新获取应用信息
      await fetchAppInfo()
    } else {
      message.error('修改失败：' + res.data.message)
    }
  } catch (error) {
    console.error('修改失败：', error)
    message.error('修改失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (appInfo.value) {
    formData.appName = appInfo.value.appName || ''
    formData.cover = appInfo.value.cover || ''
    formData.priority = appInfo.value.priority || 0
  }
  formRef.value?.clearValidate()
}

// 进入对话页面
const goToChat = () => {
  if (appInfo.value?.id) {
    router.push(`/app/chat/${appInfo.value.id}`)
  }
}

// 打开预览
const openPreview = () => {
  if (appInfo.value?.codeGenType && appInfo.value?.id) {
    const url = getStaticPreviewUrl(appInfo.value.codeGenType, String(appInfo.value.id))
    window.open(url, '_blank')
  }
}

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
#appEditPage {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-content {
  flex: 1;
}

.page-title {
  margin: 12px 0 0 0;
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

:deep(.ant-breadcrumb) {
  font-size: 14px;
}

:deep(.ant-breadcrumb-link) {
  color: #666;
}

:deep(.ant-breadcrumb-link:hover) {
  color: #1890ff;
}

.edit-container {
  border-radius: 12px;
}

:deep(.ant-card) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: none;
}

:deep(.ant-card-head) {
  background: linear-gradient(90deg, #f8f9fa, #ffffff);
  border-bottom: 1px solid #f0f0f0;
  border-radius: 12px 12px 0 0 !important;
  padding: 0 24px;
}

:deep(.ant-card-head-title) {
  font-weight: 600;
  color: #2c3e50;
  padding: 16px 0;
}

.form-card {
  height: 100%;
}

.form-input,
.form-textarea {
  border-radius: 8px;
  padding: 8px 12px;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
  padding: 8px 12px;
}

:deep(.ant-input-prefix) {
  color: #999;
}

.priority-slider {
  margin-top: 12px;
}

.priority-slider :deep(.ant-slider-mark-text) {
  font-size: 12px;
  color: #666;
}

.priority-display {
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.cover-preview {
  margin-top: 16px;
  padding: 16px;
  border: 1px dashed #e1e8f0;
  border-radius: 8px;
  background: #fafcff;
  text-align: center;
}

.cover-image {
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.submit-button {
  background: linear-gradient(90deg, #1890ff, #0050b3);
  border: none;
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
}

.submit-button:hover {
  box-shadow: 0 6px 12px rgba(24, 144, 255, 0.4);
}

/* 信息卡片样式 */
.info-card {
  height: 100%;
}

.app-info-header {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.app-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff, #0050b3);
  border-radius: 50%;
  color: white;
  font-size: 24px;
}

.app-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.info-descriptions {
  margin-bottom: 24px;
}

:deep(.ant-descriptions-item-label) {
  background: #fafcff;
  font-weight: 500;
  color: #666;
  width: 100px;
}

.info-value {
  font-weight: 500;
  color: #2c3e50;
}

.preview-actions {
  display: flex;
  align-items: center;
  height: 100%;
}

.not-deployed {
  color: #999;
  font-size: 12px;
}

.app-stats {
  display: flex;
  justify-content: space-between;
  background: #fafcff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .header-actions {
    width: 100%;
  }

  :deep(.ant-btn-link) {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  #appEditPage {
    padding: 16px;
  }

  .page-header {
    padding: 16px;
  }

  .page-title {
    font-size: 20px;
  }

  :deep(.ant-row) {
    flex-direction: column;
  }

  :deep(.ant-col) {
    width: 100%;
    margin-bottom: 16px;
  }

  .app-stats {
    flex-direction: column;
    gap: 16px;
  }

  .stat-item {
    border-bottom: 1px solid #f0f0f0;
    padding-bottom: 16px;
  }

  .stat-item:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}
</style>
