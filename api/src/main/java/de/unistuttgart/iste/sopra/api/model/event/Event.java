package de.unistuttgart.iste.sopra.api.model.event;

import de.unistuttgart.iste.sopra.api.model.recipe.Recipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Represents an event entity in the system.
 * Each event is associated with a {@link Recipe} and contains information
 * about the start time, end time, the number of persons, and the recipe used.
 */
@Entity
@Table(name = "events")
public class Event {

    /**
     * Unique identifier for the event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    /**
     * Start time of the event.
     * Must not be null.
     */
    @NotNull
    private LocalDateTime startTime;

    /**
     * End time of the event.
     * Must not be null.
     */
    @NotNull
    private LocalDateTime endTime;

    /**
     * The recipe associated with this event.
     * This is a many-to-one relationship.
     */
    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * Number of persons participating in the event.
     * Must be at least 1.
     */
    @Min(1)
    private int persons;

    /**
     * Default constructor for JPA.
     */
    public Event() {
    }

    /**
     * Constructs a new Event with the specified details.
     *
     * @param startTime The start time of the event.
     * @param endTime   The end time of the event.
     * @param recipe    The recipe associated with the event.
     * @param persons   The number of persons participating in the event.
     */
    public Event(LocalDateTime startTime, LocalDateTime endTime, Recipe recipe, int persons) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.recipe = recipe;
        this.persons = persons;
    }

    /**
     * Gets the unique identifier of the event.
     *
     * @return The ID of the event.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the event.
     *
     * @param id The ID to set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the start time of the event.
     *
     * @return The start time of the event.
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the event.
     *
     * @param startTime The start time to set.
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time of the event.
     *
     * @return The end time of the event.
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the event.
     *
     * @param endTime The end time to set.
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the recipe associated with the event.
     *
     * @return The associated recipe.
     */
    public Recipe getRecipe() {
        return recipe;
    }

    /**
     * Sets the recipe associated with the event.
     *
     * @param recipe The recipe to associate with the event.
     */
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    /**
     * Gets the number of persons participating in the event.
     *
     * @return The number of persons.
     */
    public int getPersons() {
        return persons;
    }

    /**
     * Sets the number of persons participating in the event.
     *
     * @param persons The number of persons to set.
     */
    public void setPersons(int persons) {
        this.persons = persons;
    }
}