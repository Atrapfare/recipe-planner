<script setup>
// Import necessary components
import { RouterView } from 'vue-router'; // Component to display the current route's view
import Navbar from "@/components/NavbarComponent.vue"; // Navigation bar
import Footer from "@/components/FooterComponent.vue"; // Footer
</script>

<template>
  <!-- Navigation -->
  <Navbar />

  <!-- Main container for the dynamic content of the current route -->
  <div class="container mt-5 min-vh-100">
    <RouterView /> <!-- Renders the component for the current route -->
  </div>

  <!-- Footer -->
  <Footer />
</template>

<style scoped>

</style>