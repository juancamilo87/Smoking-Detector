package fi.ubiss2015.smokingrecognition;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Barometer;
import com.aware.Gyroscope;
import com.aware.Magnetometer;
import com.aware.providers.Aware_Provider;

public class MainActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        Intent intent = new Intent(this, DetectorService.class);
        startService(intent);
        Aware.startPlugin(this, "com.aware.plugin.android.wear");
//        Aware.setSetting(this, Aware_Preferences.DEBUG_FLAG, true);




//        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, true);
//        Aware.setSetting(this,Aware_Preferences.FREQUENCY_ACCELEROMETER,20000);
//
//        Aware.setSetting(this, Aware_Preferences.STATUS_GYROSCOPE, true);
//        Aware.setSetting(this,Aware_Preferences.FREQUENCY_GYROSCOPE,20000);
//
//        Aware.setSetting(this, Aware_Preferences.STATUS_MAGNETOMETER, true);
//        Aware.setSetting(this,Aware_Preferences.FREQUENCY_MAGNETOMETER,20000);
//
//        Aware.setSetting(this,"study_id",371);
//        Aware.setSetting(this, Aware_Preferences.STATUS_WEBSERVICE,true);
//        Aware.setSetting(this, Aware_Preferences.WEBSERVICE_SERVER,"https://api.awareframework.com/index.php/webservice/index/371/lh2omoZ7IgUo");
//        Aware.setSetting(this, Aware_Preferences.FREQUENCY_WEBSERVICE, 5);
//        Aware.setSetting(this, "study_start", System.currentTimeMillis());
//
//        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
        //TODO: processing of data on the watch and creating DB with just timestamps when smoked with probability.



    }



}
