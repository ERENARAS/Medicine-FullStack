<template>
  <div class="doctor-dashboard-container">
    <div class="navbar">
      <h1 class="logo">Medicine</h1>
      <div class="nav-links">
        <a href="#">Reçete Yaz</a>
        <a href="#">ATM Konumları</a>
        <a href="#">Hakkında</a>
        <button @click="$emit('logout')" class="logout-btn">Çıkış Yap</button>
      </div>
    </div>

    <div class="welcome-card">
      <div class="card-content">
        <div class="info-section">
          <p class="tagline">⚕️ Kolay Ve Güvenli</p>
          <p class="tagline">🩺 Hızlı İlaç Yazma Sistemi</p>
          <h2 class="welcome-message">Merhaba, Dr. {{ doctorName }}</h2>
        </div>
        <div class="action-section">
          <button class="action-btn">Reçete Yaz</button>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="stats-column">
        <div class="stat-card today">
          <h3>Bugün Yazılan Reçeteler</h3>
          <p class="count">{{ dashboardData.todayPrescriptions }}</p>
        </div>
        <div class="stat-card allergies">
          <h3>Alerjisi Olan Hastalar</h3>
          <p class="count">?</p>
        </div>
        <div class="stat-card last-7">
          <h3>Son 7 Gün</h3>
          <p class="count">{{ dashboardData.last7DaysPrescriptions }}</p>
        </div>
        <div class="stat-card last-7">
          <h3>Son 30 Gün</h3>
          <p class="count">{{ dashboardData.last30DaysPrescriptions }}</p>
        </div>
      </div>

      <div class="recent-activities">
        <h3>Son İşlemler</h3>
        <p v-if="isLoading">Veriler yükleniyor...</p>
        <p v-else-if="errorMessage" class="error-message">Hata: {{ errorMessage }}</p>
        <table v-else class="activity-table">
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
            <td colspan="5">Henüz reçete kaydı bulunamadı.</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

// Props: Giriş yapan doktor bilgisi (App.vue tarafından sağlanacak)
const props = defineProps({
  user: Object,
});

const emit = defineEmits(['logout']);

const doctorName = computed(() => props.user?.name || 'Kullanıcı');
const doctorEmail = computed(() => props.user?.email || '');

const dashboardData = ref({
  todayPrescriptions: 0,
  last7DaysPrescriptions: 0,
  last30DaysPrescriptions: 0,
});

const recentActivities = ref([]);
const isLoading = ref(true);
const errorMessage = ref('');

// Docker veya Vite ortam değişkeninden API URL'ini al
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const fetchDashboardData = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  try {
    const response = await axios.get(`${API_BASE_URL}/api/doctor/dashboard/${doctorEmail.value}`);
    const data = response.data;

    const allPrescriptions = data.allPrescriptions || [];

    // İstatistikler için tarihleri hesapla
    const today = new Date().toISOString().substring(0, 10);
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);


    // Tablo verilerini hazırlama (Sadece reçete listesi geldiği için)
    const activityList = allPrescriptions
        .map(p => {
          // Patient verisi backendden DoctorController'da kısmen doldurulduğu için
          // bazı alanlar eksik olabilir. Basitleştirilmiş ad/soyad ayırma yapalım.
          const parts = p.patient.name.split(' ');
          return {
            patientName: parts[0] || '',
            patientSurname: parts.slice(1).join(' ') || '',
            patientAge: '?', // Yaş verisi henüz gelmiyor
            prescriptionId: p.id,
            date: p.date,
            timestamp: new Date(p.date),
          }
        })
        .sort((a, b) => b.timestamp - a.timestamp) // Tarihe göre tersten sırala (en yeni üste)
        .slice(0, 6); // İlk 6 taneyi al

    recentActivities.value = activityList;


    // İstatistikleri hesapla
    dashboardData.value.todayPrescriptions = allPrescriptions.filter(p => p.date === today).length;
    dashboardData.value.last7DaysPrescriptions = allPrescriptions.filter(p => new Date(p.date) >= sevenDaysAgo).length;
    dashboardData.value.last30DaysPrescriptions = allPrescriptions.filter(p => new Date(p.date) >= thirtyDaysAgo).length;


  } catch (error) {
    console.error("Dashboard verileri çekilemedi:", error);
    errorMessage.value = `Dashboard verileri yüklenirken bir hata oluştu: ${error.message}`;
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  // Sadece kullanıcı (doktor) bilgisi varsa veri çek
  if (doctorEmail.value) {
    fetchDashboardData();
  }
});

</script>

<style scoped>
.doctor-dashboard-container {
  width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: sans-serif;
  background-color: #ECEEF9;
}

/* Navbar */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
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
  color: #d6a2b8;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
}

/* Welcome Card */
.welcome-card {
  background-color: #252B61;
  border-radius: 16px;
  padding: 40px;
  color: #fff;
  margin-bottom: 30px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
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
  background-color: #d6a2b8;
  color: #fff;
  border: none;
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 1.1em;
  cursor: pointer;
  transition: background-color 0.3s;
}
.action-btn:hover {
  background-color: #b8869c;
}
.arrow-btn {
  font-size: 1.5em;
  margin-left: 15px;
  cursor: pointer;
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
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
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
  background-color: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
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
  color: #a2d6b8;
  font-weight: bold;
}
</style>