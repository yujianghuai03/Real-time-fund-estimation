import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles.css'

document.title = '基金实时预估V1.0'

createApp(App).use(router).use(ElementPlus).mount('#app')
