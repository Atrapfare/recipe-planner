<template>
  <div>
    <!-- Vue Cal component for calendar view -->
    <vue-cal
        class="vuecal--green-theme"
        :events="events"
        :time-from="0 * 60"
        :time-to="24 * 60"
        :disable-views="['years', 'year']"
        editable-events
        cell-contextmenu
        @cell-click="handleCellClick"
        @event-click="openRecipeFromEvent"
        @event-create="createEvent"
        @event-update="updateEvent"
        @event-delete="deleteEvent"
        @event-drop="updateEvent"
        @ready="loadInitialData"
    />

    <!-- Modal for adding or editing events -->
    <div class="modal fade" id="eventModal" tabindex="-1" aria-labelledby="eventModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="eventModalLabel">
              {{ editingEvent ? 'Edit Event' : 'Add Event' }} <!-- Display mode based on editing -->
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <!-- Dropdown to select a recipe -->
            <label for="recipe">Select Recipe:</label>
            <select v-model="selectedRecipe" id="recipe" class="form-select">
              <option v-for="recipe in recipes" :value="recipe.id" :key="recipe.id">
                {{ recipe.name }}
              </option>
            </select>
            <!-- Input for event start time -->
            <div class="mt-3">
              <label for="start-time">Start Time:</label>
              <input type="datetime-local" id="start-time" class="form-control" v-model="eventStart"/>
            </div>
            <!-- Input for event end time -->
            <div class="mt-3">
              <label for="end-time">End Time:</label>
              <input type="datetime-local" id="end-time" class="form-control" v-model="eventEnd"/>
            </div>
            <!-- Input for number of persons -->
            <div class="mt-3">
              <label for="persons">Number of Persons:</label>
              <input
                  type="number"
                  id="persons"
                  class="form-control"
                  v-model.number="eventPersons"
                  min="1"/>
            </div>
          </div>
          <div class="modal-footer">
            <!-- Button to save the event -->
            <button type="button" class="btn btn-success" @click="saveEvent">
              {{ editingEvent ? 'Update Event' : 'Create Event' }}
            </button>
            <!-- Button to view the recipe of the event -->
            <button v-if="editingEvent" type="button" class="btn btn-primary" @click="goToRecipeDetails">
              View Recipe
            </button>
            <!-- Button to delete the event -->
            <button
                v-if="editingEvent"
                type="button"
                class="btn btn-danger"
                @click="deleteEvent(editingEvent)"
            >
              <i class="fa-solid fa-trash"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import VueCal from "vue-cal"; // Import VueCal for calendar functionality
import axios from "axios"; // Import Axios for HTTP requests
import {BACKEND_IP} from '@/config.js'; // Import backend IP configuration
import router from "@/router/index.js"; // Import Vue Router
import {Modal} from 'bootstrap'; // Import Bootstrap modal functionality

