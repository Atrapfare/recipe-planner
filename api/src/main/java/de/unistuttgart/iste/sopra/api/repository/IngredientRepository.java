package de.unistuttgart.iste.sopra.api.repository;

import de.unistuttgart.iste.sopra.api.model.ingredient.Ingredient;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Ingredient} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 */
public interface IngredientRepository extends CrudRepository<Ingredient, Long> {

    /**
     * Finds an ingredient by its unique ID.
     *
     * @param id The ID of the ingredient.
     * @return An {@link Optional} containing the ingredient if found, or empty if not found.
     */
    Optional<Ingredient> findById(long id);

    /**
     * Finds an ingredient by its name.
     *
     * @param name The name of the ingredient.
     * @return An {@link Optional} containing the ingredient if found, or empty if not found.
     */
    Optional<Ingredient> findByName(String name);
}