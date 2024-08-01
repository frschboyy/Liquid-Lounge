package com.example.liquidlounge;

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

public class customAdapt extends RecyclerView.Adapter<customAdapt.ItemViewHolder> {

    private List<Item> itemList;

    public customAdapt(List<Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.productsales, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        // Combine item name and quantity into one text
        String combinedText = item.getName() + " - " + item.getQuantity();
        holder.textView.setText(combinedText);

        // Determine the drawable resource ID
        String imageName = item.getName().replace(" ", "").toLowerCase();  // Get the name of the drawable resource
        int imageResource = holder.itemView.getContext().getResources().getIdentifier(imageName, "drawable", holder.itemView.getContext().getPackageName());

        // Load the image with Glide
        Glide.with(holder.itemView.getContext())
                .load(imageResource)
                .error(R.drawable.anger)  // Error placeholder
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);  // ImageView into which the image will be loaded
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImage);
            textView = itemView.findViewById(R.id.item);
        }
    }
}
