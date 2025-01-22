package com.example.tarifapp.managers;

import com.example.tarifapp.models.Ingredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/TarifApp";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "1234c";

    public void addIngredient(Ingredient ingredient) {
        String sql = "INSERT INTO Malzemeler (MalzemeAdi, BirimFiyat, MalzemeBirim, ToplamMiktar) VALUES (?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, ingredient.getName());
            pst.setDouble(2, ingredient.getUnitPrice());
            pst.setString(3, ingredient.getUnit());
            pst.setString(4, ingredient.getTotalQuantity());
            pst.executeUpdate();

            System.out.println("Malzeme başarıyla eklendi.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllIngredients() {
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

    public Ingredient getIngredientByName(String ingredientName) {
        Ingredient ingredient = null;
        String sql = "SELECT * FROM Malzemeler WHERE MalzemeAdi = ?";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, ingredientName);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                ingredient = new Ingredient(
                        rs.getInt("MalzemeID"), // Malzeme ID
                        rs.getString("MalzemeAdi"), // Malzeme Adı
                        rs.getString("ToplamMiktar"), // Toplam Miktar
                        rs.getString("MalzemeBirim"), // Malzeme Birimi
                        rs.getDouble("BirimFiyat") // Birim Fiyat
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredient;
    }

    public static Ingredient getIngredientById(int ingredientId) {
        Ingredient ingredient = null;
        String sql = "SELECT * FROM Malzemeler WHERE MalzemeId = ?";

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, ingredientId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                ingredient = new Ingredient(
                        rs.getInt("MalzemeID"), // Malzeme ID
                        rs.getString("MalzemeAdi"), // Malzeme Adı
                        rs.getString("ToplamMiktar"), // Toplam Miktar
                        rs.getString("MalzemeBirim"), // Malzeme Birimi
                        rs.getDouble("BirimFiyat") // Birim Fiyat
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredient;
    }

}
