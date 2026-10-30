<script setup>
import { computed, ref, watch } from 'vue'
import { Search, RefreshRight, InfoFilled } from '@element-plus/icons-vue'
import { filterRows, paginateRows } from '../lib/table'
const props = defineProps({
  kind: { type: String, required: true },
  rows: { type: Array, required: true },
})
const isProduct = computed(() => props.kind === 'products')
const draft = ref(''),
  keyword = ref(''),
  status = ref(''),
  category = ref(''),
  page = ref(1),
  detail = ref(null)
const statuses = computed(() =>
  isProduct.value ? ['已上架', '待上架', '已下架'] : ['启用', '停用'],
)
const categories = computed(() => [...new Set(props.rows.map((row) => row.category))])
const filtered = computed(() =>
  filterRows(props.rows, keyword.value, {
    status: status.value,
    ...(isProduct.value ? { category: category.value } : {}),
  }),
)
const result = computed(() => paginateRows(filtered.value, page.value, 6))
watch([keyword, status, category], () => {
  page.value = 1
})
function reset() {
  draft.value = ''
  keyword.value = ''
  status.value = ''
  category.value = ''
  page.value = 1
}
</script>
<template>
  <section class="data-workspace" :aria-label="isProduct ? '商品列表' : '分类列表'">
    <div class="demo-banner">
      <el-icon><InfoFilled /></el-icon>
      <p>静态预览 · 以下为示例数据，筛选与详情可体验，暂不保存到服务器。</p>
    </div>
    <form class="filter-bar" @submit.prevent="keyword = draft">
      <label class="search-field"
        ><span>{{ isProduct ? '商品搜索' : '分类搜索' }}</span
        ><el-input
          v-model="draft"
          :aria-label="isProduct ? '商品搜索' : '分类搜索'"
          :prefix-icon="Search"
          :placeholder="isProduct ? '搜索商品名称或编号' : '搜索分类名称或编号'"
          clearable
          @clear="keyword = ''" /></label
      ><label v-if="isProduct" class="select-field"
        ><span>商品分类</span
        ><select v-model="category" aria-label="商品分类">
          <option value="">全部分类</option>
          <option v-for="value in categories" :key="value">{{ value }}</option>
        </select></label
      ><label class="select-field"
        ><span>{{ isProduct ? '上架状态' : '分类状态' }}</span
        ><select v-model="status" :aria-label="isProduct ? '上架状态' : '分类状态'">
          <option value="">全部状态</option>
          <option v-for="value in statuses" :key="value">{{ value }}</option>
        </select></label
      >
      <div class="filter-actions">
        <el-button native-type="submit" type="primary">查询</el-button
        ><el-button :icon="RefreshRight" @click="reset">重置</el-button>
      </div>
    </form>
    <div class="table-caption">
      <div>
        <h2>{{ isProduct ? '商品列表' : '分类列表' }}</h2>
        <span role="status">共 {{ filtered.length }} 条示例记录</span>
      </div>
      <span class="read-only-label">预览模式</span>
    </div>
    <div class="table-scroll" tabindex="0" aria-label="数据表格，可横向滚动">
      <el-table
        :data="result.items"
        empty-text="未找到匹配记录，请调整筛选条件"
        row-key="id"
        class="admin-table"
        ><el-table-column label="编号" prop="id" width="100" /><el-table-column
          :label="isProduct ? '商品信息' : '分类名称'"
          min-width="230"
          ><template #default="{ row }"
            ><div class="table-product">
              <img v-if="isProduct" :src="row.image" alt="示例商品示意图" width="46" height="46" />
              <div>
                <strong>{{ row.name }}</strong
                ><small>{{ isProduct ? '示例商品' : row.description }}</small>
              </div>
            </div></template
          ></el-table-column
        ><el-table-column
          v-if="isProduct"
          label="分类"
          prop="category"
          min-width="100"
        /><el-table-column v-if="isProduct" label="售价" min-width="100"
          ><template #default="{ row }">¥ {{ row.price }}</template></el-table-column
        ><el-table-column
          v-if="isProduct"
          label="库存"
          prop="stock"
          min-width="80"
        /><el-table-column v-else label="商品数量" prop="count" min-width="90" /><el-table-column
          v-if="!isProduct"
          label="排序"
          prop="sort"
          min-width="70"
        /><el-table-column label="状态" min-width="100"
          ><template #default="{ row }"
            ><span
              class="status-tag"
              :class="{
                'status-positive': ['已上架', '启用'].includes(row.status),
                'status-pending': row.status === '待上架',
              }"
              ><i></i>{{ row.status }}</span
            ></template
          ></el-table-column
        ><el-table-column label="更新日期" prop="updated" min-width="115" /><el-table-column
          label="操作"
          width="90"
          fixed="right"
          ><template #default="{ row }"
            ><el-button
              link
              class="detail-action"
              :aria-label="`查看${row.name}详情`"
              @click="detail = row"
              >详情</el-button
            ></template
          ></el-table-column
        ></el-table
      >
    </div>
    <div class="table-bottom">
      <p>每页 6 条 · 当前第 {{ result.page }} 页</p>
      <el-pagination
        :total="filtered.length"
        :page-size="6"
        :current-page="result.page"
        :pager-count="5"
        layout="prev,pager,next"
        @current-change="page = $event"
      />
    </div>
  </section>
  <el-dialog
    :model-value="Boolean(detail)"
    :title="isProduct ? '商品详情' : '分类详情'"
    width="520px"
    @update:model-value="
      (value) => {
        if (!value) detail = null
      }
    "
    ><template v-if="detail"
      ><div class="detail-banner">示例数据，仅供界面预览</div>
      <img v-if="isProduct" class="detail-image" :src="detail.image" alt="商品示意图" />
      <dl class="detail-list">
        <dt>名称</dt>
        <dd>{{ detail.name }}</dd>
        <dt>编号</dt>
        <dd>{{ detail.id }}</dd>
        <dt>状态</dt>
        <dd>{{ detail.status }}</dd>
        <template v-if="isProduct"
          ><dt>分类</dt>
          <dd>{{ detail.category }}</dd>
          <dt>示例售价</dt>
          <dd>¥ {{ detail.price }}</dd>
          <dt>示例库存</dt>
          <dd>{{ detail.stock }}</dd></template
        ><template v-else
          ><dt>说明</dt>
          <dd>{{ detail.description }}</dd>
          <dt>排序</dt>
          <dd>{{ detail.sort }}</dd></template
        >
      </dl></template
    ><template #footer><el-button @click="detail = null">关闭</el-button></template></el-dialog
  >
</template>
