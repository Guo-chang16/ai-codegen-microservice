<template>
  <a-modal
    v-model:open="visible"
    title="部署成功"
    :footer="null"
    width="500px"
    class="deploy-success-modal"
  >
    <div class="deploy-success">
      <div class="success-animation">
        <div class="success-icon">
          <CheckCircleOutlined />
        </div>
        <div class="success-pulse"></div>
      </div>

      <div class="success-content">
        <h3 class="success-title">🎉 部署成功！</h3>
        <p class="success-description">您的网站已成功上线，现在可以通过以下链接访问：</p>

        <div class="url-section">
          <div class="url-label">访问地址</div>
          <div class="deploy-url-container">
            <a-input :value="deployUrl" readonly class="url-input">
              <template #suffix>
                <a-tooltip title="复制链接">
                  <a-button type="text" @click="handleCopyUrl" class="copy-button">
                    <CopyOutlined />
                  </a-button>
                </a-tooltip>
              </template>
            </a-input>
          </div>
        </div>

        <div class="deploy-actions">
          <a-button
            type="primary"
            size="large"
            class="action-button primary"
            @click="handleOpenSite"
          >
            <RocketOutlined />
            立即访问
          </a-button>
          <a-button size="large" class="action-button secondary" @click="handleClose">
            <CloseOutlined />
            关闭
          </a-button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  CheckCircleOutlined,
  CopyOutlined,
  RocketOutlined,
  CloseOutlined,
} from '@ant-design/icons-vue'

interface Props {
  open: boolean
  deployUrl: string
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'open-site'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const handleCopyUrl = async () => {
  try {
    await navigator.clipboard.writeText(props.deployUrl)
    message.success('链接已复制到剪贴板')
  } catch (error) {
    console.error('复制失败：', error)
    message.error('复制失败')
  }
}

const handleOpenSite = () => {
  emit('open-site')
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.deploy-success-modal :deep(.ant-modal-content) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
}

.deploy-success-modal :deep(.ant-modal-header) {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border-bottom: none;
  padding: 24px;
  text-align: center;
}

.deploy-success-modal :deep(.ant-modal-title) {
  color: white;
  font-weight: 600;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.deploy-success {
  padding: 0;
}

.success-animation {
  position: relative;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
}

.success-icon {
  font-size: 48px;
  color: #52c41a;
  position: relative;
  z-index: 2;
  animation: bounce 1s ease infinite;
}

.success-pulse {
  position: absolute;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(82, 196, 26, 0.1);
  animation: pulse 2s ease-out infinite;
  z-index: 1;
}

.success-content {
  padding: 24px;
}

.success-title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  text-align: center;
}

.success-description {
  margin: 0 0 24px;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  text-align: center;
}

.url-section {
  margin-bottom: 32px;
}

.url-label {
  font-size: 12px;
  color: #888;
  font-weight: 600;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.deploy-url-container {
  position: relative;
}

.url-input {
  border-radius: 12px;
  border: 2px solid #f0f0f0;
  transition: all 0.3s ease;
}

.url-input:hover {
  border-color: #d9d9d9;
}

.copy-button {
  color: #1890ff !important;
  font-size: 16px !important;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.copy-button:hover {
  background: #f0f7ff;
  color: #40a9ff !important;
}

.deploy-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-button {
  flex: 1;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.action-button.primary {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.3);
}

.action-button.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.4);
}

.action-button.secondary {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  color: #333;
}

.action-button.secondary:hover {
  background: #ffffff;
  border-color: #bfbfbf;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

@keyframes bounce {
  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes pulse {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }
  100% {
    transform: scale(1.8);
    opacity: 0;
  }
}

/* 响应式优化 */
@media (max-width: 576px) {
  .deploy-success-modal :deep(.ant-modal) {
    width: 95% !important;
    max-width: 500px;
  }

  .deploy-success-modal :deep(.ant-modal-header) {
    padding: 20px;
  }

  .success-animation {
    height: 100px;
  }

  .success-icon {
    font-size: 40px;
  }

  .success-content {
    padding: 20px;
  }

  .success-title {
    font-size: 20px;
  }

  .deploy-actions {
    flex-direction: column;
    gap: 12px;
  }

  .action-button {
    width: 100%;
  }
}
</style>
