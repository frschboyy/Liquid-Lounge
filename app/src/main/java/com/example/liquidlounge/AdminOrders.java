package com.example.liquidlounge;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrders extends AppCompatActivity {
    private ImageView anger, emb, sad, hq, homepage , profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        // Hide action bar
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.hide();

        sad = findViewById(R.id.sadness);
        anger = findViewById(R.id.anger);
        emb = findViewById(R.id.embarassment);
        hq = findViewById(R.id.headquarters);
        homepage = findViewById(R.id.homepage);
        profile = findViewById(R.id.profile);

        // Button click listeners
        homepage.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminHome.class);
            startActivity(intent);
        });

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminProfile.class);
            startActivity(intent);
        });

        sad.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminOrderLogs.class);
            intent.putExtra("branch", "Sadness");
            startActivity(intent);
        });

        anger.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminOrderLogs.class);
            intent.putExtra("branch", "Anger");
            startActivity(intent);
        });

        emb.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminOrderLogs.class);
            intent.putExtra("branch", "Embarrassment");
            startActivity(intent);
        });

        hq.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminOrderLogs.class);
            intent.putExtra("branch", "HeadQuarters");
            startActivity(intent);
        });
    }
}