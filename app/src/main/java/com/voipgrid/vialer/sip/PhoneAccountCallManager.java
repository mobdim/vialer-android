package com.voipgrid.vialer.sip;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import com.voipgrid.vialer.R;

/**
 * Created by redmerloen on 3/22/17.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class PhoneAccountCallManager {

    public PhoneAccountCallManager(Context context) {


        ComponentName componentName = new ComponentName(context.getPackageName(), VialerConnectionService.class.getName());
        PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(componentName, VialerConnectionService.class.getName());

//        Uri sipAddress = Uri.parse(Resources.getSystem().getString(R.string.sip_host));

        PhoneAccount phoneAccount = new PhoneAccount.Builder(phoneAccountHandle, context.getString(R.string.app_name))
                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                .build();
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        telecomManager.registerPhoneAccount(phoneAccount);

//        List accounts = telecomManager.getCallCapablePhoneAccounts();

    }
}
