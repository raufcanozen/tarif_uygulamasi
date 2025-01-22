package com.example.tarifapp.controllers;

import com.example.tarifapp.managers.IngredientManager;
import com.example.tarifapp.managers.RecipeManager;
import com.example.tarifapp.models.Ingredient;
import com.example.tarifapp.models.IngredientRecipe;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import com.example.tarifapp.models.Recipe;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TarifDialogController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextField preparationTimeField;

    @FXML
    private TextArea instructionsField;

    @FXML
    private TextField quantityField;

    private Recipe recipe;

    @FXML
    private ComboBox<String> ingredientComboBox; // Malzeme seçimi için ComboBox

    // Malzeme listesi
    private List<Ingredient> ingredientsList = new ArrayList<>();
    // Malzeme Tarif listesi
    private List<IngredientRecipe> ingredientRecipeList = new ArrayList<>();

    @FXML
    private ListView<Ingredient> ingredientListView; // Malzemeleri listelemek için ListView


    private RecipeManager recipeManager = new RecipeManager();
    private IngredientManager ingredientManager = new IngredientManager();

    public void initialize() {
        // Mevcut malzemeleri yükle
        ingredientComboBox.setItems(FXCollections.observableArrayList(recipeManager.getAllIngredientNames()));
    }

    @FXML
    public void handleAddIngredient() {
        String selectedIngredient = ingredientComboBox.getValue();
        String malzemeMiktarStr = quantityField.getText();

        if (selectedIngredient != null && !selectedIngredient.isEmpty() && malzemeMiktarStr != null && !malzemeMiktarStr.isEmpty()) {
            try {
                float malzemeMiktar = Float.parseFloat(malzemeMiktarStr);
                // Seçilen malzemeyi veritabanından al
                Ingredient ingredient = ingredientManager.getIngredientByName(selectedIngredient);

                if (ingredient != null) {
                    // Yeni Ingredient_Recipe ilişkisi oluştur
                    IngredientRecipe ingredientRecipe = new IngredientRecipe(null, ingredient, malzemeMiktar);
                    ingredientRecipeList.add(ingredientRecipe);
                    ingredientsList.add(ingredient);
                    updateIngredientListView(); // ListView'u güncelle
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Hatalı miktar girdiniz.");
            }
        } else {
            showAlert("Error", "Malzeme seçip miktarını giriniz.");
        }
    }




    @FXML
    public void handleAddNewIngredient() {
        // Yeni malzeme eklemek için kullanıcıdan isim al
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Malzeme Ekle");
        dialog.setHeaderText("Yeni Malzeme Ekle");
        dialog.setContentText("Malzeme Adı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ingredientName -> {
            // Kullanıcıdan birim fiyatı al
            TextInputDialog priceDialog = new TextInputDialog();
            priceDialog.setTitle("Birim Fiyat Girişi");
            priceDialog.setHeaderText("Birim Fiyatı Girin:");
            Optional<String> priceResult = priceDialog.showAndWait();

            priceResult.ifPresent(priceStr -> {
                double unitPrice = Double.parseDouble(priceStr);

                // Kullanıcıdan toplam miktarı al
                TextInputDialog quantityDialog = new TextInputDialog();
                quantityDialog.setTitle("Toplam Miktar Girişi");
                quantityDialog.setHeaderText("Toplam Miktarı Girin:");
                Optional<String> quantityResult = quantityDialog.showAndWait();

                quantityResult.ifPresent(totalQuantityStr -> {
                    TextInputDialog unitDialog = new TextInputDialog();
                    unitDialog.setTitle("Birim Girişi");
                    unitDialog.setHeaderText("Malzeme Birimini Girin (örneğin, gram, kilogram, litre):");
                    Optional<String> unitResult = unitDialog.showAndWait();

                    unitResult.ifPresent(unit -> {
                        // Malzemeyi veritabanına ekle
                        Ingredient newIngredient = new Ingredient(0, ingredientName, totalQuantityStr, unit, unitPrice);
                        ingredientManager.addIngredient(newIngredient);

                        // ComboBox'ı güncelle
                        ingredientComboBox.getItems().add(ingredientName);
                    });
                });
            });
        });
    }


    private void updateIngredientListView() {
        // Malzeme listesini ListView'da göster
        ingredientListView.setItems(FXCollections.observableArrayList(ingredientsList));
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;

        if (recipe != null) {
            nameField.setText(recipe.getName());
            categoryField.setText(recipe.getCategory());
            preparationTimeField.setText(String.valueOf(recipe.getPreparationTime()));
            instructionsField.setText(recipe.getInstructions());
        }
    }

    public Recipe addNewRecipe() {
        // Veri doğrulaması
        if (nameField.getText().isEmpty()) {
            showAlert("Veri Hatası", "Tarif adı boş bırakılamaz.");
            return null;
        }

        if (categoryField.getText().isEmpty()) {
            showAlert("Veri Hatası", "Kategori boş bırakılamaz.");
            return null;
        }

        int preparationTime;
        try {
            preparationTime = Integer.parseInt(preparationTimeField.getText());
        } catch (NumberFormatException e) {
            showAlert("Veri Hatası", "Hazırlama süresi sayı olmalıdır.");
            return null;
        }

        if (preparationTime <= 0) {
            showAlert("Veri Hatası", "Hazırlama süresi 0'dan büyük olmalıdır");
            return null;
        }

        if (instructionsField.getText().isEmpty()) {
            showAlert("Veri Hatası", "Talimatlar boş bırakılamaz.");
            return null;
        }

        Recipe recipeCreated = new Recipe(
                recipe != null ? recipe.getId() : 0, // Yeni tarifse ID 0 olabilir
                nameField.getText(),
                categoryField.getText(),
                preparationTime,
                instructionsField.getText()
        );

        if (ingredientRecipeList.isEmpty()) {
            showAlert("Veri Hatası", "Malzeme eklemelisiniz.");
            return null;
        }
        for(var ingredient : ingredientRecipeList){
            ingredient.setRecipe(recipeCreated);
        }

        recipeCreated.setIngredients(ingredientRecipeList);

        clearForm();
        return recipeCreated;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void clearForm() {
        nameField.clear();
        categoryField.clear();
        preparationTimeField.clear();
        instructionsField.clear();
        ingredientsList.clear(); // Malzeme listesini temizle
        ingredientListView.setItems(FXCollections.observableArrayList()); // ListView'u temizle
    }

}
