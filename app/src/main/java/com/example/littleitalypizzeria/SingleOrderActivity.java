package com.example.littleitalypizzeria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.littleitalypizzeria.models.CartItem;
import com.example.littleitalypizzeria.adapters.CartItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SingleOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;
    private TextView totalTextView;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private String key;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_order);

        /*Get data from previous intent*/
        key = getIntent().getExtras().getString("orderID");
        total = getIntent().getExtras().getDouble("total");

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent homeIntent = new Intent(SingleOrderActivity.this, MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }

            }
        };

        /*Setting up order's item list*/
        recyclerView = findViewById(R.id.itemList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*Setting up the toolbar*/
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Order"+key);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        /*Setup view*/
        totalTextView = findViewById(R.id.totalTextView);
        totalTextView.setText("Total: $ "+total);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If not logged in redirect to Home
        firebaseAuth.addAuthStateListener(authStateListener);

        //Get database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("orders");

        //Make recycler options from query to get data on all order items
        FirebaseRecyclerOptions<CartItem> options = new FirebaseRecyclerOptions.Builder<CartItem>().setQuery(databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child(key).child("items"), CartItem.class).build();

        //Fill data into the recycler view
        FirebaseRecyclerAdapter<CartItem, CartItemViewHolder> adapter = new FirebaseRecyclerAdapter<CartItem, CartItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartItemViewHolder holder, final int position, @NonNull CartItem model) {
                holder.setName(model.getName());
                holder.setQuantity("x "+model.getQuantity());

                double price = (model.getQuantity()*model.getUnitPrice()*100)/100;

                holder.setPrice("$ "+price);
            }

            @NonNull
            @Override
            public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cart_item_row, parent, false);

                return new CartItemViewHolder(view);
            }
        };


        //Set the adapter for the recycler view
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //When back button in toolbar is clicked
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}