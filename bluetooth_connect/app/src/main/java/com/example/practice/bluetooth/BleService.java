package com.example.practice.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.practice.MainActivity;
import com.example.practice.constants.IntentAction;
import com.example.practice.constants.bluetooth.BleGattAttribute;
import com.example.practice.constants.bluetooth.BluetoothAction;
import com.example.practice.constants.bluetooth.BluetoothHandleStatus;
import com.example.practice.enums.bluetooth.BleActionJob;
import com.example.practice.enums.bluetooth.BleStatus;
import com.example.practice.enums.bluetooth.BluetoothStatus;
import com.example.practice.util.Helper;
import com.example.practice.util.job.JobManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BleService extends Service {
    private static final String TAG = BleService.class.getSimpleName();
    private final Binder mBinder = new LocalBinder();
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;
    private HashMap<String, BluetoothDevice> scanDevices = new HashMap<>();
    private BleStatus bleStatus = BleStatus.NONE;
    private Handler mHandler = new Handler();
    private JobManager jobManager;

    private static final long SCAN_PERIOD = 5000;

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            addDevice(device);
        }

        private void addDevice(BluetoothDevice device) {
            if (!scanDevices.containsKey(device.getAddress())) {
                scanDevices.put(device.getAddress(), device);
            }
        }
    };

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            MainActivity.sBluetoothController.sendMessage(BluetoothHandleStatus.BLUETOOTH_DEVICE_LIST, getScanDevices());
            mHandler.postDelayed(scanRunnable, SCAN_PERIOD);
        }
    };

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            Log.d(TAG, "onServiceChanged");
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange");
            Log.d(TAG, String.valueOf(newState));

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    MainActivity.sBluetoothController.sendMessage(BluetoothHandleStatus.GATT_CONNECTED, mBluetoothGatt.getDevice());
                    jobManager.addJob(new BleJob(BleActionJob.BLE_DISCOVER_SERVICES, new Object[]{gatt}));
                    bleStatus = BleStatus.SET_INITIAL;
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    MainActivity.sBluetoothController.sendMessage(BluetoothHandleStatus.DISCONNECTED);
                    bleStatus = BleStatus.NONE;
                    gatt.close();
                    mBluetoothGatt = null;
                    break;
                default:
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    BluetoothGattService service = gatt.getService(UUID.fromString(BleGattAttribute.SERVICE_DEVICE_INFORMATION));
                    BluetoothGattCharacteristic firmwareCharacteristic = service.getCharacteristic(UUID.fromString(BleGattAttribute.CHARACTERISTIC_FIRMWARE));

                    jobManager.addJob(new BleJob(BleActionJob.BLE_READ_CHARACTERISTIC, new Object[]{gatt, firmwareCharacteristic}));
                } catch (NullPointerException exception) {
                    Log.w(TAG, "Device not support");
                    gatt.close();
                    Log.w(TAG, "Disconnect device");
                }
            }

            jobManager.jobFinish();
        }

        /**
         * Async task, need to wait
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead");
            if (characteristic.getUuid() == UUID.fromString(BleGattAttribute.CHARACTERISTIC_READ)) {
                boolean isGen2 = false;
                boolean isGen2Device = false;

                switch (Helper.getHexString(characteristic.getValue())) {
                    case "00 00 00 00 ":
                        break;
                    case "00 00 00 01 ":
                        isGen2Device = true;
                        break;
                    default:
                        isGen2 = true;
                        isGen2Device = true;
                }

                BluetoothGattService service;
                BluetoothGattCharacteristic writeCharacteristic;

                if ((service = gatt.getService(UUID.fromString(BleGattAttribute.SERVICE_1_CONFIG))) != null) {
                    if (isGen2) {
                        writeCharacteristic = service.getCharacteristic(UUID.fromString(BleGattAttribute.CHARACTERISTIC_WRITE_F3));
                    } else {
                        writeCharacteristic = service.getCharacteristic(UUID.fromString(BleGattAttribute.CHARACTERISTIC_WRITE));
                    }

                    jobManager.addJob(new BleJob(BleActionJob.BLE_SET_CHARACTERISTIC_NOTIFICATION, new Object[]{gatt, true}));
                    jobManager.addJob(new BleJob(BleActionJob.BLE_STOP, new Object[]{gatt, writeCharacteristic}));
                    jobManager.addJob(new BleJob(BleActionJob.BLE_SET_TIME, new Object[]{gatt, writeCharacteristic}));
                }

                bleStatus = BleStatus.CONNECTED;
            }

            jobManager.jobFinish();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite");

            jobManager.jobFinish();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");
            broadcastUpdate(IntentAction.BLE_DATA_INTENT, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorRead");
            Log.d(TAG, String.format("Description: %s", Helper.getHexString(descriptor.getValue())));

            jobManager.jobFinish();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorWrite");
            Log.d(TAG, String.format("Description: %s", Helper.getHexString(descriptor.getValue())));

            jobManager.jobFinish();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "onReadRemoteRssi");
            Log.d(TAG, String.format("rssi: %d", rssi));
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }


    };

    public BleService() {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mBluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
        this.jobManager = new JobManager();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    public void startScan() {
        Log.d(TAG, "Start Scan");
        this.scanDevices.clear();
        MainActivity.sBluetoothController.sendMessage(BluetoothHandleStatus.SCANNING);
        this.mBluetoothLeScanner.startScan(this.leScanCallback);

        this.mHandler.postDelayed(this.scanRunnable, SCAN_PERIOD);
    }

    private ArrayList<BluetoothDevice> getScanDevices() {
        return Helper.MapBluetoothDevices2List(this.scanDevices);
    }

    public void stopScan() {
        Log.d(TAG, "Stop Scan");
        if (MainActivity.sBluetoothController.getBluetoothStatus() == BluetoothStatus.SCANNING) {
            this.mBluetoothLeScanner.stopScan(this.leScanCallback);
            this.mHandler.removeCallbacks(scanRunnable);
            MainActivity.sBluetoothController.sendMessage(BluetoothHandleStatus.SCAN_FINISHED);
        }
    }

    public boolean connect(final String address, Context context) {
        Log.d(TAG, String.format("Connect to device address: %s", address));
        if (address == null) {
            return false;
        }
        this.stopScan();

        try {
            final BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
            if (device != null) {
                Log.d(TAG, "Success get device");
                mBluetoothGatt = device.connectGatt(context, true, this.bluetoothGattCallback);
                this.jobManager.startRunJob();
            } else {
                Log.d(TAG, "Failed to get device");
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Device not found with provided address.");
            return false;
        }

        return true;
    }

    public void disconnect() {
        if (this.mBluetoothGatt != null) {
            jobManager.stopRunJob();
            this.mBluetoothGatt.disconnect();
            this.mBluetoothGatt.close();
        }
    }

    public void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "broadcastUpdate");
        final Intent intent = new Intent(action);
        if (characteristic != null) {
            switch (action) {
                case BluetoothAction.ACTION_GATT_CONNECTED:
                    intent.putExtra(IntentAction.EXTRA_DEVICE_INTENT, this.mBluetoothGatt.getDevice());
                    break;
            }
        }

        MainActivity.sBluetoothController.sendBroadcast(intent);
    }

    public BluetoothGatt getGatt() {
        return this.mBluetoothGatt;
    }

    public BluetoothDevice getGattDevice() {
        return mBluetoothGatt != null ? mBluetoothGatt.getDevice() : null;
    }
}
