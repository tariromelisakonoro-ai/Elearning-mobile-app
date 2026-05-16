package com.example.learn_express;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

/**
 * BroadcastReceiver that listens for network connectivity changes
 * and notifies registered listeners so activities can react in real-time.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    private NetworkStateListener listener;

    public NetworkStateReceiver(NetworkStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isOnline = NetworkUtils.isOnline(context);
            if (isOnline) {
                listener.onNetworkAvailable();
            } else {
                listener.onNetworkLost();
            }
        }
    }
}
