package de.unistuttgart.iste.sopra.api.repository;

import de.unistuttgart.iste.sopra.api.model.event.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for managing {@link Event} entities.
 * Extends {@link CrudRepository} to provide basic CRUD operations.
 */
public interface EventRepository extends CrudRepository<Event, Long> {

    /**
     * Finds all events associated with a specific recipe ID.
     *
     * @param id The ID of the recipe.
     * @return A list of events associated with the given recipe ID.
     */
    List<Event> findByRecipeId(long id);

    /**
     * Counts the number of events associated with a specific recipe ID.
     *
     * @param recipeId The ID of the recipe.
     * @return The count of events associated with the recipe.
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.recipe.id = :recipeId")
    long countByRecipeId(@Param("recipeId") long recipeId);

}