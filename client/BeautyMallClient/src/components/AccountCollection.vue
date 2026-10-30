<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, Star, Tickets } from '@element-plus/icons-vue'
const props = defineProps({ section: { type: String, required: true } })
const route = useRoute()
const router = useRouter()
const filters = [
  { key: 'all', label: '全部订单' },
  { key: 'pending-payment', label: '待付款' },
  { key: 'pending-shipment', label: '待发货' },
  { key: 'pending-review', label: '待评价' },
  { key: 'refund', label: '退款' },
  { key: 'after-sales', label: '售后' },
]
const current = computed(
  () => filters.find((item) => item.key === route.query.status) || filters[0],
)
const content = computed(
  () =>
    ({
      addresses: {
        icon: Location,
        title: '收货地址服务准备中',
        description: '地址管理开放后，你可以在这里新增、编辑收货地址，并设置默认地址。',
      },
      favorites: {
        icon: Star,
        title: '收藏服务准备中',
        description: '收藏功能开放后，你喜欢的美妆产品将在这里集中展示。',
      },
      orders: {
        icon: Tickets,
        title: '订单服务准备中',
        description: '订单功能开放后，你可以在这里查看订单进度，处理付款、评价、退款与售后。',
      },
    })[props.section],
)
function selectStatus(status) {
  router.replace({
    name: 'profile',
    query: { tab: 'orders', ...(status === 'all' ? {} : { status }) },
  })
}
</script>
<template>
  <div class="account-collection">
    <div v-if="section === 'orders'" class="order-filters" role="group" aria-label="订单状态筛选">
      <button
        v-for="item in filters"
        :key="item.key"
        :aria-pressed="current.key === item.key"
        :class="{ selected: current.key === item.key }"
        @click="selectStatus(item.key)"
      >
        {{ item.label }}
      </button>
    </div>
    <div class="collection-empty" role="status" aria-live="polite">
      <el-icon class="collection-empty-icon" aria-hidden="true"
        ><component :is="content.icon"
      /></el-icon>
      <p v-if="section === 'orders'" class="collection-context">{{ current.label }}</p>
      <h3>{{ content.title }}</h3>
      <p>{{ content.description }}</p>
      <router-link class="text-link" to="/#discover">探索美妆灵感</router-link>
    </div>
  </div>
</template>
