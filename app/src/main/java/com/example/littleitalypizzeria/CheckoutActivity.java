package com.example.littleitalypizzeria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.littleitalypizzeria.models.CartItem;
import com.example.littleitalypizzeria.models.OrderInfo;
import com.example.littleitalypizzeria.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class CheckoutActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView totalTextView, messageView;
    private Button selectTimeButton, checkoutButton;

    private String type;
    private double total;
    private boolean timeSelected;
    private ArrayList<CartItem> cartItems;
    private String selectedTime;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent homeIntent = new Intent(CheckoutActivity.this, MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }

            }
        };

        /*Get data from cart intent*/
        type = getIntent().getExtras().getString("type");
        total = getIntent().getExtras().getDouble("total");
        cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cart");

        /*Setting up the toolbar*/
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        timeSelected = false;

        /*Setup the view according to type of checkout*/
        selectTimeButton = findViewById(R.id.selectTimeButton);
        totalTextView = findViewById(R.id.totalTextView);
        messageView = findViewById(R.id.messageView);
        checkoutButton = findViewById(R.id.checkoutButton);
        totalTextView.setText("$ "+total);

        if(type.equals("pickup")) {
            selectTimeButton.setText("Select Pickup Time");
            messageView.setText("Pickup time should be after 30 minutes from the time of order");
        }

        //When "Select Time" buttons is clicked
        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime();
            }
        });

        //When "Checkout" button is clicked
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeOrder();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // If not logged in redirect to Home
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //When back button in toolbar is clicked
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTime() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(CheckoutActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                if(type.equals("pickup")){

                    if(Utils.isPickupTimeValid(selectedHour,selectedMinute)){
                        timeSelected = true;
                        selectedTime = String.format("%d:%d", selectedHour, selectedMinute);
                        Toast.makeText(CheckoutActivity.this, "Pickup time selected!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(CheckoutActivity.this, "Please input valid pickup time", Toast.LENGTH_SHORT).show();
                    }

                }else if(type.equals("dine_in")){
                    if(Utils.isReservationTimeValid(selectedHour,selectedMinute)){
                        timeSelected = true;
                        selectedTime = String.format("%d:%d", selectedHour, selectedMinute);
                        Toast.makeText(CheckoutActivity.this, "Reservation time selected!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(CheckoutActivity.this, "Please input valid reservation time", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private void placeOrder() {

        if(timeSelected){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("orders").child(firebaseAuth.getCurrentUser().getUid());

            //Get the key when items are pushed to the database
            String pushKey = databaseReference.push().getKey();

            //Insert order items into the database
            for(int i=0; i<cartItems.size(); i++){
                databaseReference.child(pushKey).child("items").child(cartItems.get(i).getItemID()).setValue(cartItems.get(i));
            }

            //Insert other order information into the database
            OrderInfo orderInfo = new OrderInfo();

            orderInfo.setOrderedAt(Utils.getDateTime());
            orderInfo.setTotal(total);
            orderInfo.setType(type);

            if(type.equals("dine_in")){
                orderInfo.setReservationTime(selectedTime);
            }else if(type.equals("pickup")){
                orderInfo.setPickupTime(selectedTime);
            }

            databaseReference.child(pushKey).child("info").setValue(orderInfo);

            //Clear cart after placing order
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("cart");
            cartRef.child(firebaseAuth.getCurrentUser().getUid()).removeValue();

            Toast.makeText(this, "Successfully placed order!", Toast.LENGTH_LONG).show();

            //Send user home
            Intent homeIntent = new Intent(CheckoutActivity.this, UserActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }else{
            Toast.makeText(this, "Please select a time!", Toast.LENGTH_SHORT).show();
        }



    }
}