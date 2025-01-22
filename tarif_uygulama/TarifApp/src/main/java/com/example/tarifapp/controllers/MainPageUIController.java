package com.example.tarifapp.controllers;

import com.example.tarifapp.managers.RecipeManager;
import com.example.tarifapp.models.Ingredient;
import com.example.tarifapp.models.IngredientRecipe;
import com.example.tarifapp.models.Recipe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.fxml.FXMLLoader;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainPageUIController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Recipe> recipeList;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private TextArea recipeDetails;

    @FXML
    private ComboBox<String> filterCategory; // Kategori filtreleme ComboBox'ı

    @FXML
    private ComboBox<String> filterIngredients;

    @FXML
    private ComboBox<String> filterCost;

    @FXML
    private ListView<Ingredient> ingredientListView; // Malzeme seçimi için ListView


    private ObservableList<Recipe> recipes = FXCollections.observableArrayList();
    private RecipeManager recipeManager = new RecipeManager();

    @FXML
    public void initialize() {
        // Veritabanından tarifleri yükle
        recipes.addAll(recipeManager.getAllRecipes());
        recipeList.setItems(recipes);

        // Tariflerin malzeme yeterliliklerine göre renklerini belirle
        loadRecipesWithSufficiency();
        refreshCellFactory();

        // Sıralama seçenekleri
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "Hazırlama Süresi", "Maliyet", "Kategori"
        );

        // Kategori filtrelerini veritabanından al
        ArrayList<String> categories = recipeManager.getAllCategories();
        filterCategory.setItems(FXCollections.observableArrayList(categories));

        // Malzeme sayısı ve maliyet aralığı için diğer filtreleri ekle
        filterIngredients.setItems(FXCollections.observableArrayList("1 Malzeme", "2 Malzeme", "3+ Malzeme"));
        filterCost.setItems(FXCollections.observableArrayList("0-100 TL",
         "100-200 TL",
         "200-300 TL",
         "300-400 TL",
         "400-500 TL",
         "500 TL ve Üzeri"));

        sortComboBox.setItems(sortOptions);
        sortComboBox.getSelectionModel().selectFirst();

        // Veritabanından malzemeleri al
        ObservableList<Ingredient> ingredients = FXCollections.observableArrayList(recipeManager.getAllIngredients());
        ingredientListView.setItems(ingredients);
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Recipe> filteredRecipes = recipes.filtered(recipe -> recipe.getName().toLowerCase().contains(searchText));
        recipeList.setItems(filteredRecipes);

        refreshCellFactory();
    }

    @FXML
    public void handleIngredientSelection() {
        // Kullanıcı bir malzemeye tıkladığında seçili hale getirin
        ingredientListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    public void handleIngredientSearch() {
        ObservableList<Ingredient> selectedIngredients = ingredientListView.getSelectionModel().getSelectedItems();
        ObservableList<Recipe> matchedRecipes = FXCollections.observableArrayList();

        // Her tarif için geçici eşleşme yüzdesini hesapla ve tariflerle birlikte tut
        List<Pair<Recipe, Float>> recipeWithPercentageList = new ArrayList<>();

        for (Recipe recipe : recipes) {
            List<IngredientRecipe> ingredients = recipe.getIngredients();
            int matchCount = 0;

            for (IngredientRecipe ingredientRecipe : ingredients) {
                for (Ingredient selectedIngredient : selectedIngredients) {
                    if (ingredientRecipe.getIngredient().getName().equalsIgnoreCase(selectedIngredient.getName())) {
                        matchCount++;
                    }
                }
            }

            // Eşleşme varsa tarifleri listeye ekle
            if (matchCount > 0) {
                // Eşleşme yüzdesini hesapla
                float percentage = (float) matchCount / ingredients.size() * 100;
                recipeWithPercentageList.add(new Pair<>(recipe, percentage));
            }
        }

        // Eşleşme yüzdesine göre tarifleri sıralama
        recipeWithPercentageList.sort((p1, p2) -> Float.compare(p2.getValue(), p1.getValue()));

        // Sıralanmış tarifleri matchedRecipes'e ekle
        for (Pair<Recipe, Float> pair : recipeWithPercentageList) {
            matchedRecipes.add(pair.getKey());
        }

        // Sonuçları listeye yerleştir
        recipeList.setItems(matchedRecipes);

        // Renkleri güncelle
        refreshCellFactory();
    }


    @FXML
    public void handleAddRecipe() {
        // Tarif eklemek için dialog aç
        Dialog<Recipe> dialog = createRecipeDialog(new Recipe(0, "", "", 0, ""));
        dialog.setTitle("Tarif Ekle");

        dialog.showAndWait().ifPresentOrElse(
                recipe -> {
                    if (recipeManager.addRecipe(recipe) != -1) {
                        // Malzeme yeterliliğini kontrol et ve tarife kaydet
                        boolean sufficient = recipeManager.checkIngredientsSufficiency(recipe);
                        recipe.setSufficientIngredients(sufficient);
                        recipes.add(recipe);
                    } else {
                        showAlert("Error", "Tarif zaten mevcut, tekrar eklenmedi.");
                    }
                    setNewAdded();
                    refresh();
                },
                () -> {
                    setNewAdded();

                }
        );
    }


    private void setNewAdded() {
        // Kategori filtrelerini veritabanından al
        ArrayList<String> categories = recipeManager.getAllCategories();
        filterCategory.setItems(FXCollections.observableArrayList(categories));

        // Veritabanından malzemeleri al
        ObservableList<Ingredient> ingredients = FXCollections.observableArrayList(recipeManager.getAllIngredients());
        ingredientListView.setItems(ingredients);
    }


    @FXML
    public void handleUpdateRecipe() {
        // Seçili tarif ile yeni bir dialog aç.
        Recipe selectedRecipe = recipeList.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            Dialog<Recipe> dialog = createRecipeDialog(selectedRecipe);
            dialog.setTitle("Tarif Güncelle");

            dialog.showAndWait().ifPresentOrElse(
                    updatedRecipe -> {
                        if (recipeManager.updateRecipe(updatedRecipe) != -1) {
                            boolean sufficient = recipeManager.checkIngredientsSufficiency(updatedRecipe);
                            updatedRecipe.setSufficientIngredients(sufficient);
                            refresh();
                        } else {
                            showAlert("Error", "Tarif zaten mevcut, güncellenemedi.");
                        }
                    },
                    () -> {
                        setNewAdded();

                    }
            );
        } else {
            showAlert("Error", "Güncelleme için tarif seçiniz.");
        }
    }


    private void refresh() {
        recipes.clear();
        recipes.addAll(recipeManager.getAllRecipes());
        recipeList.setItems(recipes);

        loadRecipesWithSufficiency();

        refreshCellFactory();

    }

    private void refreshCellFactory() {
        // Tariflerin malzeme yeterliliklerine göre renklerini belirle
        recipeList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Recipe recipe, boolean empty) {
                super.updateItem(recipe, empty);

                // Hücre boşsa sıfırla
                if (empty || recipe == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(recipe.getName());

                    setStyle("");  // hücre stilini sıfırla

                    //Yeterlilik durumuna göre renk ayarla
                    if (recipe.hasSufficientIngredients()) {
                        setStyle("-fx-background-color: green;");
                    } else {
                        setStyle("-fx-background-color: red;");
                    }
                }
            }
        });



        // ListView'i güncelle
        recipeList.refresh(); // Tarife göre hücreler yenilenir
    }



    @FXML
    public void handleDeleteRecipe() {
        Recipe selectedRecipe = recipeList.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            recipeManager.deleteRecipe(selectedRecipe.getId()); // Tarifi sil
            recipes.remove(selectedRecipe); // Tarifi listeden kaldır
            refreshCellFactory();
        } else {
            showAlert("Error", "Silmek için tarif seçiniz.");
        }
    }

    private Dialog<Recipe> createRecipeDialog(Recipe recipe) {
        Dialog<Recipe> dialog = new Dialog<>();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tarifapp/TarifDialog.fxml"));
            dialog.setDialogPane(loader.load());
            TarifDialogController controller = loader.getController();
            controller.setRecipe(recipe);

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return controller.addNewRecipe();
                }
                return null;
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dialog;
    }

    @FXML
    public void handleRecipeSelected() {
        Recipe selectedRecipe = recipeList.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            selectedRecipe.clearList();
            // Malzemeleri veritabanından çek ve tarif nesnesine ekle
            List<Ingredient> ingredients = recipeManager.getIngredientsForRecipe(selectedRecipe.getId());
            selectedRecipe.getIngredientsList().addAll(ingredients); // Malzemeleri ekle

            List<IngredientRecipe> ingredientRecipes = recipeManager.getIngredientRecipeForRecipe(selectedRecipe.getId());
            selectedRecipe.getIngredients().addAll(ingredientRecipes);

            StringBuilder details = new StringBuilder();
            details.append("Tarif Adı: ").append(selectedRecipe.getName()).append("\n")
                    .append("Kategori: ").append(selectedRecipe.getCategory()).append("\n")
                    .append("Hazırlama Süresi: ").append(selectedRecipe.getPreparationTime()).append(" dakika\n")
                    .append("Talimatlar: ").append(selectedRecipe.getInstructions()).append("\n")
                    .append("Malzemeler:\n");

            for (IngredientRecipe ingredientRecipe : ingredientRecipes) {
                Ingredient ingredient = ingredientRecipe.getIngredient();
                float usedQuantity = ingredientRecipe.getQuantity();  // Tarif için kullanılan miktar

                details.append("- ").append(ingredient.getName())
                        .append(" (").append(ingredient.getUnitPrice()).append(" TL")  // Birim fiyat
                        .append(", ").append(usedQuantity).append(" ")  // Tarif için kullanılan miktar
                        .append(ingredient.getUnit())  // Birim
                        .append(", Depodaki Miktar: ").append(ingredient.getTotalQuantity())  // Depodaki toplam miktar
                        .append(" ").append(ingredient.getUnit()).append(")\n");  // Birim
            }


            details.append("Toplam Maliyet: ").append(selectedRecipe.getTotalCost()).append(" TL");

            // Detayları göster
            recipeDetails.setText(details.toString());
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleSortRecipes() {
        String selectedSort = sortComboBox.getValue();

        Comparator<Recipe> comparator = null;
        switch (selectedSort) {
            case "Hazırlama Süresi":
                comparator = Comparator.comparingInt(Recipe::getPreparationTime);
                break;
            case "Kategori":
                comparator = Comparator.comparing(Recipe::getCategory);
                break;
            case "Maliyet":
                comparator = Comparator.comparing(Recipe::getTotalCost);
                break;
        }

        if (comparator != null) {
            FXCollections.sort(recipes, comparator);
            recipeList.setItems(recipes);  // Listeyi yeniden göster
        }

        refreshCellFactory();
    }

    @FXML
    public void handleFilter() {
        String selectedCategory = filterCategory.getValue();
        String selectedIngredients = filterIngredients.getValue();
        String selectedCost = filterCost.getValue();

        ObservableList<Recipe> filteredRecipes = recipes.filtered(recipe -> {
            boolean categoryMatch = selectedCategory == null || recipe.getCategory().equals(selectedCategory);
            boolean ingredientsMatch = selectedIngredients == null ||
                    (selectedIngredients.equals("1 Malzeme") && recipe.getIngredients().size() == 1) ||
                    (selectedIngredients.equals("2 Malzeme") && recipe.getIngredients().size() == 2) ||
                    (selectedIngredients.equals("3+ Malzeme") && recipe.getIngredients().size() >= 3);
            boolean costMatch = selectedCost == null || checkCostRange(recipe, selectedCost);

            return categoryMatch && ingredientsMatch && costMatch;
        });

        recipeList.setItems(filteredRecipes);

        refreshCellFactory();
    }

    public void loadRecipesWithSufficiency() {
        List<Recipe> recipes = recipeManager.getAllRecipes();
        for (Recipe recipe : recipes) {
            boolean sufficient = recipeManager.checkIngredientsSufficiency(recipe);
            recipe.setSufficientIngredients(sufficient);
        }
        this.recipes.setAll(recipes);  // Recipe listesine yükle
    }


    private boolean checkCostRange(Recipe recipe, String selectedCost) {
        // Tarif maliyetini hesapla ve aralığa göre kontrol et
        double totalCost = recipe.getTotalCost(); // Bu metodun tarifin toplam maliyetini döndürmesi gerekir
        switch (selectedCost) {
            case "0-100 TL":
                return totalCost >= 0 && totalCost <= 100;
            case "100-200 TL":
                return totalCost > 100 && totalCost <= 200;
            case "200-300 TL":
                return totalCost > 200 && totalCost <= 300;
            case "300-400 TL":
                return totalCost > 300 && totalCost <= 400;
            case "400-500 TL":
                return totalCost > 400 && totalCost <= 500;
            case "500 TL ve Üzeri":
                return totalCost > 500;
            default:
                return false;
        }
    }

    @FXML
    public void handleClearFilters() {
        // Tüm filtreleri sıfırla
        filterCategory.getSelectionModel().clearSelection();
        filterIngredients.getSelectionModel().clearSelection();
        filterCost.getSelectionModel().clearSelection();

        // Tüm tarifleri yeniden göster
        recipeList.setItems(recipes);

        refreshCellFactory();
    }


}
