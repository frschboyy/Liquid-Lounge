package com.example.liquidlounge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Checkout extends AppCompatActivity implements OnMapReadyCallback {
    // Firebase instances
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    DocumentReference docRef, docRef1, docRef2, docRef3, docRef4;
    CollectionReference colRef, colRef1;

    // UI elements
    private GoogleMap mMap;
    private ImageView home, profile, pickAddress, cart;
    private TextView address, totalPrice;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button purchase;

    private static final int AUTOCOMPLETE_REQUEST_CODE = 100;
    private int total = 0;
    private String branch ="";
    private String addressStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Hide Action Bar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }

        // Initialize firebase instances
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        docRef = fStore.document("users/" + mAuth.getUid());

        // Initialize UI elements
        cart = findViewById(R.id.viewCart);
        pickAddress = findViewById(R.id.find);
        home = findViewById(R.id.homepage);
        profile = findViewById(R.id.profile);
        address = findViewById(R.id.address);
        radioGroup = findViewById(R.id.radioGroup);
        radioButton = findViewById(R.id.tail);
        radioButton.setVisibility(View.GONE);
        purchase = findViewById(R.id.purchase);
        totalPrice = findViewById(R.id.totalPrice);

        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreference = getSharedPreferences("CheckoutPrefs", MODE_PRIVATE);
        String savedAddress = sharedPreference.getString("address", "");
        String savedLatLng = sharedPreference.getString("latlng", "");
        String savedBranch = sharedPreference.getString("branch", "");

        // Set the saved address and update the map
        if (!savedAddress.isEmpty() && !savedLatLng.isEmpty()) {
            address.setText(savedAddress);
            String[] latLngParts = savedLatLng.split(",");
            LatLng latLng = new LatLng(Double.parseDouble(latLngParts[0]), Double.parseDouble(latLngParts[1]));
            if (mMap != null) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        }

        // Set the saved branch selection
        if (!savedBranch.isEmpty()) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                RadioButton button = (RadioButton) radioGroup.getChildAt(i);
                if (button.getText().toString().equals(savedBranch)) {
                    button.setChecked(true);
                    radioButton = button;
                    break;
                }
            }
        }

        priceAtCheckout();

        // Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyBuKRf8LDiaw_jUz3FuFdlSoftrXfxPmqg");
        }

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set the click listener for radio buttons in radio group
        radioGroup.setOnCheckedChangeListener((group, checkedID) -> {
            radioButton = findViewById(checkedID);

            // Save the branch selection in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("CheckoutPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("branch", radioButton.getText().toString());
            editor.apply();
        });

        // Set the click listener on map image
        pickAddress.setOnClickListener(view -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        home.setOnClickListener(view -> {
            Intent intent = new Intent(this, UserHome.class);
            startActivity(intent);
        });

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
        });

        totalPrice.setOnClickListener(view -> {
            Intent intent = new Intent(this, Cart.class);
            startActivity(intent);
        });
        cart.setOnClickListener(view -> {
            Intent intent = new Intent(this, Cart.class);
            startActivity(intent);
        });

        purchase.setOnClickListener(view -> {
            addressStr = address.getText().toString();
            branch = radioButton.getText().toString();

            if (addressStr.isEmpty()) {
                Toast.makeText(this, "Please Enter An Address!!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (branch.isEmpty()) {
                Toast.makeText(this, "Please Select A Branch!!", Toast.LENGTH_SHORT).show();
                return;


            }// Disable the button to prevent multiple clicks
            purchase.setClickable(false);
            purchase.setText("Processing...");

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
                                    checkInventory(global,details);
                                } else {
                                    Log.d("Product Details", "No products selected");
                                }
                            } else {
                                Log.d("Simple Debug - globalSelected: ", ""+productDetails.selectedProducts);
                            }
                        } else {
                            // Handle case where the document doesn't exist
                            Log.d("Exist", "No such document");
                        }
                    }).addOnFailureListener(e -> {
                        // Handle failures
                        Log.w("Retrieval", "Error getting document", e);
                        purchase.setClickable(true);
                        purchase.setText("Purchase");
                        Toast.makeText(this,"failed!",Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void priceAtCheckout() {
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Number priceObj = (Number) documentSnapshot.get("totalPrice");
                        total = (priceObj != null) ? priceObj.intValue() : 0;
                        totalPrice.setText("KSh " + total);

                        if (total == 0){
                            showCartEmptyDialog();
                        }
                    }
                    else {
                        Log.d("Firebase", "No such document");
                    }
                })
                .addOnFailureListener(e ->{
                    Log.d("Firebase", "Error retrieving document");
                });
    }

    private void showCartEmptyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Cart Empty!!")
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                    Intent intent = new Intent(this, UserHome.class);
                    startActivity(intent);
                })
                .setIcon(R.drawable.empty)
                .show();
    }

    private void checkInventory(Map<String, Object> global, Map<String, Object> details) {
        AtomicInteger processedEntries = new AtomicInteger(0);
        AtomicBoolean checkPassed = new AtomicBoolean(true);
        int totalEntries = global.size();

        for (Map.Entry<String, Object> entry : global.entrySet()) {
            String productName = entry.getKey();
            Map<String, Object> productDetail = (Map<String, Object>) entry.getValue();

            // Retrieve product details
            Number quantityObj = (Number) productDetail.get("quantity");
            String category = (String) productDetail.get("category");
            if (category.equals("energy"))
                category = "energyDrinks";
            if (category.equals("soft"))
                category = "softDrinks";

            // Convert quantity to an int safely
            int quantity = (quantityObj != null) ? quantityObj.intValue() : 0;

            docRef1 = fStore.document("branches/" + branch + "/inventory/" + category);
            docRef1.get()
                    .addOnSuccessListener(documentSnapshot1 -> {
                        if (documentSnapshot1.exists()) {
                            Map<String, Object> amounts = documentSnapshot1.getData();
                            if (amounts != null) {
                                // Get product amount in inventory
                                Number amountObj = (Number) amounts.get(productName);
                                int amount = (amountObj != null) ? amountObj.intValue() : 0;

                                // Check if product is in stock
                                int updatedAmount = amount - quantity;
                                if (updatedAmount < 0) {
                                    showLowInventoryDialog(productName, amount);
                                    checkPassed.set(false);
                                    purchase.setClickable(true);
                                    purchase.setText("Purchase");
                                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        if (processedEntries.incrementAndGet() == totalEntries && checkPassed.get()) {
                            proceedWithTransaction(global, details);
                        }
                    }).addOnFailureListener(e -> {
                        Log.d("Firebase", "Failed to retrieve amounts");
                    });
        }
    }

    private void proceedWithTransaction(Map<String, Object> global, Map<String, Object> userDetails) {
        // Iterate through all products in globalProducts
        for (Map.Entry<String, Object> entry : global.entrySet()) {
            Map<String, Object> productDetail = (Map<String, Object>) entry.getValue();

            String productName = entry.getKey();
            Number quantityObj = (Number) productDetail.get("quantity");
            Number priceObj = (Number) productDetail.get("price");
            String category = (String) productDetail.get("category");
            Log.d("Firebase", productName + " of category " +category);
            if (category.equals("energy"))
                category = "energyDrinks";
            if (category.equals("soft"))
                category = "softDrinks";

            int quantity = (quantityObj != null) ? quantityObj.intValue() : 0;
            double price = (priceObj != null) ? priceObj.doubleValue() : 0.0;

            Product p = new Product(productName, quantity, price, category);
            productDetails.selectedProducts.add(p);

            DocumentReference inventoryRef = fStore.document("branches/" + branch + "/inventory/" + category);
            double subtotal = price * quantity;

            // Update inventory
            String finalCategory = category;
            inventoryRef.update(productName, FieldValue.increment(-quantity))
                    .addOnSuccessListener(aVoid -> {
                        // Log inventory update
                        Log.d("Firebase", "Updated inventory for " + productName + " in category " + finalCategory);

                        DocumentReference branchSalesRef = fStore.document("branches/" + branch);

                        // Update branch sales
                        branchSalesRef.update("total sales", FieldValue.increment(subtotal))
                                .addOnSuccessListener(bVoid -> {
                                    DocumentReference globalSalesRef = fStore.document("revenue/Total Sales");

                                    // Update global total sales
                                    globalSalesRef.update("Total", FieldValue.increment(subtotal))
                                            .addOnSuccessListener(cVoid -> {

                                                // Update global product sales
                                                globalSalesRef.update("Total - " + productName, FieldValue.increment(quantity))
                                                        .addOnSuccessListener(dVoid -> {
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.d("Firebase", "Failed to update global product sales", e);
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.d("Firebase", "Failed to update global total sales", e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("Firebase", "Failed to update branch sales", e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Firebase", "Failed to update inventory for " + productName, e);
                    });
        }
        // Retrieve
        String name = (String) userDetails.get("Name");
        String email = (String) userDetails.get("Email");
        String phone = (String) userDetails.get("Phone Number");
        Number priceObj = (Number) userDetails.get("totalPrice");
        double price = (priceObj != null) ? priceObj.doubleValue() : 0.0;

        colRef = fStore.collection("orders/" + mAuth.getUid() + "/Order Logs");
        Map<String, Object> newLog = new HashMap<>();
        newLog.put("Products Ordered", productDetails.selectedProducts);
        newLog.put("Shipping Address", addressStr);
        newLog.put("Total Price", price);
        Timestamp timestamp = Timestamp.now();
        newLog.put("Date/Time", timestamp);

        colRef.add(newLog)
                .addOnSuccessListener(dVoid -> {

                    colRef1 = fStore.collection("order logs");
                    Map<String, Object> newGlobalLog = new HashMap<>();
                    newGlobalLog.put("Customer Email", email);
                    newGlobalLog.put("Customer Name", name);
                    newGlobalLog.put("Customer Number", phone);
                    newGlobalLog.put("Products Ordered", productDetails.selectedProducts);
                    newGlobalLog.put("Shipping Address", addressStr);
                    newGlobalLog.put("Total Price", price);
                    newGlobalLog.put("Customer ID", mAuth.getUid());
                    newGlobalLog.put("Date/Time", timestamp);
                    newGlobalLog.put("Branch", branch);

                    colRef1.add(newGlobalLog)
                            .addOnSuccessListener(eVoid -> {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("Name", name);
                                updates.put("ID", mAuth.getUid());
                                updates.put("Email", email);
                                updates.put("Phone Number", phone);

                                docRef.set(updates)
                                        .addOnSuccessListener(fVoid -> {
                                            purchase.setClickable(true);
                                            purchase.setText("Purchase");
                                            productDetails.selectedProducts.clear();
                                            Toast.makeText(this, "Purchase Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this, UserHome.class);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {

                                        });
                            })
                            .addOnFailureListener(e -> {

                            });
                })
                .addOnFailureListener(e -> {

                });
    }

    private void showLowInventoryDialog(String productName, int amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(productName + ":\n" + amount + " items in stock.\n\n1. Try another branch\n2. Reduce quantity.")
                .setCancelable(false)
                .setTitle("Low Inventory")
                .setNegativeButton("1.", (dialog, id) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("2.", (dialog, id) -> {
                    Intent intent = new Intent(this, Cart.class);
                    startActivity(intent);
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            String address = place.getAddress();
            LatLng latLng = place.getLatLng();
            if (latLng != null && mMap != null) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
            // Use the address as needed
            this.address.setText(address);

            // Save the address in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("CheckoutPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("address", address);
            editor.putString("lat", String.valueOf(latLng.latitude));
            editor.putString("lng", String.valueOf(latLng.longitude));
            editor.apply();

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            // Handle the error
            Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker for Nairobi
        LatLng position = getLastPosition(); // Nairobi coordinates
        mMap.addMarker(new MarkerOptions().position(position));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }

    public LatLng getLastPosition() {
        SharedPreferences sharedPreferences = getSharedPreferences("CheckoutPrefs", MODE_PRIVATE);
        double lat = Double.parseDouble(sharedPreferences.getString("lat", String.valueOf(-1.286389))); // Default to Nairobi
        double lng = Double.parseDouble(sharedPreferences.getString("lng", String.valueOf(36.817223))); // Default to Nairobi
        return new LatLng(lat, lng);
    }
}