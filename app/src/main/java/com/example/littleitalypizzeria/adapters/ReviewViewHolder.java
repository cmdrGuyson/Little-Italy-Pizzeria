package com.example.littleitalypizzeria.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.littleitalypizzeria.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReviewViewHolder extends RecyclerView.ViewHolder {

    private View view;

    public ReviewViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setName(String userID) {
        final TextView nameView = view.findViewById(R.id.name);


        //Getting user's name from database using userID
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user");
        userRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("firstName").getValue().toString() + " " + snapshot.child("lastName").getValue().toString();
                nameView.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void setReviewBody(String body) {
        TextView reviewBody = view.findViewById(R.id.reviewBody);
        reviewBody.setText(body);
    }
}