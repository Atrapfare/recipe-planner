<template>
  <form @submit.prevent="submitIngredient">
    <!-- Form to edit an existing ingredient -->
    <div class="form-group">
      <label for="name" class="mb-1">Ingredient Name</label>
      <!-- Input field for editing the ingredient name, bound to 'ingredient.name' -->
      <input v-model="ingredient.name" type="text" class="form-control" id="name" placeholder="Enter ingredient name" required />
    </div>

    <div class="form-group mt-3 d-flex">
      <!-- Badge to toggle the 'cooled' property of the ingredient -->
      <span @click="ingredient.cooled = !ingredient.cooled" :class="['badge', ingredient.cooled ? 'text-bg-primary' : 'text-bg-outline-primary', 'me-1', 'clickable']">
        <i class="fa-solid fa-temperature-low"></i> requires cooling
      </span>
      <!-- Badge to toggle the 'freeze' property of the ingredient -->
      <span @click="ingredient.freeze = !ingredient.freeze" :class="['badge', ingredient.freeze ? 'text-bg-info' : 'text-bg-outline-info', 'me-1', 'clickable']">
        <i class="fa-solid fa-snowflake"></i> freezed
      </span>
    </div>

    <div class="modal-footer mt-4">
      <!-- Button to reset the form to the original ingredient data -->
      <button type="button" class="btn btn-danger" data-bs-dismiss="modal" @click="resetForm">Cancel</button>
      <!-- Button to submit the updated ingredient data -->
      <button type="submit" class="btn btn-success">Save Changes</button>
    </div>
  </form>
</template>

<script>
import axios from 'axios';
import {BACKEND_IP} from '@/config.js';

export default {
  name: 'EditIngredientItem',
  props: {
    // The ID of the ingredient to edit, passed as a prop
    ingredientId: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      ingredient: {
        // Holds the name of the ingredient
        name: '',
        // Indicates if the ingredient requires cooling
        cooled: false,
        // Indicates if the ingredient can be frozen
        freeze: false,
      },
    };
  },
  created() {
    // Load the ingredient details when the component is created
    this.loadIngredient();
  },
  methods: {
    /**
     * Resets the form fields to their original values by reloading the ingredient.
     */
    resetForm() {
      this.loadIngredient();
    },
    /**
     * Loads the ingredient details from the backend.
     * Fetches the data for the given ingredient ID and populates the form fields.
     */
    async loadIngredient() {
      try {
        const response = await axios.get(`${BACKEND_IP}/ingredients/${this.ingredientId}`);
        if (response.status === 200) {
          // Populate the ingredient data
          this.ingredient = response.data;
        }
      } catch (error) {
        // Handle errors during data loading
        console.error('Error loading ingredient:', error);
        alert('There was an error loading the ingredient details. Please try again later.');
      }
    },
    /**
     * Submits the updated ingredient data to the backend.
     * Sends a PUT request to update the ingredient and emits an event on success.
     */
    async submitIngredient() {
      try {
        const response = await axios.put(
            `${BACKEND_IP}/ingredients/${this.ingredientId}`,
            {
              name: this.ingredient.name,
              cooled: this.ingredient.cooled,
              freeze: this.ingredient.freeze,
            },
            {
              headers: {'Content-Type': 'application/json'},
            }
        );

        if (response.status === 200) {
          // Notify user and emit an event to inform the parent component
          alert('Ingredient updated successfully!');
          this.$emit('ingredient-edited');
        }
      } catch (error) {
        if (error.response.status === 409) {
          alert('Ingredient exists already!');
          this.resetForm();
        } else {
          // Log the error and notify the user
          console.error('Error adding ingredient:', error);
          alert('There was an error adding the ingredient. Please try again later.');
        }
      }
    },
  },
};
</script>

<style scoped>
.form-group label {
  font-weight: bold;
}

.badge.text-bg-outline-primary {
  color: #0d6efd;
  background-color: transparent;
  border: 1px solid #0d6efd;
}

.badge.text-bg-outline-info {
  color: #0dcaf0;
  background-color: transparent;
  border: 1px solid #0dcaf0;
}

.clickable {
  cursor: pointer;
}
</style>