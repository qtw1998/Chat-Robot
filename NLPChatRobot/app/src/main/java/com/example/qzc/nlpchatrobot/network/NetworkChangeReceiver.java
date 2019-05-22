package com.example.qzc.nlpchatrobot.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    //这个类用于显示用户网络状态
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
