package com.example.littleitalypizzeria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.littleitalypizzeria.fragments.ConfirmAddressFragment;
import com.example.littleitalypizzeria.models.CartItem;
import com.example.littleitalypizzeria.models.OrderInfo;
import com.example.littleitalypizzeria.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutDeliveryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private TextView totalTextView, messageView;
    private Button checkoutButton;
    private GoogleMap mMap;
    private Button btn;

    private final static int PLACE_PICKER_REQUEST = 999;
    private final static int LOCATION_REQUEST_CODE = 23;

    private String type;
    private String location;
    private double total;
    private boolean selected;
    private ArrayList<CartItem> cartItems;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_delivery);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent homeIntent = new Intent(CheckoutDeliveryActivity.this, MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }
            }
        };

        /*Configure map*/
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Request to use location permissions
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);

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

        //Location hasn't been selected yet
        selected = false;

        totalTextView = findViewById(R.id.totalTextView);
        messageView = findViewById(R.id.messageView);
        checkoutButton = findViewById(R.id.checkoutButton);
        totalTextView.setText("$ " + total);

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

    private void placeOrder() {

        if (selected) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("orders").child(firebaseAuth.getCurrentUser().getUid());

            //Get the key when items are pushed to the database
            String pushKey = databaseReference.push().getKey();

            //Insert order items into the database
            for (int i = 0; i < cartItems.size(); i++) {
                databaseReference.child(pushKey).child("items").child(cartItems.get(i).getItemID()).setValue(cartItems.get(i));
            }

            //Insert other order information into the database
            OrderInfo orderInfo = new OrderInfo();

            orderInfo.setOrderedAt(Utils.getDateTime());
            orderInfo.setTotal(total);
            orderInfo.setType(type);
            orderInfo.setLocation(location);

            databaseReference.child(pushKey).child("info").setValue(orderInfo);

            //Clear cart after placing order
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("cart");
            cartRef.child(firebaseAuth.getCurrentUser().getUid()).removeValue();

            Toast.makeText(this, "Successfully placed order!", Toast.LENGTH_LONG).show();

            //Send user home
            Intent homeIntent = new Intent(CheckoutDeliveryActivity.this, UserActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        } else {
            Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            //If permissions for location is requested
            case LOCATION_REQUEST_CODE: {

                //If permissions have been granted by the user
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Set the current location as the starting location in the map
                    mMap.setMyLocationEnabled(true);
                    mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location location) {
                            LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                    ltlng, 16f);
                            mMap.animateCamera(cameraUpdate);
                        }
                    });

                    //When user clicks on the map
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);

                            //Get address of location using latitude and longitude
                            markerOptions.title(getAddress(latLng));
                            mMap.clear();

                            //Animate camera to new user selected location
                            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                                    latLng, 15);
                            mMap.animateCamera(location);
                            mMap.addMarker(markerOptions);
                        }
                    });


                } else {

                    //If location permissions are denied then send the user home
                    Intent homeIntent = new Intent(CheckoutDeliveryActivity.this, UserActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }


    private String getAddress(LatLng latLng){

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {

            //Get the address of the location from longitude and latitude from geocoder object
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            String address = addresses.get(0).getAddressLine(0);

            /*
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
             */

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {

                ft.remove(prev);
            }
            ft.addToBackStack(null);

            //Open up the confirm address fragment and show address by passing arguments
            ConfirmAddressFragment dialogFragment = new ConfirmAddressFragment();

            //Bundle to be sent to Fragment
            Bundle args = new Bundle();
            args.putDouble("lat", latLng.latitude);
            args.putDouble("long", latLng.longitude);
            args.putString("address", address);
            dialogFragment.setArguments(args);
            dialogFragment.show(ft, "dialog");

            location = address;
            selected = true;

            return address;

        } catch (IOException e) {
            e.printStackTrace();
            return "No Address Found";

        }
    }
}
