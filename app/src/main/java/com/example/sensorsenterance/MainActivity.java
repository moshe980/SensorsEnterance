package com.example.sensorsenterance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    ImageView orientationImg;
    ImageView proximitysImg;
    ImageView lightImg;
    ImageView magnetImg;
    TextView textView;
    EditText enter_codeET;
    Button sendBtn;
    SensorManager sensorManager;
    Sensor orientationSensor;
    Sensor lightSensor;
    Sensor magnetSensor;
    Sensor proximitySensor;
    boolean orientationFlag, lightFlag, magnetFlag, proximityFlag = false;
    boolean dirtyFlag = false;
    Context context = this;
    final int code = Math.abs(new Random().nextInt());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.SEND_SMS
                        , Manifest.permission.READ_PHONE_STATE
                        , Manifest.permission.READ_PHONE_NUMBERS
                },
                SEND_SMS_PERMISSION_REQUEST_CODE);
        enter_codeET = findViewById(R.id.enter_code);
        enter_codeET.setVisibility(View.GONE);

        sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setVisibility(View.GONE);

        orientationImg = findViewById(R.id.orientation);
        proximitysImg = findViewById(R.id.proximitys);
        lightImg = findViewById(R.id.light);
        magnetImg = findViewById(R.id.magnet);

        textView = findViewById(R.id.text);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (sendBtn.getText().toString()) {
                    case "Send":
                        sendSMS(enter_codeET.getText().toString());
                        sendBtn.setText("LOGIN");
                        enter_codeET.getText().clear();
                        enter_codeET.setHint("Enter the code you receive");

                        break;
                    case "LOGIN":
                        if (enter_codeET.getText().toString().equals(String.valueOf(code))) {
                            Intent intent = new Intent(context, SecondActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(context, "Wrong password!", Toast.LENGTH_LONG).show();
                        }

                        break;

                }

            }
        });


    }

    public void checkFlags() {
        if (orientationFlag && lightFlag && magnetFlag && proximityFlag && !dirtyFlag) {
            enter_codeET.setVisibility(View.VISIBLE);
            sendBtn.setVisibility(View.VISIBLE);
            dirtyFlag = true;


        }
    }

    public void sendSMS(String numPhone) {
        String smsMassage = String.valueOf(code);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(numPhone, null, smsMassage, null, null);


    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, proximitySensor, 2 * 1000 * 1000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                int lightValue = (int) event.values[0];

                if (lightValue == 0) {
                    lightImg.setImageResource(R.drawable.green_light);
                    Glide.with(this).load(R.drawable.green_light).into(lightImg);
                    lightFlag = true;
                } else {
                    Glide.with(this).load(R.drawable.red_light).into(lightImg);
                    lightFlag = false;

                }
                checkFlags();
                break;

            case Sensor.TYPE_ORIENTATION:
                int degree = Math.round(event.values[0]);

                if (degree < 10 && degree > -10) {
                    Glide.with(this).load(R.drawable.green_orientation).into(orientationImg);
                    orientationFlag = true;
                } else {
                    Glide.with(this).load(R.drawable.red_orientation).into(orientationImg);
                    orientationFlag = false;

                }
                checkFlags();

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                float azimuth = Math.round(event.values[0]);
                float pitch = Math.round(event.values[1]);
                float roll = Math.round(event.values[2]);

                int tesla = (int) Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));


                if (tesla >= 200) {
                    Glide.with(this).load(R.drawable.green_magnet).into(magnetImg);
                    magnetFlag = true;
                } else {
                    Glide.with(this).load(R.drawable.red_magnet).into(magnetImg);
                    magnetFlag = false;

                }
                checkFlags();

                break;

            case Sensor.TYPE_PROXIMITY:
                float proximityValue = event.values[0];

                if (proximityValue < proximitySensor.getMaximumRange()) {
                    Glide.with(this).load(R.drawable.green_proximity).into(proximitysImg);
                    proximityFlag = true;
                } else {
                    Glide.with(this).load(R.drawable.red_proximity).into(proximitysImg);
                    proximityFlag = false;
                }
                checkFlags();

                break;

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}