<template>
  <div>
    <!-- Header Section -->
    <h1 class="text-center flex-grow-1">Ingredients</h1>

    <!-- Floating Button for Adding Ingredient -->
    <button type="button" class="btn btn-primary floating-button rounded-circle" data-bs-toggle="modal" data-bs-target="#addIngredientModal">
      <i class="fa-solid fa-plus"></i>
    </button>

    <!-- Search and Filter Section -->
    <div class="mb-3">
      <div class="d-flex align-items-center">
        <!-- Search Input -->
        <input type="text" v-model="searchQuery" class="form-control me-3" placeholder="Search" />

        <!-- Filter: Used in Recipe -->
        <span
            @click="filters.usedInRecipe = !filters.usedInRecipe"
            :class="['badge', filters.usedInRecipe ? 'text-bg-secondary' : 'text-bg-outline-secondary', 'me-3', 'clickable']"
        >
          <i class="fa-solid fa-utensils"></i> in recipe
        </span>

        <!-- Filter: Requires Cooling -->
        <span
            @click="filters.cooled = !filters.cooled"
            :class="['badge', filters.cooled ? 'text-bg-primary' : 'text-bg-outline-primary', 'me-3', 'clickable']"
        >
          <i class="fa-solid fa-temperature-low"></i> cooled
        </span>

        <!-- Filter: Freezed -->
        <span
            @click="filters.freeze = !filters.freeze"
            :class="['badge', filters.freeze ? 'text-bg-info' : 'text-bg-outline-info', 'clickable']"
        >
          <i class="fa-solid fa-snowflake"></i> freezed
        </span>
      </div>
    </div>

    <!-- Ingredients List Section -->
    <div class="row">
      <!-- Message if No Ingredients Found -->
      <div v-if="paginatedIngredients.length === 0" class="text-center my-5">
        <h4>No ingredients found</h4>
      </div>

      <!-- Render Each Ingredient Item -->
      <div
          class="col-md-4 col-sm-6 col-lg-3"
          v-for="ingredient in paginatedIngredients"
          :key="ingredient.id"
      >
        <IngredientItemComponent
            :ingredient="ingredient"
            @edit-ingredient="editIngredient"
            @delete-ingredient="deleteIngredient"
        />
      </div>
    </div>

    <!-- Pagination Section -->
    <div v-if="paginatedIngredients.length !== 0">
      <div class="d-flex justify-content-center mt-4">
        <nav>
          <ul class="pagination">
            <!-- Previous Page Button -->
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="currentPage--" :disabled="currentPage === 1">Previous</button>
            </li>

            <!-- Page Number Buttons -->
            <li class="page-item" :class="{ active: page === currentPage }" v-for="page in totalPages" :key="page">
              <button class="page-link" @click="goToPage(page)">{{ page }}</button>
            </li>

            <!-- Next Page Button -->
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="currentPage++" :disabled="currentPage === totalPages">Next</button>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- Modal for Adding New Ingredient -->
    <div class="modal fade text-dark" data-bs-backdrop="static" id="addIngredientModal" tabindex="-1"
         aria-labelledby="addIngredientModalLabel"
         aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="addIngredientModalLabel">Add New Ingredient</h5>
            <!-- Close Button -->
            <i class="fa-solid fa-xmark text-danger ms-auto close-icon" data-bs-dismiss="modal" aria-label="Close"></i>
          </div>
          <div class="modal-body">
            <AddIngredientItem @ingredient-added="onIngredientAdded"></AddIngredientItem>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal for Editing Ingredient -->
    <div class="modal fade text-dark" data-bs-backdrop="static" id="editIngredientModal" tabindex="-1"
         aria-labelledby="editIngredientModalLabel"
         aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="editIngredientModalLabel">Edit Ingredient</h5>
            <!-- Close Button -->
            <i class="fa-solid fa-xmark text-danger ms-auto close-icon" data-bs-dismiss="modal" aria-label="Close"></i>
          </div>
          <div class="modal-body">
            <!-- Edit Ingredient Form -->
            <EditIngredientItem
                v-if="selectedIngredientId !== null"
                :ingredient-id="selectedIngredientId"
                @ingredient-edited="onIngredientEdited">
            </EditIngredientItem>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'; // Axios for API requests
import AddIngredientItem from '@/components/ingredient/AddIngredientItemComponent.vue'; // Component for adding an ingredient
import EditIngredientItem from '@/components/ingredient/EditIngredientItemComponent.vue'; // Component for editing an ingredient
import IngredientItemComponent from '@/components/ingredient/IngredientItemComponent.vue'; // Component for displaying an ingredient item
import {BACKEND_IP} from '@/config.js'; // Backend API IP configuration
import { Modal } from 'bootstrap'; // Bootstrap modal utility

