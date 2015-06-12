package fi.ubiss2015.smokingrecognition;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Gyroscope;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Gyroscope_Provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class DetectorService extends Service {

    private Handler handler;

    private Runnable runnable;
    private Classifier classifier;

    private ArrayList<Float> acc_s_x;
    private ArrayList<Float> acc_s_y;
    private ArrayList<Float> acc_s_z;
    private ArrayList<Float> gyr_s_x;
    private ArrayList<Float> gyr_s_y;
    private ArrayList<Float> gyr_s_z;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Classify","OnStart");
        acc_s_x = new ArrayList<>();
        acc_s_y = new ArrayList<>();
        acc_s_z = new ArrayList<>();
        gyr_s_x = new ArrayList<>();
        gyr_s_y = new ArrayList<>();
        gyr_s_z = new ArrayList<>();
        //File modelFile = new File("res/iomodelrf.model");
        Context context = getApplicationContext();
        Resources resources = context.getResources();
        InputStream is = resources.openRawResource(R.raw.random_forest);
        //InputStream is = new FileInputStream(modelFile);

        //InputStream is = Plugin.class.getClassLoader().getResourceAsStream("iomodelrf.model");
        try {
            Log.d("Classify","Building classifier");
            classifier = (Classifier) SerializationHelper.read(is);
            Log.d("Classify","Classifier built");
        }
        catch (Exception e) {
            e.printStackTrace();
        }



        Aware.setSetting(this, Aware_Preferences.DEBUG_FLAG, true);

        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER, 20000);

        Aware.setSetting(this, Aware_Preferences.STATUS_GYROSCOPE, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_GYROSCOPE, 20000);

        Aware.setSetting(this, Aware_Preferences.STATUS_MAGNETOMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_MAGNETOMETER, 20000);

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(Accelerometer.ACTION_AWARE_ACCELEROMETER);
        broadcastFilter.addAction(Gyroscope.ACTION_AWARE_GYROSCOPE);
        registerReceiver(sensorReceiver, broadcastFilter);

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {

                Log.d("Classify","30 sec trigger");



                if(acc_s_x.size()>=20&&gyr_s_x.size()>=20)
                {
                    Log.d("Classify","More than 20");

                    preprocess(new ArrayList<Float>(acc_s_x), new ArrayList<Float>(acc_s_y), new ArrayList<Float>(acc_s_z),
                            new ArrayList<Float>(gyr_s_x), new ArrayList<Float>(gyr_s_y), new ArrayList<Float>(gyr_s_z));

                }

                handler.postDelayed(this,30000);
            }
        };

        handler.postDelayed(runnable, 30000);
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
//        Message msg = mServiceHandler.obtainMessage();
//        msg.arg1 = startId;
//        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    private void preprocess(ArrayList<Float> acc_x, ArrayList<Float> acc_y, ArrayList<Float> acc_z, ArrayList<Float> gyr_x, ArrayList<Float> gyr_y, ArrayList<Float> gyr_z) {
        Log.d("Classify","Preprocessing data");
        acc_s_x.clear();
        acc_s_y.clear();
        acc_s_z.clear();
        gyr_s_x.clear();
        gyr_s_y.clear();
        gyr_s_z.clear();
        ArrayList<Float> data = new ArrayList<>();
        float mean_a_x = getMean(acc_x);
        float mean_a_y = getMean(acc_y);
        float mean_a_z = getMean(acc_z);
        float mean_g_x = getMean(gyr_x);
        float mean_g_y = getMean(gyr_y);
        float mean_g_z = getMean(gyr_z);

        data.add(mean_a_x);
        data.add(mean_a_y);
        data.add(mean_a_z);
        data.add(getVariance(acc_x, mean_a_x));
        data.add(getVariance(acc_y, mean_a_y));
        data.add(getVariance(acc_z, mean_a_z));
        data.add(mean_g_x);
        data.add(mean_g_y);
        data.add(mean_g_z);
        data.add(getVariance(gyr_x, mean_g_x));
        data.add(getVariance(gyr_y,mean_g_y));
        data.add(getVariance(gyr_z,mean_g_z));

        classify(data);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void classify(ArrayList<Float> data)
    {
        Log.d("Classify","Classifying data");
        ArrayList<Attribute> attributes = new ArrayList<>();
        Attribute aMean_x = new Attribute("mean_x");
        Attribute aMean_y = new Attribute("mean_y");
        Attribute aMean_z = new Attribute("mean_z");
        Attribute aVar_x = new Attribute("var_x");
        Attribute aVar_y = new Attribute("var_y");
        Attribute aVar_z = new Attribute("var_z");
        Attribute aMean_g_x = new Attribute("mean_g_x");
        Attribute aMean_g_y = new Attribute("mean_g_y");
        Attribute aMean_g_z = new Attribute("mean_g_z");
        Attribute aVar_g_x = new Attribute("var_g_x");
        Attribute aVar_g_y = new Attribute("var_g_y");
        Attribute aVar_g_z = new Attribute("var_g_z");

        List class_categories = new ArrayList(2);
        class_categories.add("0");
        class_categories.add("1");
        Attribute smoking_prediction = new Attribute("label_g", class_categories);
        attributes.add(aMean_x);
        attributes.add(aMean_y);
        attributes.add(aMean_z);
        attributes.add(aVar_x);
        attributes.add(aVar_y);
        attributes.add(aVar_z);
        attributes.add(aMean_g_x);
        attributes.add(aMean_g_y);
        attributes.add(aMean_g_z);
        attributes.add(aVar_g_x);
        attributes.add(aVar_g_y);
        attributes.add(aVar_g_z);
        attributes.add(smoking_prediction);

        Instances instances = new Instances("new_data",attributes,0);
        instances.setClass(smoking_prediction);

        Instance values = new DenseInstance(12);
        values.setValue(attributes.get(0),data.get(0));
        values.setValue(attributes.get(1),data.get(1));
        values.setValue(attributes.get(2),data.get(2));
        values.setValue(attributes.get(3),data.get(3));
        values.setValue(attributes.get(4),data.get(4));
        values.setValue(attributes.get(5),data.get(5));
        values.setValue(attributes.get(6),data.get(6));
        values.setValue(attributes.get(7),data.get(7));
        values.setValue(attributes.get(8),data.get(8));
        values.setValue(attributes.get(9),data.get(9));
        values.setValue(attributes.get(10),data.get(10));
        values.setValue(attributes.get(11),data.get(11));

        values.setDataset(instances);

        int max = 0;
        try {
            double[] result = classifier.distributionForInstance(values);
            for (int i = 0; i < result.length; i++)
                if (result[i] >= result[max])
                    max = i;
            String prediction = instances.classAttribute().value(max);
            double probability = result[max];
            store_data(prediction, probability);
        }catch (Exception e){
            Log.e("Result","Unable to detect class type", e);
        }
    }

    private void store_data(String prediction, double probability) {
        Log.d("Classify", prediction + " with probability: " + probability);

        Intent intent = new Intent("ACTION_AWARE_WEAR_SEND_MESSAGE");

        String message = "";
        message += System.currentTimeMillis()+"#"+prediction+"#"+probability;
        intent.putExtra("message",message);


        intent.putExtra("topic","/smoking_prediction");
        sendBroadcast(intent);
    }

    private float getMean(ArrayList<Float> array)
    {
        float sum = 0;

        for (Float data_point : array) {
            sum += data_point;
        }
        return (float)sum/(float)array.size();
    }

    private float getVariance(ArrayList<Float> array, float mean)
    {
        float sumDiffsSquared = 0;
        for (float value : array)
        {
            float diff = value - mean;
            diff *= diff;
            sumDiffsSquared += diff;
        }
        return sumDiffsSquared  / (array.size() - 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sensorReceiver);
    }

    private SensorReceiver sensorReceiver = new SensorReceiver();

    public class SensorReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER))
            {
                ContentValues values = (ContentValues) intent.getExtras().get(Accelerometer.EXTRA_DATA);
                acc_s_x.add((float)values.get(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
                acc_s_y.add((float)values.get(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
                acc_s_z.add((float)values.get(Accelerometer_Provider.Accelerometer_Data.VALUES_0));

            }
            if(intent.getAction().equals(Gyroscope.ACTION_AWARE_GYROSCOPE))
            {
                ContentValues values = (ContentValues) intent.getExtras().get(Gyroscope.EXTRA_DATA);
                gyr_s_x.add((float)values.get(Gyroscope_Provider.Gyroscope_Data.VALUES_0));
                gyr_s_y.add((float)values.get(Gyroscope_Provider.Gyroscope_Data.VALUES_0));
                gyr_s_z.add((float)values.get(Gyroscope_Provider.Gyroscope_Data.VALUES_0));

            }
        }
    }
}
