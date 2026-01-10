package com.cgana.trmsdriver.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Network Monitor (Module 6 Part 1)
 * Monitors network connectivity in real-time
 */
public class NetworkMonitor {

    private Context context;
    private ConnectivityManager connectivityManager;
    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    private ConnectivityManager.NetworkCallback networkCallback;

    public NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager)
            this.context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Set initial connection state
        isConnected.postValue(checkConnection());

        // Register network callback
        registerNetworkCallback();
    }

    public LiveData<Boolean> getConnectionStatus() {
        return isConnected;
    }

    public boolean isCurrentlyConnected() {
        return checkConnection();
    }

    private boolean checkConnection() {
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    private void registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    isConnected.postValue(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    isConnected.postValue(false);
                }
            };

            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    isConnected.postValue(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    isConnected.postValue(checkConnection());
                }
            };

            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }

    public void unregister() {
        if (networkCallback != null && connectivityManager != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                // Already unregistered
            }
        }
    }
}

