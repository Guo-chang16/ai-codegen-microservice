<template>
  <div class="user-info" :class="{ 'user-info--compact': size === 'small' }">
    <a-avatar :src="user?.userAvatar" :size="size" class="user-avatar">
      <template #default>
        {{ user?.userName?.charAt(0)?.toUpperCase() || 'U' }}
      </template>
    </a-avatar>

    <div v-if="showName" class="user-details">
      <span class="user-name">{{ user?.userName || '未知用户' }}</span>
      <span v-if="user?.userProfile" class="user-profile">{{ user.userProfile }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  user?: API.UserVO
  size?: number | 'small' | 'default' | 'large'
  showName?: boolean
}

withDefaults(defineProps<Props>(), {
  size: 'default',
  showName: true,
})
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s ease;
}

.user-info--compact {
  gap: 8px;
}

.user-avatar {
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.user-details {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-profile {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-info:hover .user-name {
  color: #1890ff;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .user-info {
    gap: 8px;
  }

  .user-name {
    font-size: 13px;
  }

  .user-profile {
    font-size: 11px;
  }
}
</style>
