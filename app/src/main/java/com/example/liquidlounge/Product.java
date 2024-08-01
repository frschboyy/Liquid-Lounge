package com.example.liquidlounge;

import com.google.firebase.storage.StorageReference;

import java.io.Serializable;

public class Product{
    private String name, category;
    private int quantity;
    private double price;

    public Product(String name, int quantity, double price, String category) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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
}
