<template>
  <!-- Navigation bar template -->
  <nav class="navbar navbar-expand-lg navbar-light bg-light shadow-lg">
    <div class="container-fluid">
      <!-- Brand link to the home page -->
      <a class="navbar-brand">
        <router-link class="nav-link" to="/" @click="closeMenu">Recipe Planner</router-link>
      </a>
      <!-- Button for toggling the navbar on smaller screens -->
      <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      <!-- Collapsible navbar content -->
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav">
          <!-- Navigation links -->
          <li class="nav-item">
            <router-link class="nav-link" to="/" @click="closeMenu">Calendar</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/recipes" @click="closeMenu">Recipes</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/ingredients" @click="closeMenu">Ingredients</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/shopping" @click="closeMenu">Shopping</router-link>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script>
import { Collapse } from 'bootstrap';

export default {
  name: 'Navbar', // Component name
  methods: {
    // Method to close the navbar menu
    closeMenu() {
      const navbar = document.getElementById('navbarNav');
      if (navbar) {
        const bsCollapse = new Collapse(navbar, { toggle: false });
        bsCollapse.hide(); // Hides the navbar menu
      }
    },
  },
};
</script>

<style scoped>
/* Add any specific styles for the navbar here */
</style>