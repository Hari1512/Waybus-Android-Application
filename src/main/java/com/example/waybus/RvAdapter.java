package com.example.waybus;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.Viewholder> {

    private final Context context;

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference myref;

    FirebaseUser firebaseUser;

    private static final String TAG = "Hari";
    ArrayList<String> mylist ;






    public RvAdapter(Context context, ArrayList<String> mylist) {
        this.mylist = mylist;
        this.context = context;


    }


    @NonNull
    @Override
    public RvAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);

        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RvAdapter.Viewholder holder, int position) {



        holder.courseNameTV.setText(mylist.get(position));
        holder.on.setVisibility(View.VISIBLE);













    }

    @Override
    public int getItemCount() {
        return mylist.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {


        private TextView courseNameTV;
        ImageView on;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            courseNameTV = itemView.findViewById(R.id.idTVCourseName);
            on = itemView.findViewById((R.id.onoff));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, mylist.get(getAdapterPosition()));

                    firebaseDatabase = FirebaseDatabase.getInstance();
                    mAuth = FirebaseAuth.getInstance();
                    databaseReference = firebaseDatabase.getReference("Registered users");
                    myref = databaseReference.child("Drivers");
                    firebaseUser = mAuth.getCurrentUser();

                    Log.d(TAG,"onclick");


                    myref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot s : snapshot.getChildren()){
                              String  key = s.getKey();
                                Log.d(TAG,  key);
                                String busname = s.child("name").getValue(String.class);
                                Log.d(TAG, busname + key);

                                if(busname.equals(mylist.get(getAdapterPosition()))){
                                    Log.d(TAG, "equal: ");
                                    Intent intent = new Intent(context, MapsActivity.class);
                                    intent.putExtra("message", key);
                                    context.startActivity(intent);
                                }
                            }



                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                    }
                 });




                }
            }
    }

