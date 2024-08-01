package com.example.liquidlounge;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.List;

public class priceChangeAdapter extends RecyclerView.Adapter<priceChangeAdapter.ViewHolder> {

    private final List<String> productNameList;
    private final List<String> categoryNames;
    private final boolean[] isEditing; // Array to keep track of editing state for each item
    private final FirebaseFirestore fStore; // Firestore instance
    private final Context context;
    private final int cat;

    public priceChangeAdapter(Context context, List<String> productNameList, List<String> categoryNames, int cat) {
        this.productNameList = productNameList;
        this.categoryNames = categoryNames;
        this.context = context;
        this.cat = cat;
        isEditing = new boolean[productNameList.size()];
        fStore = FirebaseFirestore.getInstance();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView productName;
        public EditText newPrice;
        public ImageView submit;
        public ImageView cancel;

        public ViewHolder(View view) {
            super(view) ;
            productName = view.findViewById(R.id.itemName);
            newPrice = view.findViewById(R.id.newPrice);
            submit = view.findViewById(R.id.update);
            cancel = view.findViewById(R.id.cancel);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.price_change, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String productName = productNameList.get(position);

        holder.productName.setText(productName);
        holder.newPrice.setText("");
        holder.cancel.setImageResource(R.drawable.x);
        holder.submit.setImageResource(R.drawable.check);

        // Handle visibility based on editing state
        if (isEditing[position]) {
            if (cat == 0) holder.newPrice.setHint("Amount");
            else holder.newPrice.setHint("New Price");
            holder.newPrice.setVisibility(View.VISIBLE);
            holder.submit.setVisibility(View.VISIBLE);
            holder.cancel.setVisibility(View.VISIBLE);
        } else {
            holder.newPrice.setText("");
            holder.newPrice.setVisibility(View.INVISIBLE);
            holder.submit.setVisibility(View.INVISIBLE);
            holder.cancel.setVisibility(View.INVISIBLE);
        }

        holder.productName.setOnClickListener(v -> {
            isEditing[position] = !isEditing[position];
            notifyItemChanged(position); // Refresh the item
        });

        holder.submit.setOnClickListener(v -> {
            String quantity = holder.newPrice.getText().toString().trim();

            // Check if the input is not empty
            if (quantity.isEmpty()) {
                Toast.makeText(context, "Field cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt to parse the input
            int amount;
            try {
                amount = Integer.parseInt(quantity);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid input!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the quantity is valid
            if (amount <= 0) {
                Toast.makeText(context, "Price must be greater than zero!", Toast.LENGTH_LONG).show();
                return;
            }

            updateProductPrice(productName, amount);

            // Exit editing mode and refresh the item
            isEditing[position] = false;
            notifyItemChanged(position);
        });

        holder.cancel.setOnClickListener(v -> {
            holder.newPrice.setText("");
            isEditing[position] = false; // Exit editing mode
            notifyItemChanged(position);
        });
    }

    private void updateProductPrice(String productName, int Quantity) {
        if (cat == 1) {
            DocumentReference docRef = fStore.document("branches/Prices");
            docRef.update(productName, Quantity)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Price updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Firebase", "Error updating product price");
                    });
        }
        else {
            DocumentReference docRef = fStore.document("branches/HQ/inventory/" + categoryNames.get(productNameList.indexOf(productName)));
            docRef.update(productName, FieldValue.increment(Quantity))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Stock updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Firebase", "Error updating product price");
                    });
        }
    }

    @Override
    public int getItemCount() {
        return productNameList.size();
    }
}
