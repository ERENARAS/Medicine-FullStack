<template>
  <div id="app">
    <Signup v-if="currentMode === 'signup'" @switch-mode="switchMode" />
    <Login v-else-if="currentMode === 'login'" @switch-mode="switchMode" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Signup from './components/Signup.vue';
import Login from './components/Login.vue';

const currentMode = ref('login');

const switchMode = (mode) => {
  currentMode.value = mode;
  console.log(`Mod değiştirildi: ${mode}`);
};
</script>

<style>

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
