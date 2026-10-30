import { createApp } from 'vue'
import { ElAlert, ElAvatar, ElButton, ElForm, ElFormItem, ElIcon, ElInput } from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'
const app = createApp(App)
for (const component of [ElAlert, ElAvatar, ElButton, ElForm, ElFormItem, ElIcon, ElInput])
  app.use(component)
app.use(router).mount('#app')
