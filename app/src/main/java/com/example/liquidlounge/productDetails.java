package com.example.liquidlounge;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

public class productDetails {
    private static FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static DocumentReference docRef = fStore.collection("users").document(mAuth.getUid());

    static List<Product> selectedProducts = new ArrayList<>();
    static List<String> categoryNames = new ArrayList<>();

    public static List<Product> getSelectedProducts() {
        return selectedProducts;
    }

    public static void deleteProducts(List<Product> productFamily) {
        for(Product latest : productFamily){
            // Remove the product from the internal list if it exists
            selectedProducts.removeIf(product -> product.getName().equals(latest.getName()));
        }
    }

    public static void addSelectedProducts(List<Product> selectedProducts) {
        productDetails.selectedProducts.addAll(selectedProducts);
    }

    public static List<String> getCategoryNames() {
        return categoryNames;
    }

    public static void removeCategoryName(String category) {
        categoryNames.remove(category);
    }

    public static void addCategoryName(String category, Context c) {
        categoryNames.add(category);
        Toast.makeText(c,"category added: " + category, Toast.LENGTH_SHORT).show();
    }
}
