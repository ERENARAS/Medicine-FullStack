<template>
  <v-container fluid class="pa-0 fill-height write-prescription-container">
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
            Hasta Bilgileri
          </v-card-title>
        </v-card>

        <v-card flat class="content-card">
          <v-row no-gutters class="fill-height">
            <v-col cols="12" md="6" class="pa-6">
              <v-card class="patient-list-card elevation-0" rounded="lg">
                <div class="d-flex justify-space-between align-center mb-4">
                  <v-card-title class="sub-headline pa-0">Sıradaki Hastalar</v-card-title>
                  <v-btn
                      icon
                      size="small"
                      variant="text"
                      @click="fetchPatients"
                      :loading="isLoading"
                  >
                    <v-icon>mdi-refresh</v-icon>
                  </v-btn>
                </div>

                <v-alert v-if="errorFetchingPatients" type="error" density="compact" closable @click:close="errorFetchingPatients = null">
                  {{ errorFetchingPatients }}
                </v-alert>

                <v-skeleton-loader v-if="isLoading" type="table-row-divider@4"></v-skeleton-loader>

                <v-table v-else class="mt-4 transparent-table" hover>
                  <thead>
                  <tr>
                    <th class="text-left font-weight-bold text-caption">Adı</th>
                    <th class="text-left font-weight-bold text-caption">Soyadı</th>
                    <th class="text-left font-weight-bold text-caption">Yaş</th>
                    <th class="text-left font-weight-bold text-caption">Tarih</th>
                    <th class="text-center font-weight-bold text-caption">İşlem</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr
                      v-for="patient in patients"
                      :key="patient.email"
                      :class="{'selected-row': patient.email === form.email}"
                      class="patient-row"
                  >
                    <td>{{ getFirstName(patient.name) }}</td>
                    <td>{{ getLastName(patient.name) }}</td>
                    <td>{{ patient.age || calculateAge() }}</td>
                    <td>{{ formatDate(patient.date) }}</td>
                    <td class="text-center">
                      <v-btn
                          icon
                          size="x-small"
                          variant="tonal"
                          color="primary"
                          @click="selectPatient(patient)"
                      >
                        <v-icon size="small">mdi-arrow-right</v-icon>
                      </v-btn>
                    </td>
                  </tr>
                  <tr v-if="patients.length === 0 && !errorFetchingPatients && !isLoading">
                    <td colspan="5" class="text-center text-medium-emphasis py-8">Kayıtlı hasta bulunamadı.</td>
                  </tr>
                  </tbody>
                </v-table>
              </v-card>
            </v-col>

            <v-col cols="12" md="6" class="pa-6">
              <v-card class="write-form-card elevation-0" rounded="lg">
                <v-card-title class="sub-headline">Reçete Yaz</v-card-title>

                <v-form ref="prescriptionForm" @submit.prevent="handleContinue">
                  <v-text-field
                      v-model="form.name"
                      label="Hasta Adı"
                      variant="outlined"
                      density="compact"
                      :rules="[rules.required]"
                      class="mt-4"
                  ></v-text-field>

                  <v-text-field
                      v-model="form.surname"
                      label="Hasta Soyadı"
                      variant="outlined"
                      density="compact"
                      :rules="[rules.required]"
                      class="mt-6"
                  ></v-text-field>

                  <v-text-field
                      v-model="form.email"
                      label="Hasta Kimlik No / Email"
                      variant="outlined"
                      density="compact"
                      :rules="[rules.required, rules.email]"
                      class="mt-6 mb-8"
                  ></v-text-field>

                  <v-btn
                      type="submit"
                      color="#252B61"
                      block
                      size="large"
                      class="mb-3 text-white"
                      :disabled="!isFormValid"
                  >
                    Devam
                  </v-btn>
                </v-form>

                <p class="text-center text-caption text-medium-emphasis">veya</p>

                <v-btn
                    color="#A2D6B8"
                    block
                    size="large"
                    class="mt-3 text-white"
                    @click="selectFirstPatient"
                    :disabled="patients.length === 0"
                >
                  Sıradaki Hasta İle Devam Et
                </v-btn>

                <v-chip
                    v-if="form.email && selectedPatientInfo"
                    class="mt-4"
                    color="success"
                    variant="tonal"
                    closable
                    @click:close="clearForm"
                >
                  <v-icon start>mdi-check-circle</v-icon>
                  {{ selectedPatientInfo }}
                </v-chip>
              </v-card>
            </v-col>
          </v-row>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

