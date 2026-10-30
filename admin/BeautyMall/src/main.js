import { createApp } from 'vue'
import {
  ElButton,
  ElInput,
  ElIcon,
  ElTable,
  ElTableColumn,
  ElPagination,
  ElDialog,
  ElDrawer,
  ElTag,
  ElConfigProvider,
} from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'
const app = createApp(App)
for (const component of [
  ElButton,
  ElInput,
  ElIcon,
  ElTable,
  ElTableColumn,
  ElPagination,
  ElDialog,
  ElDrawer,
  ElTag,
  ElConfigProvider,
])
  app.use(component)
app.use(router).mount('#app')
