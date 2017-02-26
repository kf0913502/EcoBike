package com.ecobike.alnoor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainWindow extends AppCompatActivity {

    BluetoothSocket BTS;
    String LOG_TAG = "EcoBike";
    boolean LOG_MODE = true;
    boolean searchMode = false;
    boolean connected = false;
    TextView content;
    Button actionButton;
    int sessionPoints = 0;
    int lifeTimePoints = 0;
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            return false;
        }
    };

    BluetoothAdapter BTA = BluetoothAdapter.getDefaultAdapter();

    public void showLifeTimePoints()
    {
        content.setText("Lifetime points: " + lifeTimePoints + "\n\n Press button to start!");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.ecobike.alnoor.EcoBike.R.layout.activity_main_window);
        content = (TextView)findViewById(com.ecobike.alnoor.EcoBike.R.id.fullscreen_content);
        actionButton = (Button)findViewById(com.ecobike.alnoor.EcoBike.R.id.action_button);

        showLifeTimePoints();
        actionButton.setText("START");


        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!connected)
                { searchMode = true;
                actionButton.setText("Finding bike...");
                content.setText("Finding Bike...please wait");
                    actionButton.setEnabled(false);
                }
                else
                {
                    try {
                        BTS.close();
                        connected = false;
                        actionButton.setText("START");

                        lifeTimePoints += sessionPoints;
                        sessionPoints = 0;
                        showLifeTimePoints();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        if (getActionBar() != null)
            getActionBar().hide();
        else if (getSupportActionBar() != null)
            getSupportActionBar().hide();


        final Handler connecting = new Handler(Looper.getMainLooper());


        connecting.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!BTA.isDiscovering() && searchMode) {
                    Log.i(LOG_TAG, "Started Discovery");
                    actionButton.setEnabled(false);
                    BTA.startDiscovery();
                }


                if (connected)
                {
                    try {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(BTS.getInputStream()));
                        String sensorRead = reader.readLine();
                        if (sensorRead.charAt(0) == 's') {
                            sensorRead = sensorRead.substring(1);
                            Log.v("bike", sensorRead);
                            sessionPoints += (int) (Math.abs(Float.parseFloat(sensorRead)) * 100);
                            content.setText("START PEDDLING!!\nSession points : " + Integer.toString(sessionPoints));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                connecting.postDelayed(this,1000);
            }
        }, 1000);




        BroadcastReceiver devicesFoundListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction() == BluetoothDevice.ACTION_FOUND)
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    Log.i(LOG_TAG, "Found device " + device.getName());

                    if (device.getName() != null && device.getName().equals("HC-05"))
                    {
                        searchMode = false;
                        actionButton.setText(com.ecobike.alnoor.EcoBike.R.string.connecting);
                        content.setText(com.ecobike.alnoor.EcoBike.R.string.connectingUC);



                        try {
                            BTS =  connectBTDevice(device);

                            connected = true;

                            actionButton.setText(com.ecobike.alnoor.EcoBike.R.string.disconnect);
                            actionButton.setEnabled(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            actionButton.setEnabled(true);
                            content.setText(com.ecobike.alnoor.EcoBike.R.string.connect_failed);
                            actionButton.setText(com.ecobike.alnoor.EcoBike.R.string.find_barrier);
                        }


                    }
                }

                else if (intent.getAction() == BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                {
                    if (!connected && searchMode)
                    {
                        searchMode = false;
                        content.setText(com.ecobike.alnoor.EcoBike.R.string.no_barrier_found);
                        actionButton.setText(com.ecobike.alnoor.EcoBike.R.string.find_barrier);
                        actionButton.setEnabled(true);
                    }


                }

            }
        };


        IntentFilter events = new IntentFilter();
        events.addAction(BluetoothDevice.ACTION_FOUND);
        events.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(devicesFoundListener, events);

    }

    private void sessionEnded() {
    }


    BluetoothSocket connectBTDevice(BluetoothDevice BTD) throws IOException {
        Method method;

        try {
            method = BTD.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );

            BluetoothSocket bluetoothSocket = (BluetoothSocket) method.invoke(BTD, 1);
            bluetoothSocket.connect();
            return bluetoothSocket;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
