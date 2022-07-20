package com.example.waybus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class MyReceiver2 extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if(!isOnline(context)){
                Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT).show();
            }
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }
    public boolean isOnline(Context context) {
        try{

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());

        }catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }
}