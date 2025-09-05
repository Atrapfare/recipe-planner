<template>
  <div class="container mt-4">
    <!-- Header -->
    <h1 class="text-center mb-4">Shopping List</h1>

    <div class="row">
      <!-- Left column: Planned Recipes -->
      <div class="col-sm-12 col-md-6 col-lg-6">
        <div class="card mb-4">
          <div class="card-header bg-primary text-white">
            <h3 class="mb-0">Planned Recipes</h3>
          </div>
          <div class="card-body">
            <!-- Group events by date -->
            <div v-for="(events, date) in groupedEvents" :key="date" class="mb-4">
              <h5 class="text-secondary">{{ date }}</h5>
              <div class="card-deck">
                <!-- Render events for each date -->
                <div
                    v-for="event in events"
                    :key="event.id"
                    class="card event-item"
                    :class="{ 'border-primary': selectedEventIds.includes(event.id), 'shadow-sm': selectedEventIds.includes(event.id) }"
                    @click="toggleSelection(event.id)"
                >
                  <div class="card-body">
                    <h5 class="card-title">{{ event.recipe.name }}</h5>
                    <p class="card-text">{{ formatDate(event.startTime) }}
                      <!-- Badge to show number of persons -->
                      <span class="badge text-bg-warning ms-2">
                        <i class="fa-solid fa-person"></i> {{ event.persons }}
                      </span>
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right column: Shopping List -->
      <div class="col-sm-12 col-md-6 col-lg-6">
        <div class="card mb-4">
          <div class="card-header bg-success text-white">
            <h3 class="mb-0">Your List</h3>
          </div>
          <div class="card-body">
            <!-- Display shopping list with transition -->
            <transition-group name="list" tag="ul" class="list-group" v-if="shoppingList.length">
              <li
                  v-for="item in shoppingList"
                  :key="item.id"
                  :class="{ 'checked': item.checked }"
                  class="list-group-item d-flex justify-content-between align-items-center"
              >
                <div class="d-flex align-items-center">
                  <input
                      type="checkbox"
                      @change="removeItem(item)"
                      class="form-check-input me-2"
                  >
                  <strong>{{ item.name }}</strong>:
                  {{ (item.quantity * getEventPersons(item.eventId)).toFixed(2) }} {{ item.unit }}
                  <!-- Optional badges for cooled and frozen items -->
                  <span v-if="item.cooled" class="badge text-bg-primary ms-2">
                    <i class="fa-solid fa-temperature-low"></i> cooled
                  </span>
                  <span v-if="item.freeze" class="badge text-bg-info ms-2">
                    <i class="fa-solid fa-snowflake"></i> freezed
                  </span>
                </div>
              </li>
            </transition-group>
            <!-- Placeholder text if no items are selected -->
            <p v-else class="text-muted">Select planned events to generate your shopping list.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// Import dependencies
import axios from 'axios';
import {BACKEND_IP} from '@/config.js';

export default {
  data() {
    return {
      futureEvents: [], // List of future events
      selectedEventIds: [], // Selected event IDs
      shoppingList: [], // Items in the shopping list
    };
  },
  async created() {
    try {
      // Fetch events from API
      const response = await axios.get(`${BACKEND_IP}/events`);
      const now = new Date();
      // Filter events to include only those in the future
      this.futureEvents = response.data.filter(
          (event) => new Date(event.startTime) > now
      );
    } catch (error) {
      console.error("API Error:", error);
    }
  },
  computed: {
    groupedEvents() {
      const grouped = {};
      // Group events by date
      this.futureEvents.forEach(event => {
        const date = new Date(event.startTime).toLocaleDateString(undefined, {
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        });
        if (!grouped[date]) {
          grouped[date] = [];
        }
        grouped[date].push(event);
      });
      return grouped;
    },
  },
  methods: {
    toggleSelection(eventId) {
      // Add or remove event ID from selectedEventIds
      const index = this.selectedEventIds.indexOf(eventId);
      if (index === -1) {
        this.selectedEventIds.push(eventId);
      } else {
        this.selectedEventIds.splice(index, 1);
      }
      this.updateShoppingList();
    },
    formatDate(dateString) {
      // Format date string for display
      const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      };
      return new Date(dateString).toLocaleDateString(undefined, options);
    },
    getEventPersons(eventId) {
      // Retrieve the number of persons for a specific event
      const event = this.futureEvents.find((ev) => ev.id === eventId);
      return event ? event.persons : 1;
    },
    async updateShoppingList() {
      try {
        if (this.selectedEventIds.length === 0) {
          this.shoppingList = [];
          return;
        }
        // Fetch shopping list from the API based on selected events
        const response = await axios.post(
            `${BACKEND_IP}/shopping/filter`,
            this.selectedEventIds
        );
        // Enhance shopping list items with eventId
        this.shoppingList = response.data.map(item => {
          const relatedEvent = this.futureEvents.find(event =>
              this.selectedEventIds.includes(event.id)
          );
          return {
            ...item,
            eventId: relatedEvent ? relatedEvent.id : null,
          };
        });
      } catch (error) {
        console.error("API Error:", error);
      }
    },
    removeItem(item) {
      // Remove an item from the shopping list
      this.shoppingList = this.shoppingList.filter((i) => i.id !== item.id);
    },
  },
};
</script>

<style>
.container {
  max-width: 80rem;
}

.card-deck {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.card {
  flex: 1 1 calc(33.333% - 1rem);
  min-width: 200px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.event-item {
  transition: transform 0.2s, box-shadow 0.2s;
}

.event-item:hover {
  transform: scale(1.03);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.border-primary {
  border: 2px solid #007bff !important;
}

.shadow-sm {
  box-shadow: 0 4px 6px rgba(0, 123, 255, 0.2) !important;
}

.list-group-item.checked {
  text-decoration: line-through;
  color: #6c757d;
}

.list-enter-active, .list-leave-active {
  transition: opacity 0.5s ease-in-out;
}

.list-enter {
  opacity: 0;
}

.list-leave-to {
  opacity: 0;
}
</style>