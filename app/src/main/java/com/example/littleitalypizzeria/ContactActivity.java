package com.example.littleitalypizzeria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.littleitalypizzeria.adapters.InquiryViewHolder;
import com.example.littleitalypizzeria.adapters.ReviewViewHolder;
import com.example.littleitalypizzeria.models.Inquiry;
import com.example.littleitalypizzeria.models.Review;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ContactActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ImageView imageView;
    private Button postButton;
    private EditText inquiryBody;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private int CALL_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent homeIntent = new Intent(ContactActivity.this, MainActivity.class);
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
        getSupportActionBar().setTitle("Contact us");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);

        /*Make an inquiry*/
        postButton = findViewById(R.id.postButton);
        inquiryBody = findViewById(R.id.inquiryBody);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeInquiry();
            }
        });

        /*Load all user made inquiries */
        recyclerView = findViewById(R.id.inquiry_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*Call button*/
        imageView = findViewById(R.id.callButton);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // If not logged in redirect to Home
        firebaseAuth.addAuthStateListener(authStateListener);

        /*Populating inquiry list */
        Query query = FirebaseDatabase.getInstance().getReference().child("inquiries").child(firebaseAuth.getCurrentUser().getUid());
        query.keepSynced(true);
        FirebaseRecyclerOptions<Inquiry> options = new FirebaseRecyclerOptions.Builder<Inquiry>().setQuery(query, Inquiry.class).build();
        FirebaseRecyclerAdapter<Inquiry, InquiryViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Inquiry, InquiryViewHolder>(options) {

            @NonNull
            @Override
            public InquiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inquiry_row, parent, false);
                return new InquiryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull InquiryViewHolder holder, int position, @NonNull Inquiry model) {
                holder.setBody(model.getBody());
                holder.setResponse(model.getResponse());
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //When back button in toolbar is clicked
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    //Method to pst an inquiry
    private void makeInquiry() {
        String inquiry = inquiryBody.getText().toString();
        String uid = firebaseAuth.getCurrentUser().getUid();

        //If input is valid
        if(!TextUtils.isEmpty(inquiry.trim())){

            //Create new Inquiry object
            Inquiry inquiryObj = new Inquiry(inquiry, uid);

            //Upload object to database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("inquiries").child(uid);
            databaseReference.push().setValue(inquiryObj);

            inquiryBody.setText("");

            Toast.makeText(this, "Successfully made inquiry!", Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(this, "Inquiry body is empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeCall() {
        String number = "0999217356";

        if(ContextCompat.checkSelfPermission(ContactActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ContactActivity.this, new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST);
        }else{
            String dial = "tel:"+number;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CALL_REQUEST){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall();
            }else{
                Toast.makeText(this, "You need to give permissions to make a call", Toast.LENGTH_SHORT).show();
            }
        }

    }
}