package com.example.littleitalypizzeria.models;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.littleitalypizzeria.MainActivity;
import com.example.littleitalypizzeria.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MyAccountActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText firstNameEditText, lastNameEditText, contactNumberEditText, passwordEditText, confirmPasswordEditText, oldPasswordEditText;
    private Button changeNameButton, changeNumberButton, changePasswordButton;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference databaseReference;


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        /*Setting up authorization*/
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent homeIntent = new Intent(MyAccountActivity.this, MainActivity.class);
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        getSupportActionBar().setTitle("Edit Account Details");

        //Firebase database Reference to current user's User object
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseAuth.getCurrentUser().getUid());

        /*When "Change Name" button is clicked*/
        changeNameButton = findViewById(R.id.changeNameButton);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeName();
            }
        });

        /*When "Change Phone Number" button is clicked*/
        changeNumberButton = findViewById(R.id.changeNumberButton);
        contactNumberEditText = findViewById(R.id.phoneNumberEditText);
        changeNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeNumber();
            }
        });

        /*When "Change Password" button is clicked*/
        progressDialog = new ProgressDialog(this);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changePassword();
            }
        });
    }

    //When back button in toolbar is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //When back button in toolbar is clicked
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //Method to change name
    private void changeName() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)){
            Toast.makeText(this, "Please enter valid data!", Toast.LENGTH_SHORT).show();
        }else{
            databaseReference.child("firstName").setValue(firstName);
            databaseReference.child("lastName").setValue(lastName);
            Toast.makeText(this, "Successfully Updated!", Toast.LENGTH_SHORT).show();
        }

        //Clear fields
        firstNameEditText.setText("");
        lastNameEditText.setText("");
    }

    //Method to change phone number
    private void changeNumber() {
        String number = contactNumberEditText.getText().toString().trim();

        if (TextUtils.isEmpty(number) || !Utils.isContactNumberValid(number)){
            Toast.makeText(this, "Please enter valid data!", Toast.LENGTH_SHORT).show();
        }else{
            databaseReference.child("contactNumber").setValue(number);
            Toast.makeText(this, "Successfully Updated!", Toast.LENGTH_SHORT).show();
        }

        contactNumberEditText.setText("");
    }

    //Method to change password
    private void changePassword() {
        final String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        final String oldPassword = oldPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Please enter valid data!", Toast.LENGTH_SHORT).show();
        }else if(password.length()<8){
            Toast.makeText(this, "Please enter a strong password!", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
        }else{
            //Show progress
            progressDialog.setMessage("Changing password...");
            progressDialog.show();

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    //Get email of user
                    String email = snapshot.child("email").getValue().toString();

                    //Get current user
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    //Make AuthCredential object from email and old password
                    AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MyAccountActivity.this, "Password changed!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MyAccountActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(MyAccountActivity.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MyAccountActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }

        passwordEditText.setText("");
        oldPasswordEditText.setText("");
        confirmPasswordEditText.setText("");

    }
}