package com.example.practice;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practice.bluetooth.BluetoothController;
import com.example.practice.constants.IntentAction;
import com.example.practice.constants.Permission;
import com.example.practice.constants.RequestCode;
import com.example.practice.constants.bluetooth.BluetoothAction;
import com.example.practice.enums.bluetooth.BluetoothStatus;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView tvDeviceList;
    public static BluetoothController sBluetoothController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Permission.PERMISSION_REQUEST_COARSE_LOCATION);
        }

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!sBluetoothController.isBluetoothEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, RequestCode.REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        this.tvDeviceList = this.findViewById(R.id.tv_main_device_list);

        try {
            MainActivity.sBluetoothController = new BluetoothController(this);
            this.registerBluetoothReceiver();
        } catch (NullPointerException e) {
            e.printStackTrace();
            this.finish();
        }
    }

    public void getBondedDevices(View view) {
        Toast.makeText(this, "Start get bonded devices", Toast.LENGTH_SHORT).show();
        Set<BluetoothDevice> pairedDevices = sBluetoothController.getBondedDevices();

        StringBuilder text = new StringBuilder();
        for (BluetoothDevice device : pairedDevices) {
            text.append(String.format("Name: %s, Mac: %s\n", device.getName(), device.getAddress()));
        }
        if (pairedDevices.size() == 0) {
            text.append("None");
        }

        this.tvDeviceList.setText(text.toString());
    }

    public void getScanDevices(View view) {
        if (MainActivity.sBluetoothController.getBluetoothStatus() == BluetoothStatus.IDEL) {
            Toast.makeText(this, "Start get scan devices", Toast.LENGTH_SHORT).show();
            MainActivity.sBluetoothController.startScan();
        } else {
            Toast.makeText(this, "Stop get scan devices", Toast.LENGTH_SHORT).show();
            MainActivity.sBluetoothController.stopScan();
        }

    }

    public void getDiscoveryDevices(View view) {
        Toast.makeText(this, "Start get discovery devices", Toast.LENGTH_SHORT).show();
        MainActivity.sBluetoothController.startDiscovery();
    }

    private void registerBluetoothReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IntentAction.DISCOVERY_INTENT);
        intentFilter.addAction(IntentAction.SCAN_INTENT);

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case IntentAction.DISCOVERY_INTENT:
                        ArrayList<BluetoothDevice> discoveryDevices = intent.getParcelableArrayListExtra("discovery_list");
                        StringBuilder sb = new StringBuilder();
                        if (discoveryDevices != null && discoveryDevices.size() > 0) {
                            for (BluetoothDevice device : discoveryDevices) {
                                sb.append(String.format("Name: %s, Address: %s\n", device.getName(), device.getAddress()));
                            }
                        } else {
                            sb.append("None devices");
                        }

                        tvDeviceList.setText(sb.toString());
                        break;
                    case IntentAction.SCAN_INTENT:
                        ArrayList<BluetoothDevice> scanDevices = intent.getParcelableArrayListExtra("scan_list");
                        sb = new StringBuilder();
                        if (scanDevices != null && scanDevices.size() > 0) {
                            for (BluetoothDevice device : scanDevices) {
                                sb.append(String.format("Name: %s, Address: %s\n", device.getName(), device.getAddress()));
                            }
                        } else {
                            sb.append("None devices");
                        }

                        tvDeviceList.setText(sb.toString());
                        break;
                    case BluetoothAction.ACTION_GATT_SERVICES_DISCOVERED:
                        break;
                    default:
                }
            }
        }, intentFilter);
    }

    public void connectDevice(View view) {
        Toast.makeText(this, "Start connect Device", Toast.LENGTH_SHORT).show();
        MainActivity.sBluetoothController.connectBleDevice("00:00:00:00:00:00");
    }
}