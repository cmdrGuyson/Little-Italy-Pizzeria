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
import android.widget.Toast;

import com.example.littleitalypizzeria.models.Item;
import com.example.littleitalypizzeria.adapters.ItemViewHolder;
import com.example.littleitalypizzeria.utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView itemList;
    private Query query;
    private Toolbar toolbar;
    private Button categoryButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CharSequence[] categories;

    //Selected category
    private int selectedIndexCategory;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseRecyclerAdapter<Item, ItemViewHolder> firebaseRecyclerAdapter;
        private FirebaseRecyclerOptions<Item> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {
                    Intent userIntent = new Intent(MainActivity.this, UserActivity.class);
                    userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(userIntent);
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

        query = FirebaseDatabase.getInstance().getReference().child("items");
        query.keepSynced(true);

        /*Logic related to categories*/
        categoryButton = findViewById(R.id.category_button);
        selectedIndexCategory = 0; //Set as "All"
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
                holder.setTitle(model.getName());
                holder.setPrice(model.getPrice());
                holder.setDescription(model.getDescription());
                holder.setImage(model.getImageURL());
            }

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_row, parent, false);

                return new ItemViewHolder(view);
            }
        };

        itemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.register_option: {
                //Direct to register
                Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(registerIntent);
                break;
            }
            case R.id.login_option: {
                //Direct to login page
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                break;
            }
            case R.id.contact_option: {
                Toast.makeText(this, "Please login to make an inquiry", Toast.LENGTH_SHORT).show();

                //DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("items");
                //ref.push().setValue(new Item("Ravioli", "Pasta", "Fermentum dui faucibus in ornare quam viverra. Integer vitae justo eget magna fermentum iaculis eu non diam. Amet mattis vulputate enim nulla.", 8.49, "https://firebasestorage.googleapis.com/v0/b/little-italy-pizzeria-20e21.appspot.com/o/ravioli.jpg?alt=media&token=0965c0ad-75d7-4f4b-b14c-06d8922609d8"));
                //ref.push().setValue(new Item("Fettuccine", "Pasta", "Massa sapien faucibus et molestie ac feugiat sed. Ullamcorper sit amet risus nullam eget felis eget nunc. Molestie ac feugiat sed lectus vestibulum.", 8.49, "https://firebasestorage.googleapis.com/v0/b/little-italy-pizzeria-20e21.appspot.com/o/fettuccine.jpg?alt=media&token=2ac0c69d-3554-4b5b-ae3c-d9496585a059"));
                //ref.push().setValue(new Item("Lasagna", "Pasta", "Bibendum neque egestas congue quisque egestas diam in. Massa ultricies mi quis hendrerit dolor magna eget est. Sed odio morbi quis commodo. Amet nulla facilisi morbi tempus iaculis urna.", 9.29, "https://firebasestorage.googleapis.com/v0/b/little-italy-pizzeria-20e21.appspot.com/o/lasagna.jpg?alt=media&token=d87f16e4-a843-4262-9a48-07c894ec939c"));
                //ref.push().setValue(new Item("Vanilla Ice Cream", "Dessert", "Lacus sed turpis tincidunt id aliquet. Nunc sed id semper risus in hendrerit gravida rutrum quisque. Bibendum neque egestas congue quisque egestas diam in. Massa ultricies mi quis hendrerit dolor magna eget est. Sed odio morbi quis commodo. Amet nulla facilisi morbi tempus iaculis urna.", 3.69, "https://firebasestorage.googleapis.com/v0/b/little-italy-pizzeria-20e21.appspot.com/o/vanilla_ice_cream.jpg?alt=media&token=391126eb-0ed0-470e-bfcd-1020f82a74a2"));

                break;

            } case R.id.menu_option: {
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
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
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
            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this, R.style.AlertDialogTheme);
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