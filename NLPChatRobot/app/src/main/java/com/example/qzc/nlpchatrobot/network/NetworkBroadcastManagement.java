package com.example.qzc.nlpchatrobot.network;

import android.content.Context;
import android.content.IntentFilter;

public class NetworkBroadcastManagement implements BroadcastManagement {

    private Context mContext;
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    public NetworkBroadcastManagement(Context context){
        mContext = context;
    }


    @Override
    public void registerReceiver() {
        //注册网络状态监听器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        mContext.registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    public void unregisterReceiver() {
        //注销网络状态监听器
        mContext.unregisterReceiver(networkChangeReceiver);
    }
}
