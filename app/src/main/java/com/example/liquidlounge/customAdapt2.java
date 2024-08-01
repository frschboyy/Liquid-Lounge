package com.example.liquidlounge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class customAdapt2 extends RecyclerView.Adapter<customAdapt2.ViewHolder> {

    private final Context context;
    private final List<logData> dataList;

    public customAdapt2(Context context, List<logData> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current logData item
        logData currentItem = dataList.get(position);
        // Set the text of the TextView to the string representation of logData
        holder.textView.setText(currentItem.toString());
    }

    @Override
    public int getItemCount() {
        return dataList.size(); // Return the size of the list
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.name); // Ensure this ID matches your layout
        }
    }
}