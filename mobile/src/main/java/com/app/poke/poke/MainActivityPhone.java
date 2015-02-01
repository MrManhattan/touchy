package com.app.poke.poke;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONObject;
import org.w3c.dom.Node;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by staefy on 31/01/15.
 *
 * Intermediary between wearable and server.
 *
 */
public class MainActivityPhone extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,MessageApi.MessageListener,GoogleApiClient.OnConnectionFailedListener{

    /**********Defines ************/
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String TAG = "GCM Android";
    public static final String START_ACTIVITY_PATH = "/start/MainActivity";
    /**********Globals************/
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid;
    TextView textView;
    public static GoogleApiClient mGoogleApiClient;
    Socket socket;

    /**********"Phone Main" - Connect to GCM and GAC************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_phone);



        /**********SOCKET CODE ************/
        IO.Options opts = new IO.Options();
        //opts.forceNew = true;
       // opts.reconnection = false;
        final Socket socket;
        try {
           socket = IO.socket("http://192.168.1.69", opts);


        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                socket.emit("Poke.ready", "hi");
                //socket.disconnect();
            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        }).on("touch", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                Log.i(TAG, "Received JSON");
            }
        });
        socket.connect(); //After connect we send a touch object

        }catch(Exception e){
            Log.i(TAG, "Receive/Send/Socket ERROR!!");
        }







        /********** Init globals ************/
        context = getApplicationContext();
        textView = (TextView) findViewById(R.id.textView);
        /********** Connect to GAC - will end up in connected or failed handler ************/
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if(mGoogleApiClient == null){
            Log.i(TAG, "GAC null, connect will fail");
        }else{
            Log.i(TAG, "GAC ok. Trying to connect...");
            mGoogleApiClient.connect();
        }
        /********** Register for google play ************/
        if( checkPlayServices() ){
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            Log.i(TAG, "Registration ID is" + regid);

            if (regid.isEmpty()) {
                registerInBackground();
            }else{
                textView.append("GCM initiated");
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

    }

    /********** Communicating pokes with wearable************/
    /**
     *The START_ACTIVITY_PATH is not used and should be removed
     *sends message between paired devices
     * @param nodeId
     * @return
     */
    public static void sendPokedMessage(String nodeId) {
        Log.d(TAG, "Inside sendMessage()");
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, nodeId, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        }
                    }
                }
        );
    }
    @Override //Forward poke to server
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "MessageEvent: " + messageEvent.getData().toString());
        //Send the poke to server
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    //SOCKET CODE
                    // Sending an object
                    JSONObject obj = new JSONObject();
                    obj.put("to","haemp");
                    socket.emit("Poke.poke", obj);


                    Bundle data = new Bundle();
                    data.putString("to", PokeConfig.TO_ID);
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(PokeConfig.SENDER_ID + "@gcm.googleapis.com", id, data);
                    msg = "Server call sent.";
                } catch (Exception ex) {
                    msg = "Server call not sent. Error :" + ex.getMessage();
                }
                return msg;
            }
        }.execute(null, null, null);

    }
    /**
     * Goes through connected devices and returns them
     * Used in sending message between wearable and phone
     * @param
     * @return Collection<String>
     */
    public static Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (com.google.android.gms.wearable.Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    /********** Not so important Handlers ************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity_phone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * We end up here if GAC.connect() was succesfull.
     * And add listener to the DataApi object
     * @param bundle
     * @return
     */
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected()" );
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    /**
     *Unused
     * @param i
     * @return
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * We end up here when we re-enter the app
     * Reconnect GAC and checkPLayServices
     * @param
     * @return
     */
    @Override
    protected void onResume() {
        Log.i(TAG, "in onResume()");
        super.onStart();
        mGoogleApiClient.connect();
        checkPlayServices();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "in onPause()");
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GAC.connect() FAILED!");
    }

    /********** Private helpers for GCM *********************/
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Fetches registration ID for this user from the shared preferences
     * @param context
     * @return
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivityPhone.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(PokeConfig.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                textView.append(msg + "\n");
            }
        }.execute(null, null, null);

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }







}
