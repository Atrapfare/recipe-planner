package de.unistuttgart.iste.sopra.api.controller;

import de.unistuttgart.iste.sopra.api.ApiVersion1;
import de.unistuttgart.iste.sopra.api.model.event.Event;
import de.unistuttgart.iste.sopra.api.model.recipe.Recipe;
import de.unistuttgart.iste.sopra.api.repository.EventRepository;
import de.unistuttgart.iste.sopra.api.repository.RecipeRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Controller for managing events.
 * Provides CRUD operations for {@link Event} and synchronizes recipe usage statistics.
 */
@RestController
@ApiVersion1
public class EventController {

    /**
     * Synchronizes recipe usage statistics after the bean is constructed.
     */
    @PostConstruct
    public void initializeRecipeUsage() {
        synchronizeRecipeUsage();
    }

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    /**
     * Retrieves all events from the repository.
     *
     * @return A list of all events.
     */
    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = (List<Event>) eventRepository.findAll();
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves a specific event by its ID.
     *
     * @param id The ID of the event to retrieve.
     * @return The requested event if found.
     * @throws ResponseStatusException if the event is not found.
     */
    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable("id") long id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()) {
            return ResponseEntity.ok(event.get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found!");
    }

    /**
     * Creates a new event and updates the recipe usage statistics.
     *
     * @param requestBody The event data to create.
     * @return The created event.
     * @throws ResponseStatusException if the recipe associated with the event is not found.
     */
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createEvent(@Valid @RequestBody Event requestBody) {
        Recipe recipe = recipeRepository.findById(requestBody.getRecipe().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found!"));

        Event event = new Event();
        event.setStartTime(requestBody.getStartTime());
        event.setEndTime(requestBody.getEndTime());
        event.setRecipe(recipe);
        event.setPersons(requestBody.getPersons());

        event = eventRepository.save(event);

        recipe.setUsed((int) eventRepository.countByRecipeId(recipe.getId()));
        recipeRepository.save(recipe);

        return event;
    }

    /**
     * Updates an existing event and adjusts recipe usage statistics.
     *
     * @param id           The ID of the event to update.
     * @param updatedEvent The updated event data.
     * @return The updated event.
     * @throws ResponseStatusException if the event or associated recipe is not found.
     */
    @PutMapping("/events/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable("id") long id, @Valid @RequestBody Event updatedEvent) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found!"));

        Recipe newRecipe = recipeRepository.findById(updatedEvent.getRecipe().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found!"));

        Recipe oldRecipe = existingEvent.getRecipe();

        existingEvent.setStartTime(updatedEvent.getStartTime());
        existingEvent.setEndTime(updatedEvent.getEndTime());
        existingEvent.setRecipe(newRecipe);
        existingEvent.setPersons(updatedEvent.getPersons());

        eventRepository.save(existingEvent);

        oldRecipe.setUsed((int) eventRepository.countByRecipeId(oldRecipe.getId()));
        newRecipe.setUsed((int) eventRepository.countByRecipeId(newRecipe.getId()));
        recipeRepository.save(oldRecipe);
        recipeRepository.save(newRecipe);

        return ResponseEntity.ok(existingEvent);
    }

    /**
     * Deletes an event and updates recipe usage statistics.
     *
     * @param id The ID of the event to delete.
     * @return A response indicating the operation was successful.
     * @throws ResponseStatusException if the event is not found.
     */
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found!"));

        Recipe recipe = event.getRecipe();

        eventRepository.delete(event);

        recipe.setUsed((int) eventRepository.countByRecipeId(recipe.getId()));
        recipeRepository.save(recipe);

        return ResponseEntity.noContent().build();
    }

    /**
     * Synchronizes the usage count for all recipes by calculating their usage across events.
     *
     * @return A response indicating the operation was successful.
     */
    @PutMapping("/recipes/synchronize-used")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> synchronizeRecipeUsage() {
        List<Recipe> recipes = StreamSupport
                .stream(recipeRepository.findAll().spliterator(), false)
                .toList();

        for (Recipe recipe : recipes) {
            long usageCount = eventRepository.countByRecipeId(recipe.getId());
            recipe.setUsed((int) usageCount);
            recipeRepository.save(recipe);
        }

        return ResponseEntity.ok().build();
    }
}