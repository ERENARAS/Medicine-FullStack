<template>
  <div class="signup-container">
    <div class="form-section">
      <h2>Medicine</h2>

      <form @submit.prevent="registerUser" class="signup-form">
        <label for="name">İsim Soyisim</label>
        <input type="text" id="name" v-model="form.name" required />

        <label for="email">Email adresi</label>
        <input type="email" id="email" v-model="form.email" required />

        <label for="password">Şifre</label>
        <input type="password" id="password" v-model="form.password" required />

        <div class="checkbox-group">
          <input type="checkbox" id="terms" v-model="form.acceptTerms" required />
          <label for="terms" class="small-label">I agree to the terms & policy</label>
        </div>

        <button type="submit" :disabled="!form.acceptTerms || isLoading">
          {{ isLoading ? 'Kaydediliyor...' : 'Kayıt Ol' }}
        </button>
      </form>

      <p class="login-link">
        Daha önce hesabın var mı? <a href="#" @click.prevent="$emit('switch-mode', 'login')">Giriş Yap</a>
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

// Form verileri
const form = ref({
  name: '',
  email: '',
  password: '',
  acceptTerms: false,
});

// Arayüz durumu
const isLoading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');


const registerUser = async () => {
  // Alanlar boşsa veya şartlar kabul edilmemişse durdur
  if (!form.value.name || !form.value.email || !form.value.password || !form.value.acceptTerms) {
    errorMessage.value = 'Lütfen tüm alanları doldurun ve şartları kabul edin.';
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {

    const response = await axios.post('http://localhost:8080/api/auth/register', {
      name: form.value.name,
      email: form.value.email,
      password: form.value.password,
    });

    successMessage.value = response.data;

    form.value.name = '';
    form.value.email = '';
    form.value.password = '';
    form.value.acceptTerms = false;

  } catch (error) {

    if (error.response && error.response.data) {
      errorMessage.value = error.response.data;
    } else {
      errorMessage.value = 'API bağlantı hatası veya bilinmeyen bir hata oluştu.';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>

.signup-container {
  width: 1000px;
  height: 600px;
  margin: 50px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: white;
  display: flex;
}
.form-section {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
}
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
h1 {
  font-size: 2.5em;
  margin-bottom: 5px;
  font-weight: 300;
  color: #333;
}
h2 {
  font-size: 2em;
  margin-top: 0;
  margin-bottom: 30px;
  color: #555;
  font-weight: 400;
}
.signup-form {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
label {
  margin-top: 15px;
  font-size: 0.9em;
  color: #666;
}
input[type="text"], input[type="email"], input[type="password"] {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1em;
}
.checkbox-group {
  display: flex;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 20px;
}
.small-label {
  font-size: 0.8em;
  margin-top: 0;
  margin-left: 5px;
}
button {
  padding: 10px 20px;
  background-color: #a2d6b8;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1em;
  transition: background-color 0.3s;
}
button:hover:not(:disabled) {
  background-color: #87c39f;
}
button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
.login-link {
  margin-top: 20px;
  font-size: 0.9em;
  color: #666;
}
.login-link a {
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
  border: 1px solid #f5c6cb;
}
</style>
