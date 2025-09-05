<template>
  <div class="card mb-3 p-2 shadow-sm ingredient-card">
    <div class="card-body p-2">
      <!-- Display the name of the ingredient -->
      <h5 class="card-title mb-2">{{ ingredient.name }}</h5>
      <div class="ingredient-tags mb-2">
        <!-- Display a badge if the ingredient is used in a recipe -->
        <span
            v-if="ingredient.usedInRecipe"
            class="badge text-bg-secondary me-1"
            @mouseover="initializePopover"
            @mouseleave="destroyPopover"
            ref="popoverTrigger"
        >
        <i class="fa-solid fa-utensils"></i> in recipe
      </span>
        <!-- Display a badge if the ingredient requires cooling -->
        <span v-if="ingredient.cooled" class="badge text-bg-primary me-1">
          <i class="fa-solid fa-temperature-low"></i> cooled
        </span>
        <!-- Display a badge if the ingredient can be frozen -->
        <span v-if="ingredient.freeze" class="badge text-bg-info me-1">
          <i class="fa-solid fa-snowflake"></i> freezed
        </span>
      </div>
      <div class="d-flex justify-content-end">
        <!-- Button to edit the ingredient, emits an 'edit-ingredient' event -->
        <button
            type="button"
            class="btn text-secondary bg-transparent btn-sm me-2"
            @click="$emit('edit-ingredient', ingredient.id)"
        >
          <i class="fa-solid fa-pen"></i>
        </button>
        <!-- Button to delete the ingredient, emits a 'delete-ingredient' event -->
        <!-- Button is disabled if the ingredient is used in a recipe -->
        <button
            type="button"
            class="btn text-danger bg-transparent border-0 btn-sm"
            @click="$emit('delete-ingredient', ingredient.id)"
            :disabled="ingredient.usedInRecipe"
            :title="ingredient.usedInRecipe ? 'Cannot delete, used in recipe' : ''"
        >
          <i class="fa-solid fa-trash"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { Popover } from "bootstrap";
import axios from "axios";
import { BACKEND_IP } from "@/config.js";

export default {
  props: {
    ingredient: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      recipes: [], // List of recipes using the ingredient
      popoverInstance: null, // Bootstrap Popover instance
    };
  },
  methods: {
    // Fetch recipes for the ingredient from the backend
    async fetchRecipesForIngredient() {
      try {
        const response = await axios.get(`${BACKEND_IP}/ingredients/${this.ingredient.id}/recipes`);
        this.recipes = response.data;
      } catch (error) {
        console.error("Error fetching recipes:", error);
        this.recipes = [];
      }
    },
    // Initialize and display the Bootstrap Popover with recipe data
    initializePopover() {
      if (!this.$refs.popoverTrigger) return;

      this.fetchRecipesForIngredient().then(() => {
        const triggerEl = this.$refs.popoverTrigger;

        if (triggerEl) {
          const content = document.createElement("div");
          if (this.recipes.length > 0) {
            const ul = document.createElement("ul");
            ul.classList.add("list-group", "p-0", "m-0");

            this.recipes.forEach((recipe) => {
              const li = document.createElement("li");
              li.classList.add("list-group-item");

              const link = document.createElement("a");
              link.textContent = recipe.name;
              link.href = "javascript:void(0)";
              link.classList.add("text-decoration-none", "text-dark");

              link.addEventListener("click", () => this.navigateToRecipe(recipe.id));

              li.appendChild(link);
              ul.appendChild(li);
            });

            content.appendChild(ul);
          } else {
            const noRecipesMessage = document.createElement("strong");
            noRecipesMessage.textContent = "No recipes found";
            content.appendChild(noRecipesMessage);
          }

          // Initialize Popover
          this.popoverInstance = Popover.getOrCreateInstance(triggerEl, {
            content: content,
            html: true,
            placement: "top",
            trigger: "manual",
          });

          this.popoverInstance.show();

          const popoverElement = document.querySelector(".popover");
          popoverElement.addEventListener("mouseenter", () => clearTimeout(this.closePopoverTimeout));
          popoverElement.addEventListener("mouseleave", this.destroyPopover);
        }
      });
    },
    // Destroy the current Popover instance
    destroyPopover() {
      if (this.popoverInstance) {
        this.closePopoverTimeout = setTimeout(() => {
          if (this.popoverInstance) {
            this.popoverInstance.hide();
            this.popoverInstance = null;
          }
        }, 200); // Kurze Verzögerung, um Interaktionen zu ermöglichen
      }
    },
    navigateToRecipe(recipeId) {
      this.$router.push({ path: "/recipes", query: { recipeid: recipeId } });

      // Verzögerung, bevor das Popover geschlossen wird
      setTimeout(() => {
        this.destroyPopover();
      }, 100);
    }
  },
};
</script>

<style scoped>
.ingredient-card {
  transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
}

.ingredient-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.4) !important;
}
</style>