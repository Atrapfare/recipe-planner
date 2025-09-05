<template>
  <form @submit.prevent="submitIngredient">
    <!-- Form for adding a new ingredient -->
    <div class="form-group">
      <label for="name" class="mb-1">Ingredient Name</label>
      <!-- Input field for the ingredient name, bound to the 'ingredient.name' property -->
      <input v-model="ingredient.name" type="text" class="form-control" id="name" placeholder="Enter ingredient name"
             required/>
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
      <!-- Button to cancel and reset the form -->
      <button type="button" class="btn btn-danger" data-bs-dismiss="modal" @click="resetForm">Cancel</button>
      <!-- Button to submit the form -->
      <button type="submit" class="btn btn-success">Save Changes</button>
    </div>
  </form>
</template>

<script>
import axios from 'axios';
import {BACKEND_IP} from '@/config.js';

export default {
  name: 'AddIngredientItem',
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
  methods: {
    /**
     * Resets the form fields to their initial state.
     */
    resetForm() {
      this.ingredient = {
        name: '',
        cooled: false,
        freeze: false,
      };
    },
    /**
     * Submits the ingredient data to the backend.
     * Sends a POST request to the backend to create a new ingredient.
     * Emits an event upon successful creation.
     */
    async submitIngredient() {
      try {
        const response = await axios.post(
            `${BACKEND_IP}/ingredients`,
            {
              name: this.ingredient.name,
              cooled: this.ingredient.cooled,
              freeze: this.ingredient.freeze,
            },
            {
              headers: { 'Content-Type': 'application/json' },
            }
        );

        if (response.status === 201) {
          // Notify user and emit an event to inform the parent component
          alert('Ingredient added successfully!');
          this.$emit('ingredient-added');
          this.resetForm();
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