export default {
  components: {VueCal}, // Register VueCal component
  data() {
    return {
      events: [], // List of calendar events
      recipes: [], // List of available recipes
      selectedRecipe: null, // ID of the selected recipe
      eventStart: "", // Event start time
      eventEnd: "", // Event end time
      eventPersons: 1, // Number of persons for the event
      editingEvent: null, // Holds the event being edited, if any
    };
  },
  methods: {
    async createEvent(eventData) {
      // Create a new event via the API
      try {
        const response = await axios.post(`${BACKEND_IP}/events`, eventData);
        this.events.push({
          id: response.data.id,
          start: new Date(response.data.startTime),
          end: new Date(response.data.endTime),
          title: `Rezept ID: ${response.data.recipe.id}`,
        });
      } catch (error) {
        //console.error("Fehler beim Erstellen eines Events:", error);
      }
    },
    async fetchRecipes() {
      // Fetch all recipes from the backend
      try {
        const response = await axios.get(`${BACKEND_IP}/recipes`);
        this.recipes = response.data;
      } catch (error) {
        console.error("Fehler beim Laden der Rezepte:", error);
      }
    },
    async fetchEvents() {
      // Fetch all events from the backend
      try {
        const response = await axios.get(`${BACKEND_IP}/events`);
        this.events = response.data.map((event) => ({
          id: event.id,
          start: new Date(event.startTime),
          end: new Date(event.endTime),
          title: `${this.getRecipeName(event.recipe.id)} <br> ${event.persons} person`,
          recipeId: event.recipe.id,
          persons: event.persons,
        }));
      } catch (error) {
        console.error("Error loading events:", error);
      }
    },
    openRecipeFromEvent(event) {
      // Open modal to edit the selected event
      this.editingEvent = event;

      const start = new Date(event.start);
      const end = new Date(event.end);

      this.eventStart = start.toLocaleDateString("en-CA") + "T" + start.toTimeString().slice(0, 5);
      this.eventEnd = end.toLocaleDateString("en-CA") + "T" + end.toTimeString().slice(0, 5);

      this.selectedRecipe = event.recipeId;
      this.eventPersons = event.persons;

      const modalElement = document.getElementById("eventModal");
      const modal = new Modal(modalElement, {
        backdrop: true,
        keyboard: false
      });
      modal.show();
    },

    goToRecipeDetails() {
      // Navigate to recipe details page
      const modal = new Modal(document.getElementById("eventModal"));
      modal.hide();
      if (this.selectedRecipe) {
        router.push(`/recipes?recipeid=${this.selectedRecipe}`);
      } else {
        alert("No recipe selected.");
      }
    },
    getRecipeName(recipeId) {
      // Get the name of a recipe by its ID
      const recipe = this.recipes.find(r => r.id === recipeId);
      return recipe ? recipe.name : "Unknown Recipe";
    },
    handleCellClick(cellInfo) {
      // Handle a click on a calendar cell
      console.log("Cell Info:", cellInfo);

      const clickedDate = cellInfo?.date
          ? new Date(cellInfo.date)
          : new Date(cellInfo);

      if (isNaN(clickedDate.getTime())) {
        console.error("Invalid date:", cellInfo.date);
        return;
      }

      this.eventStart = clickedDate.toISOString().slice(0, 16);
      this.eventEnd = new Date(clickedDate.getTime() + 60 * 60 * 2000)
          .toISOString()
          .slice(0, 16);

      this.selectedRecipe = null;
      this.editingEvent = null;

      const modalElement = document.getElementById("eventModal");
      const modal = new Modal(modalElement, {
        backdrop: true,
        keyboard: false
      });
      modal.show();
    },
    async saveEvent() {
      if (!this.selectedRecipe) {
        alert("Please choose a recipe.");
        return;
      }

      if (new Date(this.eventStart) >= new Date(this.eventEnd)) {
        alert("Please enter a valid time: Start time must be before end time.");
        return;
      }

      const newEvent = {
        startTime: this.eventStart,
        endTime: this.eventEnd,
        persons: this.eventPersons,
        recipe: {id: this.selectedRecipe},
      };

      try {
        if (this.editingEvent) {
          const response = await axios.put(
              `${BACKEND_IP}/events/${this.editingEvent.id}`,
              newEvent
          );
          const updatedEvent = response.data;
          this.events = this.events.map((event) =>
              event.id === updatedEvent.id
                  ? {
                    id: updatedEvent.id,
                    start: new Date(updatedEvent.startTime),
                    end: new Date(updatedEvent.endTime),
                    title: `${this.getRecipeName(updatedEvent.recipe.id)} (${updatedEvent.persons} persons)`,
                    recipeId: updatedEvent.recipe.id,
                  }
                  : event
          );
        } else {
          const response = await axios.post(`${BACKEND_IP}/events`, newEvent);
          const createdEvent = response.data;
          this.events.push({
            id: createdEvent.id,
            start: new Date(createdEvent.startTime),
            end: new Date(createdEvent.endTime),
            title: `${this.getRecipeName(createdEvent.recipe.id)} (${createdEvent.persons} persons)`,
            recipeId: createdEvent.recipe.id,
          });
        }
        const modal = Modal.getInstance(document.getElementById("eventModal"));
        modal.hide();
      } catch (error) {
        console.error(
            this.editingEvent
                ? "Error updating the event:"
                : "Error adding the event:",
            error
        );
        alert("Error saving the event.");
      }
    },
    async updateEvent({event, oldDate, newDate, originalEvent}) {
      // Update an event via drag-and-drop
      try {
        console.log("Event updated via drag-and-drop:", {event, oldDate, newDate, originalEvent});

        // Extract the original event ID from either the originalEvent or event object
        const originalEventId = originalEvent?.id || event?.id;
        const originalEventFromList = this.events.find(e => e.id === originalEventId);

        if (!originalEventFromList) {
          alert("The original event was not found.");
          return;
        }

        // Retain recipe ID and other data from the original event
        const recipeId = originalEventFromList.recipeId;
        const persons = originalEventFromList.persons;

        if (!recipeId) {
          alert("The event does not have an associated recipe ID.");
          return;
        }

        // Create a new event object with updated data
        const updatedEvent = {
          startTime: new Date(event.start).toISOString(),
          endTime: new Date(event.end).toISOString(),
          recipe: {id: recipeId},
          persons: persons,
        };

        // API call to update the event on the backend
        const response = await axios.put(`${BACKEND_IP}/events/${originalEventFromList.id}`, updatedEvent);
        // Update the event in the local list with the response data
        const updatedEventData = response.data;
        this.events = this.events.map(e =>
            e.id === updatedEventData.id
                ? {
                  id: updatedEventData.id,
                  start: new Date(updatedEventData.startTime),
                  end: new Date(updatedEventData.endTime),
                  title: `${this.getRecipeName(updatedEventData.recipe.id)} (${updatedEventData.persons} persons)`,
                  recipeId: updatedEventData.recipe.id,
                  persons: updatedEventData.persons,
                }
                : e
        );

        alert("The event was successfully updated.");
      } catch (error) {
        console.error("Error updating the event:", error);
        alert("The event could not be updated.");
      }
    },

    async deleteEvent(event) {
      // Delete an event from the backend and update the frontend
      if (!event) {
        console.error("No event specified for deletion.");
        return;
      }

      try {
        await axios.delete(`${BACKEND_IP}/events/${event.id}`);

        // Remove the deleted event from the local list
        this.events = this.events.filter(e => e.id !== event.id);

        const modal = Modal.getInstance(document.getElementById("eventModal"));
        modal.hide();

        alert("Event deleted successfully.");
      } catch (error) {
        console.error("Error deleting the event:", error);
        alert("Failed to delete the event.");
      }
    },
    async loadInitialData() {
      // Fetch recipes and events when the component is loaded
      await this.fetchRecipes();
      await this.fetchEvents();
      this.startUpdatingData(); // Start periodic updates for data
    },
    startUpdatingData() {
      // Periodically update recipes and events
      this.updateInterval = setInterval(this.fetchRecipes, 10000); // Update recipes every 10 seconds
      this.updateInterval = setInterval(this.fetchEvents, 1000); // Update events every second
    },
  },
  mounted() {
// Load data when the component is mounted
    this.loadInitialData();
  },
};
</script>
<style scoped>
.vuecal--green-theme {
  height: 85vh;
  overflow-y: auto;
}
</style>
