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
import com.google.android.gms.common.api.AbstractPendingResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
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
                                            //-------- TODO Open channel-----------
                                            PendingResult<ChannelApi.OpenChannelResult> pendingResult = Wearable.ChannelApi.openChannel(mGoogleApiClient, nodeid, "test_id");
                                            pendingResult.setResultCallback(new ResultCallback<ChannelApi.OpenChannelResult>() {
                                                @Override
                                                public void onResult(ChannelApi.OpenChannelResult openChannelResult) {
                                                    Channel channel = openChannelResult.getChannel();
                                                    System.out.println("Channel open");
                                                    channel.getOutputStream(mGoogleApiClient).setResultCallback(new ResultCallback<Channel.GetOutputStreamResult>() {
                                                        @Override
                                                        public void onResult(Channel.GetOutputStreamResult getOutputStreamResult) {
                                                            byte bData[] = short2byte(sData);
                                                            try {
                                                                OutputStream Ostream = getOutputStreamResult.getOutputStream();

                                                                bData[0]= 66;

                                                                System.out.println("Before write");
                                                                PrintWriter out = new PrintWriter(
                                                                    new BufferedWriter(
                                                                            new OutputStreamWriter(Ostream )
                                                                    ), true);

                                                                out.println("Hej");
                                                                out.flush();
                                                                    System.out.println("Flushing");

                                                                    System.out.println("After neew write");



                                                            }catch (Exception e){}


                                                        }
                                                    });
                                                }
                                            });



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
    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

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