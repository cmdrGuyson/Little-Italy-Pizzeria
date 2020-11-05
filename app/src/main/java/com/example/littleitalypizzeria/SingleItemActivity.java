package com.example.littleitalypizzeria;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.littleitalypizzeria.adapters.ReviewViewHolder;
import com.example.littleitalypizzeria.fragments.QuantityIncrementFragment;
import com.example.littleitalypizzeria.models.CartItem;
import com.example.littleitalypizzeria.models.Item;
import com.example.littleitalypizzeria.models.OnInputListener;
import com.example.littleitalypizzeria.models.Review;
import com.example.littleitalypizzeria.utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class SingleItemActivity extends AppCompatActivity implements OnInputListener {

    private String itemID;

    private Toolbar toolbar;
    private ImageView imageView;
    private TextView itemNameView, itemDescriptionView, itemPriceView;
    private EditText reviewBodyEditText;
    private Button postButton, addToCartButton;
    private RecyclerView recyclerView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference databaseReference;

    private String name, description, imageURL, type;
    private double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_item);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Login"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(SingleItemActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        /*Setting up the toolbar*/
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        /*Getting information on a single item*/
        itemID = getIntent().getExtras().getString("itemID");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("items");
        databaseReference.keepSynced(true);

        imageView = findViewById(R.id.itemImage);
        itemNameView = findViewById(R.id.itemName);
        itemDescriptionView = findViewById(R.id.itemDescription);
        itemPriceView = findViewById(R.id.itemPrice);

        databaseReference.child(itemID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Get data from snapshot
                name = snapshot.child("name").getValue().toString();
                description = snapshot.child("description").getValue().toString();
                type = snapshot.child("type").getValue().toString();
                imageURL = snapshot.child("imageURL").getValue().toString();
                price = (double) snapshot.child("price").getValue();

                //Display data
                itemNameView.setText(name);
                itemDescriptionView.setText(description);
                itemPriceView.setText("$ "+String.valueOf(price));

                //Enable offline viewing of image
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*Handle Review Posting*/
        reviewBodyEditText = findViewById(R.id.reviewBody);
        postButton = findViewById(R.id.postButton);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postReview();
            }
        });

        /*Load all reviews*/
        recyclerView = findViewById(R.id.review_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        /*When "Add to Cart" is clicked*/
        addToCartButton = findViewById(R.id.addToCartButton);
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                addItemToCart();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If not logged in redirect to Login page
        firebaseAuth.addAuthStateListener(authStateListener);

        //Populating review list
        Query query = FirebaseDatabase.getInstance().getReference().child("reviews").orderByChild("itemID").equalTo(itemID);
        query.keepSynced(true);
        FirebaseRecyclerOptions<Review> options = new FirebaseRecyclerOptions.Builder<Review>().setQuery(query, Review.class).build();
        FirebaseRecyclerAdapter<Review, ReviewViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Review, ReviewViewHolder>(options) {

            @NonNull
            @Override
            public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_row, parent, false);
                return new ReviewViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ReviewViewHolder holder, int position, @NonNull Review model) {
                holder.setName(model.getUserID());
                holder.setReviewBody(model.getReviewBody());
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //When toolbar buttons are clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //When back button in toolbar is clicked
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        //When cart button is clicked
        else if(item.getItemId()==R.id.cart){
            //Direct to cart intent
            Intent cartIntent = new Intent(SingleItemActivity.this, CartActivity.class);
            cartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(cartIntent);
        }else if(item.getItemId()==R.id.share){
            shareItem();
        }
        return super.onOptionsItemSelected(item);
    }

    //Method to post a review
    private void postReview() {


        final String reviewBody = reviewBodyEditText.getText().toString();
        final String userID = firebaseAuth.getCurrentUser().getUid();

        //If review body is not empty
        if(!TextUtils.isEmpty(reviewBody.trim())){

            //Create new Review object with gathered data
            Review review = new Review(itemID, reviewBody, userID);

            //Upload review to firebase database
            DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference();
            reviewRef.child("reviews").push().setValue(review);

            reviewBodyEditText.setText("");

            Toast.makeText(this, "Successfully made review!", Toast.LENGTH_SHORT).show();
        }else{
            //If it is empty notify user
            Toast.makeText(this, "You didn't write anything!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create cart button on toolbar
        getMenuInflater().inflate(R.menu.item_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Method to add an item to the cart
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addItemToCart() {

        QuantityIncrementFragment dialog = new QuantityIncrementFragment();
        dialog.show(getSupportFragmentManager(), "QuantityIncrementFragment");

    }

    //This method will be called by the quantity increment fragment
    @Override
    public void sendInput(int input) {
        final int quantity = input;

        final DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("cart").child(firebaseAuth.getCurrentUser().getUid());

        databaseReference.child(itemID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Get data from snapshot
                String name = snapshot.child("name").getValue().toString();
                double unitPrice = (double) snapshot.child("price").getValue();

                CartItem cartItem = new CartItem(itemID, name, unitPrice, quantity);
                cartRef.child(itemID).setValue(cartItem);

                Toast.makeText(SingleItemActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SingleItemActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareItem() {

        //Details of the item
        Item item = new Item(name, type, description, price, imageURL);

        //Create the share message using utility class
        String message = Utils.createShareString(item);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);

        //Start a ACTION_SEND intent
        startActivity(Intent.createChooser(share, "Share LIP Food!"));
    }
}