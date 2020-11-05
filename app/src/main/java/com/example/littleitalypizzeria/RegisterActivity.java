package com.example.littleitalypizzeria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.littleitalypizzeria.models.User;
import com.example.littleitalypizzeria.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameField, lastNameField, emailField, passwordField, confirmPasswordField, phoneNumberField;
    private Button registerButton;
    private ProgressDialog progressDialog;
    private TextView loginTextView;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        firstNameField = findViewById(R.id.firstNameEditText);
        lastNameField = findViewById(R.id.lastNameEditText);
        emailField = findViewById(R.id.emailEditText);
        passwordField = findViewById(R.id.passwordEditText);
        confirmPasswordField = findViewById(R.id.confirmPasswordEditText);
        phoneNumberField = findViewById(R.id.phoneNumberEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Redirect to login page
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
            }
        });

    }

    private void registerUser() {

        final String firstName = firstNameField.getText().toString().trim();
        final String lastName = lastNameField.getText().toString().trim();
        final String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString();
        final String confirmPassword = confirmPasswordField.getText().toString();
        final String contactNumber = phoneNumberField.getText().toString();

        //If fields are empty
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(contactNumber)) {
            Toast.makeText(this, "Please enter valid data!", Toast.LENGTH_SHORT).show();
        }
        //If password and confirm password are different
        else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
        }
        //If email is not valid
        else if (!Utils.isEmailValid(email)){
            Toast.makeText(this, "Please enter valid email!", Toast.LENGTH_SHORT).show();
        }
        //If phone number is not valid
        else if (!Utils.isContactNumberValid(contactNumber)){
            Toast.makeText(this, "Please enter valid contact number!", Toast.LENGTH_SHORT).show();
        }
        else {

            //Show progress
            progressDialog.setMessage("Registering user...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        String userID = firebaseAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserReference = FirebaseDatabase.getInstance().getReference().child("user").child(userID);
                        User user = new User(email,firstName,lastName,contactNumber);
                        currentUserReference.setValue(user);

                        //Redirect to home page
                        Intent homePageIntent = new Intent(RegisterActivity.this, UserActivity.class);
                        homePageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homePageIntent);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                }
            });
        }
    }

}