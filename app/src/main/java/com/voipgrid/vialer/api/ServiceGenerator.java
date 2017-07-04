package com.voipgrid.vialer.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.gson.GsonBuilder;
import com.voipgrid.vialer.R;
import com.voipgrid.vialer.logging.RemoteLogger;
import com.voipgrid.vialer.onboarding.SetupActivity;
import com.voipgrid.vialer.util.AccountHelper;
import com.voipgrid.vialer.util.ConnectivityHelper;
import com.voipgrid.vialer.util.JsonStorage;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.String.format;


/**
 * Project: Vialer
 * Package: com.voipgrid.vialer.api
 * Class  : ServiceGenerator
 * <p/>
 * Created by Eltjo Veninga (eltjo@peperzaken.nl) on 24-07-15 13:33
 * <p/>
 * Copyright (c) 2015 Peperzaken BV. All rights reserved.
 */
public class ServiceGenerator {

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder = new Retrofit.Builder();

    private static ServiceGenerator instance = null;
    private static boolean taken = false;

    private ServiceGenerator() {
    }

    public static ServiceGenerator getInstance() throws PreviousRequestNotFinishedException {
        if (taken) {
            throw new PreviousRequestNotFinishedException("Not ready for new request yet");
        }
        if (instance == null) {
            instance = new ServiceGenerator();
        }
        taken = true;
        return instance;
    }

    /**
     * Function to get the user agent string to use in the http requests.
     * @param context
     * @return
     */
    public static String getUserAgentHeader(Context context) {
        String appName = context.getString(R.string.app_name);
        String version = "?";
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return format("%s/%s (Android; %s, %s %s)", appName, version, Build.VERSION.RELEASE, Build.MANUFACTURER, Build.PRODUCT);
    }

    /**
     * Function to create the HttpClient to be used by retrofit for API calls.
     * @param context
     * @param username
     * @param password
     * @return
     */
    private static OkHttpClient getHttpClient(final Context context, final String username,
                                              final String password) {
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                if (username != null && password != null) {
                    requestBuilder.header("Authorization", Credentials.basic(username, password));
                }

                requestBuilder.header("User-Agent", getUserAgentHeader(context));

                if (ConnectivityHelper.get(context).hasNetworkConnection()) {
                    int maxAge = 60; // read from cache for 1 minute
                    requestBuilder.header("Cache-Control", "public, max-age=" + maxAge);
                } else {
                    int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                    requestBuilder.header("Cache-Control",
                            "public, only-if-cached, max-stale=" + maxStale);
                }

                Request request = requestBuilder.build();

                Response response = chain.proceed(request);

                // Check if we get a 401 and are not in the onboarding.
                if (response.code() == 401 &&
                        !context.getClass().getSimpleName().equals(
                                SetupActivity.class.getSimpleName())) {
                    new RemoteLogger(context, ServiceGenerator.class).w("Logged out on 401 API response");
                    // Clear logged in values.
                    new JsonStorage(context).clear();
                    new AccountHelper(context).clearCredentials();
                    if (context instanceof Activity) {
                        // Start onboarding.
                        Intent intent = new Intent(context, SetupActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        ((Activity) context).finish();
                    }
                }

                return response;
            }
        });

        httpClient.cache(getCache(context));

        return httpClient.build();
    }

    public static <S> S createPortalService(final Context context, Class<S> serviceClass,
                                            String username, String password) {
        return createService(context, serviceClass, getVgApiUrl(context),
                username, password);
    }

    /**
     * Create a service for given api class and URL.
     * @param context
     * @param serviceClass
     * @param baseUrl
     * @param <S>
     * @return
     */
    public static <S> S createService(final Context context, Class<S> serviceClass, String baseUrl) {
        return createService(context, serviceClass, baseUrl, null, null);
    }

    /**
     * Create a service for given api class and URL.
     * @param context
     * @param serviceClass
     * @param baseUrl
     * @param username
     * @param password
     * @param <S>
     * @return
     */
    public static <S> S createService(final Context context, Class<S> serviceClass, String baseUrl,
                                      String username, String password) {

        builder.baseUrl(baseUrl)
                .client(getHttpClient(context, username, password))
                .addConverterFactory(
                        GsonConverterFactory.create(new GsonBuilder().serializeNulls().create()));

        Retrofit retrofit = builder.build();

        return retrofit.create(serviceClass);
    }

    public static Cache getCache(Context context) {
        return new Cache(context.getCacheDir(), 1024 * 1024 * 10);
    }

    public static String getVgApiUrl(Context context) {
        return context.getString(R.string.api_url);
    }

    public void release() {
        taken = false;
    }
}
