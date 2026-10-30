<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { createUsersApi } from '../lib/users'
const token = ref(sessionStorage.getItem('beauty-admin-token') || '')
const currentId = ref(sessionStorage.getItem('beauty-admin-id') || '')
const router = useRouter()
const rows = ref([]),
  page = ref(1),
  total = ref(0),
  busy = ref(false),
  saving = ref(false),
  error = ref(''),
  editing = ref(false)
const form = reactive({ id: '', realName: '', phone: '', email: '' })
function logout() {
  token.value = ''
  currentId.value = ''
  rows.value = []
  total.value = 0
  sessionStorage.removeItem('beauty-admin-token')
  sessionStorage.removeItem('beauty-admin-id')
  router.replace('/login')
}
const api = createUsersApi({ token: () => token.value, unauthorized: logout })
async function load() {
  busy.value = true
  error.value = ''
  try {
    const data = await api.page(page.value, 10)
    rows.value = data.records || []
    total.value = data.totalCount
  } catch (e) {
    rows.value = []
    total.value = 0
    error.value = e.message
  } finally {
    busy.value = false
  }
}
function edit(row) {
  Object.assign(form, {
    id: row.id,
    realName: row.realName || '',
    phone: row.phone || '',
    email: row.email || '',
  })
  editing.value = true
}
async function save() {
  saving.value = true
  try {
    await api.edit(form.id, {
      realName: form.realName.trim(),
      phone: form.phone.trim(),
      email: form.email.trim(),
    })
    editing.value = false
    ElMessage.success('用户信息已保存')
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}
async function disable(row) {
  try {
    await ElMessageBox.confirm(
      `确认禁用 ${row.realName || row.email}？禁用后该用户将无法登录。`,
      '禁用用户',
      { confirmButtonText: '确认禁用', cancelButtonText: '取消', type: 'warning' },
    )
  } catch {
    return
  }
  busy.value = true
  try {
    await api.disable(row.id)
    ElMessage.success('用户已禁用')
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    busy.value = false
  }
}
onMounted(() => {
  if (token.value) load()
})
</script>
<template>
  <section class="data-workspace" aria-label="用户管理">
    <p v-if="error" class="service-banner" role="alert">{{ error }}</p>
    <template v-if="token">
      <div class="table-caption">
        <div>
          <h2>用户列表</h2>
          <span>共 {{ total }} 位用户</span>
        </div>
        <div>
          <el-button :disabled="busy" @click="load">刷新</el-button
          ><el-button :disabled="busy" @click="logout">退出登录</el-button>
        </div>
      </div>
      <div :aria-busy="busy">
        <el-table
          :data="rows"
          class="admin-table"
          :empty-text="busy ? '正在加载用户…' : error ? '加载失败，请点击刷新重试' : '暂无用户'"
        >
          <el-table-column prop="id" label="用户编号" min-width="100" />
          <el-table-column label="姓名" min-width="120"
            ><template #default="{ row }">{{ row.realName || '未填写' }}</template></el-table-column
          >
          <el-table-column prop="email" label="邮箱" min-width="220" />
          <el-table-column label="联系电话" min-width="140"
            ><template #default="{ row }">{{ row.phone || '未填写' }}</template></el-table-column
          >
          <el-table-column label="状态" min-width="90"
            ><template #default="{ row }">{{
              Number(row.status) === 1 ? '正常' : '已禁用'
            }}</template></el-table-column
          >
          <el-table-column label="操作" min-width="160" fixed="right"
            ><template #default="{ row }"
              ><el-button link :disabled="busy" @click="edit(row)">编辑</el-button
              ><el-button
                link
                :disabled="busy || Number(row.status) !== 1 || String(row.id) === currentId"
                @click="disable(row)"
                >禁用</el-button
              ></template
            ></el-table-column
          >
        </el-table>
      </div>
      <div class="table-bottom">
        <p>每页 10 条</p>
        <el-pagination
          v-model:current-page="page"
          :page-size="10"
          :total="total"
          :disabled="busy"
          :pager-count="5"
          layout="prev, pager, next"
          @current-change="load"
        />
      </div>
    </template>
    <el-dialog
      v-model="editing"
      title="编辑用户"
      width="480px"
      :close-on-click-modal="false"
      :close-on-press-escape="!saving"
      :show-close="!saving"
    >
      <form class="user-edit" @submit.prevent="save">
        <label
          >姓名<input
            v-model="form.realName"
            required
            pattern=".*\S.*"
            maxlength="64"
            :disabled="saving"
        /></label>
        <label
          >邮箱<input v-model="form.email" required type="email" maxlength="254" :disabled="saving"
        /></label>
        <label
          >联系电话<input v-model="form.phone" type="tel" maxlength="20" :disabled="saving"
        /></label>
        <div>
          <el-button :disabled="saving" @click="editing = false">取消</el-button
          ><el-button type="primary" native-type="submit" :loading="saving">保存修改</el-button>
        </div>
      </form>
    </el-dialog>
  </section>
</template>
<style scoped>
.user-login {
  max-width: 440px;
  padding: 32px;
}
.user-login p {
  color: #626267;
  line-height: 1.7;
}
.user-login label,
.user-edit label {
  display: grid;
  gap: 10px;
  margin: 20px 0;
}
input {
  width: 100%;
  box-sizing: border-box;
  min-height: 44px;
  border: 1px solid #d6d6db;
  border-radius: 5px;
  padding: 10px 12px;
  font: inherit;
  background: white;
  color: #171717;
}
input:focus-visible {
  outline: 2px solid #171717;
  outline-offset: 2px;
}
</style>
