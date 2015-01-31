package com.app.poke.poke;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.util.*;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.*;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.sql.Connection;
import java.util.Date;

import static com.google.android.gms.wearable.DataApi.*;

public class MainActivityWear extends Activity implements GoogleApiClient.ConnectionCallbacks,DataListener,GoogleApiClient.OnConnectionFailedListener{

    private TextView mTextView;
    public GoogleApiClient mGoogleApiClient;
    public PutDataMapRequest putDataMapReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);


        //Create GAC and test data map
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        testDataMap();



        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                Button btnRecord = (Button) stub.findViewById(R.id.btnRecord);

                btnRecord.setOnClickListener(new WatchViewStub.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        putDataMapReq.getDataMap().putString("meddelande", "Hej changed!");
                        Date d = new Date();
                        putDataMapReq.getDataMap().putLong("time", d.getTime());
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapReq.asPutDataRequest());
                    }
                });
            }
        });


    }

    // Create a data map and put data in it
    private void testDataMap() {
        putDataMapReq = PutDataMapRequest.create("/minData");
        //putDataMapReq.getDataMap().putString("meddelande","Hej!");
        Date d = new Date();
        putDataMapReq.getDataMap().putLong("time", d.getTime());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        //Wearable.DataApi.addListener(mGoogleApiClient, this);
        PendingResult<DataItemResult> pendingResult =
                 Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        pendingResult.setResultCallback(new ResultCallback<DataItemResult>() {
            @Override
            public void onResult(final DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.d("NVM", "Data item set: " + result.getDataItem().getUri());
                }
            }
        });
    }




    public void onConnected(Bundle bundle) {
        Log.d("krattaGAC", "onConnected: ");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/minData") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    mTextView.setText(Long.toString(dataMap.getLong("time")));
                    //Get vibrator service
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                    //Vibrate method needs a pattern and to reapeat number ( -1 = no repeat)
                    long[] vibrationPattern = {0, 1, 100, 400};
                    int indexInPatternToRepeat = -1;
                    vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
