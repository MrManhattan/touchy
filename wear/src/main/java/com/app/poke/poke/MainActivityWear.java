package com.app.poke.poke;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by staefy on 31/01/15.
 */
public class MainActivityWear extends Activity implements GoogleApiClient.ConnectionCallbacks,MessageApi.MessageListener,GoogleApiClient.OnConnectionFailedListener {
    /********** Defines ************/
    public static final String START_ACTIVITY_PATH = "/start/MainActivity";
    public static final String TAG = "GCM Android";
    /********** Globals ************/
    private TextView mTextView;
    public GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        //Create GAC
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();



        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                Button btnRecord = (Button) stub.findViewById(R.id.btnRecord);
                btnRecord.setOnClickListener(new WatchViewStub.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            new AsyncTask<Void, Void, String>() {

                                @Override
                                protected String doInBackground(Void... params) {
                                    Collection<String> nodes = getNodes();
                                    String nodeid = nodes.iterator().next();
                                    Log.d(TAG, "Node id: "+nodeid);
                                    sendPokeMessage(nodeid);
                                    return null;
                                }
                            }.execute(null, null, null);

                        }catch (Exception e){
                            Log.d(TAG, e.getMessage());
                        }
                    }
                });
            }
        });


    }

    /********** Communicating pokes with phone************/
    /**
     *The START_ACTIVITY_PATH is not used and should be removed
     *sends message between paired devices
     * @param nodeId
     * @return
     */
    public void sendPokeMessage(String nodeId) {
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

    @Override //Handle the received poke (vibrating etc)
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "MessageEvent: " + messageEvent.getData().toString());
        //Vibrate device
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Vibrate method needs a pattern and to reapeat number ( -1 = no repeat)
        long[] vibrationPattern = {0, 1, 100, 400};
        int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);

    }
    /**
     * Goes through connected devices and returns them
     * Used in sending message between wearable and phone
     * @param
     * @return Collection<String>
     */
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (com.google.android.gms.wearable.Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("krattaGAC", "in onConnected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("krattaGAC", "in onConnectionFailed");
    }
    @Override
    protected void onResume() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //Wearable.DataApi.removeListener(mGoogleApiClient, this);
        //mGoogleApiClient.disconnect();
    }
}
