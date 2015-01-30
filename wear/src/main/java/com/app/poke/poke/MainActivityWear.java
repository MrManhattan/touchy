package com.app.poke.poke;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivityWear extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                Button btnRecord = (Button) stub.findViewById(R.id.btnRecord);

                btnRecord.setOnClickListener(new WatchViewStub.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        mTextView.setText("Guling guling kom nu lilla fuling!");

                        //Get vibrator service
                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                        //Vibrate method needs a pattern and to reapeat number ( -1 = no repeat)
                        long[] vibrationPattern = {0, 1, 100, 400};
                        int indexInPatternToRepeat = -1;
                        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
                    }
                });
            }
        });


    }
}
