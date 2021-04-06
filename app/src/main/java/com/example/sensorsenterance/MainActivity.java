package com.example.sensorsenterance;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    ImageView orientationImg;
    ImageView wifiImg;
    ImageView lightImg;
    ImageView magnetImg;
    ImageView bluetoothImg;
    ImageView muteImg;

    float[] mGravity;
    float[] mGeomagnetic;
    float degree;
    TextView textView;
    EditText enter_codeET;
    Button sendBtn;
    SensorManager sensorManager;
    WifiManager mainWifi;
    BluetoothAdapter mBluetoothAdapter;
    AudioManager audioManager;
    Sensor lightSensor;
    Sensor magnetSensor;
    Sensor accelerometer;
    boolean orientationFlag, lightFlag, magnetFlag, wifiFlag, bluetoothFlag, muteFlag = false;
    boolean dirtyFlag = false;
    Context context = this;
    final int code = Math.abs(new Random().nextInt());

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("pttt", "Is Granted");
                    Log.d("pttt", "action ! !");
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    sendSMS(enter_codeET.getText().toString());
                    sendBtn.setText("LOGIN");
                    enter_codeET.getText().clear();
                    enter_codeET.setHint("Enter the code you received");
                } else {
                    getPermission(Manifest.permission.SEND_SMS);


                    Log.d("pttt", "No Granted");
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

                }
            });

    private ActivityResultLauncher<String> firstRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("pttt", "Is Granted");
                    Log.d("pttt", "action ! !");
                    sendSMS(enter_codeET.getText().toString());
                    sendBtn.setText("LOGIN");
                    enter_codeET.getText().clear();
                    enter_codeET.setHint("Enter the code you receive");
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    requestWithExplainDialog("Need permission for send your code");
                    Log.d("pttt", "No Granted");

                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    ActivityResultLauncher<Intent> manuallyActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                            Log.d("pttt", "action ! !");
                            sendSMS(enter_codeET.getText().toString());
                            sendBtn.setText("LOGIN");
                            enter_codeET.getText().clear();
                            enter_codeET.setHint("Enter the code you receive");
                        } else if (true) {
                            getPermission(Manifest.permission.SEND_SMS);
                        } else {

                            Log.d("pttt", "Cant Action ! !");
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enter_codeET = findViewById(R.id.enter_code);
        enter_codeET.setVisibility(View.GONE);

        sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setVisibility(View.GONE);

        orientationImg = findViewById(R.id.orientation);
        wifiImg = findViewById(R.id.wifi);
        lightImg = findViewById(R.id.light);
        magnetImg = findViewById(R.id.magnet);
        bluetoothImg = findViewById(R.id.bluetooth);
        muteImg = findViewById(R.id.mute);

        textView = findViewById(R.id.text);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                switch (sendBtn.getText().toString()) {
                    case "Send":
                        if (isNumPhoneOk()) {
                            getPermission(MainActivity.this, Manifest.permission.SEND_SMS);
                        } else {
                            Toast.makeText(context, "Enter valid phone number!", Toast.LENGTH_LONG).show();
                        }


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

    private void getPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            sendSMS(enter_codeET.getText().toString());
            sendBtn.setText("LOGIN");
            enter_codeET.getText().clear();
            enter_codeET.setHint("Enter the code you receive");
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
            requestWithExplainDialog("Need permission for send your code");
        } else {

            firstRequestPermissionLauncher.launch(permission);
        }
    }

    private void requestWithExplainDialog(String message) {
        AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    private void getPermission(String permission) {
        if (shouldShowRequestPermissionRationale(permission)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            requestWithExplainDialog("Need permission for send your code");

        } else if (!shouldShowRequestPermissionRationale(permission)) {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            manuallyDialog(permission);
        } else {
            Log.d("pttt", "Cant Action ! !");
        }
    }

    private void manuallyDialog(String permission) {
        if (shouldShowRequestPermissionRationale(permission)) {
            Log.d("pttt", "Cant Action ! !");
            return;
        }

        String message = "Setting screen if user have permanently disable the permission by clicking Don't ask again checkbox.";
        AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        manuallyActivityResultLauncher.launch(intent);
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    public void checkFlags() {
        if (orientationFlag && lightFlag && magnetFlag && wifiFlag && bluetoothFlag && muteFlag && !dirtyFlag) {
            enter_codeET.setVisibility(View.VISIBLE);
            sendBtn.setVisibility(View.VISIBLE);
            dirtyFlag = true;
            sensorManager.unregisterListener(this);
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
        if (!dirtyFlag) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
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
                    Glide.with(this).load(R.drawable.green_light).into(lightImg);
                    lightFlag = true;
                } else {
                    Glide.with(this).load(R.drawable.red_light).into(lightImg);
                    lightFlag = false;

                }
                break;

            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values;
                float azimuth = Math.round(mGeomagnetic[0]);
                float pitch = Math.round(mGeomagnetic[1]);
                float roll = Math.round(mGeomagnetic[2]);


                int tesla = (int) Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));

                if (tesla <=30) {
                    Glide.with(this).load(R.drawable.green_magnet).into(magnetImg);
                    magnetFlag = true;
                } else {
                    Glide.with(this).load(R.drawable.red_magnet).into(magnetImg);

                    magnetFlag = false;

                }
                break;

        }

        if (mGravity != null && mGeomagnetic != null) {
            float rotation[] = new float[9];
            float Orientation[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(rotation, Orientation, mGravity, mGeomagnetic);
            SensorManager.getRotationMatrix(rotation, null, mGravity, mGeomagnetic);
            SensorManager.getOrientation(rotation, Orientation);

           degree = (float) (-Orientation[0] * 180 / Math.PI);
            System.out.println(degree);

            if (degree < 20 && degree > -20) {
                Glide.with(this).load(R.drawable.green_orientation).into(orientationImg);
                orientationFlag = true;
            } else {
                Glide.with(this).load(R.drawable.red_orientation).into(orientationImg);
                orientationFlag = false;
            }

        }
        if (mainWifi.isWifiEnabled()) {
            Glide.with(this).load(R.drawable.red_wifi).into(wifiImg);


            wifiFlag = false;
        } else {
            Glide.with(this).load(R.drawable.green_wifi).into(wifiImg);

            wifiFlag = true;
        }

        if (mBluetoothAdapter.isEnabled()) {
            Glide.with(this).load(R.drawable.red_bluetooth).into(bluetoothImg);
            bluetoothFlag = false;
        } else {
            Glide.with(this).load(R.drawable.green_bluetooth).into(bluetoothImg);
            bluetoothFlag = true;
        }

        if (isMuted(audioManager)) {
            Glide.with(this).load(R.drawable.green_mute).into(muteImg);

            muteFlag = true;
        } else {
            Glide.with(this).load(R.drawable.red_mute).into(muteImg);
            muteFlag = false;
        }


        checkFlags();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public static boolean isMuted(AudioManager audioManager) {
        return audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT || audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
    }

    public boolean isNumPhoneOk() {
        if (enter_codeET.getText().length() == 10) {
            return true;
        } else {
            return false;
        }
    }

}