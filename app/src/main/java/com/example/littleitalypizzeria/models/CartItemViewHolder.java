package com.example.littleitalypizzeria.models;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.littleitalypizzeria.R;

//Class to handle each item in the item list in the main window
public class CartItemViewHolder extends RecyclerView.ViewHolder {

    View view;

    public CartItemViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setName(String name){
        TextView itemName = view.findViewById(R.id.itemName);
        itemName.setText(name);
    }

    public void setQuantity(String quantity){
        TextView itemQuantity = view.findViewById(R.id.itemQuantity);
        itemQuantity.setText(quantity);
    }

    public void setPrice(String price){
        TextView itemPrice = view.findViewById(R.id.itemPrice);
        itemPrice.setText(price);
    }
}