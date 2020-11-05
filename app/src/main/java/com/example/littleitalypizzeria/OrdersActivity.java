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

import com.example.littleitalypizzeria.models.Order;
import com.example.littleitalypizzeria.adapters.OrderItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent homeIntent = new Intent(OrdersActivity.this, MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }

            }
        };

        /*Setting up the toolbar*/
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Orders");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        /*Setting up cart list*/
        recyclerView = findViewById(R.id.orderList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If not logged in redirect to Home
        firebaseAuth.addAuthStateListener(authStateListener);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("orders");

        FirebaseRecyclerOptions<Order> options = new FirebaseRecyclerOptions.Builder<Order>().setQuery(databaseReference.child(firebaseAuth.getCurrentUser().getUid()), Order.class).build();

        FirebaseRecyclerAdapter<Order, OrderItemViewHolder> adapter = new FirebaseRecyclerAdapter<Order, OrderItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderItemViewHolder holder, final int position, @NonNull Order model) {
                //Get order key
                final String key = getRef(position).getKey();

                //Setup each order view by getting data
                holder.setOrderID(key);
                final double total = model.getInfo().getTotal();
                holder.setTotal(total);
                holder.setOrderedAt(model.getInfo().getOrderedAt());
                holder.setType(model.getInfo().getType());

                //When an order is clicked display more info
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderIntent = new Intent(OrdersActivity.this, SingleOrderActivity.class);
                        orderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        orderIntent.putExtra("orderID", key);
                        orderIntent.putExtra("total", total);
                        startActivity(orderIntent);
                    }
                });
            }

            @NonNull
            @Override
            public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_row, parent, false);

                return new OrderItemViewHolder(view);
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