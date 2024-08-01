package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminHome extends AppCompatActivity {
    RecyclerView productSalesList;
    private customAdapt adapter;

    private ImageView sad, anger, emb, hq, orders, profile;
    private TextView salesAnger, salesEmb, salesSad, salesHead;
    private Button productSales;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Hide action bar
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.hide();

        sad = findViewById(R.id.sadness);
        anger = findViewById(R.id.anger);
        emb = findViewById(R.id.embarassment);
        hq = findViewById(R.id.headquarters);
        orders = findViewById(R.id.orders);
        profile = findViewById(R.id.profile);

        salesAnger = findViewById(R.id.salesAnger);
        salesEmb = findViewById(R.id.salesEmb);
        salesSad = findViewById(R.id.salesSad);
        salesHead = findViewById(R.id.salesHead);

        productSales = findViewById(R.id.button);

        productSalesList = findViewById(R.id.recyclerViewSales);
        productSalesList.setLayoutManager(new LinearLayoutManager(this));

        fStore = FirebaseFirestore.getInstance();

        // Button click listeners

        orders.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminOrders.class);
            startActivity(intent);
        });

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminProfile.class);
            startActivity(intent);
        });

        sad.setOnClickListener(view -> {
            branchSales("Sadness", salesSad);
        });

        anger.setOnClickListener(view -> {
            branchSales("Anger", salesAnger);
        });

        emb.setOnClickListener(view -> {
            branchSales("Embarrassment", salesEmb);
        });

        hq.setOnClickListener(view -> {
            branchSales("HQ", salesHead);
        });

        productSales.setOnClickListener(view -> {
            DocumentReference docRef = fStore.document("revenue/Total Sales");
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> fields = document.getData();

                        // Get product list
                        List<Item> products = new ArrayList<>();
                        for (Map.Entry<String, Object> entry : fields.entrySet()) {
                            String fieldName = entry.getKey();
                            if (fieldName.startsWith("Total - ")) {
                                Number quantityObj = (Number) entry.getValue();
                                int quantity = (quantityObj != null) ? quantityObj.intValue() : 0;
                                products.add(new Item(fieldName.substring(7), quantity));
                            }
                        }
                        adapter = new customAdapt(products);
                        productSalesList.setAdapter(adapter);
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                }
            });
        });
    }

    private void branchSales(String branch, TextView salesTextView) {
        DocumentReference docRef = fStore.document("branches/" + branch);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Object salesTotalObj = document.get("total sales");
                    if (salesTotalObj != null) {
                        Number salesTotal = (Number) salesTotalObj;
                        int total = salesTotal.intValue();
                        salesTextView.setText("Total sales: Ksh " + total);
                        salesTextView.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("Firestore", "Field 'total sales' does not exist in the document.");
                    }
                } else {
                    Log.d("Firestore", "No such document");
                }
            } else {
                Log.d("Firestore", "get failed with ", task.getException());
            }
        });
    }

}
