<template>
  <div>
    <!-- Page Title -->
    <h1 class="text-center flex-grow-1">Recipes</h1>

    <!-- Floating buttons for adding recipes and exporting JSON -->
    <button
        type="button"
        class="btn btn-primary rounded-circle floating-button"
        data-bs-toggle="modal"
        data-bs-target="#addRecipeModal"
        style="bottom: 20px;"
    >
      <i class="fa-solid fa-plus"></i>
    </button>

    <button
        type="button"
        class="btn btn-danger rounded-circle floating-button"
        style="bottom: 90px;"
    @click="exportToJSON"
    >
    <i class="fa-solid fa-file-code"></i>
    </button>

    <button
        type="button"
        class="btn btn-success rounded-circle floating-button"
        style="bottom: 160px;"
    @click="exportRecipesToPDF"
    >
    <i class="fa-solid fa-file-pdf"></i>
    </button>

    <!-- Search and filter section -->
    <div class="mb-3">
      <div class="row align-items-center mb-3">
        <!-- Search bar -->
        <div class="col-sm-12 col-md-8 col-lg-9 mb-2">
          <input
              type="text"
              v-model="searchQuery"
              class="form-control"
              placeholder="Search"
          />
        </div>

        <!-- Sort dropdown -->
        <div class="col-sm-12 col-md-4 col-lg-3 mb-2">
          <select class="form-select" v-model="sortBy" @change="setSortOrder(sortBy)">
            <option value="default">Sort by Default</option>
            <option value="used-asc">Sort by Used (Ascending)</option>
            <option value="used-desc">Sort by Used (Descending)</option>
          </select>
        </div>

        <!-- Filter buttons -->
        <div class="d-flex flex-wrap">
          <!-- Filters for difficulty levels -->
          <span
              @click="toggleFilter('easy')"
              class="m-1"
              :class="['badge', filters.easy ? 'text-bg-success' : 'text-bg-outline-success', 'me-2', 'clickable', 'visible-filter']"
          >
            Easy
          </span>
          <span
              @click="toggleFilter('medium')"
              class="m-1"
              :class="['badge', filters.medium ? 'text-bg-warning' : 'text-bg-outline-warning', 'me-2', 'clickable', 'visible-filter']"
          >
            Medium
          </span>
          <span
              @click="toggleFilter('hard')"
              class="m-1"
              :class="['badge', filters.hard ? 'text-bg-danger' : 'text-bg-outline-danger', 'me-2', 'clickable', 'visible-filter']"
          >
            Hard
          </span>
          <!-- Filter for quick recipes -->
          <span
              @click="filters.quick = !filters.quick"
              class="m-1"
              :class="['badge', filters.quick ? 'text-bg-primary' : 'text-bg-outline-primary', 'clickable', 'visible-filter']"
          >
            <i class="fa-solid fa-clock"></i> Quick
          </span>
          <!-- Filter for favorite recipes -->
          <span
              @click="filters.favorites = !filters.favorites"
              class="m-1"
              :class="['badge', filters.favorites ? 'text-bg-danger' : 'text-bg-outline-danger', 'clickable', 'visible-filter']"
          >
            <i class="fa-solid fa-heart"></i> Favourites
          </span>
        </div>
      </div>
    </div>

    <!-- Recipe cards -->
    <div class="row">
      <!-- Display message if no recipes are found -->
      <div v-if="paginatedRecipes.length === 0" class="text-center my-5">
        <h4>No recipes found</h4>
      </div>
      <!-- Display recipe cards -->
      <div class="col-md-4" v-for="recipe in paginatedRecipes" :key="recipe.id">
        <div class="card mb-3 p-2 shadow-sm recipe-card" @click="showRecipe(recipe.id)">
          <!-- Recipe image -->
          <img v-if="recipe.image" :src="recipe.image" alt="Recipe Image" class="card-img-top recipe-image">
          <div class="card-body p-2">
            <h5 class="card-title mb-2">{{ recipe.name }}</h5>

            <!-- Display badges for recipe usage, time, and difficulty -->
            <div class="mb-2">
              <span v-if="recipe.used !== 0" class="badge text-bg-secondary me-2">
                <i class="fa-solid fa-ranking-star"></i> {{ recipe.used }}x
              </span>
              <span v-if="recipe.time" class="badge text-bg-primary me-2">
                <i class="fa-solid fa-clock"></i> {{ recipe.time }} min
              </span>
              <span v-if="recipe.difficulty"
                    :class="getDifficultyBadgeClass(recipe.difficulty)">
                {{ recipe.difficulty }}
              </span>
            </div>

            <!-- Short description of the recipe -->
            <p class="card-text mb-2">{{ getShortDescription(recipe.description) }}</p>

            <!-- Action buttons for favorite, edit, and delete -->
            <div class="d-flex justify-content-end">
              <button type="button" class="btn text-danger-emphasis bg-transparent btn-sm me-2"
                      @click.stop="toggleFavorite(recipe)">
                <i :class="recipe.favorite ? 'fa-solid fa-heart' : 'fa-regular fa-heart'"></i>
              </button>
              <button type="button" class="btn text-secondary bg-transparent btn-sm me-2"
                      @click.stop="editRecipe(recipe.id)">
                <i class="fa-solid fa-pen"></i>
              </button>
              <button type="button" class="btn text-danger bg-transparent border-0 btn-sm"
                      @click.stop="deleteRecipe(recipe.id)">
                <i class="fa-solid fa-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="paginatedRecipes.length !== 0">
      <div class="d-flex justify-content-center mt-4">
        <nav>
          <ul class="pagination">
            <!-- Previous button -->
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="currentPage--" :disabled="currentPage === 1">Previous</button>
            </li>
            <!-- Page numbers -->
            <li class="page-item" :class="{ active: page === currentPage }" v-for="page in totalPages" :key="page">
              <button class="page-link" @click="currentPage = page">{{ page }}</button>
            </li>
            <!-- Next button -->
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="currentPage++" :disabled="currentPage === totalPages">Next</button>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- Modals for add, edit, and show recipe -->
    <div class="modal fade text-dark" id="addRecipeModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Add New Recipe</h5>
            <i class="fa-solid fa-xmark text-danger ms-auto close-icon" data-bs-dismiss="modal"></i>
          </div>
          <div class="modal-body">
            <AddRecipeItem @recipe-added="onRecipeAdded"></AddRecipeItem>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade text-dark" id="editRecipeModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Edit Recipe</h5>
            <i class="fa-solid fa-xmark text-danger ms-auto close-icon" data-bs-dismiss="modal"></i>
          </div>
          <div class="modal-body">
            <EditRecipeItem v-if="selectedRecipeId" :recipe-id="selectedRecipeId"
                            @recipe-edited="onRecipeEdited"></EditRecipeItem>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade text-dark" id="showRecipeModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Recipe Details</h5>
            <i class="fa-solid fa-xmark text-danger ms-auto close-icon" data-bs-dismiss="modal"></i>
          </div>
          <div class="modal-body">
            <ShowRecipeItem
                v-if="selectedRecipeId"
                :recipe-id="selectedRecipeId"
                @edit-recipe="handleEditFromShow"
                @delete-recipe="handleDeleteFromShow"
                @close-recipe="handleCloseShowRecipeModal">
            </ShowRecipeItem>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import AddRecipeItem from '@/components/recipe/AddRecipeItemComponent.vue';
