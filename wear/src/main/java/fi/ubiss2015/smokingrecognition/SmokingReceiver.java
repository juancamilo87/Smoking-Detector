package fi.ubiss2015.smokingrecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aware.Accelerometer;
import com.aware.Gyroscope;
import com.aware.Magnetometer;

/**
 * Created by JuanCamilo on 6/10/2015.
 */
public class SmokingReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Message4", "Topic: " + intent.getStringExtra("topic"));
        Log.d("Message4", "Message: " + intent.getStringExtra("message"));
        if(intent.getStringExtra("topic").equals("/smoking_trigger"))
        {
            String message = intent.getStringExtra("message");
            String label = message;
            Log.d("Message4", "Label: "+ label);

            Intent newAccelerometerLabel = new Intent(Accelerometer.ACTION_AWARE_ACCELEROMETER_LABEL);
            newAccelerometerLabel.putExtra(Accelerometer.EXTRA_LABEL, label);
            context.sendBroadcast(newAccelerometerLabel);

            Intent newGyroscopeLabel = new Intent(Gyroscope.ACTION_AWARE_GYROSCOPE_LABEL);
            newGyroscopeLabel.putExtra(Gyroscope.EXTRA_LABEL, label);
            context.sendBroadcast(newGyroscopeLabel);

            Intent newMagnetometerLabel = new Intent(Magnetometer.ACTION_AWARE_MAGNETOMETER_LABEL);
            newMagnetometerLabel.putExtra(Magnetometer.EXTRA_LABEL, label);
            context.sendBroadcast(newMagnetometerLabel);
        }


    }
}
