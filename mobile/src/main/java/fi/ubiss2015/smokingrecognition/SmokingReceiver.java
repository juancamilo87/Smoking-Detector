package fi.ubiss2015.smokingrecognition;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
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
        if(intent.getStringExtra("topic").equals("/smoking_prediction"))
        {
            SharedPreferences prefs = context.getSharedPreferences("fi.ubiss2015.smokingrecognition", Context.MODE_PRIVATE);
            int counter = prefs.getInt("counter",0);
            counter++;
            String message = intent.getStringExtra("message");

            String[] splitted = message.split("#");
            long timestamp = Long.parseLong(splitted[0]);
            int smoking = Integer.parseInt(splitted[1]);
            float probability = Float.parseFloat(splitted[2]);

            ContentValues data = new ContentValues();
            data.put(Provider.Smoking_Data.TIMESTAMP, timestamp);
            data.put(Provider.Smoking_Data.CLASSIFICATION, smoking);
            data.put(Provider.Smoking_Data.PROBABILITY, probability);

            context.getContentResolver().insert(Provider.Smoking_Data.CONTENT_URI, data);

            if(counter==8)
            {
                int smoke_count = 0;

                Cursor cursor = context.getContentResolver().query(Provider.Smoking_Data.CONTENT_URI, new String[]{Provider.Smoking_Data.TIMESTAMP, Provider.Smoking_Data.CLASSIFICATION}, null, null, "timestamp DESC LIMIT 8");
                if(cursor != null && cursor.moveToFirst())
                {
                    do {
                        smoke_count += cursor.getInt(1);

                    }while(cursor.moveToNext());

                    float smoke_average = (float) smoke_count/(float)8;

                    ContentValues data_s = new ContentValues();
                    data_s.put(Provider.Smoking_Summary.TIMESTAMP, System.currentTimeMillis());
                    data_s.put(Provider.Smoking_Summary.SUMMARY, smoke_average);

                    if(cursor != null && !cursor.isClosed())
                    {
                        cursor.close();
                    }
                    context.getContentResolver().insert(Provider.Smoking_Summary.CONTENT_URI, data_s);
                }

                counter = 0;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("counter", counter);
            editor.commit();
        }

    }
}
