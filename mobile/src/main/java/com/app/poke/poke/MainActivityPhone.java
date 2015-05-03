package com.app.poke.poke;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.test.suitebuilder.annotation.Suppress;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
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
    Context context;

    Handler uiThreadHandler = new Handler();
    TextView textView;
    public static GoogleApiClient mGoogleApiClient;
    Socket socket;
    String response;
    Button btnNotify;

    /**********"Phone Main" - Connect to GCM and GAC************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_phone);

        /********** Init globals ************/
        context = getApplicationContext();
        textView = (TextView) findViewById(R.id.textView);
        btnNotify = (Button) findViewById(R.id.btnNotify);



        btnNotify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try{
                    System.out.println("Write to socket...");
                    PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream()
                        )
                    ), true);

                    out.println("Hej");
                    System.out.println("After writing to socket...");
                }catch(Exception e){

                }
            }
        });


        final byte micData[] = new byte[2048];

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
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                Collection<String> nodes = getNodes();
                String nodeid = nodes.iterator().next();
                Log.d(TAG, "Node id: " + nodeid);
                //-------- TODO Open channel-----------
                PendingResult<ChannelApi.OpenChannelResult> pendingResult = Wearable.ChannelApi.openChannel(mGoogleApiClient, nodeid, "test_id");
/*
                Wearable.ChannelApi.addListener(mGoogleApiClient,new ChannelApi.ChannelListener() {
                    @Override
                    public void onChannelOpened(Channel channel) {
                        System.out.print(micData);
                    }

                    @Override
                    public void onChannelClosed(Channel channel, int i, int i2) {
                        System.out.print(micData);
                    }

                    @Override
                    public void onInputClosed(Channel channel, int i, int i2) {
                        System.out.print(micData);
                    }

                    @Override
                    public void onOutputClosed(Channel channel, int i, int i2) {
                        System.out.print(micData);
                    }
                });*/
                pendingResult.setResultCallback(new ResultCallback<ChannelApi.OpenChannelResult>() {

                    @Override
                    public void onResult(ChannelApi.OpenChannelResult openChannelResult) {
                        final Channel channel = openChannelResult.getChannel();
                        System.out.println("Channel open");

                       channel.getInputStream(mGoogleApiClient).setResultCallback(new ResultCallback<Channel.GetInputStreamResult>() {
                            @Override
                            public void onResult(Channel.GetInputStreamResult getInputStreamResult) {

                                try {
                                    BufferedReader br = new BufferedReader(new InputStreamReader(getInputStreamResult.getInputStream()));
                                    while(true){
                                        System.out.println("Before read");
                                       //String var = br.readLine();
                                        //System.out.println(var);


                                        while(getInputStreamResult.getInputStream().available()>0){
                                            getInputStreamResult.getInputStream().skip(1);
                                        }
                                       // getInputStreamResult.getInputStream().read(micData,0,1);
                                       // System.out.println(micData);
                                        System.out.println("After read");
                                    }


                                } catch (Exception e) {
                                    System.out.println(e.toString());
                                }


                            }
                        });
                    }
                });
                return null;
            }
        }.execute(null, null, null);

        // Figure out what IP we're on
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        // start the server
        System.out.println(ip);
        new Thread(new StartServerThread(this, ip)).start();

        // Connect to server to send information
        new Thread(new ConnectToSocket(this)).start();
    }

    /********** Communicating pokes with wearable************/
    /**
     *The START_ACTIVITY_PATH is not used and should be removed
     *sends message between paired devices
     * @param nodeId
     * @return
     */
    public static void sendPokedStartMessage(String nodeId) {
        Log.d(TAG, "Inside sendMessage()");
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, nodeId,"start", new byte[0]).setResultCallback(
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
    public static void sendPokedStopMessage(String nodeId) {
        Log.d(TAG, "Inside sendMessage()");
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, nodeId, "stop", new byte[0]).setResultCallback(
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
    public void onMessageReceived(final MessageEvent messageEvent) {


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



}
