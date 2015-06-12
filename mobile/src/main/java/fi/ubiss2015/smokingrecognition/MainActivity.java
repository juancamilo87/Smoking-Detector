package fi.ubiss2015.smokingrecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aware.Aware;

/**
 * Created by JuanCamilo on 6/10/2015.
 */
public class MainActivity extends Activity {

    private Button start_stop_button;
    private boolean smoking;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activiy_layout);
        start_stop_button = (Button) findViewById(R.id.btn_start_stop);
        prefs = getSharedPreferences("fi.ubiss2015.smokingrecognition", MODE_PRIVATE);
        Aware.startPlugin(this,"com.aware.plugin.android.wear");
        smoking = prefs.getBoolean("smoking",false);
        if(smoking)
        {
            start_stop_button.setText("Stop");
        }
        else
        {
            start_stop_button.setText("Start");
        }

        start_stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("ACTION_AWARE_WEAR_SEND_MESSAGE");
                if(smoking)
                {
                    intent.putExtra("message", "stop");
                    start_stop_button.setText("Start");
                }
                else
                {
                    intent.putExtra("message","start");
                    start_stop_button.setText("Stop");
                }
                intent.putExtra("topic","/smoking_trigger");
                sendBroadcast(intent);

                smoking = !smoking;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("smoking",smoking);
                editor.commit();


            }
        });

        //TODO: Query database of watch from phone and visualize.


    }
}
