package com.example.qzc.nlpchatrobot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    //This class is used to show the network connection status
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            //Toast.makeText(context, "Network is available! ", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Network is unavailable! \nPlease check your network connection! ", Toast.LENGTH_SHORT).show();
        }
    }
}
