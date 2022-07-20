package com.example.waybus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Login extends AppCompatActivity {





    private static final String TAG = "Login";
    EditText etLoginEmail;
    EditText etLoginPassword;
    TextView tvRegisterHere;
    TextView tvforpass;
    Button btnLogin;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference df;
    DatabaseReference uf;
    ProgressDialog progressdialog;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    BroadcastReceiver broadcastReceiver = new MyReceiver2();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }



        rb();
checkLocationPermission();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Registered users");
        df=databaseReference.child("Drivers");
        uf=databaseReference.child("Users");

        mAuth = FirebaseAuth.getInstance();







        etLoginEmail = findViewById(R.id.etEmail);
        etLoginPassword = findViewById(R.id.etPass);
        tvRegisterHere = findViewById(R.id.tvRegisterHere);
        btnLogin = findViewById(R.id.btnLogin);
        tvforpass = findViewById(R.id.fp);




        btnLogin.setOnClickListener(view -> {

            String email = etLoginEmail.getText().toString();
            String password = etLoginPassword.getText().toString();

            if (TextUtils.isEmpty(email)){
                        etLoginEmail.setError("Email cannot be empty");
                        etLoginEmail.requestFocus();
                    }else if (TextUtils.isEmpty(password)){
                        etLoginPassword.setError("Password cannot be empty");
                        etLoginPassword.requestFocus();
                    }else {
                progressdialog = new ProgressDialog(Login.this);
                progressdialog.show();
                progressdialog.setContentView(R.layout.progress_dialog);
                progressdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    progressdialog.dismiss();
                                    Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                    startactivitybasedonuser();
                                }else{
                                    progressdialog.dismiss();
                                    Toast.makeText(Login.this, "Log in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }
                }, 3000);

            }
        });
        tvRegisterHere.setOnClickListener(view ->{
            startActivity(new Intent(Login.this, Register.class));
        });
        tvforpass.setOnClickListener(view ->{
            startActivity(new Intent(Login.this, fpa.class));
        });
    }



    private void startactivitybasedonuser(){
        firebaseUser =mAuth.getCurrentUser();

        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users dusers = snapshot.child(firebaseUser.getUid()).getValue(Users.class);
                Log.d(TAG, "entered");
                if (dusers != null) {
                    Log.d(TAG, "driver entered ");
                    if (dusers.Type.equals("Driver")) {
                        Log.d(TAG, dusers.name);
                        Intent intent = new Intent(Login.this, Shareloc.class);
                        intent.putExtra("message", dusers.name);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
                else{
                    Log.d(TAG, "else entered ");
                    uf.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users users =  snapshot.child(firebaseUser.getUid()).getValue(Users.class);
                            Log.d(TAG, "l entered ");
                            if (users != null && users.Type.equals("User")) {
                                Log.d(TAG, "user entered ");
                                Intent intent =new Intent(Login.this, UsersActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }


                Log.d(TAG, "failed");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }







    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this Application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


    public  void  checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location permission")
                        .setMessage("We need location permission to use this App")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Login.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        Constants.MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                Log.d(TAG, " 1st permission asking");
                requestLocationPermission();
            }
        } else {
            checkBackgroundLocation();
        }
    }
    private void checkBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, " checking back");
                requestBackgroundLocationPermission();
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                Constants.MY_PERMISSIONS_REQUEST_LOCATION);
    }

    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, " ask back");

            new AlertDialog.Builder(this)
                    .setTitle("Location permission")
                    .setMessage("We need  background location permission to use this App.So, select 'Allow all the time'. ")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(Login.this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    Constants.MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION);

                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.MY_PERMISSIONS_REQUEST_LOCATION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, " 1st permission  granted");
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, " back per asking");
                        checkBackgroundLocation();
                        //Request location updates:

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();

                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        new AlertDialog.Builder(this)
                                .setTitle("Location permission")
                                .setMessage("We need location permission to use this App")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //Prompt the user once explanation has been shown
                                        ActivityCompat.requestPermissions(Login.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                Constants.MY_PERMISSIONS_REQUEST_LOCATION);
                                    }
                                })
                                .create()
                                .show();
                    }else{
                        Toast.makeText(
                                this,
                                "You Can't use this app without location permission",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    /*if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(TAG, " should show true");
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.example.waybus")));
                    }*/

                }
                return;
            }
            case Constants.MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION:{

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    ) {


                        Toast.makeText(
                                this,
                                "Granted Background Location Permission",
                                Toast.LENGTH_LONG
                        ).show();
                    }else {

                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        Toast.makeText(this, " background permission denied", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

            }

        }
    }
    protected void rb() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
        {
            registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }






}




