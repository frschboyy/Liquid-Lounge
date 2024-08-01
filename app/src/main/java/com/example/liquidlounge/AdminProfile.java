package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminProfile extends AppCompatActivity {
    // UI elements
    ImageView gone, gone1, home, orders, logout;
    Button changePrice, restock;
    RecyclerView itemsList, itemList1;

    // Firebase references
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    DocumentReference docRef;
    CollectionReference colRef;

    // List to store product/category names
    List<String> productNames = new ArrayList<>();
    List<String> categoryNames = new ArrayList<>();

    // Adapter for product names
    private priceChangeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        // Hide action bar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }

        // Initialize Firebase Instance
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        home = findViewById(R.id.homepage);
        orders = findViewById(R.id.orders);
        logout = findViewById(R.id.logout);
        gone = findViewById(R.id.gone);
        gone1 = findViewById(R.id.gone1);
        changePrice = findViewById(R.id.viewList);
        restock = findViewById(R.id.restock);

        // Initialize Recycler View components
        itemsList = findViewById(R.id.productList);
        itemList1 = findViewById(R.id.productRestockList);

        // Set click listeners for UI elements
        changePrice.setOnClickListener(view -> {
            itemList1.setVisibility(View.GONE);
            gone1.setVisibility(View.GONE);
            loadProductNames(1);

        });

        restock.setOnClickListener(view -> {
            itemsList.setVisibility(View.GONE);
            gone.setVisibility(View.GONE);
            loadProductNames(0);

        });

        gone.setOnClickListener(view -> {
            gone.setVisibility(View.INVISIBLE);
            itemsList.setVisibility(View.GONE);
        });

        gone1.setOnClickListener(view -> {
            gone1.setVisibility(View.INVISIBLE);
            itemList1.setVisibility(View.GONE);
        });

        orders.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminOrders.class);
            startActivity(intent);
        });

        home.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminHome.class);
            startActivity(intent);
        });

        logout.setOnClickListener(view -> {
            mAuth.signOut();
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, UserLogin.class);
            startActivity(intent);
            finish();
        });
    }

    // Fetch product names from Firebase
    private void loadProductNames(int type) {
        productNames.clear();
        colRef = fStore.collection("branches/HQ/inventory");
        colRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {

                            final int totalDocs = querySnapshot.size();
                            final int[] processedDocs = {0};

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                document.getReference().get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                DocumentSnapshot snapshot = task1.getResult();
                                                if (snapshot.exists()) {
                                                    Log.d("Firebase Debug:", "Product Names: " + snapshot.getData().keySet());
                                                    String docID = document.getId();
                                                    Map<String, Object> data = snapshot.getData();
                                                    if (data != null) {
                                                        for (String fieldName : data.keySet()) {
                                                            String entry = fieldName;
                                                            categoryNames.add(docID);
                                                            productNames.add(entry);
                                                        }
                                                    }
                                                }

                                                processedDocs[0]++;
                                                if (processedDocs[0] == totalDocs){
                                                    Log.d("Firebase Debug1:", "Product Names: " + productNames.toString());
                                                    if (type == 0) {
                                                        itemList1.setLayoutManager(new LinearLayoutManager(this));
                                                        adapter = new priceChangeAdapter(this, productNames,categoryNames, 0);
                                                        itemList1.setAdapter(adapter);
                                                        gone1.setVisibility(View.VISIBLE);
                                                        itemList1.setVisibility(View.VISIBLE);
                                                    }
                                                    else {
                                                        itemsList.setLayoutManager(new LinearLayoutManager(this));
                                                        adapter = new priceChangeAdapter(this, productNames,categoryNames, 1);
                                                        itemsList.setAdapter(adapter);
                                                        gone.setVisibility(View.VISIBLE);
                                                        itemsList.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            } else {
                                                Log.w("Firebase", "Error getting document.", task.getException());
                                            }
                                        });
                            }
                        }
                    } else {
                        Log.w("Firebase", "Error getting document.", task.getException());
                    }
                });
    }
}