import EditRecipeItem from '@/components/recipe/EditRecipeItemComponent.vue';
import ShowRecipeItem from '@/components/recipe/ShowRecipeItemComponent.vue';
import {BACKEND_IP} from '@/config.js';
import {Modal} from 'bootstrap';
import router from "@/router/index.js";
import jsPDF from 'jspdf';

export default {
  name: 'Recipes',
  components: {
    AddRecipeItem,
    EditRecipeItem,
    ShowRecipeItem,
  },
  data() {
    return {
      recipes: [], // List of all recipes
      selectedRecipeId: null, // ID of the currently selected recipe for edit or view
      searchQuery: '', // Search input value
      filters: {
        easy: false, // Filter for 'easy' recipes
        medium: false, // Filter for 'medium' recipes
        hard: false, // Filter for 'hard' recipes
        quick: false, // Filter for quick recipes
        favorites: false, // Filter for favorite recipes
      },
      currentPage: 1, // Current page in pagination
      pageSize: 6, // Number of recipes per page
      sortBy: 'default', // Sorting method for recipes
    };
  },
  created() {
    // Fetch recipes when the component is created
    this.fetchRecipes().then(() => {
      const recipeId = this.$route.query.recipeid; // Check if a specific recipe ID is provided in the route
      if (recipeId) {
        this.showRecipe(parseInt(recipeId));
      }
    });
  },
  computed: {
    // Filter recipes based on user-selected filters
    filteredRecipes() {
      return this.recipes.filter((recipe) => {
        const matchesSearch = recipe.name.toLowerCase().includes(this.searchQuery.toLowerCase());
        const matchesFilters =
            (!this.filters.easy || recipe.difficulty.toLowerCase() === 'easy') &&
            (!this.filters.medium || recipe.difficulty.toLowerCase() === 'medium') &&
            (!this.filters.hard || recipe.difficulty.toLowerCase() === 'hard') &&
            (!this.filters.quick || (recipe.time && parseInt(recipe.time) < 30)) &&
            (!this.filters.favorites || recipe.favorite);
        return matchesSearch && matchesFilters;
      });
    },
    // Sort recipes based on selected sorting method
    sortedRecipes() {
      const sorted = [...this.filteredRecipes];
      if (this.sortBy === 'used-asc') {
        sorted.sort((a, b) => a.used - b.used); // Ascending by usage
      } else if (this.sortBy === 'used-desc') {
        sorted.sort((a, b) => b.used - a.used); // Descending by usage
      }
      return sorted;
    },
    // Paginate recipes based on the current page and page size
    paginatedRecipes() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.sortedRecipes.slice(start, end);
    },
    // Calculate total number of pages
    totalPages() {
      return Math.ceil(this.sortedRecipes.length / this.pageSize);
    },
  },
  methods: {
    // Fetch all recipes from the backend
    async fetchRecipes() {
      try {
        const response = await axios.get(`${BACKEND_IP}/recipes`);
        this.recipes = response.data;
      } catch (error) {
        console.error('Error fetching recipes:', error);
        alert('There was an error fetching recipes');
      }
    },
    // Toggle filters for recipe difficulty
    toggleFilter(filterType) {
      if (filterType === 'easy') {
        this.filters.easy = !this.filters.easy;
        if (this.filters.easy) {
          this.filters.medium = false;
          this.filters.hard = false;
        }
      } else if (filterType === 'medium') {
        this.filters.medium = !this.filters.medium;
        if (this.filters.medium) {
          this.filters.easy = false;
          this.filters.hard = false;
        }
      } else if (filterType === 'hard') {
        this.filters.hard = !this.filters.hard;
        if (this.filters.hard) {
          this.filters.easy = false;
          this.filters.medium = false;
        }
      }
    },
    // Set sorting method and reset to the first page
    setSortOrder(order) {
      this.sortBy = order;
      this.currentPage = 1;
    },
    // Truncate recipe descriptions for display
    getShortDescription(description) {
      if (!description) return '';
      const maxCharsPerLine = 40;
      const maxLines = 3;
      let shortDescription = '';
      let currentLine = 0;
      let currentLineLength = 0;
      for (const char of description) {
        if (currentLine >= maxLines) break;
        shortDescription += char;
        currentLineLength++;
        if (currentLineLength >= maxCharsPerLine || char === '\n') {
          currentLine++;
          currentLineLength = 0;
        }
      }
      return shortDescription.trim();
    },
    // Show the details of a selected recipe
    showRecipe(recipeId) {
      this.selectedRecipeId = recipeId;

      // Close all open modals (if any)
      const allModals = document.querySelectorAll('.modal');
      allModals.forEach((modal) => {
        const modalInstance = Modal.getInstance(modal);
        if (modalInstance) modalInstance.hide();
      });

      // Remove all lingering backdrops
      const backdrops = document.querySelectorAll('.modal-backdrop');
      backdrops.forEach((backdrop) => backdrop.remove());

      // Reset body styles
      document.body.classList.remove('modal-open');
      document.body.style.overflow = '';
      document.body.style.paddingRight = '';

      // Open the showRecipe modal
      const modalElement = document.getElementById('showRecipeModal');
      if (modalElement) {
        const modalInstance = Modal.getOrCreateInstance(modalElement);
        modalInstance.show();
      }
    },
    // Edit a recipe
    editRecipe(recipeId) {
      this.selectedRecipeId = recipeId;
      const modalElement = document.getElementById('editRecipeModal');
      if (modalElement) {
        const modalInstance = Modal.getOrCreateInstance(modalElement);
        modalInstance.show();
      }
    },
    // Delete a recipe by ID
    async deleteRecipe(recipeId) {
      if (confirm('Are you sure you want to delete this recipe?')) {
        try {
          await axios.delete(`${BACKEND_IP}/recipes/${recipeId}`);
          alert('Recipe deleted successfully!');
          await this.fetchRecipes();
        } catch (error) {
          console.error('Error deleting recipe:', error);
          alert('There was an error deleting the recipe.');
        }
      }
    },
    // Add a new recipe
    onRecipeAdded() {
      this.fetchRecipes();

      const modal = document.getElementById('addRecipeModal');
      if (!modal) {
        console.error('Modal element not found');
        return;
      }

      const bootstrapModal = Modal.getInstance(modal);
      if (bootstrapModal) {
        bootstrapModal.hide();
      } else {
        console.error('Bootstrap Modal instance not found');
      }

      const backdrops = document.querySelectorAll('.modal-backdrop');
      backdrops.forEach((backdrop) => backdrop.remove());

      document.body.classList.remove('modal-open');
      document.body.style.overflow = '';
      document.body.style.paddingRight = '';
    },
    // Update an edited recipe
    async onRecipeEdited(updatedRecipe) {
      const index = this.recipes.findIndex(recipe => recipe.id === updatedRecipe.id);
      if (index !== -1) {
        this.recipes.splice(index, 1, updatedRecipe);
      } else {
        this.recipes.push(updatedRecipe);
      }
      const modal = document.getElementById('editRecipeModal');
      const bootstrapModal = Modal.getInstance(modal);
      bootstrapModal?.hide();

      this.selectedRecipeId = null; // Reset der Auswahl
    },
    // Handle edit action from the recipe details modal
    handleEditFromShow(recipeId) {
      const modal = Modal.getInstance(document.getElementById('showRecipeModal'));
      modal.hide();
      this.editRecipe(recipeId);
    },
    // Handle delete action from the recipe details modal
    async handleDeleteFromShow(recipeId) {
      const modal = Modal.getInstance(document.getElementById('showRecipeModal'));
      modal.hide();
      await this.deleteRecipe(recipeId);
    },
    // Handle close action for the recipe details modal
    handleCloseShowRecipeModal() {
      const modal = Modal.getInstance(document.getElementById('showRecipeModal'));
      modal.hide();
      router.push("/recipes")
    },
    // Get the CSS class for difficulty badges
    getDifficultyBadgeClass(difficulty) {
      switch (difficulty.toLowerCase()) {
        case 'easy':
          return 'badge text-bg-success';
        case 'medium':
          return 'badge text-bg-warning';
        case 'hard':
          return 'badge text-bg-danger';
        default:
          return 'badge text-bg-secondary';
      }
    },
    // Export all recipes to a JSON file
    exportToJSON() {
      const jsonContent = JSON.stringify(this.recipes, null, 2);
      const blob = new Blob([jsonContent], {type: 'application/json;charset=utf-8;'});
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'recipes.json');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    // Export all recipes to a PDF file
    async exportRecipesToPDF() {
      try {
        // JSON-Daten abrufen
        const response = await axios.get(`${BACKEND_IP}/recipes`);
        const recipes = response.data;

        // Neues PDF-Dokument erstellen
        const doc = new jsPDF();

        // Überschrift für das PDF
        doc.setFontSize(16);
        doc.text('Recipe List', 10, 10);
        let yPosition = 20;

        // Rezepte durchlaufen und hinzufügen
        recipes.forEach((recipe, index) => {
          if (yPosition > 250) { // Neue Seite hinzufügen, wenn der Platz ausgeht
            doc.addPage();
            yPosition = 10;
          }

          // Rezepttitel
          doc.setFontSize(14);
          doc.text(`${index + 1}. ${recipe.name}`, 10, yPosition);
          yPosition += 8;

          // Zutaten
          if (recipe.ingredients && recipe.ingredients.length > 0) {
            doc.setFontSize(12);
            doc.text('Ingredients:', 12, yPosition);
            yPosition += 6;

            recipe.ingredients.forEach((ingredient) => {
              const line = `- ${ingredient.quantity} ${ingredient.unit} ${ingredient.ingredient.name}`;
              doc.text(line, 14, yPosition);
              yPosition += 5;
            });
          }

          // Beschreibung
          if (recipe.description) {
            doc.setFontSize(12);
            doc.text('Description:', 12, yPosition);
            yPosition += 6;

            const descriptionLines = doc.splitTextToSize(recipe.description, 180);
            descriptionLines.forEach((line) => {
              if (yPosition > 280) { // Neue Seite für lange Beschreibungen
                doc.addPage();
                yPosition = 10;
              }
              doc.text(line, 14, yPosition);
              yPosition += 5;
            });
          }

          // Details wie Schwierigkeit und Zeit
          if (recipe.difficulty || recipe.time) {
            const difficulty = recipe.difficulty ? `Difficulty: ${recipe.difficulty}` : '';
            const time = recipe.time ? `Time: ${recipe.time} mins` : '';
            doc.text(`${difficulty} ${time}`, 12, yPosition);
            yPosition += 10;
          }

          yPosition += 5; // Abstand zwischen Rezepten
        });

        // PDF speichern
        doc.save('recipes.pdf');
      } catch (error) {
        console.error('Error exporting recipes to PDF:', error);
        alert('Failed to export recipes to PDF.');
      }
    },
    // Toggle the favorite status of a recipe
    async toggleFavorite(recipe) {
      try {
        await axios.patch(`${BACKEND_IP}/recipes/${recipe.id}/favorite`);
        recipe.favorite = !recipe.favorite;
      } catch (error) {
        console.error('Error toggling favorite:', error);
        alert('There was an error toggling the favorite status.');
      }
    },
    showFavorites() {
      this.filters.favorites = !this.filters.favorites;
    },
  },
};
</script>

<style scoped>
.recipe-card {
  transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
  cursor: pointer;
}

.recipe-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.4) !important;
}

.recipe-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-bottom: 1px solid #ddd;
}

.close-icon {
  cursor: pointer;
}

.badge {
  cursor: pointer;
  transition: background-color 0.3s ease-in-out, color 0.3s ease-in-out;
}

.text-bg-outline-success {
  background-color: transparent;
  border: 1px solid #198754;
  color: #198754;
}

.text-bg-outline-warning {
  background-color: transparent;
  border: 1px solid #ffc107;
  color: #ffc107;
}

.text-bg-outline-danger {
  background-color: transparent;
  border: 1px solid #dc3545;
  color: #dc3545;
}

.text-bg-outline-primary {
  background-color: transparent;
  border: 1px solid #0d6efd;
  color: #0d6efd;
}

.floating-button {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  transition: box-shadow 0.3s ease-in-out;
  position: fixed;
  right: 20px;
  z-index: 1050;
}

.floating-button:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.4);
}

.floating-button i {
  transition: transform 0.3s ease-in-out;
}

.floating-button:hover i {
  transform: scale(1.1); /* Leichte Animation beim Hover */
}

.placeholder {
  opacity: max(0.25) !important;
}
</style>