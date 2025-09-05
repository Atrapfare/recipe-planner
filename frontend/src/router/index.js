import { createRouter, createWebHistory } from 'vue-router';
// Importing component views for the routes
import Calendar from '@/views/calendar/CalendarView.vue'; // Calendar view for the home page
import Recipe from '@/views/recipe/Recipes.vue'; // Recipes view for managing recipes
import Shopping from '@/views/shopping/Shopping.vue'; // Shopping view for managing shopping lists
import Ingredient from '@/views/ingredient/Ingredients.vue'; // Ingredients view for managing ingredients

// Defining application routes with corresponding components
const routes = [
    { path: '/', name: 'Calendar', component: Calendar }, // Route for the calendar view
    { path: '/recipes', name: 'Recipe', component: Recipe }, // Route for the recipes view
    { path: '/shopping', name: 'Shopping', component: Shopping }, // Route for the shopping view
    { path: '/ingredients', name: 'Ingredient', component: Ingredient }, // Route for the ingredients view
];

// Creating the Vue Router instance with history mode and defined routes
const router = createRouter({
    history: createWebHistory(), // Using HTML5 history mode for clean URLs
    routes, // Setting up routes for the application
});

// Exporting the router to be used in the Vue application
export default router;