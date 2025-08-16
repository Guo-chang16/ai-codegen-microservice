<template>
  <a-modal
    v-model:open="visible"
    title="应用详情"
    :footer="null"
    width="420px"
    class="app-detail-modal"
  >
    <div class="app-detail-content">
      <!-- 应用封面 -->
      <div class="app-cover-section" v-if="app?.cover">
        <img :src="app.cover" :alt="app.appName" class="app-cover-image" />
      </div>
      <div class="app-cover-placeholder" v-else>
        <div class="placeholder-icon">🤖</div>
      </div>

      <!-- 应用名称 -->
      <div class="app-name-section">
        <h2 class="app-title">{{ app?.appName || '未命名应用' }}</h2>
      </div>

      <!-- 应用基础信息 -->
      <div class="app-basic-info">
        <div class="info-grid">
          <div class="info-item">
            <div class="info-icon">👤</div>
            <div class="info-content">
              <div class="info-label">创建者</div>
              <UserInfo :user="app?.user" size="small" />
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">⏰</div>
            <div class="info-content">
              <div class="info-label">创建时间</div>
              <div class="info-value">{{ formatTime(app?.createTime) }}</div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">⚙️</div>
            <div class="info-content">
              <div class="info-label">生成类型</div>
              <a-tag v-if="app?.codeGenType" color="blue" class="type-tag">
                {{ formatCodeGenType(app.codeGenType) }}
              </a-tag>
              <div v-else class="info-value">未知类型</div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">📊</div>
            <div class="info-content">
              <div class="info-label">应用ID</div>
              <div class="info-value app-id">#{{ app?.id || 'N/A' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作栏（仅本人或管理员可见） -->
      <div v-if="showActions" class="app-actions">
        <div class="action-buttons">
          <a-button
            type="primary"
            size="large"
            class="action-button edit-button"
            @click="handleEdit"
          >
            <template #icon>
              <EditOutlined />
            </template>
            编辑应用
          </a-button>
          <a-popconfirm
            title="确定要删除这个应用吗？"
            @confirm="handleDelete"
            ok-text="确定"
            cancel-text="取消"
            placement="top"
          >
            <a-button danger size="large" class="action-button delete-button">
              <template #icon>
                <DeleteOutlined />
              </template>
              删除应用
            </a-button>
          </a-popconfirm>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import UserInfo from './UserInfo.vue'
import { formatTime } from '@/utils/time'
import { formatCodeGenType } from '../utils/codeGenTypes.ts'

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const handleEdit = () => {
  emit('edit')
}

const handleDelete = () => {
  emit('delete')
}
</script>

<style scoped>
.app-detail-modal :deep(.ant-modal-content) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.app-detail-modal :deep(.ant-modal-header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: none;
  padding: 20px 24px;
}

.app-detail-modal :deep(.ant-modal-title) {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.app-detail-content {
  padding: 0;
}

.app-cover-section {
  width: 100%;
  height: 140px;
  overflow: hidden;
  position: relative;
}

.app-cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.app-cover-section:hover .app-cover-image {
  transform: scale(1.05);
}

.app-cover-placeholder {
  width: 100%;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.placeholder-icon {
  font-size: 2.5rem;
  opacity: 0.6;
}

.app-name-section {
  padding: 16px 20px 0;
  text-align: center;
}

.app-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.3;
}

.app-basic-info {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.info-item:hover {
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.info-icon {
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 2px;
}

.info-content {
  flex: 1;
}

.info-label {
  font-size: 11px;
  color: #666;
  font-weight: 600;
  margin-bottom: 2px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.info-value {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.app-id {
  font-family: 'Monaco', 'Consolas', monospace;
  background: #e9ecef;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.type-tag {
  margin-top: 4px;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 16px;
  font-weight: 500;
}

.app-actions {
  padding: 0 20px 20px;
  border-top: 1px solid #f0f0f0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.action-button {
  flex: 1;
  height: 42px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.edit-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.edit-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.delete-button {
  border: 2px solid #ff4d4f;
}

.delete-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}

/* 固定窗口大小 */
.app-detail-modal :deep(.ant-modal) {
  max-width: 420px;
  margin: 0 auto;
}

/* 响应式优化 */
@media (max-width: 480px) {
  .app-detail-modal :deep(.ant-modal) {
    width: 95% !important;
    max-width: 420px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .app-cover-section,
  .app-cover-placeholder {
    height: 120px;
  }

  .app-title {
    font-size: 16px;
  }

  .info-item {
    padding: 10px;
  }
}
</style>
