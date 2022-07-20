package com.example.waybus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG = "UsersActivity";
    Button startbutton;

    Spinner s1,s2;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    RecyclerView courseRV;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference reference;
    DatabaseReference statusreference ;
    TextView uname;
    ImageView busmap;
    ProgressDialog progressdialog;
    DatabaseReference lf;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Registered users");
        reference =firebaseDatabase.getReference("Routes");
        DatabaseReference re  = databaseReference.child("Users");
        statusreference = firebaseDatabase.getReference("Status");
        mAuth = FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        uname = (TextView) findViewById(R.id.uname);
        busmap = findViewById(R.id.busimg);

        startbutton = (Button) findViewById(R.id.startbt);
        s1 = (Spinner) findViewById(R.id.fromsp);
        s2 = (Spinner) findViewById(R.id.tosp);


        re.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uname.setText(snapshot.child(firebaseUser.getUid()).child("name").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        busmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersActivity.this,MapsActivity2.class);
                startActivity(intent);
            }
        });


        courseRV = findViewById(R.id.idRVCourse);
        String[] from = {"Puducherry","Villianur","PEC"};
        ArrayAdapter bd =new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,from);
        s1.setAdapter(bd);

        String[] to = {"Puducherry","Villianur","PEC"};
        ArrayAdapter td =new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,to);
        s2.setAdapter(td);

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressdialog = new ProgressDialog(UsersActivity.this);
                progressdialog.show();
                progressdialog.setContentView(R.layout.progress_dialog);
                progressdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(s1.getSelectedItem().toString().equals(s2.getSelectedItem().toString()) ){
                            ArrayList<String> mylist = new ArrayList<String>();
                            mylist.clear();
                            RvAdapter adapter = new RvAdapter(UsersActivity.this ,mylist);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UsersActivity.this, LinearLayoutManager.VERTICAL, false);
                            courseRV.setLayoutManager(linearLayoutManager);
                            courseRV.setAdapter(adapter);
                            progressdialog.dismiss();
                            Toast.makeText(UsersActivity.this,"Select different locations",Toast.LENGTH_SHORT).show();
                        }else if((s1.getSelectedItem().toString().equals("Puducherry") && s2.getSelectedItem().toString().equals("PEC")) ||(s1.getSelectedItem().toString().equals("PEC") && s2.getSelectedItem().toString().equals("Puducherry")) ){
                            lf =reference.child("Puducherry-PEC");
                            lf.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ArrayList<String> mylist = new ArrayList<String>();
                                    ArrayList<String> finallist = new ArrayList<String>();
                                    ArrayList<String> copy = new ArrayList<String>();

                                    for(DataSnapshot s : snapshot.getChildren()){
                                        mylist.add(s.getValue().toString());
                                    }

                                    for(int j=0;j< mylist.size();j++){
                                        DatabaseReference d =statusreference.child(mylist.get(j));


                                        int finalJ = j;

                                        d.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                finallist.add(snapshot.getValue().toString());
                                                if(finallist.get(finalJ).contains("Online")){
                                                    int i=0;
                                                    copy.add(i,mylist.get(finalJ));
                                                }
                                                if(finalJ == (mylist.size()-1) && copy.size() == 0){
                                                    progressdialog.dismiss();
                                                    Toast.makeText(UsersActivity.this,"No Buses Available",Toast.LENGTH_SHORT).show();
                                                }

                                                RvAdapter adapter = new RvAdapter(UsersActivity.this, copy);
                                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UsersActivity.this, LinearLayoutManager.VERTICAL, false);
                                                courseRV.setLayoutManager(linearLayoutManager);
                                                courseRV.setAdapter(adapter);
                                                progressdialog.dismiss();



                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else if((s1.getSelectedItem().toString().equals("Puducherry") && s2.getSelectedItem().toString().equals("Villianur")) ||(s1.getSelectedItem().toString().equals("Villianur") && s2.getSelectedItem().toString().equals("Puducherry"))){
                            lf =reference.child("Puducherry-Villianur");
                            lf.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ArrayList<String> mylist = new ArrayList<String>();
                                    ArrayList<String> finallist = new ArrayList<String>();
                                    ArrayList<String> copy = new ArrayList<String>();

                                    for(DataSnapshot s : snapshot.getChildren()){
                                        mylist.add(s.getValue().toString());
                                    }

                                    for(int j=0;j< mylist.size();j++){
                                        DatabaseReference d =statusreference.child(mylist.get(j));


                                        int finalJ = j;

                                        d.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                finallist.add(snapshot.getValue().toString());
                                                if(finallist.get(finalJ).contains("Online")){
                                                    int i=0;
                                                    copy.add(i,mylist.get(finalJ));
                                                }

                                                if(finalJ == (mylist.size()-1) && copy.size() == 0){
                                                    progressdialog.dismiss();
                                                    Toast.makeText(UsersActivity.this,"No Buses Available",Toast.LENGTH_SHORT).show();
                                                }

                                                RvAdapter adapter = new RvAdapter(UsersActivity.this, copy);
                                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UsersActivity.this, LinearLayoutManager.VERTICAL, false);
                                                courseRV.setLayoutManager(linearLayoutManager);
                                                courseRV.setAdapter(adapter);
                                                progressdialog.dismiss();


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }else{
                            ArrayList<String> mylist = new ArrayList<String>();
                            mylist.clear();
                            RvAdapter adapter = new RvAdapter(UsersActivity.this ,mylist);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UsersActivity.this, LinearLayoutManager.VERTICAL, false);
                            courseRV.setLayoutManager(linearLayoutManager);
                            courseRV.setAdapter(adapter);
                            progressdialog.dismiss();
                            Toast.makeText(UsersActivity.this,"No Buses available",Toast.LENGTH_SHORT).show();
                        }



                    }
                },4000);

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

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.Logout:
                mAuth.signOut();
                Intent i = new Intent(UsersActivity.this,Login.class);
                startActivity(i);
                return true;
            case R.id.About:
                HelloFragment helloFragment = new HelloFragment();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                findViewById(R.id.layout).setVisibility(View.GONE);
                transaction.add(R.id.container, helloFragment, "helloFragment");
                transaction.commit();

        }
        return super.onOptionsItemSelected(item);
    }
}