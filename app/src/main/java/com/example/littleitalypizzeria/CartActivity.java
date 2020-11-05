package com.example.littleitalypizzeria;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.littleitalypizzeria.models.CartItem;
import com.example.littleitalypizzeria.adapters.CartItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CartActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button checkoutButton;
    private TextView totalAmountView;
    private CharSequence[] checkoutOptions;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference databaseReference;

    private double cartTotal = 0;
    private Map<String, Double> prices = new Hashtable<>();
    private Map<String, CartItem> cartItems = new Hashtable<>();
    private double newTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent homeIntent = new Intent(CartActivity.this, MainActivity.class);
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
        getSupportActionBar().setTitle("Cart");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        /*Setting up cart list*/
        recyclerView = findViewById(R.id.cartList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        checkoutButton = findViewById(R.id.checkoutButton);
        totalAmountView = findViewById(R.id.totalTextView);

        checkoutOptions = new CharSequence[]{
                "Pickup at Store",
                "Reserve for dine in",
                "Deliver to me"
        };

        //Handle Checkout
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                handleCheckout();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If not logged in redirect to Home
        firebaseAuth.addAuthStateListener(authStateListener);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("cart");

        FirebaseRecyclerOptions<CartItem> options = new FirebaseRecyclerOptions.Builder<CartItem>().setQuery(databaseReference.child(firebaseAuth.getCurrentUser().getUid()), CartItem.class).build();

        FirebaseRecyclerAdapter<CartItem, CartItemViewHolder> adapter = new FirebaseRecyclerAdapter<CartItem, CartItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartItemViewHolder holder, final int position, @NonNull CartItem model) {
                holder.setName(model.getName());
                holder.setQuantity("x "+model.getQuantity());

                double price = Math.floor(model.getQuantity()*model.getUnitPrice()*100)/100;

                holder.setPrice("$ "+price);

                //Add CartItem to ArrayList
                cartItems.put(model.getItemID(), new CartItem(model.getItemID(), model.getName(), model.getUnitPrice(), model.getQuantity()));

                prices.put(model.getItemID(), price);
                cartTotal += price;

                cartTotal = Math.floor(cartTotal * 100) / 100;

                totalAmountView.setText("Total: $ "+cartTotal);

                //Option to remove single items from cart
                final String item_key = getRef(position).getKey();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(View view) {
                        removeSingleItem(item_key, position);
                    }
                });
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

    //When toolbar buttons are clicked
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //When back button in toolbar is clicked
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        //When "Clear Cart" button is clicked
        else if (item.getItemId() == R.id.clearCart) {
            clearCart();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create clear cart
        getMenuInflater().inflate(R.menu.cart_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Method to clear the cart
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void clearCart() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(CartActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Clear Cart");
        builder.setMessage("Are you sure that you want to clear the cart?");
        builder.setBackground(getResources().getDrawable(R.drawable.button_shape, null));

        //When "Clear" button is clicked
        builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                databaseReference.child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                totalAmountView.setText("Total: $ 0.00");

            }
        });

        //When cancel button is clicked
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    //Method to handle single item removing from the cart
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void removeSingleItem(final String itemID, final int position) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(CartActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Remove Item");
        builder.setMessage("Are you sure that you want remove this item?");
        builder.setBackground(getResources().getDrawable(R.drawable.button_shape, null));

        //When "Remove" button is clicked
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child(itemID).removeValue();

                //Remove the selected item price from array list
                prices.remove(itemID);
                //Remove selected item from itemList
                cartItems.remove(itemID);

                newTotal = 0;

                //Calculate new total from array list and display
                prices.forEach(new BiConsumer<String, Double>() {
                    @Override
                    public void accept(String k, Double v) {
                        newTotal += v;
                    }
                });

                newTotal = Math.floor(newTotal*100)/100;
                cartTotal = newTotal;

                totalAmountView.setText("Total: $ "+newTotal);

            }
        });

        //When cancel button is clicked
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void handleCheckout() {

        //If there are items in the cart
        if(!cartItems.isEmpty()){

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(CartActivity.this);
            builder.setTitle("Select checkout option");builder.setSingleChoiceItems(checkoutOptions, 0, new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    String type = "";

                    List<CartItem> cartItemList = getList();

                    if(i==0){
                        /**Pickup from the store*/
                        type="pickup";
                        //Direct to checkout intent
                        Intent checkoutIntent = new Intent(CartActivity.this, CheckoutActivity.class);

                        checkoutIntent.putExtra("type", type);
                        checkoutIntent.putExtra("total", cartTotal);
                        checkoutIntent.putExtra("cart", (Serializable) cartItemList);

                        startActivity(checkoutIntent);
                    }else if(i==1){
                        /**Reserve for dine in*/
                        type="dine_in";
                        //Direct to checkout intent
                        Intent checkoutIntent = new Intent(CartActivity.this, CheckoutActivity.class);

                        checkoutIntent.putExtra("type", type);
                        checkoutIntent.putExtra("total", cartTotal);
                        checkoutIntent.putExtra("cart", (Serializable) cartItemList);

                        startActivity(checkoutIntent);
                    }else{
                        /**Deliver*/
                        type="deliver";

                        Intent d_checkoutIntent = new Intent(CartActivity.this, CheckoutDeliveryActivity.class);
                        d_checkoutIntent.putExtra("type", type);
                        d_checkoutIntent.putExtra("total", cartTotal);
                        d_checkoutIntent.putExtra("cart", (Serializable) cartItemList);

                        startActivity(d_checkoutIntent);
                    }



                    dialogInterface.dismiss();
                }
            });
            builder.setBackground(getResources().getDrawable(R.drawable.button_shape, null));
            builder.show();
        }else{
            Toast.makeText(this, "There are no items in the cart!", Toast.LENGTH_SHORT).show();
        }
    }

    //Prevent total from multiplying if "Back" button is clicked on checkout intent
    @Override
    protected void onPause() {
        super.onPause();
        cartTotal = 0;
        cartItems.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<CartItem> getList(){

        final List<CartItem> cartItemsList = new ArrayList<>();

        cartItems.forEach(new BiConsumer<String, CartItem>() {
            @Override
            public void accept(String k, CartItem v) {
                cartItemsList.add(v);
            }
        });

        return cartItemsList;
    }
}