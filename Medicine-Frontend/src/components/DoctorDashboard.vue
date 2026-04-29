<template>
  <v-container fluid class="doctor-dashboard-container">

    <v-sheet class="navbar" color="transparent">
      <h1 class="logo">Medicine</h1>
      <div class="nav-links">
        <a href="#" @click="$emit('switch-mode', 'write-prescription')">Reçete Yaz</a>
        <a href="#">ATM Konumları</a>
        <a href="#">Hakkında</a>
        <v-btn @click="$emit('logout')" class="logout-btn" variant="outlined" size="small">Çıkış Yap</v-btn>
      </div>
    </v-sheet>

    <v-card class="welcome-card elevation-4">
      <div class="card-content">
        <div class="info-section">
          <p class="tagline">
            <v-icon color="#a2d6b8" size="small">mdi-medical-bag</v-icon> Kolay Ve Güvenli
          </p>
          <p class="tagline ml-4">
            <v-icon color="#a2d6b8" size="small">mdi-stethoscope</v-icon> Hızlı İlaç Yazma Sistemi
          </p>
          <h2 class="welcome-message">Merhaba, Dr. {{ doctorName }}</h2>
        </div>
        <div class="action-section">
          <v-btn class="action-btn elevation-2" @click="$emit('switch-mode', 'write-prescription')">
            Reçete Yaz
          </v-btn>
        </div>
      </div>
    </v-card>

    <div class="content-grid">

      <div class="stats-column">
        <v-card class="stat-card today elevation-1">
          <h3>Bugün Yazılan Reçeteler</h3>
          <p class="count">{{ dashboardData.todayPrescriptions }}</p>
        </v-card>
        <v-card class="stat-card allergies elevation-1">
          <h3>Alerjisi Olan Hastalar</h3>
          <p class="count">{{ dashboardData.allergicPatientCount }}</p>
        </v-card>
        <v-card class="stat-card last-7 elevation-1">
          <h3>Son 7 Gün</h3>
          <p class="count">{{ dashboardData.last7DaysPrescriptions }}</p>
        </v-card>
        <v-card class="stat-card last-7 elevation-1">
          <h3>Son 30 Gün</h3>
          <p class="count">{{ dashboardData.last30DaysPrescriptions }}</p>
        </v-card>
      </div>

      <v-card class="recent-activities elevation-2">
        <h3>Son İşlemler</h3>
        <p v-if="isLoading">
          <v-progress-linear indeterminate color="primary"></v-progress-linear>
          Veriler yükleniyor...
        </p>
        <p v-else-if="errorMessage" class="error-message">Hata: {{ errorMessage }}</p>

        <v-table v-else class="activity-table">
          <thead>
          <tr>
            <th>Adı</th>
            <th>Soyadı</th>
            <th>Yaş</th>
            <th>Reçete Kodu</th>
            <th>Tarih</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in recentActivities" :key="item.prescriptionId">
            <td>{{ item.patientName }}</td>
            <td>{{ item.patientSurname }}</td>
            <td>{{ item.patientAge }}</td>
            <td>{{ item.prescriptionId.substring(0, 8) }}...</td>
            <td>{{ item.date }}</td>
          </tr>
          <tr v-if="recentActivities.length === 0">
            <td colspan="5" class="text-center">Henüz reçete kaydı bulunamadı.</td>
          </tr>
          </tbody>
        </v-table>
      </v-card>
    </div>
  </v-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
// Vuetify bileşenleri import edilmedi, bu yüzden VBtn, VCard vs. burada tanımlanmadı.
// Ancak main.js'de global olarak kaydedildiği varsayıldı.

const props = defineProps({
  user: Object,
});

const emit = defineEmits(['logout', 'switch-mode']);

const doctorName = computed(() => props.user?.name || 'Kullanıcı');
const doctorEmail = computed(() => props.user?.email || '');

const dashboardData = ref({
  todayPrescriptions: 0,
  last7DaysPrescriptions: 0,
  last30DaysPrescriptions: 0,
  allergicPatientCount: '?',
});

