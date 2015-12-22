package com.voipgrid.vialer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Helper class to check connectivity of the device
 */
public class ConnectivityHelper {

    private final ConnectivityManager mConnectivityManager;
    private final TelephonyManager mTelephonyManager;

    /**
     * Constuctor supply connectivity and telephoney manager
     *
     * @param connectivityManager
     * @param telephonyManager
     */
    public ConnectivityHelper(ConnectivityManager connectivityManager,
                              TelephonyManager telephonyManager) {
        mConnectivityManager = connectivityManager;
        mTelephonyManager = telephonyManager;
    }

    /**
     * Check the device current connectivity state based on the active network
     * @return
     */
    public boolean hasNetworkConnection() {
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Check if the device is connected via wifi or LTE connection
     * @return
     */
    public boolean hasFastData() {
        /* NB this is a very rough approximation to a 'fast data connection' */
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        return info.isConnected() && !info.isRoaming() &&
                (info.getType() == ConnectivityManager.TYPE_WIFI ||
                        mTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE);
    }

    public static ConnectivityHelper get(Context context) {
        ConnectivityManager c = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        TelephonyManager t = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        return new ConnectivityHelper(c,t);
    }
}
