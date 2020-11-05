package com.example.littleitalypizzeria.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.littleitalypizzeria.R;

public class InquiryViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public InquiryViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setBody(String body){
        TextView bodyView = view.findViewById(R.id.body);
        bodyView.setText(body);
    }

    public void setResponse(String response){
        TextView responseView = view.findViewById(R.id.response);
        responseView.setText(response);
    }
}
