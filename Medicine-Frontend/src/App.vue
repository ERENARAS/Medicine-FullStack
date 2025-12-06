<template>
  <div id="app">
    <Signup v-if="currentMode === 'signup'" @switch-mode="switchMode" />

    <Login v-else-if="currentMode === 'login'" @switch-mode="switchMode" @login-success="handleLoginSuccess" />

    <DoctorDashboard v-else-if="currentUser.role === 'doctor'" :user="currentUser" @logout="handleLogout" />
    <div v-else>
      <h2>Hoş Geldiniz, {{ currentUser.name }}</h2>
      <p>Bu rol için henüz bir ana sayfa tasarlanmamıştır: {{ currentUser.role }}</p>
      <button @click="handleLogout">Çıkış Yap</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Signup from './components/Signup.vue';
import Login from './components/Login.vue';
import DoctorDashboard from './components/DoctorDashboard.vue';

// currentMode: 'login', 'signup', 'dashboard' vb.
const currentMode = ref('login');
const currentUser = ref({ role: null, name: null, email: null }); // Giriş yapan kullanıcının bilgisi

const switchMode = (mode) => {
  currentMode.value = mode;
  console.log(`Mod değiştirildi: ${mode}`);
};

// Login.vue'dan gelen başarılı giriş bilgisini yakalar
const handleLoginSuccess = (user) => {
  currentUser.value.name = user.name;
  currentUser.value.email = user.email;

  // E-posta uzantısına göre rol belirleme (Domain'deki mantığı taklit ederiz)
  if (user.email.endsWith('@dr.medicine')) {
    currentUser.value.role = 'doctor';
  } else if (user.email.endsWith('@pt.medicine')) {
    currentUser.value.role = 'patient';
  } else if (user.email.endsWith('@ph.medicine')) {
    currentUser.value.role = 'pharmacy';
  }

  // Rol doktor ise dashboard'a yönlendir
  if (currentUser.value.role === 'doctor') {
    currentMode.value = 'dashboard';
    console.log(`Giriş Başarılı. Rol: ${currentUser.value.role}`);
  } else {
    // Diğer roller için şimdilik bir placeholder göster
    currentMode.value = 'other';
  }
};

// Çıkış yapma işlemi
const handleLogout = () => {
  currentUser.value = { role: null, name: null, email: null };
  currentMode.value = 'login';
  console.log('Çıkış yapıldı.');
};
</script>

<style>
/* ... (Mevcut stiller) */
body {
  background-color: #f0f0f0;
  font-family: Arial, sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  margin: 0;
}
</style>