package com.example.waybus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Splash extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;

    private static final String TAG = "Splash";
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference df;
    DatabaseReference uf;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        broadcastReceiver = new MyReceiver2();
        rb();



        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Registered users");
        df=databaseReference.child("Drivers");
        uf=databaseReference.child("Users");

        mAuth = FirebaseAuth.getInstance();


        new Handler().postDelayed(() -> {

            if(mAuth.getCurrentUser() != null){
                // startActivity(new Intent(Login.this,UsersActivity.class));
                startactivitybasedonuser();

            }else
                startActivity(new Intent(Splash.this,Login.class));


//            Intent i = new Intent(Splash.this,Login.class);
//            overridePendingTransition(R.anim.trans,R.anim.trans);
//            startActivity(i);


        },2000);
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
                        Intent intent = new Intent(Splash.this, Shareloc.class);
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
                                Intent intent =new Intent(Splash.this, UsersActivity.class);
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
