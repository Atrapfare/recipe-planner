package de.unistuttgart.iste.sopra.api.controller;

import de.unistuttgart.iste.sopra.api.ApiVersion1;
import de.unistuttgart.iste.sopra.api.model.event.Event;
import de.unistuttgart.iste.sopra.api.model.ingredient.Ingredient;
import de.unistuttgart.iste.sopra.api.model.recipe.Recipe;
import de.unistuttgart.iste.sopra.api.model.recipe.RecipeIngredient;
import de.unistuttgart.iste.sopra.api.repository.EventRepository;
import de.unistuttgart.iste.sopra.api.repository.IngredientRepository;
import de.unistuttgart.iste.sopra.api.repository.RecipeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for managing recipes.
 * Provides CRUD operations for {@link Recipe} and functionalities like generating shopping lists.
 */
@RestController
@ApiVersion1
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * Retrieves all recipes from the database.
     *
     * @return A list of all recipes.
     */
    @GetMapping("/recipes")
    public ResponseEntity<List<Recipe>> getRecipes() {
        List<Recipe> recipes = (List<Recipe>) recipeRepository.findAll();
        return ResponseEntity.ok(recipes);
    }

    /**
     * Retrieves a specific recipe by its ID.
     *
     * @param id The ID of the recipe to retrieve.
     * @return The requested recipe if found.
     * @throws ResponseStatusException if the recipe is not found.
     */
    @GetMapping("/recipes/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable("id") long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);
        if (recipe.isPresent()) {
            return ResponseEntity.ok(recipe.get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Recipe with ID %s not found!", id));
    }

    /**
     * Generates a shopping list based on upcoming events.
     *
     * @return A shopping list of ingredients required for future events.
     */
    @GetMapping("/shopping")
    public ResponseEntity<List<Map<String, Object>>> getShoppingList() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = new ArrayList<>();
        eventRepository.findAll().forEach(events::add);

        // Filter future events
        List<Event> futureEvents = events.stream()
                .filter(event -> event.getStartTime().isAfter(now))
                .toList();

        // Count recipes used in future events
        Map<Recipe, Long> recipeUsageCount = futureEvents.stream()
                .collect(Collectors.groupingBy(Event::getRecipe, Collectors.counting()));

        Map<String, Map<String, Object>> ingredientMap = new HashMap<>();

        for (Map.Entry<Recipe, Long> entry : recipeUsageCount.entrySet()) {
            Recipe recipe = entry.getKey();
            long eventCount = entry.getValue();

            for (RecipeIngredient ri : recipe.getIngredients()) {
                long ingredientId = ri.getIngredient().getId();
                String unit = ri.getUnit();
                String key = ingredientId + "_" + unit;

                double totalQuantity = ri.getQuantity() * eventCount;

                if (ingredientMap.containsKey(key)) {
                    // Update quantity if ingredient is already in the map
                    Map<String, Object> existingIngredient = ingredientMap.get(key);
                    double newQuantity = (double) existingIngredient.get("quantity") + totalQuantity;
                    existingIngredient.put("quantity", newQuantity);
                } else {
                    // Add new ingredient to the map
                    Map<String, Object> ingredientInfo = new HashMap<>();
                    ingredientInfo.put("id", ingredientId);
                    ingredientInfo.put("name", ri.getIngredient().getName());
                    ingredientInfo.put("quantity", totalQuantity);
                    ingredientInfo.put("unit", unit);
                    ingredientInfo.put("cooled", ri.getIngredient().isCooled());
                    ingredientInfo.put("freeze", ri.getIngredient().isFreeze());
                    ingredientInfo.put("usedInRecipe", ri.getIngredient().isUsedInRecipe());
                    ingredientMap.put(key, ingredientInfo);
                }
            }
        }

        List<Map<String, Object>> shoppingList = new ArrayList<>(ingredientMap.values());
        return ResponseEntity.ok(shoppingList);
    }

    /**
     * Generates a filtered shopping list based on selected event IDs.
     *
     * @param eventIds A list of event IDs to filter by.
     * @return A filtered shopping list based on the selected events.
     */
    @PostMapping("/shopping/filter")
    public ResponseEntity<List<Map<String, Object>>> getShoppingListFiltered(@RequestBody List<Long> eventIds) {
        List<Event> selectedEvents = (List<Event>) eventRepository.findAllById(eventIds);
        Map<Recipe, Long> recipeUsageCount = selectedEvents.stream()
                .collect(Collectors.groupingBy(Event::getRecipe, Collectors.counting()));

        Map<String, Map<String, Object>> ingredientMap = new HashMap<>();

        for (Map.Entry<Recipe, Long> entry : recipeUsageCount.entrySet()) {
            Recipe recipe = entry.getKey();
            long eventCount = entry.getValue();

            for (RecipeIngredient ri : recipe.getIngredients()) {
                String key = ri.getIngredient().getId() + "_" + ri.getUnit();
                double totalQuantity = ri.getQuantity() * eventCount;

                ingredientMap.merge(key, new HashMap<>(Map.of(
                        "id", ri.getIngredient().getId(),
                        "name", ri.getIngredient().getName(),
                        "quantity", totalQuantity,
                        "unit", ri.getUnit(),
                        "cooled", ri.getIngredient().isCooled(),
                        "freeze", ri.getIngredient().isFreeze(),
                        "usedInRecipe", ri.getIngredient().isUsedInRecipe()
                )), (oldVal, newVal) -> {
                    oldVal.put("quantity", (double) oldVal.get("quantity") + totalQuantity);
                    return oldVal;
                });
            }
        }

        return ResponseEntity.ok(new ArrayList<>(ingredientMap.values()));
    }

    /**
     * Creates a new recipe.
     *
     * @param requestBody The recipe data to create.
     * @return The created recipe.
     * @throws ResponseStatusException if any ingredient in the recipe is not found.
     */
    @PostMapping("/recipes")
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe createRecipe(@Valid @RequestBody Recipe requestBody) {
        Recipe recipe = new Recipe();
        recipe.setName(requestBody.getName());
        recipe.setDescription(requestBody.getDescription());
        recipe.setDifficulty(requestBody.getDifficulty());
        recipe.setTime(requestBody.getTime());
        recipe.setImage(requestBody.getImage());

        List<RecipeIngredient> recipeIngredients = new ArrayList<>();

        // Validate and create RecipeIngredient objects
        for (RecipeIngredient ri : requestBody.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(ri.getIngredient().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            String.format("Ingredient with ID %s not found!", ri.getIngredient().getId())));

            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setRecipe(recipe);
            recipeIngredient.setIngredient(ingredient);
            recipeIngredient.setQuantity(ri.getQuantity());
            recipeIngredient.setUnit(ri.getUnit());

            recipeIngredients.add(recipeIngredient);
        }

        recipe.setIngredients(recipeIngredients);
        return recipeRepository.save(recipe);
    }

    /**
     * Updates an existing recipe.
     *
     * @param id          The ID of the recipe to update.
     * @param requestBody The updated recipe data.
     * @return The updated recipe.
     * @throws ResponseStatusException if the recipe is not found.
     */
    @PutMapping("/recipes/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Long id, @Valid @RequestBody Recipe requestBody) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        existingRecipe.setName(requestBody.getName());
        existingRecipe.setDescription(requestBody.getDescription());
        existingRecipe.setDifficulty(requestBody.getDifficulty());
        existingRecipe.setTime(requestBody.getTime());
        if (requestBody.getImage() != null && !requestBody.getImage().isEmpty()) {
            existingRecipe.setImage(requestBody.getImage());
        }

        // Map existing ingredients for efficient updates
        Map<Long, RecipeIngredient> existingIngredientsMap = existingRecipe.getIngredients().stream()
                .collect(Collectors.toMap(ri -> ri.getIngredient().getId(), ri -> ri));

        List<RecipeIngredient> updatedIngredients = new ArrayList<>();

        for (RecipeIngredient ri : requestBody.getIngredients()) {
            RecipeIngredient recipeIngredient = existingIngredientsMap.getOrDefault(
                    ri.getIngredient().getId(),
                    new RecipeIngredient()
            );

            recipeIngredient.setRecipe(existingRecipe);
            recipeIngredient.setIngredient(ri.getIngredient());
            recipeIngredient.setQuantity(ri.getQuantity());
            recipeIngredient.setUnit(ri.getUnit());

            updatedIngredients.add(recipeIngredient);
        }

        existingRecipe.getIngredients().clear();
        existingRecipe.getIngredients().addAll(updatedIngredients);

        Recipe updatedRecipe = recipeRepository.save(existingRecipe);
        return new ResponseEntity<>(updatedRecipe, HttpStatus.OK);
    }

    /**
     * Deletes a recipe by its ID and any linked events.
     *
     * @param id The ID of the recipe to delete.
     * @return A response indicating successful deletion.
     * @throws ResponseStatusException if the recipe is not found.
     */
    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable("id") long id) {
        Optional<Recipe> recipeOptional = recipeRepository.findById(id);
        if (recipeOptional.isPresent()) {
            Recipe recipe = recipeOptional.get();

            // Delete all linked events
            List<Event> linkedEvents = eventRepository.findByRecipeId(id);
            if (!linkedEvents.isEmpty()) {
                eventRepository.deleteAll(linkedEvents);
            }

            recipeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Recipe with ID %s not found!", id));
    }

    /**
     * Toggles the favorite status of a recipe.
     *
     * @param id The ID of the recipe to toggle.
     * @return A response indicating the operation was successful.
     * @throws ResponseStatusException if the recipe is not found.
     */
    @PatchMapping("/recipes/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        recipe.setFavorite(!recipe.isFavorite());
        recipeRepository.save(recipe);
        return ResponseEntity.noContent().build();
    }
}