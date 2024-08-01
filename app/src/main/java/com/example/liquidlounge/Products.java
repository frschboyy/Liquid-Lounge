package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Products extends AppCompatActivity implements OnItemClickListener {
    // Firebase instances
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    DocumentReference docRef;

    // UI elements
    private RecyclerView recyclerView;
    private customAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ImageView home, checkout, profile;
    private Button viewCart;
    private ProgressBar progressBar; // ProgressBar

    // Product lists
    private List<String> productNames;
    private List<Product> productList = new ArrayList<>();
    private List<Product> selectedProducts = new ArrayList<>();

    // Intent and category
    private Intent intent;
    private String category;

    private int totalPrice = 0; // Total price

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_inventory);

        // Hide action bar
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.hide();

        // Initialize firebase instances
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components for recycler view
        recyclerView = findViewById(R.id.productList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new customAdapter(productList, this, this);
        recyclerView.setAdapter(adapter);

        // Initialize other UI components
        home = findViewById(R.id.homepage);
        checkout = findViewById(R.id.orders);
        profile = findViewById(R.id.profile);
        viewCart = findViewById(R.id.viewCart);
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar

        // Retrieve category from intent
        intent = getIntent();
        category = intent.getStringExtra("category");

        // Show ProgressBar and fetch products
        progressBar.setVisibility(View.VISIBLE);
        fetchProducts(); // Fetch products based on category from Firestore

        viewCart.setOnClickListener(view -> {
            saveContext();
            intent = new Intent(this, Cart.class);
            startActivity(intent);
        });

        checkout.setOnClickListener(view -> {
            saveContext();
            intent = new Intent(this, Checkout.class);
            startActivity(intent);
        });

        home.setOnClickListener(view -> {
            saveContext();
            intent = new Intent(this, UserHome.class);
            startActivity(intent);
        });

        profile.setOnClickListener(view -> {
            saveContext();
            intent = new Intent(this, Profile.class);
            startActivity(intent);
        });
    }

    private void retrieveContext() {
        docRef = fStore.document("users/" + mAuth.getUid());
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> details = documentSnapshot.getData();
                        if (details != null) {
                            // Get the map for globally selected products
                            Map<String, Object> global = (Map<String, Object>) details.get("globalProducts");

                            // Reset totalPrice before calculation
                            totalPrice = 0;

                            // Check if locally selected products is null or empty
                            if (global != null && !global.isEmpty()) {
                                // Iterate through all products in globalProducts
                                for (Map.Entry<String, Object> entry : global.entrySet()) {
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

                                    totalPrice += (int) (quantity*price);
                                }
                                viewCart.setText("View cart - KSh " + totalPrice);
                                viewCart.setVisibility(View.VISIBLE);
                                Log.d("Simple Debug - globalSelected: ", ""+productDetails.selectedProducts);
                            }

                            // Get the list for globally selected categories
                            List<String> categoryList = (List<String>) details.get("categories");
                            if (categoryList != null) {
                                productDetails.categoryNames.clear();
                                productDetails.categoryNames.addAll(categoryList);
                            }
                            Log.d("Simple Debug - category: ", ""+productDetails.categoryNames);

                            // Get the map for locally selected products
                            Map<String, Object> local = (Map<String, Object>) details.get("localProducts - " + category);

                            // Check if locally selected products is null or empty
                            if (local != null && !local.isEmpty()) {
                                // Iterate through all products in 'localProducts - soft'
                                for (Map.Entry<String, Object> entry : local.entrySet()) {
                                    String productName = entry.getKey();
                                    Map<String, Object> productDetail = (Map<String, Object>) entry.getValue();

                                    // Retrieve the quantity of the product
                                    Number quantityObj = (Number) productDetail.get("quantity");
                                    int quantity = (quantityObj != null) ? quantityObj.intValue() : 0;

                                    // Update locally selected product list with retrieved quantity
                                    for (Product product : productList) {
                                        if (product.getName().equals(productName)) {
                                            product.setQuantity(quantity);
                                            selectedProducts.add(product);
                                            int productIndex = productList.indexOf(product);
                                            if(productIndex != -1){
                                                ((customAdapter) adapter).notifyItemChanged(productIndex);
                                            }
                                            break;
                                        }
                                    }
                                    adapter.notifyDataSetChanged();

                                    Log.d("Simple Debug - productList: ", ""+productList);
                                    Log.d("Simple Debug - localSelected: ", ""+selectedProducts);
                                }
                            } else {
                                adapter.notifyDataSetChanged();
                                Log.d("Product Details", "No products selected");
                            }
                            progressBar.setVisibility(View.GONE); // Hide ProgressBar when data is loaded
                        }
                    } else {
                        // Handle case where the document doesn't exist
                        Log.d("Exist", "No such document");
                    }
                }).addOnFailureListener(e -> {
                    // Handle failures
                    Log.w("Retrieval", "Error getting document", e);
                });
    }

    private void fetchProducts() {
        switch(category) {
            case "soft":
                docRef = fStore.document("branches/HQ/inventory/softDrinks");
                break;
            case "energy":
                docRef = fStore.document("branches/HQ/inventory/energyDrinks");
                break;
            case "alcohol":
                docRef = fStore.document("branches/HQ/inventory/alcohol");
                break;
            case "dairy":
                docRef = fStore.document("branches/HQ/inventory/dairy");
                break;
            default:
                docRef = fStore.document("branches/HQ/inventory/water");
        }

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {
                Map<String, Object> data = documentSnapshot.getData();
                if(data != null){
                    productNames = new ArrayList<>(data.keySet()); // extract product names

                    // populate products list with retrieved data
                    for(String name : productNames) {
                        getProductPrice(name);
                    }
                }
                else Log.d("Firestore", "Document data was null.");
            } else Log.d("Firestore", "Document does not exist.");
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error fetching document", e);
            Toast.makeText(this, "Error fetching products", Toast.LENGTH_SHORT).show();
        });
    }

    private void getProductPrice(String name) {
        docRef = fStore.collection("branches").document("Prices");
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double price = documentSnapshot.getDouble(name);
                        if (price != null) {
                            Product p = new Product(name, 0, price, category);
                            productList.add(p);

                            // Check if this was the last item to notify data set changed
                            if (productList.size() == productNames.size()) {
                                retrieveContext();
                            }
                        } else {
                            Log.d("Firestore", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error retrieving document", e);
                });
    }

    public void saveContext(){
        boolean exists = productDetails.getCategoryNames().contains(category);
        Log.d("Simple Debug655: ", ""+productList);
        Log.d("Simple Debug654: ", ""+productDetails.categoryNames);

        // Save local and global lists
        if (exists) {
            if (selectedProducts.isEmpty()) {
                productDetails.removeCategoryName(category);
            }
            productDetails.deleteProducts(productList);
            productDetails.addSelectedProducts(selectedProducts);
            Log.d("Simple Debug65: ", ""+selectedProducts);
            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
        } else {
            if (!selectedProducts.isEmpty()) {
                productDetails.addSelectedProducts(selectedProducts);
                productDetails.addCategoryName(category, this);
                Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
            }
        }
        // Convert List<Product> to HashMap<String, Product>
        Map<String, Map<String, Object>> local = convertListToMap(selectedProducts, 0);
        Map<String, Map<String, Object>> global = convertListToMap(productDetails.selectedProducts,1);

        docRef = fStore.collection("users").document(mAuth.getUid());

        Map<String, Object> details = new HashMap<>();
        details.put("localProducts - " + category, local);
        details.put("globalProducts", global);
        details.put("categories", productDetails.categoryNames);
        details.put("totalPrice", totalPrice);
        Log.d("Simple Debug6", ""+productDetails.categoryNames);

        docRef.update(details)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Selected products saved");
                }).addOnFailureListener(e -> {
                    Log.w("Firestore", "Error saving selections", e);
                });
    }

    private Map<String, Map<String, Object>> convertListToMap(List<Product> list, int num) {
        Map<String, Map<String, Object>> listMap = new HashMap<>();

        for (Product product : list) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", product.getName());
            data.put("quantity", product.getQuantity());
            data.put("price", product.getPrice());
            if(num == 1)
                data.put("category", product.getCategory());

            listMap.put(product.getName(), data);
        }
        return listMap;
    }


    public void updateItems(Product p, int num){
        if(num == 2) {
            if (!selectedProducts.contains(p)) selectedProducts.add(p);
            totalPrice += (int) p.getPrice();
        }
        else {
            if(p.getQuantity() <= 0) selectedProducts.remove(p);
            totalPrice -= (int) p.getPrice();
        }

        viewCart.setText("View cart - KSh " + totalPrice);
        if(totalPrice <= 0)
            viewCart.setVisibility(View.GONE);
        else
            viewCart.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAddItemClick(Product product) {
        updateItems(product, 2);
    }

    @Override
    public void onMinusItemClick(Product product) {
        updateItems(product, 1);
    }
}