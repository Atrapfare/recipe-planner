// Import Bootstrap CSS for styling
import 'bootstrap/dist/css/bootstrap.min.css';

// Import Bootstrap JS for interactive components
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

// Import FontAwesome CSS for icons
import './assets/fontawesome/css/fontawesome.min.css';
import './assets/fontawesome/css/brands.min.css';
import './assets/fontawesome/css/solid.min.css';
import './assets/fontawesome/css/regular.min.css';

// Import VueCal CSS for calendar styling
import 'vue-cal/dist/vuecal.css';

// Import Vue and the main app component
import { createApp } from 'vue';
import App from './App.vue';

// Import the router for navigation
import router from './router';

// Create the Vue app instance
const app = createApp(App);

// Use the router in the app
app.use(router);

// Mount the app to the DOM element with id 'app'
app.mount('#app');