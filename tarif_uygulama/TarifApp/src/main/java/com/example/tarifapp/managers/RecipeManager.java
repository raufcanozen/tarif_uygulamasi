package com.example.tarifapp.managers;

import com.example.tarifapp.models.Ingredient;
import com.example.tarifapp.models.IngredientRecipe;
import com.example.tarifapp.models.Recipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    private static final String URL = "jdbc:postgresql://localhost:5432/TarifApp";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "1234c";

    public Recipe getRecipeWithId(int recipeId) {
        String sql = "SELECT * FROM Tarifler WHERE tarifid = " + recipeId;
        Recipe recipe = null;

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                recipe = new Recipe(
                        rs.getInt("TarifID"),
                        rs.getString("TarifAdi"),
                        rs.getString("Kategori"),
                        rs.getInt("HazirlamaSuresi"),
                        rs.getString("Talimatlar")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return recipe;
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT * FROM Tarifler";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Recipe recipe = new Recipe(
                        rs.getInt("TarifID"),
                        rs.getString("TarifAdi"),
                        rs.getString("Kategori"),
                        rs.getInt("HazirlamaSuresi"),
                        rs.getString("Talimatlar")
                );
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return recipes;
    }

    public int addRecipe(Recipe recipe) {
        String checkDuplicateSql = "SELECT TarifID FROM Tarifler WHERE TarifAdi = ?";
        String insertRecipeSql = "INSERT INTO Tarifler (TarifAdi, Kategori, HazirlamaSuresi, Talimatlar) VALUES (?, ?, ?, ?) RETURNING TarifID";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement checkPst = con.prepareStatement(checkDuplicateSql);
             PreparedStatement insertPst = con.prepareStatement(insertRecipeSql)) {

            // Duplicate kontrolü yap
            checkPst.setString(1, recipe.getName());
            ResultSet checkRs = checkPst.executeQuery();

            if (checkRs.next()) {
                // Eğer aynı tarif adı ve kategoriye sahip bir tarif zaten varsa uyarı ver
                System.out.println("Tarif zaten mevcut, tekrar eklenmedi.");
                return -1;
            } else {
                // Tarif bilgilerini ekle
                insertPst.setString(1, recipe.getName());
                insertPst.setString(2, recipe.getCategory());
                insertPst.setInt(3, recipe.getPreparationTime());
                insertPst.setString(4, recipe.getInstructions());
                ResultSet rs = insertPst.executeQuery();

                if (rs.next()) {
                    int recipeId = rs.getInt("TarifID"); // Eklenen tarifin ID'sini al

                    // Tarifin malzemelerini tarif_malzeme tablosuna ekle
                    addIngredientsToRecipe(recipeId, recipe.getIngredients());
                    System.out.println("Recipe added successfully.");
                    return 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


    private void addIngredientsToRecipe(int recipeId, List<IngredientRecipe> ingredients) {
        String sql = "INSERT INTO tarif_malzeme (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(sql)) {

            for (IngredientRecipe ingredientRecipe : ingredients) {
                pst.setInt(1, recipeId); // Tarif ID
                pst.setInt(2, ingredientRecipe.getIngredient().getId()); // Malzeme ID
                pst.setDouble(3, ingredientRecipe.getQuantity()); // Malzemenin tarifteki miktarı
                pst.addBatch(); // Toplu ekleme için
            }

            pst.executeBatch(); // Tüm malzemeleri bir kerede ekle
            System.out.println("Ingredients added to recipe successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public int updateRecipe(Recipe recipe) {
        String checkDuplicateSql = "SELECT TarifID FROM Tarifler WHERE TarifAdi = ? AND TarifID != ?";
        String updateRecipeSql = "UPDATE Tarifler SET TarifAdi = ?, Kategori = ?, HazirlamaSuresi = ?, Talimatlar = ? WHERE TarifID = ?";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement checkPst = con.prepareStatement(checkDuplicateSql);
             PreparedStatement updatePst = con.prepareStatement(updateRecipeSql)) {

            // Duplicate kontrolü yap
            checkPst.setString(1, recipe.getName());
            checkPst.setInt(2, recipe.getId());
            ResultSet checkRs = checkPst.executeQuery();

            if (checkRs.next()) {
                // Eğer aynı tarif adına sahip başka bir tarif zaten varsa uyarı ver
                System.out.println("Aynı ada ve kategoriye sahip başka bir tarif mevcut, güncelleme yapılamadı.");
                return -1;
            } else {
                // Tarif bilgilerini güncelle
                updatePst.setString(1, recipe.getName());
                updatePst.setString(2, recipe.getCategory());
                updatePst.setInt(3, recipe.getPreparationTime());
                updatePst.setString(4, recipe.getInstructions());
                updatePst.setInt(5, recipe.getId());
                updatePst.executeUpdate();

                // Önce tarifin mevcut malzemelerini sil
                removeIngredientsFromRecipe(recipe.getId());

                // Güncellenen malzemeleri ekle
                addIngredientsToRecipe(recipe.getId(), recipe.getIngredients());
                System.out.println("Recipe updated successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void removeIngredientsFromRecipe(int recipeId) {
        String sql = "DELETE FROM tarif_malzeme WHERE TarifID = ?";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, recipeId);
            pst.executeUpdate();
            System.out.println("Ingredients removed from recipe successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deleteRecipe(int recipeId) {
        // Önce tarif ile ilişkili malzemeleri sil
        String deleteIngredientsSql = "DELETE FROM tarif_malzeme WHERE TarifID = ?";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(deleteIngredientsSql)) {
            pst.setInt(1, recipeId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sonrasında tarifi sil
        String deleteRecipeSql = "DELETE FROM tarifler WHERE TarifID = ?";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(deleteRecipeSql)) {
            pst.setInt(1, recipeId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<String> getAllCategories() {
        ArrayList<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT Kategori FROM Tarifler";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("Kategori"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public List<Ingredient> getIngredientsForRecipe(int recipeId) {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT m.MalzemeID, m.MalzemeAdi, m.BirimFiyat, m.MalzemeBirim, m.ToplamMiktar FROM tarif_malzeme tm " +
                "JOIN Malzemeler m ON tm.MalzemeID = m.MalzemeID WHERE tm.TarifID = ?";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, recipeId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("MalzemeID"), // Malzeme ID
                        rs.getString("MalzemeAdi"), // Malzeme Adı
                        rs.getString("ToplamMiktar"), // Toplam Miktar
                        rs.getString("MalzemeBirim"), // Malzeme Birimi
                        rs.getDouble("BirimFiyat") // Birim Fiyat
                );
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredients;
    }



    public List<String> getAllIngredientNames() {
        List<String> ingredients = new ArrayList<>();
        String sql = "SELECT MalzemeAdi FROM Malzemeler";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ingredients.add(rs.getString("MalzemeAdi"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredients;
    }


    public List<IngredientRecipe> getIngredientRecipeForRecipe(int recipeId) {
        List<IngredientRecipe> ingredientRecipes = new ArrayList<>();
        String sql = "SELECT tm.malzemeid, tm.malzememiktar FROM tarif_malzeme tm WHERE tm.TarifID = ?";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, recipeId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                IngredientRecipe ingredientRecipe = new IngredientRecipe(
                        getRecipeWithId(recipeId),
                        IngredientManager.getIngredientById(rs.getInt("MalzemeID")), // Malzeme ID
                        rs.getFloat("malzememiktar") // Birim Fiyat
                );
                ingredientRecipes.add(ingredientRecipe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredientRecipes;
    }


    public boolean checkIngredientsSufficiency(Recipe recipe) {
        recipe.setIngredients(getIngredientRecipeForRecipe(recipe.getId()));
        return recipe.checkIngredientsSufficiency();
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM Malzemeler";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("MalzemeID"), // Malzeme ID
                        rs.getString("MalzemeAdi"), // Malzeme Adı
                        rs.getString("ToplamMiktar"), // Toplam Miktar
                        rs.getString("MalzemeBirim"), // Malzeme Birimi
                        rs.getDouble("BirimFiyat") // Birim Fiyat
                );
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredients;
    }
}
