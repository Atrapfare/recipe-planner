<template>
  <div class="card border-0 mb-3">
    <div class="row g-0">
      <div class="col-md-4">
        <!-- Recipe image display -->
        <img :src="recipe.image" class="img-fluid rounded-start" alt="Recipe Image">
      </div>
      <div class="col-md-8">
        <div class="card-body border-0">
          <!-- Recipe title -->
          <h5 class="card-title">{{ recipe.name }}</h5>

          <!-- Display time and difficulty badges -->
          <div class="mb-3">
            <!-- Display preparation time if available -->
            <span v-if="recipe.time" class="badge text-bg-primary me-2">
              <i class="fa-solid fa-clock"></i> {{ recipe.time }} min
            </span>
            <!-- Display difficulty badge based on recipe difficulty -->
            <span v-if="recipe.difficulty" :class="getDifficultyBadgeClass(recipe.difficulty)">
              {{ recipe.difficulty }}
            </span>
          </div>

          <!-- Portion scaling input -->
          <div class="d-flex align-items-center mb-3">
            <label for="portionInput" class="me-2">Scale to Portions:</label>
            <input
                id="portionInput"
                type="number"
                class="form-control w-25"
                min="1"
                max="20"
                v-model.number="scaleFactor"
                @input="scaleIngredients"
            />
          </div>

          <!-- Ingredients table -->
          <h6>Ingredients:</h6>
          <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
              <th scope="col">Ingredient</th>
              <th scope="col">Quantity</th>
              <th scope="col">Unit</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(ingredient, index) in scaledIngredients" :key="index">
              <td>{{ ingredient.ingredient.name }}</td>
              <td>{{ ingredient.quantity }}</td>
              <td>{{ ingredient.unit }}</td>
            </tr>
            </tbody>
          </table>

          <hr>
          <!-- Recipe description with formatted line breaks -->
          <p class="card-text" v-html="formatDescription(recipe.description)"></p>
        </div>
      </div>
    </div>

    <!-- Action buttons for editing, deleting, or closing the recipe -->
    <div class="d-flex justify-content-between">
      <div>
        <!-- Edit recipe button -->
        <button type="button" class="btn btn-primary me-2" @click="editRecipe">
          Edit
        </button>
        <!-- Delete recipe button -->
        <button type="button" class="btn btn-danger" @click="deleteRecipe">
          Delete
        </button>
      </div>
      <!-- Close recipe button -->
      <button type="button" class="btn btn-secondary" @click="closeRecipe">
        Close
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import {BACKEND_IP} from '@/config.js';
import { Modal } from 'bootstrap';

export default {
  name: 'ShowRecipeItem',
  props: {
    // ID of the recipe to be displayed
    recipeId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      recipe: {
        name: '', // Recipe name
        description: '', // Recipe description
        ingredients: [], // List of recipe ingredients
        image: '', // Recipe image URL
        time: '', // Preparation time
        difficulty: '' // Difficulty level
      },
      scaleFactor: 1, // Scaling factor for ingredient quantities
      scaledIngredients: [] // Scaled ingredient list
    };
  },
  async created() {
    // Load the recipe details when the component is created
    await this.loadRecipe();
  },
  watch: {
    // Watch for changes to the recipeId prop and reload the recipe
    recipeId: {
      immediate: true,
      handler() {
        this.loadRecipe();
      }
    }
  },
  beforeMount() {
    // Clean up modal backdrop before mounting
    this.cleanupBackdrop();
  },
  mounted() {
    // Handle modal-related events
    const modalElement = document.getElementById('showRecipeModal');
    if (modalElement) {
      modalElement.addEventListener('hide.bs.modal', () => {
        this.resetScale();
        this.cleanupBackdrop();
      });
    }
  },
  beforeUnmount() {
    // Remove modal-related event listeners
    const modalElement = document.getElementById('showRecipeModal');
    if (modalElement) {
      modalElement.removeEventListener('hide.bs.modal', this.resetScale);
    }
  },
  methods: {
    cleanupBackdrop() {
      // Remove leftover modal backdrops if they exist
      if (!document.body.classList.contains("modal-open")) {
        const backdrops = document.querySelectorAll(".modal-backdrop");
        backdrops.forEach((backdrop) => backdrop.remove());
      }
    },
    async loadRecipe() {
      // Fetch the recipe details from the backend
      try {
        const response = await axios.get(`${BACKEND_IP}/recipes/${this.recipeId}`);
        this.recipe = response.data;
        this.scaleIngredients();
      } catch (error) {
        console.error('Error loading recipe:', error);
        alert('Error loading recipe');
      }
    },
    scaleIngredients() {
      // Scale ingredient quantities based on the scale factor
      this.scaledIngredients = this.recipe.ingredients.map((ingredient) => {
        return {
          ingredient: ingredient.ingredient,
          quantity: (ingredient.quantity * this.scaleFactor).toFixed(2),
          unit: ingredient.unit
        };
      });
    },
    resetScale() {
      // Reset the scale factor and recalculate ingredients
      this.scaleFactor = 1;
      this.scaleIngredients();
    },
    closeRecipe() {
      // Emit event to notify the parent component to close the recipe
      this.$emit('close-recipe');
    },
    editRecipe() {
      // Emit event to notify the parent component to edit the recipe
      this.$emit('edit-recipe', this.recipe.id);
    },
    async deleteRecipe() {
      // Emit event to notify the parent component to delete the recipe
      this.$emit('delete-recipe', this.recipe.id);
    },
    getDifficultyBadgeClass(difficulty) {
      // Return the appropriate badge class based on the difficulty level
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
    formatDescription(description) {
      // Format the description by replacing line breaks with <br> tags
      if (!description) return "";
      return description.replace(/\n/g, "<br>");
    },
  }
};
</script>

<style scoped>
.card {
  max-width: 800px;
  margin: 20px auto;
}

.card img {
  max-height: 250px;
  object-fit: cover;
}

.card-body {
  padding: 20px;
}

.badge {
  font-size: 0.9rem;
  transition: background-color 0.3s ease-in-out, color 0.3s ease-in-out;
}
</style>