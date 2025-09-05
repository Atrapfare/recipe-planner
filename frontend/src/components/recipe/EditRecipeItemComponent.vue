<template>
  <form @submit.prevent="submitRecipe">
    <!-- Form for editing an existing recipe -->
    <div class="form-group">
      <label for="title" class="mb-1">Recipe Title</label>
      <!-- Input for recipe title, bound to 'recipe.name' -->
      <input
          v-model="recipe.name"
          type="text"
          class="form-control"
          id="title"
          placeholder="Enter recipe title"
          required
      />
    </div>

    <div class="form-group mt-3">
      <label class="mb-1">Ingredients</label>
      <div
          v-for="(ingredient, index) in recipe.ingredients"
          :key="index"
          class="ingredient-inputs position-relative"
      >
        <!-- Input for ingredient name with autocomplete suggestions -->
        <div class="dropdown w-100">
          <input
              type="text"
              class="form-control"
              v-model="ingredient.ingredientName"
              @input="filterSuggestions(index)"
              placeholder="Ingredient"
              autocomplete="off"
              required
          />
          <!-- Dropdown menu for ingredient suggestions -->
          <ul
              v-if="suggestedIngredients[index]?.length > 0"
              class="dropdown-menu show w-100"
          >
            <li
                v-for="suggestion in suggestedIngredients[index]"
                :key="suggestion.id"
                class="dropdown-item"
                @click="selectIngredient(index, suggestion)"
            >
              {{ suggestion.name }}
            </li>
          </ul>
        </div>

        <!-- Input for ingredient quantity -->
        <input
            v-model.number="ingredient.quantity"
            type="number"
            class="form-control w-25"
            placeholder="Quantity"
            min="0.01"
            step="0.01"
            required
        />

        <!-- Dropdown for selecting ingredient unit -->
        <select v-model="ingredient.unit" class="form-control w-25" required>
          <option disabled value="">Select unit</option>
          <option v-for="unit in units" :key="unit" :value="unit">
            {{ unit }}
          </option>
        </select>

        <!-- Button to remove an ingredient -->
        <button
            type="button"
            @click="removeIngredient(index)"
            :disabled="recipe.ingredients.length === 1"
            class="btn btn-danger mt-2"
        >
          <i class="fa-solid fa-trash"></i>
        </button>
      </div>

      <!-- Button to add a new ingredient -->
      <button type="button" @click="addIngredient" class="btn btn-secondary mt-2">
        <i class="fa-solid fa-plus"></i> Add Ingredient
      </button>
    </div>

    <div class="form-group mt-3">
      <label for="description" class="mb-1">Description</label>
      <!-- Textarea for recipe description -->
      <textarea
          v-model="recipe.description"
          class="form-control"
          id="description"
          rows="5"
          placeholder="Enter recipe description"
          required
      ></textarea>
    </div>

    <div class="form-group mt-3">
      <label for="time" class="mb-1">Time</label>
      <div class="input-group">
        <!-- Input for preparation time -->
        <input
            v-model.number="recipe.time"
            type="number"
            class="form-control"
            id="time"
            placeholder="Enter preparation time"
            min="1"
            required
        />
        <span class="input-group-text">min</span>
      </div>
    </div>

    <div class="form-group mt-3">
      <label class="mb-1">Difficulty</label>
      <div class="d-flex">
        <!-- Badge for Easy difficulty -->
        <span
            @click="setDifficulty('Easy')"
            :class="[
            'badge',
            recipe.difficulty === 'Easy' ? 'text-bg-success' : 'text-bg-outline-success',
            'me-2',
            'clickable'
          ]"
        >
          Easy
        </span>
        <!-- Badge for Medium difficulty -->
        <span
            @click="setDifficulty('Medium')"
            :class="[
            'badge',
            recipe.difficulty === 'Medium' ? 'text-bg-warning' : 'text-bg-outline-warning',
            'me-2',
            'clickable'
          ]"
        >
          Medium
        </span>
        <!-- Badge for Hard difficulty -->
        <span
            @click="setDifficulty('Hard')"
            :class="[
            'badge',
            recipe.difficulty === 'Hard' ? 'text-bg-danger' : 'text-bg-outline-danger',
            'clickable'
          ]"
        >
          Hard
        </span>
      </div>
    </div>

    <div class="form-group mt-3">
      <label for="image" class="mb-1">Recipe Image</label>
      <!-- Input for uploading a recipe image -->
      <input
          type="file"
          @change="onImageChange"
          class="form-control"
          id="image"
          accept="image/*"
      />
    </div>

    <div class="modal-footer mt-3">
      <!-- Button to reset the form and reload the recipe -->
      <button
          type="button"
          class="btn btn-danger"
          data-bs-dismiss="modal"
          @click="resetForm"
      >
        Cancel
      </button>
      <!-- Button to submit the form and update the recipe -->
      <button type="submit" class="btn btn-success">Update Recipe</button>
    </div>
  </form>
</template>

<script>
import axios from "axios";
import { BACKEND_IP } from "@/config.js";
import { Modal } from 'bootstrap';

