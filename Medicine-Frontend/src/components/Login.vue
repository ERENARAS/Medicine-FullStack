<template>
  <div class="login-container">
    <div class="form-section">
      <h2 class="main-title">Medicine</h2>
      <h3 class="welcome-text">Tekrardan Hoşgeldin!</h3>

      <form @submit.prevent="loginUser" class="login-form">

        <label for="email" class="input-label">Email</label>
        <input type="email" id="email" v-model="form.email" required class="form-input email-input" placeholder="email" />

        <div class="password-group">
          <label for="password" class="input-label">Şifre</label>
          <a href="#" class="forgot-password">Şifremi unuttum</a>
        </div>
        <input type="password" id="password" v-model="form.password" required class="form-input" placeholder="şifre" />

        <button type="submit" :disabled="isLoading" class="login-button">
          {{ isLoading ? 'Giriş Yapılıyor...' : 'Giriş' }}
        </button>
      </form>

      <p class="signup-link">
        Hesabın yok mu? <a href="#" @click.prevent="$emit('switch-mode', 'signup')" class="link-style">Kayıt Ol</a>
      </p>

      <p v-if="successMessage" class="message success">{{ successMessage }}</p>
      <p v-if="errorMessage" class="message error">{{ errorMessage }}</p>
    </div>

    <div class="visual-section">
      <img src="/signup_img.png" alt="Kayıt Sayfası Görseli" class="signup-image" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const emit = defineEmits(['switch-mode']);

const form = ref({
  email: '',
  password: '',
});

const isLoading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

// 💡 YENİ: API URL'ini ortam değişkeninden al.
// Eğer Docker Build sırasında bu değişken gelmezse, eski adresi yedek olarak kullanır.
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Giriş işlemini yürüten fonksiyon
const loginUser = async () => {
  if (!form.value.email || !form.value.password) {
    errorMessage.value = 'Lütfen e-posta ve şifrenizi girin.';
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    // 💡 GÜNCELLENDİ: API_BASE_URL değişkenini kullan
    const response = await axios.post(`${API_BASE_URL}/api/auth/login`, {
      email: form.value.email,
      password: form.value.password,
    });

    console.log("Giriş Başarılı. Kullanıcı Bilgisi:", response.data);
    successMessage.value = `Giriş başarılı! Hoş geldiniz: ${response.data.name}`;
    errorMessage.value = '';

    form.value.email = '';
    form.value.password = '';

  } catch (error) {
    if (error.response && error.response.data) {
      errorMessage.value = error.response.data;
    } else {
      // Daha anlaşılır bir hata mesajı ekleyelim
      errorMessage.value = `API bağlantı hatası oluştu. Lütfen Backend (8080) ve DB'nin çalıştığını kontrol edin. Detay: ${error.message}`;
    }
    successMessage.value = '';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* Genel Yapı ve Box Stilleri */
.login-container {
  display: flex;
  width: 1000px;
  height: 600px;
  margin: 50px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: white;
}
.form-section {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
}
/* Görsel Bölüm - Yeni desen */
.visual-section {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.signup-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  display: block;
}

/* Başlıklar ve Metinler */
.main-title {
  font-size: 2em;
  font-weight: 500;
  margin-top: 0;
  color: #333;
}
.welcome-text {
  font-size: 2.2em;
  font-weight: 300;
  margin-top: 50px; /* Görseldeki büyük boşluk */
  margin-bottom: 50px;
  color: #333;
}

/* Form ve Öğeleri */
.login-form {
  display: flex;
  flex-direction: column;
}

.input-label {
  font-size: 0.9em;
  color: #666;
  margin-top: 5px;
  /* padding-left: 5px; */
}
.form-input {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1em;
  margin-bottom: 25px;
}
.form-input.email-input {
  margin-bottom: 5px;
}


.password-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 25px;
}
.forgot-password {
  font-size: 0.8em;
  color: #d6a2b8;
  text-decoration: none;
}
.forgot-password:hover {
  text-decoration: underline;
}


.login-button {
  padding: 10px 20px;
  background-color: #a2d6b8;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1em;
  transition: background-color 0.3s;
  margin-top: 20px;
}
.login-button:hover:not(:disabled) {
  background-color: #87c39f;
}
.login-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  color: #888;
}


.signup-link {
  margin-top: 30px;
  font-size: 0.9em;
  color: #666;
}
.link-style {
  color: #d6a2b8;
  text-decoration: none;
  font-weight: bold;
}


.message {
  padding: 10px;
  border-radius: 4px;
  margin-top: 15px;
  font-size: 0.9em;
}
.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}
.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6fb;
}
</style>