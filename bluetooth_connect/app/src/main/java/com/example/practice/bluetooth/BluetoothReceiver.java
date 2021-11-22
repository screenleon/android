package com.example.practice.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.practice.MainActivity;
import com.example.practice.constants.bluetooth.BluetoothAction;
import com.example.practice.constants.bluetooth.BluetoothHandleStatus;
import com.example.practice.util.Helper;

import java.util.ArrayList;
import java.util.HashMap;

public class BluetoothReceiver {
    private static final String TAG = BluetoothReceiver.class.getSimpleName();
    private static HashMap<String, BluetoothDevice> DiscoverDevices = null;

    public static final BroadcastReceiver FOUND_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, String.format("FOUND_RECEIVER Action: %s", action));
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (DiscoverDevices.containsKey(device.getAddress())) {
                    DiscoverDevices.put(device.getAddress(), device);
                }
            }
        }
    };

    public static final BroadcastReceiver DISCOVERY_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, String.format("DISCOVERY_RECEIVER Action: %s", action));
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    BluetoothReceiver.DiscoverDevices = new HashMap<>();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    MainActivity.sBluetoothController.stopDiscovery();
                    MainActivity.sBluetoothController.sendMessage(BluetoothHandleStatus.DISCOVERY_FINISHED, BluetoothReceiver.getDiscoverDeviceList());
                    break;
            }
        }
    };

    public static final BroadcastReceiver GATT_UPDATE_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothAction.ACTION_GATT_CONNECTED:
                    MainActivity.sBluetoothController.sendMessage(BluetoothHandleStatus.GATT_CONNECTED);
                    break;
                case BluetoothAction.ACTION_GATT_DISCONNECTED:
                    MainActivity.sBluetoothController.sendMessage(BluetoothHandleStatus.DISCONNECTED);
                    break;
            }
        }
    };

    public static ArrayList<BluetoothDevice> getDiscoverDeviceList() {
        return Helper.MapBluetoothDevices2List(DiscoverDevices);
    }
}
