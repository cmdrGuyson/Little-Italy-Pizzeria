package com.example.littleitalypizzeria.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.littleitalypizzeria.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

//Class to handle each item in the item list in the main window
public class ItemViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public ItemViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setTitle(String title) {
        TextView item_title = view.findViewById(R.id.itemName);
        item_title.setText(title);
    }

    public void setPrice(double price) {
        TextView item_description = view.findViewById(R.id.itemPrice);
        item_description.setText("$ "+String.valueOf(price));
    }

    public void setDescription(String description) {
        TextView item_description = view.findViewById(R.id.itemDescription);
        item_description.setText(description);
    }

    public void setImage(final String imageURL) {
        final ImageView imageView = view.findViewById(R.id.itemImage);
        //Load the downloaded image if it exists
        Picasso.get().load(imageURL).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception e) {
                //If the user hasn't downloaded images yet
                Picasso.get().load(imageURL).into(imageView);
            }
        });
    }
}