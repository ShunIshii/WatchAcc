package fi.tgl.watchacc;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager manager;

    private ArrayList<Long> timeData;
    private ArrayList<Long> currentTimeData;
    private ArrayList<ArrayList<Float>> data;
    private long time;
    private boolean isFirst;
    private boolean isMeasuring;

    private EditText idText;
    private int id;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idText = findViewById(R.id.id_Text);
        startButton = findViewById(R.id.Start_Button);
        isMeasuring = false;
        time = 0l;
        id = 0;

        //get sensor manager
        manager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor acc = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        Log.d(TAG, "onSensorChanged" + x + ", " + y + ", " + z);

        if (isMeasuring) {
            for (int i = 0; i < 3; i++)
            {
                data.get(i).add(event.values[i]);
            }
            if (isFirst)
            {
                time = event.timestamp;
                isFirst = false;
            }
            timeData.add(event.timestamp - time);
            currentTimeData.add(System.currentTimeMillis());
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onClickButton(View v) {
        if (isMeasuring){
            isMeasuring = false;
            startButton.setText("START");
            OutputFile();
        }
        else {
            if (idText.getText().toString().equals("")) {
                Toast.makeText(this, "Input you ID", Toast.LENGTH_SHORT).show();
            }
            else {
                isMeasuring = true;
                isFirst = true;
                startButton.setText("STOP");
                data = new ArrayList<>();
                timeData = new ArrayList<>();
                currentTimeData = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    ArrayList<Float> arr = new ArrayList<>();
                    data.add(arr);
                }
                id = Integer.parseInt(idText.getText().toString());
            }

        }
    }

    private void OutputFile() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_kkmmss");
        String filename = sdf.format(date) + ".csv";
        Log.d(TAG, filename);
        try {
            FileOutputStream fout = openFileOutput("watch_" + String.format("%03d", id) + "_" + filename, MODE_PRIVATE);
            String comma = ",";
            String newline = "\n";
            for (int i = 0; i < data.get(0).size(); i++) {
                for (int j = 0; j < 3; j++)
                {
                    fout.write(String.valueOf(data.get(j).get(i)).getBytes());
                    fout.write(comma.getBytes());
                }
                fout.write(String.format("%.6f", Float.parseFloat(timeData.get(i).toString())/1000000000f).getBytes());
                fout.write(comma.getBytes());
                fout.write(String.valueOf(currentTimeData.get(i)).getBytes());
                fout.write(newline.getBytes());
            }
            fout.close();
            Log.d(TAG, "File created.");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Cannot open file.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Cannot write string.");
            e.printStackTrace();
        }
    }
}
