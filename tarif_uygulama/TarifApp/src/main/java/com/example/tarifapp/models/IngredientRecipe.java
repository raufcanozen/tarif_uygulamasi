package com.example.tarifapp.models;

public class IngredientRecipe {
    private Recipe recipe;         // Tarif bilgisi
    private Ingredient ingredient; // Malzeme bilgisi
    private float quantity;        // Tarif için kullanılacak miktar

    // Constructors
    public IngredientRecipe(Recipe recipe, Ingredient ingredient, float quantity) {
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    // Getter ve Setter'lar
    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
}
