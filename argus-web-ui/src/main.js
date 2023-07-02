import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

const pinia = createPinia()

createApp(App)
    .use(pinia)
    .use(router)
    .mount('#app')
