<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElPagination } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getCatalogPage } from '../lib/catalog'
const route = useRoute(),
  router = useRouter()
const keyword = computed(() => (typeof route.query.q === 'string' ? route.query.q : ''))
const search = ref(keyword.value)
watch(keyword, (value) => {
  search.value = value
})
const result = computed(() => getCatalogPage(keyword.value, route.query.page, route.query.size))
function navigate(page = 1, size = result.value.pageSize, q = keyword.value) {
  router.push({
    name: 'products',
    query: {
      ...(q.trim() ? { q: q.trim() } : {}),
      ...(page > 1 ? { page: String(page) } : {}),
      ...(size !== 8 ? { size: String(size) } : {}),
    },
  })
}
function submitSearch() {
  navigate(1, result.value.pageSize, search.value)
}
function resetSearch() {
  search.value = ''
  navigate(1, result.value.pageSize, '')
}
</script>
<template>
  <div class="catalog-page section-wrap">
    <div class="catalog-heading">
      <div>
        <p class="section-label">发现美妆好物</p>
        <h1>全部商品</h1>
        <p>从日常护理到色彩表达，寻找适合自己的选择。</p>
      </div>
      <form
        class="catalog-search"
        role="search"
        aria-label="搜索商品"
        @submit.prevent="submitSearch"
      >
        <el-input
          v-model="search"
          aria-label="商品搜索关键词"
          placeholder="搜索商品名称、分类"
          :prefix-icon="Search"
          clearable
          maxlength="100"
          @clear="resetSearch"
          size="large"
        />
        <el-button type="primary" native-type="submit" size="large">搜索</el-button>
      </form>
    </div>
    <p class="catalog-demo-note">
      商品服务准备中 · 当前展示 24 件示例商品，仅用于预览搜索与分页，图片为统一示意图。
    </p>
    <div class="catalog-toolbar">
      <p role="status" aria-live="polite">
        {{ keyword ? `“${keyword}”的搜索结果` : '浏览全部商品' }}
        <span>{{ result.total }} 件商品</span>
      </p>
      <label class="catalog-size"
        >每页显示<select
          aria-label="每页商品数量"
          :value="result.pageSize"
          @change="navigate(1, Number($event.target.value))"
        >
          <option :value="8">8 件</option>
          <option :value="12">12 件</option>
          <option :value="24">24 件</option>
        </select></label
      >
    </div>
    <div v-if="result.total" class="product-grid">
      <article v-for="item in result.items" :key="item.id" class="product-item">
        <div class="product-image">
          <img
            :src="item.image"
            alt="美妆产品示意图，非该商品实拍"
            :style="{ objectPosition: item.imagePosition }"
            width="1536"
            height="1024"
            loading="lazy"
          /><span>示例商品</span>
        </div>
        <p class="product-category">{{ item.category }}</p>
        <h2>{{ item.name }}</h2>
        <p class="product-caption">产品详情即将开放</p>
      </article>
    </div>
    <div v-else class="catalog-empty">
      <el-icon aria-hidden="true"><Search /></el-icon>
      <h2>没有找到相关商品</h2>
      <p>试试“精华”“彩妆”等关键词，或查看全部商品。</p>
      <el-button @click="resetSearch">查看全部商品</el-button>
    </div>
    <div class="catalog-pagination" v-if="result.total">
      <span>第 {{ result.page }} 页 / 共 {{ Math.ceil(result.total / result.pageSize) }} 页</span
      ><ElPagination
        :current-page="result.page"
        :page-size="result.pageSize"
        :total="result.total"
        :pager-count="5"
        layout="prev, pager, next"
        prev-text="上一页"
        next-text="下一页"
        @current-change="navigate($event)"
      />
    </div>
  </div>
</template>
