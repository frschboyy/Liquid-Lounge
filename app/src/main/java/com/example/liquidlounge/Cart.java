package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cart extends AppCompatActivity implements OnItemClickListener{

    // Firebase instances
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    DocumentReference docRef;

    // UI elements
    private RecyclerView recyclerView;
    private customAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ImageView home, checkout, profile;
    private Button purchase;

    private List<Product> productList = new ArrayList<>();

    private int totalPrice; // Total price

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Hide action bar
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.hide();

        // Initialize firebase instances
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        docRef = fStore.document("users/" + mAuth.getUid());

        // Initialize UI components for recycler view
        recyclerView = findViewById(R.id.cartList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new customAdapter(productDetails.selectedProducts, this, this);
        recyclerView.setAdapter(adapter);

        purchase = findViewById(R.id.purchase);
        home = findViewById(R.id.homepage);
        checkout = findViewById(R.id.orders);
        profile = findViewById(R.id.profile);

        // load cart
        retrieveContext();

        purchase.setOnClickListener(view -> {
            saveContext();
            Intent intent = new Intent(this, Checkout.class);
            startActivity(intent);
        });

        home.setOnClickListener(view -> {
            saveContext();
            Intent intent = new Intent(this, UserHome.class);
            startActivity(intent);
        });

        checkout.setOnClickListener(view -> {
            saveContext();
            Intent intent = new Intent(this, Checkout.class);
            startActivity(intent);
        });

        profile.setOnClickListener(view -> {
            saveContext();
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
        });
    }

    private void retrieveContext() {
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> details = documentSnapshot.getData();
                        if (details != null) {
                            productDetails.selectedProducts.clear();
                            productDetails.categoryNames.clear();
                            // Get the map for globally selected products
                            Map<String, Object> global = (Map<String, Object>) details.get("globalProducts");

                            // Check if globally selected products is null or empty
                            if (global != null && !global.isEmpty()) {
                                // Iterate through all products in globalProducts
                                for (Map.Entry<String, Object> entry : global.entrySet()) {
                                    Log.d("firebase",""+entry.getKey());
                                    String productName = entry.getKey();
                                    Map<String, Object> productDetail = (Map<String, Object>) entry.getValue();

                                    // Retrieve product details
                                    Number quantityObj = (Number) productDetail.get("quantity");
                                    Number priceObj = (Number) productDetail.get("price");
                                    String name = (String) productDetail.get("name");
                                    String category = (String) productDetail.get("category");

                                    // Convert quantity to an int safely
                                    int quantity = (quantityObj != null) ? quantityObj.intValue() : 0;
                                    // Convert price to a double safely
                                    double price = (priceObj != null) ? priceObj.doubleValue() : 0.0;

                                    // Update globally selected product list with retrieved details
                                    Product p = new Product(name, quantity, price, category);
                                    productDetails.selectedProducts.add(p);
//
                                    int productIndex = productDetails.selectedProducts.indexOf(p);
                                    if(productIndex != -1){
                                        adapter.notifyItemChanged(productIndex);
                                    }
                                }
                                adapter.notifyDataSetChanged();

                                for (Product p : productDetails.selectedProducts) {
                                    productList.add(new Product(p.getName(), p.getQuantity(), p.getPrice(), p.getCategory()));
                                }

                                // Retrieve totalPrice
                                Number priceObj = (Number) documentSnapshot.get("totalPrice");
                                // Convert quantity to an int safely
                                totalPrice = (priceObj != null) ? priceObj.intValue() : 0;

                                purchase.setText("Go to Chechout -> KSh " + totalPrice);
                                purchase.setVisibility(View.VISIBLE);
                                Log.d("Simple Debug - globalSelected: ", ""+productDetails.selectedProducts);
                                Log.d("Simple Debug - globalSelected2: ", ""+productList);


                            } else {
                                adapter.notifyDataSetChanged();
                                Log.d("Product Details", "No products selected");
                            }
                        } else {
                            Log.d("Simple Debug - globalSelected: ", ""+productDetails.selectedProducts);
                        }
                    } else {
                        // Handle case where the document doesn't exist
                        Log.d("Exist", "No such document");
                    }
                    if(totalPrice <= 0){
                        showCartEmptyDialog();
                    }
                }).addOnFailureListener(e -> {
                    // Handle failures
                    Log.w("Retrieval", "Error getting document", e);
                });
    }

    public void updateItems(Product p, int num){
        if(num == 2) totalPrice += (int) p.getPrice();
        else {
            if(p.getQuantity() <= 0){
                productDetails.selectedProducts.remove(p);
                adapter.notifyDataSetChanged();
            }
            if(p.getQuantity() == 1){
                // change icon to bin
                int index = productDetails.selectedProducts.indexOf(p);
                if(index != -1){
                    customAdapter.ViewHolder holder = (customAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
                    if(holder != null){
                        Glide.with(this).load(R.drawable.bin).into(holder.minus);  // Use the bin drawable resource
                    }
                }
            }
            totalPrice -= (int) p.getPrice();
        }

        purchase.setText("Go to Checkout ->\nKSh " + totalPrice);
        if(totalPrice <= 0 && productDetails.selectedProducts.isEmpty()){
            purchase.setVisibility(View.GONE);
            showCartEmptyDialog();
        }
        else
            purchase.setVisibility(View.VISIBLE);
    }

    private void showCartEmptyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Cart Empty!!")
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                    saveContext();
                    Intent intent = new Intent(this, UserHome.class);
                    startActivity(intent);
                })
                .setIcon(R.drawable.empty)
                .show();
    }

    @Override
    public void onAddItemClick(Product product) {
        updateItems(product, 2);
    }

    @Override
    public void onMinusItemClick(Product product) {
        updateItems(product, 1);
    }

    public void saveContext(){
        Log.d("Simple Debug665665", ""+productList);
        Log.d("Simple Debug665665ss", ""+productDetails.selectedProducts);

        // update productList to track changes
        List<Product> trackChanges = new ArrayList<>();
        for (Product old : productList) {
            boolean exists = false;
            for (Product updated : productDetails.selectedProducts) {
                if (old.getName().equals(updated.getName())) {
                    if (old.getQuantity() != updated.getQuantity()) {
                        old.setQuantity(updated.getQuantity());
                    }
                    else{
                        old = null;
                    }
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                old.setQuantity(0);
            }
            if (old != null) {
                trackChanges.add(old); // Add the modified product to the new list
            }
        }

        // Replace the original list with the updated list
        productList = trackChanges;

        for(Product latest : productDetails.selectedProducts){
            String catName = latest.getCategory();
            if(!productDetails.categoryNames.contains(catName)){
                productDetails.addCategoryName(catName, this);
            }
        }

        // Convert List<Product> to HashMap<String, Product>
        Map<String, Map<String, Object>> global = convertListToMap(productDetails.selectedProducts);

        Map<String, Object> details = new HashMap<>();
        details.put("globalProducts", global);
        details.put("categories", productDetails.categoryNames);
        details.put("totalPrice", totalPrice);
        Log.d("Simple Debug666", ""+productDetails.categoryNames);
        Log.d("Simple Debug656", ""+totalPrice);
//        Log.d("Simple Debug665", ""+productDetails.selectedProducts);
//        Log.d("Simple Debug66566", ""+productList);

        docRef.update(details)
                .addOnSuccessListener(aVoid -> {
                    updateLocalItems();
                    Log.d("Firestore", "Selected products saved");
                }).addOnFailureListener(e -> {
                    Log.w("Firestore", "Error saving selections", e);
                });
    }

    private void updateLocalItems() {
        Log.d("New List1", "" + productDetails.selectedProducts);
        Log.d("Old List1: ", "" + productList);

        // Start with the first product
        updateNextProduct(0);
    }

    private void updateNextProduct(int index) {
        if (index >= productList.size()) {
            // All products have been processed
            Log.d("updateComplete", "All products have been updated.");
            return;
        }

        Product p = productList.get(index);
        String category = p.getCategory();
        String name = p.getName();

        docRef.get().continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            Map<String, Object> details = documentSnapshot.getData();

                            if (details != null) {
                                // Get the map for locally selected products
                                Map<String, Object> local = (Map<String, Object>) details.get("localProducts - " + category);

                                // Check if locally selected products is null or empty
                                if (local != null && !local.isEmpty()) {
                                    // Find Specific Product in localList
                                    Map<String, Object> data = (Map<String, Object>) local.get(name);
                                    if (data != null) {
                                        if (p.getQuantity() > 0)
                                            data.put("quantity", p.getQuantity());
                                        else
                                            local.remove(name);

                                        // Update the `local` map in the document
                                        Map<String, Object> updateData = new HashMap<>();
                                        updateData.put("localProducts - " + category, local);

                                        // Save the updated data to Firestore
                                        return docRef.update(updateData);
                                    }
                                }
                            }
                        }
                        return Tasks.forException(new Exception("Document does not exist or details are null"));
                    } else {
                        return Tasks.forException(task.getException());
                    }
                })
                .addOnSuccessListener(updateTask -> {
                    Log.d("updateSuccess", "Document updated successfully for product: " + name);
                    updateNextProduct(index + 1);
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error updating document for product: " + name, e);
//                    updateNextProduct(index + 1);
                });
    }

    private Map<String, Map<String, Object>> convertListToMap(List<Product> list) {
        Map<String, Map<String, Object>> listMap = new HashMap<>();

        for (Product product : list) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", product.getName());
            data.put("quantity", product.getQuantity());
            data.put("price", product.getPrice());
            data.put("category", product.getCategory());

            listMap.put(product.getName(), data);
        }
        return listMap;
    }
}