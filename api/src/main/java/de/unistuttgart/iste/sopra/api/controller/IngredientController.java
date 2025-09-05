package de.unistuttgart.iste.sopra.api.controller;

import de.unistuttgart.iste.sopra.api.ApiVersion1;
import de.unistuttgart.iste.sopra.api.model.ingredient.Ingredient;
import de.unistuttgart.iste.sopra.api.model.recipe.Recipe;
import de.unistuttgart.iste.sopra.api.repository.IngredientRepository;
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

/**
 * Controller for managing ingredients.
 * Provides CRUD operations for {@link Ingredient}.
 */
@RestController
@ApiVersion1
public class IngredientController {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    /**
     * Method called after the bean is initialized.
     * Can be used for setup or initialization tasks.
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Retrieves all ingredients.
     * Also checks if each ingredient is used in any recipe and sets the flag accordingly.
     *
     * @return A list of all ingredients.
     */
    @GetMapping("/ingredients")
    public List<Ingredient> getIngredients() {
        List<Ingredient> ingredientList = (List<Ingredient>) ingredientRepository.findAll();

        for (Ingredient ingredient : ingredientList) {
            ingredient.setUsedInRecipe(recipeRepository.existsByIngredientsIngredientId(ingredient.getId()));
        }

        return ingredientList;
    }

    /**
     * Retrieves a specific ingredient by its ID.
     *
     * @param id The ID of the ingredient to retrieve.
     * @return The requested ingredient if found.
     * @throws ResponseStatusException if the ingredient is not found.
     */
    @GetMapping("/ingredients/{id}")
    public Ingredient getIngredient(@PathVariable("id") long id) {
        Optional<Ingredient> ingredient = ingredientRepository.findById(id);
        if (ingredient.isPresent()) {
            return ingredient.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Ingredient with ID %s not found!", id));
    }

    /**
     * Creates a new ingredient.
     * Ensures that the ingredient name is unique.
     *
     * @param requestBody The ingredient data to create.
     * @return The created ingredient.
     * @throws ResponseStatusException if an ingredient with the same name already exists.
     */
    @PostMapping("/ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    public Ingredient createIngredient(@Valid @RequestBody Ingredient requestBody) {
        if (ingredientRepository.findByName(requestBody.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Ingredient with name '%s' already exists!", requestBody.getName()));
        }
        Ingredient ingredient = new Ingredient();
        ingredient.setName(requestBody.getName());
        ingredient.setCooled(requestBody.isCooled());
        ingredient.setFreeze(requestBody.isFreeze());
        return ingredientRepository.save(ingredient);
    }

    /**
     * Updates an existing ingredient by its ID.
     * Ensures that the updated name is unique across all ingredients.
     *
     * @param id           The ID of the ingredient to update.
     * @param requestBody  The updated ingredient data.
     * @return The updated ingredient.
     * @throws ResponseStatusException if the ingredient is not found or the new name already exists.
     */
    @PutMapping("/ingredients/{id}")
    public Ingredient updateIngredient(@PathVariable("id") long id, @Valid @RequestBody Ingredient requestBody) {
        Ingredient existingIngredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Ingredient with ID %s not found!", id)));

        Optional<Ingredient> ingredientWithSameName = ingredientRepository.findByName(requestBody.getName());
        if (ingredientWithSameName.isPresent() && ingredientWithSameName.get().getId() != id) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Ingredient with name '%s' already exists!", requestBody.getName()));
        }

        existingIngredient.setName(requestBody.getName());
        existingIngredient.setCooled(requestBody.isCooled());
        existingIngredient.setFreeze(requestBody.isFreeze());

        return ingredientRepository.save(existingIngredient);
    }

    /**
     * Deletes an ingredient by its ID.
     * Ensures that the ingredient is not used in any recipe before deletion.
     *
     * @param id The ID of the ingredient to delete.
     * @return A response indicating successful deletion.
     * @throws ResponseStatusException if the ingredient is not found or is used in a recipe.
     */
    @DeleteMapping("/ingredients/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable("id") long id) {
        Optional<Ingredient> ingredientOptional = ingredientRepository.findById(id);
        if (ingredientOptional.isPresent()) {
            Ingredient ingredient = ingredientOptional.get();
            boolean isUsedInRecipe = recipeRepository.existsByIngredientsIngredientId(ingredient.getId());

            if (isUsedInRecipe) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete ingredient as it is used in a recipe");
            }

            ingredientRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Ingredient with ID %s not found!", id));
    }

    /**
     * Retrieves all recipes that use a specific ingredient.
     *
     * @param id The ID of the ingredient.
     * @return A list of recipes using the ingredient.
     * @throws ResponseStatusException if the ingredient is not found.
     */
    @GetMapping("/ingredients/{id}/recipes")
    public List<Recipe> getRecipesByIngredient(@PathVariable("id") long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Ingredient with ID %s not found!", id));
        }
        return recipeRepository.findByIngredientsIngredientId(id);
    }
}