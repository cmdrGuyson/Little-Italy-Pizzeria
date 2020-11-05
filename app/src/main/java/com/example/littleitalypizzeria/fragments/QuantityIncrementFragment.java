package com.example.littleitalypizzeria.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.littleitalypizzeria.R;
import com.example.littleitalypizzeria.models.OnInputListener;

public class QuantityIncrementFragment extends DialogFragment {

    public OnInputListener onInputListener;


    private static final String TAG = "QuantityIncrementFragment";

    private Button plusButton, minusButton, addToCartButton;
    private TextView quantityView;

    private int quantity = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
          View view = inflater.inflate(R.layout.fragment_increment_dialog, container, false);
          getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

          quantityView = view.findViewById(R.id.quantityView);
          plusButton = view.findViewById(R.id.plusButton);
          minusButton = view.findViewById(R.id.minusButton);
          addToCartButton = view.findViewById(R.id.addToCartButton);

          //When plus button is clicked
          plusButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  quantity+=1;
                  quantityView.setText(String.valueOf(quantity));
              }
          });

          //When minus button is clicked
          minusButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  if(quantity>1){
                      quantity-=1;
                      quantityView.setText(String.valueOf(quantity));
                  }
              }
          });

          //When add to cart button is clicked
          addToCartButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  onInputListener.sendInput(quantity);
                  getDialog().dismiss();
              }
          });


          return view;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{

            onInputListener = (OnInputListener) getActivity();

        }catch(ClassCastException ex){
            Log.e(TAG, "onAttach: CCE: " + ex.getMessage());
        }
    }
}
