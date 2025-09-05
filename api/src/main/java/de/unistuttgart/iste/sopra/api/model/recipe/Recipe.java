package de.unistuttgart.iste.sopra.api.model.recipe;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recipe entity in the system.
 * Each recipe contains a name, list of ingredients, description, difficulty level,
 * preparation time, image, usage count, and a favorite flag.
 */
@Entity
@Table(name = "recipes")
public class Recipe {

    /**
     * Unique identifier for the recipe.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    /**
     * Name of the recipe.
     * Must not be null.
     */
    @NotNull
    private String name;

    /**
     * List of ingredients associated with the recipe.
     * This is a one-to-many relationship with {@link RecipeIngredient}.
     * The cascade type ensures that associated ingredients are saved or removed along with the recipe.
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    /**
     * Description of the recipe.
     * Must not be null.
     */
    @NotNull
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    /**
     * Difficulty level of the recipe.
     * Must not be null.
     */
    @NotNull
    private String difficulty;

    /**
     * Preparation time for the recipe.
     * Must not be null.
     */
    @NotNull
    private String time;

    /**
     * Image representation of the recipe.
     * Stored as a long text.
     */
    @Lob
    @Column(name = "image", columnDefinition = "LONGTEXT")
    private String image;

    /**
     * Tracks how many times the recipe has been used in events.
     */
    private int used;

    /**
     * Indicates if the recipe is marked as a favorite.
     */
    private boolean favorite;

    /**
     * Default constructor for JPA.
     */
    public Recipe() {
    }

    /**
     * Constructs a new Recipe with the specified details.
     *
     * @param id          The unique identifier of the recipe.
     * @param name        The name of the recipe.
     * @param ingredients The list of ingredients associated with the recipe.
     * @param description The description of the recipe.
     * @param image       The image representation of the recipe.
     */
    public Recipe(long id, String name, List<RecipeIngredient> ingredients, String description, String image) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.description = description;
        this.image = image;
        this.used = 0;
        this.favorite = false;
    }

    /**
     * Gets the unique identifier of the recipe.
     *
     * @return The ID of the recipe.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the recipe.
     *
     * @param id The ID to set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the name of the recipe.
     *
     * @return The name of the recipe.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the recipe.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of ingredients associated with the recipe.
     *
     * @return The list of ingredients.
     */
    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    /**
     * Sets the list of ingredients associated with the recipe.
     *
     * @param ingredients The list of ingredients to set.
     */
    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Gets the description of the recipe.
     *
     * @return The description of the recipe.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the recipe.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the difficulty level of the recipe.
     *
     * @return The difficulty level of the recipe.
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the difficulty level of the recipe.
     *
     * @param difficulty The difficulty level to set.
     */
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Gets the preparation time for the recipe.
     *
     * @return The preparation time.
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the preparation time for the recipe.
     *
     * @param time The preparation time to set.
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Gets the image of the recipe.
     *
     * @return The image of the recipe.
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the image of the recipe.
     *
     * @param image The image to set.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the usage count of the recipe.
     *
     * @return The number of times the recipe has been used.
     */
    public int getUsed() {
        return used;
    }

    /**
     * Sets the usage count of the recipe.
     *
     * @param used The usage count to set.
     */
    public void setUsed(int used) {
        this.used = used;
    }

    /**
     * Checks if the recipe is marked as a favorite.
     *
     * @return True if the recipe is a favorite, otherwise false.
     */
    public boolean isFavorite() {
        return favorite;
    }

    /**
     * Sets the favorite status of the recipe.
     *
     * @param favorite True to mark as favorite, otherwise false.
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}