package com.example.liquidlounge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserHome extends AppCompatActivity {
    ImageView checkout, profile, soft, energy, alcohol, dairy, water;
    Button viewCart;

    // Firebase instances
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }

        viewCart = findViewById(R.id.viewCart);
        checkout = findViewById(R.id.orders);
        profile = findViewById(R.id.profile);
        soft = findViewById(R.id.soft);
        energy = findViewById(R.id.energy);
        alcohol = findViewById(R.id.alcohol);
        dairy = findViewById(R.id.dairy);
        water = findViewById(R.id.water);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        docRef = fStore.collection("users").document(mAuth.getUid());
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Number priceObj = (Number) documentSnapshot.get("totalPrice");

                                // Convert quantity to an int safely
                                int price = (priceObj != null) ? priceObj.intValue() : 0;

                                viewCart.setText("View cart - KSh " + price);
                                if (price <= 0)
                                    viewCart.setVisibility(View.GONE);
                                else
                                    viewCart.setVisibility(View.VISIBLE);
                            }
                        });

        viewCart.setOnClickListener(view -> {
            Intent intent = new Intent(this, Cart.class);
            startActivity(intent);
        });

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