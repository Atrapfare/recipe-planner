package de.unistuttgart.iste.sopra.api.repository;

import de.unistuttgart.iste.sopra.api.model.recipe.Recipe;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Recipe} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 */
public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    /**
     * Finds a recipe by its unique ID.
     *
     * @param id The ID of the recipe.
     * @return An {@link Optional} containing the recipe if found, or empty if not found.
     */
    Optional<Recipe> findById(long id);

    /**
     * Finds a recipe by its name.
     *
     * @param name The name of the recipe.
     * @return An {@link Optional} containing the recipe if found, or empty if not found.
     */
    Optional<Recipe> findByName(String name);

    /**
     * Checks if a recipe exists that includes an ingredient with the specified ID.
     *
     * @param id The ID of the ingredient.
     * @return True if a recipe with the specified ingredient exists, otherwise false.
     */
    boolean existsByIngredientsIngredientId(long id);

    List<Recipe> findByIngredientsIngredientId(long ingredientId);
}