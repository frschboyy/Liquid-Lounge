package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderLogs extends AppCompatActivity {

    CollectionReference colRef;
    FirebaseFirestore fStore;
    customAdapt2 adapter;
    TextView title;
    RecyclerView logs;
    List<logData> datalist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_logs);

        // Initialize Firebase Firestore
        fStore = FirebaseFirestore.getInstance();
        colRef = fStore.collection("order logs");

        // Retrieve and display data
        retrieveData();
    }

    private void retrieveData() {
        Intent intent = getIntent();
        String branch = intent.getStringExtra("branch");

        title = findViewById(R.id.title);
        title.setText(branch + " - ORDERS");

        logs = findViewById(R.id.logs);
        logs.setLayoutManager(new LinearLayoutManager(this));

        colRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                datalist.clear(); // Clear any existing data
                for (QueryDocumentSnapshot document : task.getResult()) {
                    logData data = document.toObject(logData.class);
                    datalist.add(data);
                }
                // Initialize adapter with the list of logData
                adapter = new customAdapt2(this, datalist);
                logs.setAdapter(adapter);
            } else {
                Log.d("Firebase", "Error getting documents: ", task.getException());
            }
        });
    }
}
