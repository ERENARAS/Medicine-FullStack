<template>
  <v-container fluid class="confirmation-container fill-height pa-0">
    <!-- Navbar -->
    <v-sheet class="navbar-area d-flex justify-space-between align-center px-6">
      <h1 class="logo">Medicine</h1>
      <div class="nav-links">
        <a href="#" @click.prevent="$emit('switch-mode', 'dashboard')">Ana Sayfa</a>
        <a href="#" class="ml-4">ATM Konumları</a>
        <a href="#" class="ml-4">Hakkında</a>
        <v-btn variant="outlined" size="small" class="logout-btn ml-6" @click="$emit('logout')">Çıkış Yap</v-btn>
      </div>
    </v-sheet>

    <!-- Main Content -->
    <v-row no-gutters class="main-content-row justify-center">
      <v-col cols="12" class="d-flex flex-column align-center" style="max-width: 1000px; width: 100%;">
        
        <!-- Main Dark Blue Card -->
        <v-card class="main-card rounded-xl pa-8" elevation="4">
          
          <!-- Patient Info Section -->
          <div class="patient-info-section mb-6">
             <v-row align="center">
                <v-col cols="auto">
                   <div class="avatar-circle d-flex justify-center align-center">
                      <v-icon color="white" size="32">mdi-account</v-icon>
                   </div>
                </v-col>
                <v-col>
                   <v-row dense class="text-white">
                      <!-- Left Column -->
                      <v-col cols="12" md="6">
                         <div class="d-flex align-center mb-2">
                            <span class="info-label">Hastanın Adı Soyadı :</span>
                            <span class="info-value pl-2">{{ fullName }}</span>
                         </div>
                         <div class="d-flex align-center mb-2">
                            <span class="info-label">Hasta TC / Email :</span>
                            <span class="info-value pl-2">{{ patient.email }}</span>
                         </div>
                         <div class="d-flex align-center">
                            <span class="info-label">Tanı :</span>
                            <span class="info-value pl-2">{{ diagnosis }}</span>
                         </div>
                      </v-col>
                      <!-- Right Column -->
                       <v-col cols="12" md="6">
                         <div class="d-flex align-center mb-2 justify-md-end">
                            <span class="info-label">Tarih :</span>
                            <span class="info-value pl-2">{{ currentDate }}</span>
                         </div>
                         <div class="d-flex align-center justify-md-end">
                            <span class="info-label">Reçete ID :</span>
                            <span class="info-value pl-2">{{ tempPrescriptionId }}</span>
                         </div>
                      </v-col>
                   </v-row>
                </v-col>
             </v-row>
             
             <v-divider class="mt-6 border-opacity-25" color="white"></v-divider>
          </div>

          <!-- Medicines Section -->
          <div class="medicines-section">
            <h2 class="text-white text-h5 mb-4 font-weight-regular">İlaçlar</h2>
            
            <v-list bg-color="transparent" class="pa-0">
               <v-slide-y-transition group>
                  <v-list-item 
                     v-for="(medicine, index) in medicines" 
                     :key="medicine.id"
                     class="medicine-list-item mb-4 rounded-pill px-6 py-3"
                  >
                     <div class="d-flex align-center w-100 justify-space-between">
                        <div class="d-flex align-center">
                           <span class="index-number text-h5 font-weight-bold mr-6">{{ index + 1 }}.</span>
                           <span class="medicine-name text-h6 font-weight-bold">{{ medicine.name }} 1 MG 50 Tablet D:1 2X1 Tok</span> 
                           <!-- Note: Dosage info is hardcoded for visual match as per design requirements, or we can make it dynamic if we had inputs -->
                        </div>
                        <v-btn icon variant="text" size="small" density="comfortable" class="close-btn bg-white" @click="removeMedicine(index)">
                           <v-icon color="#333" size="small">mdi-close</v-icon>
                        </v-btn>
                     </div>
                  </v-list-item>
               </v-slide-y-transition>
            </v-list>
            
            <div v-if="medicines.length === 0" class="text-center text-white text-body-1 py-8">
               Seçili ilaç bulunmamaktadır.
            </div>

          </div>

        </v-card>

        <!-- Start Approval Button (Outside card, bottom right aligned relative to container) -->
        <div class="d-flex justify-end w-100 mt-4 mr-0" style="max-width: 1000px;">
           <v-btn
             size="x-large"
             color="#252B61"
             rounded="xl"
             class="approve-btn text-white px-10"
             height="60"
             :loading="isSubmitting"
             @click="confirmPrescription"
           >
             Reçete Onay
           </v-btn>
        </div>

      </v-col>
    </v-row>

  </v-container>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  user: Object,
  patient: Object,
  initialMedicines: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['switch-mode', 'logout', 'confirm-prescription']);

const medicines = ref([...props.initialMedicines]);
const isSubmitting = ref(false);

const currentDate = new Date().toLocaleDateString('tr-TR');
const tempPrescriptionId = Math.floor(Math.random() * 90000000) + 10000000;

const fullName = computed(() => {
   if (props.patient && props.patient.name) {
      // Use real name, assuming name and surname are combined in `name` field or separated
      return props.patient.surname ? `${props.patient.name} ${props.patient.surname}` : props.patient.name;
   }
   return '';
});

const diagnosis = computed(() => {
   // Generate diagnosis from unique treatments of selected medicines
   const treatments = medicines.value
      .map(m => m.treatment)
      .filter(t => t && t !== 'Genel Tedavi' && t !== 'Tedavi bilgisi yok'); // filter out generic or empty
   
   if (treatments.length === 0) return 'Genel Muayene';
   
   // Get unique treatments
   const uniqueTreatments = [...new Set(treatments)];
   return uniqueTreatments.join(', ');
});

const removeMedicine = (index) => {
   medicines.value.splice(index, 1);
};

const confirmPrescription = () => {
   if (medicines.value.length === 0) {
      alert("Reçetede en az bir ilaç olmalıdır.");
      return;
   }
   
   isSubmitting.value = true;
   // Simulate small delay or just emit
   emit('confirm-prescription', medicines.value);
};

</script>

<style scoped>
.confirmation-container {
  background-color: #ECEEF9; /* Light greyish blue background */
  min-height: 100vh;
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

.main-content-row {
  width: 100%;
}

.main-card {
  background-color: #202449 !important; /* Dark blue */
  width: 100%;
  border-radius: 24px !important;
  min-height: 600px;
}

.avatar-circle {
  width: 60px;
  height: 60px;
  border: 1px solid rgba(255,255,255,0.5);
  border-radius: 50%;
}

.info-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
  font-weight: 300;
  min-width: 140px;
}

.info-value {
  color: #fff;
  font-size: 1.1rem;
  font-weight: 500;
}

.medicine-list-item {
  background-color: #A2D6B8 !important; /* The green color from design */
  color: white;
  transition: transform 0.2s ease;
}

.medicine-list-item:hover {
   transform: translateX(5px);
}

.index-number {
   color: rgba(255,255,255,0.8);
   width: 40px;
}

.medicine-name {
   color: white;
   font-weight: 600;
   letter-spacing: 0.5px;
}

.close-btn {
   transition: background-color 0.2s;
}

.approve-btn {
   font-size: 1.2rem;
   text-transform: none;
   background-color: #252B61; /* Same as card but detached */
   box-shadow: 0 4px 15px rgba(37, 43, 97, 0.3);
}
</style>
