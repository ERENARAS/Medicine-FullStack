<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// App.vue is now simplified to just render the router view
// All state management is handled by Pinia stores
// All navigation is handled by Vue Router
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