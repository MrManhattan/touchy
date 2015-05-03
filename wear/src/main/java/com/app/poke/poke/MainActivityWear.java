package com.app.poke.poke;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import java.util.Collection;
import java.util.HashSet;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by staefy on 31/01/15.
 */
public class MainActivityWear extends Activity implements GoogleApiClient.ConnectionCallbacks,MessageApi.MessageListener,GoogleApiClient.OnConnectionFailedListener {

    //-------- Globals-----------
    public static final String TAG = "GCM Android";
    private TextView mTextView;
    public GoogleApiClient mGoogleApiClient;
    public static final ChannelApi mChannelApi = new ChannelApi() {
        @Override
        public PendingResult<OpenChannelResult> openChannel(GoogleApiClient googleApiClient, String s, String s2) {
            return null;
        }

        @Override
        public PendingResult<Status> addListener(GoogleApiClient googleApiClient, ChannelListener channelListener) {
            return null;
        }

        @Override
        public PendingResult<Status> removeListener(GoogleApiClient googleApiClient, ChannelListener channelListener) {
            return null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        //--------Create GAC-----------
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        //--------UI-----------
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                Button btnRecord = (Button) stub.findViewById(R.id.btnRecord);

                //--------Setup Voice Recording-----------
                final AudioRecord audio = new AudioRecord(1,44100, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT));
                final short sData[] = new short[1024];

                //--------Button functionality-----------
                btnRecord.setOnTouchListener(new WatchViewStub.OnTouchListener() {
                    public boolean onTouch(View yourButton, MotionEvent theMotion) {
                        switch (theMotion.getAction()) {

                            //--------- Recording starts--------
                            case MotionEvent.ACTION_DOWN:
                                try {

                                    if(audio.getState() == AudioRecord.STATE_INITIALIZED){
                                        audio.startRecording();
                                    }

                                    new AsyncTask<Void, Void, String>() {
                                        @Override
                                        protected String doInBackground(Void... params) {
                                            Collection<String> nodes = getNodes();
                                            String nodeid = nodes.iterator().next();
                                            Log.d(TAG, "Node id: " + nodeid);

                                            //-------- TODO Open channel-----------
                                            mChannelApi.openChannel(mGoogleApiClient,nodeid,"test_id");


                                            return null;
                                        }
                                    }.execute(null, null, null);

                                } catch (Exception e) {
                                    Log.d(TAG, e.getMessage());
                                }
                                break;

                            //------ Recording ends and stream to phone starts------
                            case MotionEvent.ACTION_UP:
                                try {
                                    audio.stop();
                                    int numberOfShorts = audio.read(sData, 0, 1024);
                                    for(int i = 0; i < numberOfShorts; i++){

                                       // dataOutputStream.writeShort(audioData[i]);
                                    }
                                    audio.release();

                                    new AsyncTask<Void, Void, String>() {

                                        @Override
                                        protected String doInBackground(Void... params) {
                                            Collection<String> nodes = getNodes();
                                            String nodeid = nodes.iterator().next();
                                            Log.d(TAG, "Node id: " + nodeid);

                                            //--------TODO CLOSE CHANNEL-----------



                                            return null;
                                        }
                                    }.execute(null, null, null);

                                } catch (Exception e) {
                                    Log.d(TAG, e.getMessage());
                                }
                                break;
                        }
                        return true;
                    }

                });
            }
        });
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
        mGoogleApiClient.disconnect();
    }
    @Override //Handle the received poke (vibrating etc)
    public void onMessageReceived(MessageEvent messageEvent) {

    }
}