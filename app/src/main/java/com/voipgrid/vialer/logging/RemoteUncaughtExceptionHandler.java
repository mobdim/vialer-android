package com.voipgrid.vialer.logging;

import android.content.Context;
import android.os.Build;
import android.support.compat.BuildConfig;
import android.util.Log;

/**
 * Class that sends the uncaught exceptions to remote an presents the regular crash screen
 * afterwards.
 */
public class RemoteUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public RemoteUncaughtExceptionHandler(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        logStackTrace(throwable);
        mDefaultHandler.uncaughtException(thread, throwable);
    }

    private void logStackTrace(Throwable exception) {
        RemoteLogger remoteLogger = new RemoteLogger(mContext, RemoteUncaughtExceptionHandler.class, true);
        String stackTrace = Log.getStackTraceString(exception);
        String traceID = remoteLogger.generateIdentifier();

        remoteLogger.e("*************************************");
        remoteLogger.e("************ BEGIN CRASH ************");
        remoteLogger.e("************ APP INFO ************");
        remoteLogger.e("Version number: " + BuildConfig.VERSION_CODE);
        remoteLogger.e("Version name: " + BuildConfig.VERSION_NAME);
        remoteLogger.e("************ DEVICE INFORMATION ***********");
        remoteLogger.e("Brand: " + Build.BRAND);
        remoteLogger.e("Device: " + Build.DEVICE);
        remoteLogger.e("Model: " + Build.MODEL);
        remoteLogger.e("Id: " + Build.ID);
        remoteLogger.e("Product: " + Build.PRODUCT);
        remoteLogger.e("************ BUILD INFO ************");
        remoteLogger.e("SDK: " + Build.VERSION.SDK_INT);
        remoteLogger.e("Release: " + Build.VERSION.RELEASE);
        remoteLogger.e("Incremental: " + Build.VERSION.INCREMENTAL);

        remoteLogger.e("************ CAUSE OF ERROR ************");
        String[] lines = stackTrace.split(System.getProperty("line.separator"));
        for (String line : lines) {
            remoteLogger.e(traceID + "> " + line);
        }
        remoteLogger.e("************ END CRASH **************");
        remoteLogger.e("*************************************");
    }
}