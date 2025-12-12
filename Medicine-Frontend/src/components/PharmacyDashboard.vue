<template>
  <v-container fluid class="pharmacy-dashboard-container">
    
    <!-- Navbar -->
    <v-sheet class="navbar" color="transparent">
      <h1 class="logo">Medicine</h1>
      <div class="nav-links">
        <a href="#" @click="currentView = 'dashboard'">Ana Sayfa</a>
        <a href="#" @click="currentView = 'stock-management'">Stok Yönetimi</a>
        <a href="#">Hakkında</a>
        <v-btn @click="$emit('logout')" class="logout-btn" variant="outlined" size="small">Çıkış Yap</v-btn>
      </div>
    </v-sheet>

    <!-- Dashboard View -->
    <div v-if="currentView === 'dashboard'">
      <!-- Welcome Card -->
      <v-card class="welcome-card elevation-4">
        <div class="card-content">
          <div class="info-section">
            <p class="tagline">
              <v-icon color="#a2d6b8" size="small">mdi-pill</v-icon> Kolay Ve Güvenli
            </p>
            <p class="tagline ml-4">
              <v-icon color="#a2d6b8" size="small">mdi-hospital-box</v-icon> Hızlı Stok Yönetim Sistemi
            </p>
            <h2 class="welcome-message">Hoş Geldiniz, {{ pharmacyStaffName }}</h2>
            <p class="subtitle">Lütfen yapmak istediğiniz işlemi seçin.</p>
          </div>
        </div>
      </v-card>

      <!-- Menu Cards Grid -->
      <div class="menu-grid">
        <v-card class="menu-card elevation-2" @click="currentView = 'stock-management'">
          <div class="card-icon">
            <v-icon size="48" color="#252B61">mdi-package-variant</v-icon>
          </div>
          <div class="card-text">
            <h3>ATM Stok Yönetimi</h3>
            <p>ATM'lerdeki ilaç stoklarını görüntüleyin, güncelleyin ve yönetin.</p>
          </div>
        </v-card>

        <v-card class="menu-card elevation-2" @click="currentView = 'atm-locations'">
          <div class="card-icon">
            <v-icon size="48" color="#252B61">mdi-map-marker</v-icon>
          </div>
          <div class="card-text">
            <h3>ATM Konumları</h3>
            <p>Tüm ATM'lerin konumlarını ve operasyonel durumlarını harita üzerinde görün.</p>
          </div>
        </v-card>

        <v-card class="menu-card elevation-2">
          <div class="card-icon">
            <v-icon size="48" color="#252B61">mdi-chart-bar</v-icon>
          </div>
          <div class="card-text">
            <h3>Raporlar ve Analiz</h3>
            <p>Stok hareketleri ve ATM kullanım verileri hakkında detaylı raporlar alın.</p>
          </div>
        </v-card>
      </div>
    </div>

    <!-- Stock Management View -->
    <ATMStockManagement 
      v-else-if="currentView === 'stock-management'" 
      :userId="props.user.id"
      @back="currentView = 'dashboard'"
    />

    <!-- ATM Locations View -->
    <ATMLocations 
      v-else-if="currentView === 'atm-locations'" 
      :userId="props.user.id"
      @back="currentView = 'dashboard'"
    />

  </v-container>
</template>

<script setup>
import { ref, computed } from 'vue';
import ATMStockManagement from './ATMStockManagement.vue';
import ATMLocations from './ATMLocations.vue';

const props = defineProps({
  user: Object,
});

const emit = defineEmits(['logout', 'switch-mode']);

const pharmacyStaffName = computed(() => props.user?.name || 'Eczane Personeli');
const currentView = ref('dashboard'); // 'dashboard' or 'stock-management'

</script>

<style scoped>
/* Main Container */
.pharmacy-dashboard-container {
  min-height: 100vh;
  min-width: 100vw;
  background-color: #ECEEF9;
  padding-top: 15px;
  padding-bottom: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: sans-serif;
}

.pharmacy-dashboard-container > * {
  width: 100%;
  max-width: 1200px;
}

/* Navbar */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
  padding-bottom: 10px;
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
  cursor: pointer;
}

.nav-links a:hover {
  color: #a2d6b8;
}

.logout-btn {
  margin-left: 30px;
  background: none;
  border: 1px solid #d6a2b8;
  color: #d6a2b8 !important;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
  text-transform: none;
}

.logout-btn:hover {
  background-color: #d6a2b8;
  color: white !important;
}

/* Welcome Card */
.welcome-card {
  background-color: #252B61 !important;
  border-radius: 16px;
  padding: 40px;
  color: #fff;
  margin-bottom: 30px;
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
  margin-bottom: 10px;
}

.subtitle {
  font-size: 1em;
  color: #ccc;
  margin-top: 10px;
}

/* Menu Grid */
.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.menu-card {
  background-color: #fff !important;
  border-radius: 12px;
  padding: 30px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 200px;
}

.menu-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1) !important;
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.card-text h3 {
  font-size: 1.3em;
  color: #252B61;
  margin-bottom: 10px;
  font-weight: 600;
}

.card-text p {
  font-size: 0.95em;
  color: #666;
  line-height: 1.5;
  margin: 0;
}
</style>
