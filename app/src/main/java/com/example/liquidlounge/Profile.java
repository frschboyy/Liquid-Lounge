package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {
    ImageView home, checkout, logout;

    FirebaseAuth mAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar ab = getSupportActionBar();
        ab.hide();

        home = findViewById(R.id.homepage);
        checkout = findViewById(R.id.orders);
        logout = findViewById(R.id.logout);

        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        checkout.setOnClickListener(view -> {
            Intent intent = new Intent(this, Checkout.class);
            startActivity(intent);
        });

        home.setOnClickListener(view -> {
            Intent intent = new Intent(this, UserHome.class);
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
}