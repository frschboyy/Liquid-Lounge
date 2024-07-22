package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class UserHome extends AppCompatActivity {
    ImageView checkout, profile, soft, energy, alcohol, dairy, water;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ActionBar ab = getSupportActionBar();
        ab.hide();

        Intent cartData = getIntent();

        checkout = findViewById(R.id.checkout);
        profile = findViewById(R.id.profile);
        soft = findViewById(R.id.soft);
        energy = findViewById(R.id.energy);
        alcohol = findViewById(R.id.alcohol);
        dairy = findViewById(R.id.dairy);
        water = findViewById(R.id.water);

        checkout.setOnClickListener(view -> {
            Intent intent = new Intent(this, Checkout.class);
            startActivity(intent);
        });

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
        });

        soft.setOnClickListener(view -> {
            Intent intent = new Intent(this, Products.class);
            intent.putExtra("category", "soft");
            startActivity(intent);
        });

        energy.setOnClickListener(view -> {
            Intent intent = new Intent(this, Products.class);
            intent.putExtra("category", "energy");
            startActivity(intent);
        });

        alcohol.setOnClickListener(view -> {
            Intent intent = new Intent(this, Products.class);
            intent.putExtra("category", "alcohol");
            startActivity(intent);
        });

        dairy.setOnClickListener(view -> {
            Intent intent = new Intent(this, Products.class);
            intent.putExtra("category", "dairy");
            startActivity(intent);
        });

        water.setOnClickListener(view -> {
            Intent intent = new Intent(this, Products.class);
            intent.putExtra("category", "water");
            startActivity(intent);
        });
    }
}