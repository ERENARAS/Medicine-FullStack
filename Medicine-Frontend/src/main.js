import { createApp } from 'vue'
import App from './App.vue'

// Vuetify kurulumu için gerekli importlar
import 'vuetify/styles' // Vuetify CSS'i
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
// Material Design Icons
import '@mdi/font/css/materialdesignicons.css'

// Vue Router ve Pinia
import router from './router'
import { createPinia } from 'pinia'

const vuetify = createVuetify({
    components,
    directives,
    icons: {
        defaultSet: 'mdi',
    },
})

const pinia = createPinia()

createApp(App)
    .use(vuetify)
    .use(pinia)
    .use(router)
    .mount('#app')