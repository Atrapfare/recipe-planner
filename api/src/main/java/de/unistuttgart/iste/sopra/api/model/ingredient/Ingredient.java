package de.unistuttgart.iste.sopra.api.model.ingredient;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Represents an ingredient entity in the system.
 * Each ingredient includes information such as its name, cooling requirements,
 * freezing capabilities, and whether it is used in any recipe.
 */
@Entity
@Table(name = "ingredients")
public class Ingredient {

    /**
     * Unique identifier for the ingredient.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    /**
     * Name of the ingredient.
     * Must not be null.
     */
    @NotNull
    private String name;

    /**
     * Indicates if the ingredient needs to be kept cool.
     */
    private boolean cooled;

    /**
     * Indicates if the ingredient can be frozen.
     */
    private boolean freeze;

    /**
     * Indicates if the ingredient is used in any recipe.
     * This is set dynamically and not persisted in the database.
     */
    private boolean usedInRecipe;

    /**
     * Default constructor for JPA.
     */
    public Ingredient() {
    }

    /**
     * Constructs an ingredient with the specified details.
     *
     * @param id     The unique identifier of the ingredient.
     * @param name   The name of the ingredient.
     * @param cooled Whether the ingredient needs to be cooled.
     * @param freeze Whether the ingredient can be frozen.
     */
    public Ingredient(long id, String name, boolean cooled, boolean freeze) {
        this.id = id;
        this.name = name;
        this.cooled = cooled;
        this.freeze = freeze;
    }

    /**
     * Gets the unique identifier of the ingredient.
     *
     * @return The ID of the ingredient.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the ingredient.
     *
     * @param id The ID to set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the name of the ingredient.
     *
     * @return The name of the ingredient.
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Sets the name of the ingredient.
     *
     * @param name The name to set.
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Checks if the ingredient needs to be kept cool.
     *
     * @return True if the ingredient needs to be kept cool, otherwise false.
     */
    public boolean isCooled() {
        return cooled;
    }

    /**
     * Sets whether the ingredient needs to be kept cool.
     *
     * @param cooled True if the ingredient needs to be kept cool, otherwise false.
     */
    public void setCooled(boolean cooled) {
        this.cooled = cooled;
    }

    /**
     * Checks if the ingredient can be frozen.
     *
     * @return True if the ingredient can be frozen, otherwise false.
     */
    public boolean isFreeze() {
        return freeze;
    }

    /**
     * Sets whether the ingredient can be frozen.
     *
     * @param freeze True if the ingredient can be frozen, otherwise false.
     */
    public void setFreeze(boolean freeze) {
        this.freeze = freeze;
    }

    /**
     * Checks if the ingredient is used in any recipe.
     *
     * @return True if the ingredient is used in a recipe, otherwise false.
     */
    public boolean isUsedInRecipe() {
        return usedInRecipe;
    }

    /**
     * Sets whether the ingredient is used in any recipe.
     *
     * @param usedInRecipe True if the ingredient is used in a recipe, otherwise false.
     */
    public void setUsedInRecipe(boolean usedInRecipe) {
        this.usedInRecipe = usedInRecipe;
    }
}