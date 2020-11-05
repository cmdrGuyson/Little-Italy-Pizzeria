package com.example.littleitalypizzeria;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.littleitalypizzeria.models.Item;
import com.example.littleitalypizzeria.adapters.ItemViewHolder;
import com.example.littleitalypizzeria.utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView itemList;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CharSequence[] categories;
    private Button categoryButton;

    //Selected category
    private int selectedIndexCategory;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseRecyclerAdapter<Item, ItemViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerOptions<Item> options;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent homeIntent = new Intent(UserActivity.this, MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }
            }
        };

        /*Setting up drawer*/

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer
        );

        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        /*Setting up item list*/

        itemList = findViewById(R.id.item_list);
        itemList.setHasFixedSize(true);
        itemList.setLayoutManager(new LinearLayoutManager(this));

        //Firebase query to get all items. It is synced to facilitate offline access
        query = FirebaseDatabase.getInstance().getReference().child("items");
        query.keepSynced(true);

        //*Logic related to categories*/
        categoryButton = findViewById(R.id.category_button);
        selectedIndexCategory = 0; //Set default category as "All"
        //List of all categories
        categories = new CharSequence[]{
                "All",
                "Pizza",
                "Pasta",
                "Beverage",
                "Dessert",
                "Other"
        };

        /*When categories button is clicked*/
        categoryButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                onCategoryButtonClick();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // If not logged in redirect to Login page
        firebaseAuth.addAuthStateListener(authStateListener);

        options = new FirebaseRecyclerOptions.Builder<Item>().setQuery(query, Item.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Item, ItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull Item model) {
                //For all items inside the recycler view set the required fields
                holder.setTitle(model.getName());
                holder.setPrice(model.getPrice());
                holder.setDescription(model.getDescription());
                holder.setImage(model.getImageURL());

                final String item_key = getRef(position).getKey();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Set category back to "All"
                        selectedIndexCategory = 0;
                        //When clicked on a selected item, display all details of that item in a new item
                        Intent singleItemIntent = new Intent(UserActivity.this, SingleItemActivity.class);
                        singleItemIntent.putExtra("itemID", item_key);
                        startActivity(singleItemIntent);
                    }
                });
            }

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_row, parent, false);

                return new ItemViewHolder(view);
            }
        };

        //Set the adapter for the recycler view
        itemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //When a button inside the drawer is clicked
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout_option: {
                //Logout Button
                firebaseAuth.signOut();
                break;
            }
            case R.id.account_option: {
                //Account Button
                Intent accountIntent = new Intent(UserActivity.this, MyAccountActivity.class);
                accountIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(accountIntent);
                break;
            }
            case R.id.cart_option: {
                //Direct to cart intent
                Intent cartIntent = new Intent(UserActivity.this, CartActivity.class);
                cartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cartIntent);
                break;
            }
            case R.id.orders_option: {
                //Direct to orders
                Intent orderIntent = new Intent(UserActivity.this, OrdersActivity.class);
                orderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(orderIntent);
                break;
            }
            case R.id.contact_option: {
                Intent contactIntent = new Intent(UserActivity.this, ContactActivity.class);
                contactIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(contactIntent);
                break;
            }case R.id.menu_option: {
                firebaseRecyclerAdapter.updateOptions(options);
                break;
            }
        }

        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //Method to execute when category button is clicked
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onCategoryButtonClick() {

        //Create a new alert dialog to select the wanted category
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(UserActivity.this);
        builder.setTitle("Please select category");
        builder.setSingleChoiceItems(categories, selectedIndexCategory, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //change selected category
                selectedIndexCategory = i;
                updateItemList(categories[i].toString());
                dialogInterface.dismiss();
            }
        });
        builder.setBackground(getResources().getDrawable(R.drawable.button_shape, null));
        builder.show();
    }

    //Method to change list on category change
    public void updateItemList(String ctg) {
        if(ctg.equals("All")){
            firebaseRecyclerAdapter.updateOptions(options);
        }else{
            Query newQuery = FirebaseDatabase.getInstance().getReference().child("items").orderByChild("type").equalTo(ctg);
            FirebaseRecyclerOptions newOptions = new FirebaseRecyclerOptions.Builder<Item>().setQuery(newQuery, Item.class).build();
            firebaseRecyclerAdapter.updateOptions(newOptions);
        }
    }

    //Method to change list on search
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateItemListOnSearch(String ctg) {

        if(!ctg.trim().equals("")){
            ctg = Utils.formatSearchString(ctg);
            Query newQuery = FirebaseDatabase.getInstance().getReference().child("items").orderByChild("name").equalTo(ctg);
            FirebaseRecyclerOptions newOptions = new FirebaseRecyclerOptions.Builder<Item>().setQuery(newQuery, Item.class).build();
            firebaseRecyclerAdapter.updateOptions(newOptions);
        }else{
            //View all items
            firebaseRecyclerAdapter.updateOptions(options);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create search button on toolbar
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.search){
            //Alert dialog to search for an item
            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(UserActivity.this, R.style.AlertDialogTheme);
            builder.setTitle("Search for something to eat!");
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setBackground(getResources().getDrawable(R.drawable.button_shape, null));

            //When "Find" button is clicked
            builder.setPositiveButton("Find", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String searchKey = input.getText().toString();

                    updateItemListOnSearch(searchKey);
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

        return super.onOptionsItemSelected(item);
    }
}