const emit = defineEmits(['switch-mode', 'logout', 'continue-prescription']);

const props = defineProps({
  user: Object,
});

const patients = ref([]);
const isLoading = ref(false);
const errorFetchingPatients = ref(null);
const prescriptionForm = ref(null);
const form = ref({
  name: '',
  surname: '',
  email: '',
});

const rules = {
  required: value => !!value || 'Bu alan zorunludur',
  email: value => {
    const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return pattern.test(value) || value.length < 5 || 'Geçerli bir email adresi giriniz';
  }
};

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const isFormValid = computed(() => {
  return form.value.name && form.value.surname && form.value.email;
});

const selectedPatientInfo = computed(() => {
  if (form.value.name && form.value.surname) {
    return `${form.value.name} ${form.value.surname}`;
  }
  return null;
});

const getFirstName = (fullName) => {
  return fullName ? fullName.split(' ')[0] : '';
};

const getLastName = (fullName) => {
  if (!fullName) return '';
  const parts = fullName.split(' ');
  return parts.slice(1).join(' ') || '';
};

const calculateAge = () => {
  return Math.floor(Math.random() * (65 - 18 + 1)) + 18;
};

const formatDate = (dateString) => {
  if (!dateString) return new Date().toLocaleDateString('tr-TR');
  return dateString;
};

const fetchPatients = async () => {
  isLoading.value = true;
  errorFetchingPatients.value = null;

  try {
    const response = await axios.get(`${API_BASE_URL}/api/doctor/patients`, {
      timeout: 5000
    });

    patients.value = response.data.map(p => ({
      ...p,
      age: p.age || calculateAge(),
      date: p.date || new Date().toLocaleDateString('tr-TR')
    }));

  } catch (error) {
    console.error("Hasta listesi çekilemedi:", error);

    if (error.code === 'ECONNABORTED') {
      errorFetchingPatients.value = 'Bağlantı zaman aşımına uğradı. Lütfen tekrar deneyin.';
    } else if (error.response) {
      errorFetchingPatients.value = `Sunucu hatası: ${error.response.status}`;
    } else if (error.request) {
      errorFetchingPatients.value = 'Backend sunucusuna ulaşılamıyor. Lütfen backend servisinin çalıştığından emin olun.';
    } else {
      errorFetchingPatients.value = `Beklenmeyen hata: ${error.message}`;
    }
  } finally {
    isLoading.value = false;
  }
};

const selectPatient = (patient) => {
  form.value.name = getFirstName(patient.name);
  form.value.surname = getLastName(patient.name);
  form.value.email = patient.email;
};

const selectFirstPatient = () => {
  if (patients.value.length > 0) {
    selectPatient(patients.value[0]);
  }
};

const clearForm = () => {
  form.value.name = '';
  form.value.surname = '';
  form.value.email = '';
};

const handleContinue = async () => {
  const { valid } = await prescriptionForm.value.validate();

  if (valid) {
    emit('continue-prescription', {
      name: form.value.name,
      surname: form.value.surname,
      email: form.value.email
    });
  }
};

onMounted(() => {
  fetchPatients();
});
</script>

<style scoped>
.write-prescription-container {
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

/* Mutlak Konumlandırılmış Kartı Merkezleme */
.info-header-card {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;          /* her iki yana sabitle */
  margin: 0 auto;    /* ortala */
  max-width: 100%; /* navbar ve content ile aynı genişlik */
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

/* İçerik Kartı */
.content-card {
  position: relative;
  z-index: 3;
  width: 100%;
  margin-top: 100px;
  background-color: transparent !important;
  box-shadow: none !important;
  min-height: 500px;
}

.patient-list-card {
  background-color: white;
  padding: 20px;
  min-height: 400px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.write-form-card {
  background-color: white;
  padding: 30px 40px;
  height: 100%;
  border-radius: 20px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  min-height: 450px;
}

.sub-headline {
  font-size: 1.5em;
  font-weight: 600;
  color: #333;
  margin-bottom: 0;
}

.transparent-table {
  background-color: transparent !important;
}

.patient-row {
  cursor: pointer;
  transition: all 0.2s ease;
}

.patient-row:hover {
  background-color: #f0f2f5 !important;
}

.selected-row {
  background-color: #e8f5e9 !important;
}

.transparent-table td, .transparent-table th {
  padding: 12px 8px !important;
  border-bottom: 1px solid #e0e0e0 !important;
}

.text-caption {
  font-size: 0.85rem !important;
  color: #555 !important;
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