export default {
  name: 'Ingredients', // Component name
  components: {
    AddIngredientItem, // Register AddIngredientItem component
    EditIngredientItem, // Register EditIngredientItem component
    IngredientItemComponent, // Register IngredientItemComponent
  },
  data() {
    return {
      ingredients: [], // Array to hold all ingredients
      selectedIngredientId: null, // Currently selected ingredient ID for editing
      searchQuery: '', // Search query for filtering ingredients
      filters: {
        usedInRecipe: false, // Filter for ingredients used in recipes
        cooled: false, // Filter for ingredients requiring cooling
        freeze: false, // Filter for ingredients that can be frozen
      },
      currentPage: 1, // Current page for pagination
      pageSize: 12, // Number of ingredients per page
    };
  },
  created() {
    this.fetchIngredients(); // Fetch initial list of ingredients from API
    this.startUpdatingIngredients(); // Start periodic updates of ingredient list
  },
  computed: {
    // Compute filtered ingredients based on search and filter criteria
    filteredIngredients() {
      return this.ingredients.filter((ingredient) => {
        const matchesSearch = ingredient.name.toLowerCase().includes(this.searchQuery.toLowerCase()); // Match search query
        const matchesFilters =
            (!this.filters.usedInRecipe || ingredient.usedInRecipe) &&
            (!this.filters.cooled || ingredient.cooled) &&
            (!this.filters.freeze || ingredient.freeze); // Match applied filters
        return matchesSearch && matchesFilters;
      });
    },
    // Compute the total number of pages for pagination
    totalPages() {
      return Math.ceil(this.filteredIngredients.length / this.pageSize);
    },
    // Get the ingredients for the current page
    paginatedIngredients() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredIngredients.slice(start, end);
    },
  },
  methods: {
    // Fetch the list of ingredients from the backend
    async fetchIngredients() {
      try {
        const response = await axios.get(`${BACKEND_IP}/ingredients`);
        this.ingredients = response.data;
      } catch (error) {
        console.error('Error fetching ingredients:', error);
        alert('There was an error fetching ingredients.');
      }
    },
    // Start periodic updates of the ingredient list
    startUpdatingIngredients() {
      this.updateInterval = setInterval(this.fetchIngredients, 5000);
    },
    // Navigate to a specific page in the pagination
    goToPage(page) {
      this.currentPage = page;
    },
    // Handle the event when a new ingredient is added
    onIngredientAdded() {
      this.fetchIngredients(); // Refresh the ingredient list

      const modal = document.getElementById('addIngredientModal');
      if (!modal) {
        console.error('Modal element not found');
        return;
      }

      const bootstrapModal = Modal.getOrCreateInstance(modal); // Get or create the modal instance
      if (bootstrapModal) {
        bootstrapModal.hide(); // Hide the modal
      }

      modal.addEventListener('hidden.bs.modal', () => {
        document.body.classList.remove('modal-open');
        const backdrop = document.querySelector('.modal-backdrop');
        if (backdrop) {
          backdrop.remove(); // Remove modal backdrop
        }
      });
    },
    // Handle the event when an ingredient is edited
    onIngredientEdited() {
      this.fetchIngredients(); // Refresh the ingredient list
      const modal = document.getElementById('editIngredientModal');
      const bootstrapModal = Modal.getInstance(modal);
      bootstrapModal.hide(); // Hide the modal

      this.selectedIngredientId = null; // Clear the selected ingredient ID
    },
    // Open the modal for editing a specific ingredient
    editIngredient(ingredientId) {
      this.selectedIngredientId = ingredientId;
      const modalElement = document.getElementById('editIngredientModal');
      if (modalElement) {
        const modalInstance = Modal.getOrCreateInstance(modalElement);
        modalInstance.show(); // Show the modal
      }
    },
    // Delete an ingredient from the backend and refresh the list
    async deleteIngredient(ingredientId) {
      if (confirm('Are you sure you want to delete this ingredient?')) {
        try {
          await axios.delete(`${BACKEND_IP}/ingredients/${ingredientId}`);
          alert('Ingredient deleted successfully!');
          await this.fetchIngredients(); // Refresh the ingredient list
        } catch (error) {
          if (error.response && error.response.status === 409) {
            alert('Cannot delete the ingredient as it is used in a recipe.');
          } else {
            console.error('Error deleting ingredient:', error);
            alert('There was an error deleting the ingredient.');
          }
        }
      }
    },
  },
};
</script>

<style scoped>
.close-icon {
  cursor: pointer;
}

.badge {
  cursor: pointer;
  transition: background-color 0.3s ease-in-out, color 0.3s ease-in-out;
}

.text-bg-outline-secondary {
  background-color: transparent;
  border: 1px solid #6c757d;
  color: #6c757d;
}

.text-bg-outline-primary {
  background-color: transparent;
  border: 1px solid #0d6efd;
  color: #0d6efd;
}

.text-bg-outline-info {
  background-color: transparent;
  border: 1px solid #0dcaf0;
  color: #0dcaf0;
}

.floating-button {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1050;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  transition: box-shadow 0.3s ease-in-out;
}

.floating-button:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.4);
}

.floating-button i {
  transition: transform 0.3s ease-in-out;
}

.floating-button:hover i {
  transform: rotate(90deg);
}
</style>