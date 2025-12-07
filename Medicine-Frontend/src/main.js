import { createApp } from 'vue'
import App from './App.vue'

// Vuetify kurulumu için gerekli importlar
import 'vuetify/styles' // Vuetify CSS'i
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
// Material Design Icons
import '@mdi/font/css/materialdesignicons.css'

const vuetify = createVuetify({
    components,
    directives,
    icons: {
        defaultSet: 'mdi',
    },
})

// createApp yerine createApp(App).use(vuetify).mount('#app') kullanın.
createApp(App).use(vuetify).mount('#app')