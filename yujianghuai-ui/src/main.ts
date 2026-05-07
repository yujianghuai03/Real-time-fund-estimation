import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from '@/App.vue'
import '@/styles/base.scss'
import '@/styles/theme.scss'

createApp(App).use(ElementPlus).mount('#app')
