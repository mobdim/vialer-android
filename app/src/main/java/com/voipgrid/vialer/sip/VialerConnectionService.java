package com.voipgrid.vialer.sip;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.util.Log;

import static android.telecom.TelecomManager.PRESENTATION_ALLOWED;


@RequiresApi(api = Build.VERSION_CODES.M)
public class VialerConnectionService extends ConnectionService {
    private final static String TAG = VialerConnectionService.class.getSimpleName();
    private VialerConnection mVialerConnection;

    @Override
    public void onCreate() {
        super.onCreate();

        mVialerConnection = new VialerConnection(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "Connection Service onUnBind!");

        Log.e(TAG, intent.getAction());

        mVialerConnection.stopSipService();
        return super.onUnbind(intent);
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e(TAG, "out going connection");

        Uri handle = request.getAddress();

        mVialerConnection.mPhoneNumberToCall = handle.getSchemeSpecificPart();
        mVialerConnection.mContactName = "";

        mVialerConnection.setAddress(request.getAddress(), PRESENTATION_ALLOWED);
        mVialerConnection.setInitializing();

        mVialerConnection.startSipService();

        mVialerConnection.bindSipService();

        return mVialerConnection;
    }

}
