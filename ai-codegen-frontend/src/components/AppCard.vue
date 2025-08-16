<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <div class="app-image-container">
        <img
          v-if="app.cover"
          :src="app.cover"
          :alt="app.appName"
          @error="handleImageError"
          class="app-image"
        />
        <div v-else class="app-placeholder">
          <div class="placeholder-icon">🤖</div>
        </div>
      </div>

      <div class="app-overlay">
        <div class="overlay-content">
          <div v-if="featured" class="featured-tag">🌟 精选</div>
          <div class="overlay-buttons">
            <button class="action-button primary" @click.stop="handleViewChat">
              <span class="button-text">对话</span>
            </button>
            <button
              v-if="app.deployKey && app.deployKey.trim()"
              class="action-button secondary"
              @click.stop="handleViewWork"
            >
              <span class="button-text">作品</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="app-info">
      <div class="app-meta">
        <div class="app-header">
          <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        </div>

        <div class="app-author-info">
          <div class="author-avatar">
            <img
              v-if="app.user?.userAvatar"
              :src="app.user.userAvatar"
              :alt="app.user.userName"
              class="avatar-img"
            />
            <div v-else class="avatar-placeholder">
              {{ app.user?.userName?.charAt(0)?.toUpperCase() || 'U' }}
            </div>
          </div>
          <div class="author-details">
            <span class="author-name">{{
              app.user?.userName || (featured ? '官方' : '未知用户')
            }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const handleViewChat = () => {
  if (props.app.id) {
    emit('view-chat', props.app.id)
  }
}

const handleViewWork = () => {
  emit('view-work', props.app)
}

const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.style.display = 'none'
}
</script>

<style scoped>
.app-card {
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 10px 30px rgba(0, 0, 0, 0.08),
    0 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.app-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow:
    0 20px 50px rgba(0, 0, 0, 0.15),
    0 5px 15px rgba(0, 0, 0, 0.1);
}

.app-preview {
  height: 200px;
  position: relative;
  overflow: hidden;
}

.app-image-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.app-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.4s ease;
}

.app-card:hover .app-image {
  transform: scale(1.05);
  filter: brightness(0.8);
}

.app-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.placeholder-icon {
  font-size: 3rem;
  opacity: 0.6;
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, transparent 60%);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 20px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.overlay-buttons {
  display: flex;
  gap: 12px;
  transform: translateY(20px);
  transition: transform 0.3s ease;
}

.app-card:hover .overlay-buttons {
  transform: translateY(0);
}

.action-button {
  padding: 10px 20px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.action-button.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.action-button.secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

.action-button:active {
  transform: translateY(0);
}

.app-info {
  padding: 20px;
}

.app-meta {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.app-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: #1a1a1a;
  flex: 1;
  line-height: 1.3;
}

.featured-badge {
  background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 12px;
  white-space: nowrap;
}

.app-author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  flex-shrink: 0;
}

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(0, 0, 0, 0.05);
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.author-details {
  flex: 1;
  min-width: 0;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .app-preview {
    height: 160px;
  }

  .app-title {
    font-size: 16px;
  }

  .action-button {
    padding: 8px 16px;
    font-size: 13px;
  }

  .app-info {
    padding: 16px;
  }
}
</style>
