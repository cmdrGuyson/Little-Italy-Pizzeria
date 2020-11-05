package com.example.littleitalypizzeria.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.littleitalypizzeria.R;

//Class to handle each item in the orders list
public class OrderItemViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public OrderItemViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setOrderID(String orderID){
        TextView orderIDView = view.findViewById(R.id.orderID);
        orderIDView.setText("ID"+orderID);
    }

    public void setTotal(double total){
        TextView totalView = view.findViewById(R.id.totalPriceView);
        totalView.setText("$ "+total);
    }

    public void setType(String type){
        TextView typeView = view.findViewById(R.id.typeView);
        typeView.setText(type);
    }

    public void setOrderedAt(String orderedAt){
        TextView orderedAtView = view.findViewById(R.id.orderedAtView);
        orderedAtView.setText(orderedAt);
    }
}