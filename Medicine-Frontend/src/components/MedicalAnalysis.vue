<template>
  <v-container fluid class="pa-0 fill-height analysis-container">
    <v-sheet class="navbar-area d-flex justify-space-between align-center px-6">
      <h1 class="logo">Medicine</h1>
      <div class="nav-links">
        <a href="#" @click.prevent="$emit('switch-mode', 'dashboard')">Ana Sayfa</a>
        <a href="#" class="ml-4">ATM Konumları</a>
        <a href="#" class="ml-4">Hakkında</a>
        <v-btn variant="outlined" size="small" class="logout-btn ml-6" @click="$emit('logout')">Çıkış Yap</v-btn>
      </div>
    </v-sheet>

    <v-row no-gutters justify="center" class="main-content-area">
      <v-col cols="12" class="d-flex flex-column align-center" style="padding-top: 50px;">

        <v-card flat class="info-header-card">
          <v-card-title class="headline-title">
            Yapay Zeka Semptom Analizi
          </v-card-title>
        </v-card>

        <v-card flat class="content-card">
          <v-row no-gutters class="fill-height">
            <!-- Sol Taraf: Hasta Bilgisi ve Şikayet Girişi -->
            <v-col cols="12" md="6" class="pa-6">
              <v-card class="input-card elevation-0" rounded="lg">
                <v-card-title class="sub-headline mb-4">Hasta Şikayeti</v-card-title>
                
                <v-alert v-if="patient" color="info" variant="tonal" class="mb-4" density="compact">
                  <strong>Hasta:</strong> {{ patient.name }} {{ patient.surname }}
                </v-alert>

                <v-textarea
                    v-model="complaint"
                    label="Hastanın şikayetlerini buraya giriniz..."
                    variant="outlined"
                    auto-grow
                    rows="5"
                    hide-details
                    class="mb-4"
                ></v-textarea>

                <v-btn
                    color="#252B61"
                    block
                    size="large"
                    class="text-white"
                    @click="analyzeComplaint"
                    :loading="loading"
                    :disabled="!complaint"
                >
                  <v-icon start>mdi-brain</v-icon>
                  Analiz Et
                </v-btn>
              </v-card>
            </v-col>

            <!-- Sağ Taraf: Analiz Sonucu -->
            <v-col cols="12" md="6" class="pa-6">
              <v-card class="result-card elevation-0" rounded="lg">
                <v-card-title class="sub-headline mb-4">Analiz Sonucu</v-card-title>

                <v-skeleton-loader v-if="loading" type="article, paragraph"></v-skeleton-loader>

                <div v-else-if="analysisResult" class="analysis-result fade-in">
                  <v-sheet border rounded="lg" class="pa-4 bg-grey-lighten-4 mb-4">
                    <pre class="result-text">{{ analysisResult }}</pre>
                  </v-sheet>
                  
                  <v-btn
                      color="success"
                      block
                      size="large"
                      class="text-white"
                      @click="proceedToMedicineSelection"
                  >
                    İlaç Seçimine Devam Et
                    <v-icon end>mdi-arrow-right</v-icon>
                  </v-btn>
                </div>

                <div v-else class="text-center text-medium-emphasis py-12">
                  <v-icon size="64" color="grey lighten-2">mdi-text-box-search-outline</v-icon>
                  <p class="mt-4">Analiz sonucu burada görüntülenecektir.</p>
                </div>
              </v-card>
            </v-col>
          </v-row>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const props = defineProps({
  user: Object,
  patient: Object
});

const emit = defineEmits(['switch-mode', 'logout', 'analysis-complete']);

const complaint = ref('');
const analysisResult = ref('');
const loading = ref(false);
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const analyzeComplaint = async () => {
  if (!complaint.value) return;
  
  loading.value = true;
  analysisResult.value = '';

  try {
    const response = await axios.post(`${API_BASE_URL}/api/medical-analysis`, complaint.value, {
      headers: {
        'Content-Type': 'text/plain'
      }
    });
    analysisResult.value = response.data;
  } catch (error) {
    console.error("Analiz hatası:", error);
    analysisResult.value = "Analiz sırasında bir hata oluştu: " + (error.response?.data || error.message);
  } finally {
    loading.value = false;
  }
};

const proceedToMedicineSelection = () => {
  emit('analysis-complete', props.patient);
};
</script>

<style scoped>
.analysis-container {
  background-color: #ECEEF9;
  min-height: 100vh;
  min-width: 100vh;
  padding-top: 20px;
}

.navbar-area {
  background-color: transparent !important;
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 1rem 0;
  z-index: 10;
}

.logo {
  font-size: 1.8em;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.nav-links a {
  text-decoration: none;
  color: #555;
  font-weight: 500;
  transition: color 0.3s ease;
}

.nav-links a:hover {
  color: #252B61;
}

.logout-btn {
  border: 1px solid #d6a2b8 !important;
  color: #d6a2b8 !important;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #d6a2b8 !important;
  color: white !important;
}

.main-content-area {
  position: relative;
  width: 100%;
  max-width: 80%;
  margin: 0 auto;
  left: 10%;
}

.info-header-card {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  margin: 0 auto;
  max-width: 100%;
  background-color: #252B61 !important;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(37, 43, 97, 0.2);
  z-index: 1;
}

.headline-title {
  position: relative;
  z-index: 2;
  color: #fff;
  font-size: 2.2em;
  font-weight: 300;
  padding: 40px;
  letter-spacing: 0.5px;
}

.content-card {
  position: relative;
  z-index: 3;
  width: 100%;
  margin-top: 100px;
  background-color: transparent !important;
  box-shadow: none !important;
  min-height: 500px;
}

.input-card, .result-card {
  background-color: white;
  padding: 30px;
  height: 100%;
  border-radius: 20px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  min-height: 600px;
}

.sub-headline {
  font-size: 1.5em;
  font-weight: 600;
  color: #333;
}

.result-text {
  white-space: pre-wrap;
  font-family: inherit;
  color: #333;
  font-size: 1rem;
  line-height: 1.6;
}

.fade-in {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 960px) {
  .headline-title {
    font-size: 1.8em;
    padding: 30px 20px;
  }
  .content-card {
    width: 98%;
  }
  .navbar-area {
    flex-direction: column;
    gap: 1rem;
  }
}
</style>
