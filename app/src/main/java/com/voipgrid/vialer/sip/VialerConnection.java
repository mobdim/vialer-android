package com.voipgrid.vialer.sip;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;

import com.voipgrid.vialer.util.PhoneNumberUtils;

import static com.voipgrid.vialer.sip.SipConstants.ACTION_BROADCAST_CALL_STATUS;
import static com.voipgrid.vialer.sip.SipConstants.CALL_CONNECTED_MESSAGE;
import static com.voipgrid.vialer.sip.SipConstants.CALL_DISCONNECTED_MESSAGE;
import static com.voipgrid.vialer.sip.SipConstants.CALL_STATUS_KEY;


@RequiresApi(api = Build.VERSION_CODES.M)
class VialerConnection extends Connection {
    final public static String TAG = VialerConnection.class.getSimpleName();

    private Context mContext;

    private boolean mSipServiceBound = false;

    private SipService mSipService;

    // Phone number to call
    String mPhoneNumberToCall;

    // Contact name associated with the phone number.
    String mContactName;

    private BroadcastReceiver mCallStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mCallStatusReceiver: " + intent.getStringExtra(CALL_STATUS_KEY));

            switch (intent.getStringExtra(CALL_STATUS_KEY)) {
                case CALL_CONNECTED_MESSAGE:
                    setActive();
                    break;
                case CALL_DISCONNECTED_MESSAGE:
                    DisconnectCause disconnectCause = new DisconnectCause(DisconnectCause.REMOTE);
                    setDisconnected(disconnectCause);
                    break;
            }
        }
    };

    private ServiceConnection mSipServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "Service connected");
            SipService.SipServiceBinder binder = (SipService.SipServiceBinder) service;
            mSipService = binder.getService();
            mSipServiceBound = true;
            setDialing();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSipServiceBound = false;
        }
    };

    VialerConnection(Context context) {
        mContext = context;

        IntentFilter intentFilter = new IntentFilter(ACTION_BROADCAST_CALL_STATUS);
        LocalBroadcastManager.getInstance(context).registerReceiver(mCallStatusReceiver, intentFilter);

        setConnectionCapabilities(CAPABILITY_SUPPORT_HOLD);
    }

    void startSipService() {
        final Intent intent = new Intent(mContext, SipService.class);
        intent.setAction(SipConstants.ACTION_VIALER_OUTGOING_NATIVE);

        // set a phoneNumberUri as DATA for the intent to SipServiceOld.
        Uri sipAddressUri = SipUri.sipAddressUri(
                mContext,
                PhoneNumberUtils.format(mPhoneNumberToCall)
        );
        intent.setData(sipAddressUri);

        intent.putExtra(SipConstants.EXTRA_PHONE_NUMBER, mPhoneNumberToCall);
        intent.putExtra(SipConstants.EXTRA_CONTACT_NAME, mContactName);

        mContext.startService(intent);
        setInitialized();
    }

    void bindSipService() {
        Intent intent = new Intent(mContext, SipService.class);
        mContext.bindService(intent, mSipServiceConnection, Context.BIND_AUTO_CREATE);
    }

    void stopSipService() {
        mContext.stopService(new Intent(mContext, SipService.class));
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        Log.e(TAG, "on call audio state");

        Log.e(TAG, state.toString());

        super.onCallAudioStateChanged(state);
    }

    @Override
    public void onCallEvent(String event, Bundle extras) {
        Log.e(TAG, "onCallEvent");

        Log.e(TAG, event);

        super.onCallEvent(event, extras);
    }

    @Override
    public void onDisconnect() {
        Log.e(TAG, "onDisconnect!");
        try {
            mSipService.getCurrentCall().hangup(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        destroy();

        stopSipService();
        super.onDisconnect();
    }

    @Override
    public void onAbort() {
        Log.e(TAG, "onAbort");
        super.onAbort();
    }

    @Override
    public void onAnswer() {
        Log.e(TAG, "onAnswer");
        setActive();
        super.onAnswer();
    }
}
