package com.example.tarifapp.models;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private int id;
    private String name;
    private String category;
    private int preparationTime;
    private String instructions;
    private List<IngredientRecipe> ingredients; // Tarifin malzemeleri ve miktarları
    private List<Ingredient> ingredientList; // Tarifin malzemeleri ve miktarları
    private boolean sufficientIngredients;  // Malzemeler yeterli mi değil mi


    public Recipe(int id, String name, String category, int preparationTime, String instructions) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.preparationTime = preparationTime;
        this.instructions = instructions;
        this.ingredients = new ArrayList<>();
        this.ingredientList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<Ingredient> getIngredientsList() {
        return ingredientList;
    }

    public List<IngredientRecipe> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientRecipe> ingredients) {
        this.ingredients = ingredients;
    }

    public double getTotalCost() {
        double totalCost = 0;
        for (IngredientRecipe ingredient : ingredients) {
            totalCost += ingredient.getIngredient().getUnitPrice() * ingredient.getQuantity(); // Birim fiyatları topla
        }
        return totalCost;
    }

    public boolean checkIngredientsSufficiency() {
        for (IngredientRecipe ingredientRecipe : ingredients) {
            Ingredient ingredient = ingredientRecipe.getIngredient();
            float requiredQuantity = ingredientRecipe.getQuantity();  // Tarif için gereken miktar
            float availableQuantity = Float.parseFloat(ingredient.getTotalQuantity());  // Stokta mevcut miktar (varchar)

            if (availableQuantity < requiredQuantity) {
                return false;  // Bir malzeme bile eksikse tarif yapılamaz
            }
        }
        return true;  // Tüm malzemeler yeterli
    }

    public boolean hasSufficientIngredients() {
        return sufficientIngredients;
    }

    public void setSufficientIngredients(boolean sufficientIngredients) {
        this.sufficientIngredients = sufficientIngredients;
    }

    @Override
    public String toString() {
        return name + " - " + preparationTime + " min";
    }

    public void clearList() {
        this.ingredients = new ArrayList<>();
        this.ingredientList = new ArrayList<>();
    }
}
