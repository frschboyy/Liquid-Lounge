package com.example.liquidlounge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;


public class customAdapter extends RecyclerView.Adapter<customAdapter.ViewHolder> {
    private List<Product> productList;
    private Context context;
    private static OnItemClickListener listener;

    public customAdapter(List<Product> productList, Context context, OnItemClickListener listener) {
        this.productList = productList;
        this.context = context;
        customAdapter.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView prodPic, add, minus;
        TextView prodName, prodPrice, count;
        Product p;

        public ViewHolder(View view) {
            super(view);
            // Initialize RecyclerView UI
            prodName = view.findViewById(R.id.productName);
            prodPrice = view.findViewById(R.id.productPrice);
            prodPic = view.findViewById(R.id.productPicture);
            add = view.findViewById(R.id.add);
            count = view.findViewById(R.id.count);
            minus = view.findViewById(R.id.minus);

            // Set click listeners for add and remove buttons
            add.setOnClickListener(v -> {
                if (p != null) {
                    p.setQuantity(p.getQuantity() + 1);
                    updateViewItem();
                    listener.onAddItemClick(p);
                }
            });

            minus.setOnClickListener(v -> {
                if (p != null) {
                    p.setQuantity(p.getQuantity() - 1);
                    updateViewItem();
                    listener.onMinusItemClick(p);
                }
            });
        }

        // Update view item based on product quantity
        void updateViewItem() {
            if (p != null) {
                count.setText(String.valueOf(p.getQuantity()));
                if (p.getQuantity() > 0) {
                    count.setVisibility(View.VISIBLE);
                    minus.setVisibility(View.VISIBLE);
                    if (p.getQuantity() > 1){
                        minus.setImageResource(R.drawable.remove);
                    }
                } else {
                    count.setVisibility(View.INVISIBLE);
                    minus.setVisibility(View.GONE);
                }
            }
        }

        // Bind product data to RecyclerView
        public void bind(Product product) {
            this.p = product;
            prodName.setText(product.getName());
            prodPrice.setText((int) product.getPrice() + " ksh");
            count.setText(String.valueOf(product.getQuantity()));

            String imageName = product.getName().replace(" ", "").toLowerCase();  // Get the name of the drawable resource
            int imageResource = itemView.getContext().getResources().getIdentifier(imageName, "drawable", itemView.getContext().getPackageName());

            // Load the image with Glide
            Glide.with(itemView.getContext())
                    .load(imageResource)  // Load the drawable resource using its ID
                    .error(R.drawable.anger)  // Error placeholder if the drawable cannot be loaded
                    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache the image in both memory and disk
                    .into(prodPic);  // ImageView into which the image will be loaded
        }

        public void notZero(boolean isZero) {
            if (isZero) {
                minus.setVisibility(View.VISIBLE);
                count.setVisibility(View.VISIBLE);
            } else {
                minus.setVisibility(View.GONE);
                count.setVisibility(View.INVISIBLE);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
        boolean isVisible = product.getQuantity() > 0;
        holder.notZero(isVisible);
        Glide.with(context).load(R.drawable.add_item).into(holder.add);
        Glide.with(context).load(R.drawable.remove).into(holder.minus);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
