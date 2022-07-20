package com.example.waybus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    EditText etRegEmail;
    EditText etRegPassword;
    EditText etname;
    EditText etphone;
    EditText etcp;
    Button btnRegister;
    ProgressDialog progressdialog;


    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }



        setContentView(R.layout.activity_register);

        etRegEmail = findViewById(R.id.etRegEmail);
        etname = findViewById(R.id.name);
        etphone = findViewById(R.id.phone);
        etRegPassword = findViewById(R.id.etRegPass);
        btnRegister = findViewById(R.id.signup);
        etcp = findViewById(R.id.etcp);


        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Registered users");




        btnRegister.setOnClickListener(view ->{



            String email = etRegEmail.getText().toString();
            String password = etRegPassword.getText().toString();
            String name = etname.getText().toString();
            String phone = etphone.getText().toString();
            String cpassword =etcp.getText().toString();


            if (TextUtils.isEmpty(email)){
                etRegEmail.setError("Email cannot be empty");
                etRegEmail.requestFocus();
            }else if (TextUtils.isEmpty(password)){
                etRegPassword.setError("Password cannot be empty");
                etRegPassword.requestFocus();
            }else if (TextUtils.isEmpty(name)){
                etname.setError("Name cannot be empty");
                etname.requestFocus();
            }else if (TextUtils.isEmpty(phone)){
                etphone.setError("phone no cannot be empty");
                etphone.requestFocus();
            }else if (!cpassword.toString().equals(password.toString())){
                Toast.makeText(this,"Password is different. Check again",Toast.LENGTH_SHORT).show();
                etcp.requestFocus();
            }
            else {
                progressdialog = new ProgressDialog(Register.this);
                progressdialog.show();
                progressdialog.setContentView(R.layout.progress_dialog);
                progressdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    String Type = "User";
                                    Users users = new Users(email,password,Type,name,phone);
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    databaseReference.child("Users").child(firebaseUser.getUid()).setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(Register.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Register.this, Login.class));
                                                finish();
                                            }
                                        }
                                    });

                                }else{
                                    progressdialog.dismiss();
                                    Toast.makeText(Register.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });




                    }
                }, 4000);
            }
        });



    }


}