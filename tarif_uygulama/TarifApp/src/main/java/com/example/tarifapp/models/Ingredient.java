package com.example.tarifapp.models;

public class Ingredient {
    private int id; // Malzeme ID
    private String name; // Malzeme Adı
    private String totalQuantity; // Malzeme miktar
    private String unit; // Ölçü birimi
    private double unitPrice; // Birim fiyat

    public Ingredient(int id, String name, String totalQuantity, String unit, double unitPrice) {
        this.id = id;
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }

    public String getName() {
        return name;
    }

    public String getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(String totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name; // Burada malzeme adını döndürüyoruz
    }
}