const recentActivities = ref([]);
const isLoading = ref(true);
const errorMessage = ref('');

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const fetchDashboardData = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  try {
    const response = await axios.get(`${API_BASE_URL}/api/doctor/dashboard/${doctorEmail.value}`);
    const data = response.data;

    const allPrescriptions = data.allPrescriptions || [];

    const today = new Date().toISOString().substring(0, 10);
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);


    const activityList = allPrescriptions
        .map(p => {
          return {
            patientName: p.name || '',
            patientSurname: p.surname || '',
            patientAge: p.age || '?',
            prescriptionId: p.prescriptionCode || '',
            date: p.date,
            timestamp: new Date(p.date),
          }
        })
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 6);

    recentActivities.value = activityList;

    // Düzeltme: allergicPatientCount verisi eşleniyor
    dashboardData.value.todayPrescriptions = allPrescriptions.filter(p => p.date === today).length;
    dashboardData.value.last7DaysPrescriptions = allPrescriptions.filter(p => new Date(p.date) >= sevenDaysAgo).length;
    dashboardData.value.last30DaysPrescriptions = allPrescriptions.filter(p => new Date(p.date) >= thirtyDaysAgo).length;
    dashboardData.value.allergicPatientCount = data.allergicPatientCount;


  } catch (error) {
    console.error("Dashboard verileri çekilemedi:", error);
    errorMessage.value = `Dashboard verileri yüklenirken bir hata oluştu: ${error.message}`;
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  if (doctorEmail.value) {
    fetchDashboardData();
  }
});

</script>

<style scoped>
/* Vuetify bileşenlerinin üstündeki temel layout'u korur */
.doctor-dashboard-container {
  /* SAYFA ARKAPLANI */
  min-height: 100vh;
  min-width: 100vw;    /* Tüm ekran yüksekliği */
  background-color: #ECEEF9;

  /* Üstteki boşluğu kontrol et */
  padding-top: 15px;
  padding-bottom: 40px;

  /* İçerikleri ortaya topla */
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: sans-serif;
}

/* Container içindeki asıl içerik genişliği */
.doctor-dashboard-container > * {
  width: 100%;
  max-width: 1200px;              /* Eski 1200px burada */
}

/* Navbar */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}
.logo {
  font-size: 1.8em;
  font-weight: bold;
  color: #333;
}
.nav-links a {
  margin-left: 20px;
  text-decoration: none;
  color: #555;
  font-weight: 500;
}
.nav-links a:hover {
  color: #a2d6b8;
}
.logout-btn {
  margin-left: 30px;
  background: none;
  border: 1px solid #d6a2b8;
  color: #d6a2b8 !important; /* Vuetify'nin kendi renklerini override eder */
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
  text-transform: none; /* Vuetify'den gelen varsayılan büyük harf kullanımını düzeltir */
}
.logout-btn:hover {
  background-color: #d6a2b8;
  color: white !important;
}

/* Welcome Card */
.welcome-card {
  background-color: #252B61 !important; /* Vuetify card için !important eklendi */
  border-radius: 16px;
  padding: 40px;
  color: #fff;
  margin-bottom: 30px;
  /* elevation-4 kullanıldığı için box-shadow kaldırıldı, ancak eski stil korunuyor */
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tagline {
  font-size: 0.9em;
  color: #a2d6b8;
  margin: 5px 0;
  display: inline-block;
  margin-right: 20px;
}
.welcome-message {
  font-size: 2.5em;
  font-weight: 300;
  margin-top: 20px;
  margin-bottom: 0;
}

.action-section {
  display: flex;
  align-items: center;
}
.action-btn {
  background-color: #d6a2b8 !important;
  color: #fff !important;
  border: none;
  padding: 24px 30px;
  border-radius: 16px;
  font-size: 1.1em;
  cursor: pointer;
  transition: background-color 0.3s;
  text-transform: none;
  align-content: center;
}
.action-btn:hover {
  background-color: #b8869c !important;
}

/* Content Grid */
.content-grid {
  display: flex;
  gap: 30px;
}

.stats-column {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  flex: 1;
}

.stat-card {
  background-color: #fff !important;
  border-radius: 8px;
  padding: 20px;
  /* elevation-1 kullanıldığı için box-shadow kaldırıldı */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 120px;
}

.stat-card h3 {
  font-size: 1em;
  color: #666;
  margin-top: 0;
  font-weight: 400;
}

.stat-card .count {
  font-size: 3em;
  font-weight: bold;
  color: #a2d6b8;
  line-height: 1;
}

.recent-activities {
  flex: 2;
  background-color: #fff !important;
  border-radius: 12px;
  padding: 30px;
  /* elevation-2 kullanıldığı için box-shadow kaldırıldı */
}

.recent-activities h3 {
  font-size: 1.4em;
  margin-top: 0;
  margin-bottom: 20px;
}

.activity-table {
  width: 100%;
  border-collapse: collapse;
}

.activity-table th, .activity-table td {
  text-align: left;
  padding: 12px 10px;
  border-bottom: 1px solid #eee;
  font-size: 0.95em;
}

.activity-table th {
  font-weight: 500;
  color: #999;
}

.activity-table tbody tr:last-child td {
  border-bottom: none;
}
.error-message {
  color: #721c24;
  font-weight: bold;
}
</style>