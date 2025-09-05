package de.unistuttgart.iste.sopra.api.model.recipe;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unistuttgart.iste.sopra.api.model.ingredient.Ingredient;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the relationship between a {@link Recipe} and an {@link Ingredient}.
 * Each recipe can have multiple ingredients with specific quantities and units.
 */
@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {

    /**
     * Unique identifier for the recipe-ingredient relationship.
     * This field is ignored during JSON serialization.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonIgnore
    private long id;

    /**
     * The recipe associated with this relationship.
     * This is a many-to-one relationship.
     * The {@link JsonBackReference} annotation prevents infinite recursion during JSON serialization.
     */
    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    @JsonBackReference
    private Recipe recipe;

    /**
     * The ingredient associated with this relationship.
     * This is a many-to-one relationship.
     */
    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /**
     * The quantity of the ingredient required for the recipe.
     * Must be greater than or equal to 0.
     */
    @NotNull
    @Min(0)
    private double quantity;

    /**
     * The unit of measurement for the ingredient quantity (e.g., grams, liters).
     * Must not be null.
     */
    @NotNull
    private String unit;

    /**
     * Default constructor for JPA.
     */
    public RecipeIngredient() {
    }

    /**
     * Constructs a new RecipeIngredient relationship with the specified details.
     *
     * @param recipe    The recipe associated with this relationship.
     * @param ingredient The ingredient associated with this relationship.
     * @param quantity   The quantity of the ingredient required.
     * @param unit       The unit of measurement for the quantity.
     */
    public RecipeIngredient(Recipe recipe, Ingredient ingredient, double quantity, String unit) {
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    /**
     * Gets the unique identifier of the relationship.
     *
     * @return The ID of the relationship.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the relationship.
     *
     * @param id The ID to set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the recipe associated with this relationship.
     *
     * @return The associated recipe.
     */
    public Recipe getRecipe() {
        return recipe;
    }

    /**
     * Sets the recipe associated with this relationship.
     *
     * @param recipe The recipe to associate.
     */
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    /**
     * Gets the ingredient associated with this relationship.
     *
     * @return The associated ingredient.
     */
    public Ingredient getIngredient() {
        return ingredient;
    }

    /**
     * Sets the ingredient associated with this relationship.
     *
     * @param ingredient The ingredient to associate.
     */
    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    /**
     * Gets the quantity of the ingredient required.
     *
     * @return The quantity of the ingredient.
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the ingredient required.
     *
     * @param quantity The quantity to set.
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the unit of measurement for the ingredient quantity.
     *
     * @return The unit of measurement.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the unit of measurement for the ingredient quantity.
     *
     * @param unit The unit to set.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }
}