export default {
  name: "EditRecipeItem",
  props: {
    // ID of the recipe to be edited
    recipeId: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      recipe: {
        name: "",
        description: "",
        time: "",
        difficulty: "Easy",
        ingredients: [{ingredientName: "", ingredientId: null, quantity: 1, unit: ""}],
        image: "",
      },
      allIngredients: [], // List of all available ingredients
      suggestedIngredients: [[]], // Autocomplete suggestions for ingredient inputs
      units: ["grams", "milliliters", "pieces", "tablespoons", "teaspoons", "cups", "liters", "kilograms"], // Available units
    };
  },
  watch: {
    // Watch for changes to the recipe ID and reload the recipe when it changes
    recipeId: {
      immediate: true,
      async handler(newRecipeId) {
        if (newRecipeId) {
          await this.loadRecipe();
        }
      },
    },
  },
  async created() {
    // Fetch ingredients and load the recipe when the component is created
    await this.fetchIngredients();
    await this.loadRecipe();
  },
  methods: {
    async fetchIngredients() {
      // Fetch all ingredients from the backend
      try {
        const response = await axios.get(`${BACKEND_IP}/ingredients`);
        this.allIngredients = response.data;
      } catch (error) {
        console.error("Error fetching ingredients:", error);
        alert("Error fetching ingredients");
      }
    },
    async loadRecipe() {
      // Load the recipe details from the backend
      try {
        const response = await axios.get(`${BACKEND_IP}/recipes/${this.recipeId}`);
        this.recipe = {
          ...response.data,
          ingredients: response.data.ingredients.map((ri) => ({
            ingredientName: ri.ingredient.name,
            ingredientId: ri.ingredient.id,
            quantity: ri.quantity,
            unit: ri.unit,
          })),
          image: response.data.image
        };
        this.suggestedIngredients = this.recipe.ingredients.map(() => []);
      } catch (error) {
        console.error("Error loading recipe:", error);
        alert("Error loading recipe");
      }
    },
    addIngredient() {
      // Add a new ingredient row
      this.recipe.ingredients.push({ingredientName: "", ingredientId: null, quantity: 1, unit: ""});
      this.suggestedIngredients.push([]);
    },
    removeIngredient(index) {
      // Remove an ingredient row
      this.recipe.ingredients.splice(index, 1);
      this.suggestedIngredients.splice(index, 1);
    },
    resetForm() {
      // Reload the recipe details to reset the form
      this.loadRecipe();
    },
    async addNewIngredient(index) {
      const ingredientName = this.recipe.ingredients[index].ingredientName;
      if (!ingredientName) {
        alert("Ingredient name cannot be empty!");
        return;
      }

      try {
        const response = await axios.post(`${BACKEND_IP}/ingredients`, {
          name: ingredientName,
          cooled: false, // Passe die Attribute an deine Anforderungen an
          freeze: false,
        });

        const newIngredient = response.data;
        this.allIngredients.push(newIngredient);
        this.recipe.ingredients[index].ingredientId = newIngredient.id;
        alert(`Ingredient "${newIngredient.name}" added successfully!`);
      } catch (error) {
        console.error("Error adding ingredient:", error);
        alert("There was an error adding the ingredient.");
      }
    },
    filterSuggestions(index) {
      const query = this.recipe.ingredients[index].ingredientName.toLowerCase();
      if (!query) {
        this.suggestedIngredients[index] = [];
        this.recipe.ingredients[index].ingredientId = null;
      } else {
        const matches = this.allIngredients.filter((ingredient) =>
            ingredient.name.toLowerCase().includes(query)
        );
        this.suggestedIngredients[index] = matches;

        if (matches.length === 0) {
          this.suggestedIngredients[index].push({
            name: `Add "${this.recipe.ingredients[index].ingredientName}" as new ingredient`,
            id: null,
          });
        }
      }
    },
    selectIngredient(index, suggestion) {
      if (!suggestion.id) {
        this.addNewIngredient(index);
      } else {
        this.recipe.ingredients[index].ingredientName = suggestion.name;
        this.recipe.ingredients[index].ingredientId = suggestion.id;
        this.suggestedIngredients[index] = [];
      }
    },
    setDifficulty(level) {
      // Set the difficulty level of the recipe
      this.recipe.difficulty = level;
    },
    onImageChange(event) {
      // Handle image file input and convert it to a base64 string
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.recipe.image = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    },
    async submitRecipe() {
      // Submit the updated recipe details to the backend
      try {
        const payload = {
          name: this.recipe.name,
          description: this.recipe.description,
          time: this.recipe.time,
          difficulty: this.recipe.difficulty,
          image: this.recipe.image,
          ingredients: this.recipe.ingredients.map((ing) => ({
            ingredient: {id: ing.ingredientId},
            quantity: ing.quantity,
            unit: ing.unit,
          })),
        };
        const response = await axios.put(
            `${BACKEND_IP}/recipes/${this.recipeId}`,
            payload,
            {headers: {"Content-Type": "application/json"}}
        );
        if (response.status === 200) {
          alert("Recipe updated successfully!");
          this.$emit("recipe-edited", response.data);
          this.resetForm();
          const modalElement = document.querySelector("#editRecipeModal");
          const modalInstance = Modal.getInstance(modalElement);
          modalInstance?.hide();
        }
      } catch (error) {
        console.error("Error updating recipe:", error);
        alert("There was an error updating the recipe.");
      }
    },
  },
};
</script>

<style scoped>
.form-group label {
  font-weight: bold;
}

.ingredient-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.dropdown-menu {
  max-height: 200px;
  overflow-y: auto;
}

.is-invalid {
  border-color: red;
}

.badge.text-bg-outline-success {
  color: #198754;
  background-color: transparent;
  border: 1px solid #198754;
}

.badge.text-bg-outline-warning {
  color: #ffc107;
  background-color: transparent;
  border: 1px solid #ffc107;
}

.badge.text-bg-outline-danger {
  color: #dc3545;
  background-color: transparent;
  border: 1px solid #dc3545;
}

.clickable {
  cursor: pointer;
}
</style>