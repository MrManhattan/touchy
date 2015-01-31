package com.app.poke.poke;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by staefy on 31/01/15.
 *
 *
 *
 */
public class NewMainActivityPhone extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,DataApi.DataListener,GoogleApiClient.OnConnectionFailedListener{

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String TAG = "GCM Android";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;
    TextView textView;

    public static GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_phone);

        context = getApplicationContext();
        textView = (TextView) findViewById(R.id.textView);

        if( checkPlayServices() ){
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }else{
                textView.append("GCM initiated");
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        //Create GAC and test data map
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

}
