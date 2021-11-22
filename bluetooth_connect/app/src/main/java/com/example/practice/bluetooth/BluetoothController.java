package com.example.practice.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.practice.constants.IntentAction;
import com.example.practice.constants.bluetooth.BleGattAttribute;
import com.example.practice.constants.bluetooth.BluetoothAction;
import com.example.practice.constants.bluetooth.BluetoothHandleStatus;
import com.example.practice.enums.bluetooth.BluetoothStatus;
import com.example.practice.util.Helper;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothController {
    private static final String TAG = BluetoothController.class.getSimpleName();
    private final Context context;
    private final BluetoothAdapter mBluetoothAdapter;
    private static BleService mBleService;
    private static BluetoothStatus bluetoothStatus = BluetoothStatus.IDEL;
    private ServiceConnection serviceConnection = null;
    protected static Handler mHandler;

    private Handler bluetoothStatusHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Intent intent;
            switch (msg.what) {
                case BluetoothHandleStatus.DISCOVERY_FINISHED:
                    intent = new Intent(IntentAction.DISCOVERY_INTENT);
                    if (Helper.isArrayListOfType((ArrayList<?>) msg.obj, BluetoothDevice.class)) {
                        intent.putParcelableArrayListExtra("discovery_list", (ArrayList<BluetoothDevice>) msg.obj);
                    }

                    sendBroadcast(intent);
                    break;
                case BluetoothHandleStatus.SCANNING:
                    bluetoothStatus = BluetoothStatus.SCANNING;
                    break;
                case BluetoothHandleStatus.SCAN_FINISHED:
                    bluetoothStatus = BluetoothStatus.IDEL;
                    break;
                case BluetoothHandleStatus.BLUETOOTH_DEVICE_LIST:
                    intent = new Intent(IntentAction.SCAN_INTENT);
                    if (Helper.isArrayListOfType((ArrayList<?>) msg.obj, BluetoothDevice.class)) {
                        intent.putParcelableArrayListExtra("scan_list", (ArrayList<BluetoothDevice>) msg.obj);
                    }

                    sendBroadcast(intent);
                    break;
                case BluetoothHandleStatus.UPDATE_STATUS:
                    if (msg.obj instanceof BluetoothStatus) {
                        bluetoothStatus = (BluetoothStatus) msg.obj;
                    }

                    break;
                case BluetoothHandleStatus.GATT_CONNECTED:
                    bluetoothStatus = BluetoothStatus.GATT_CONNECTED;
                    BluetoothGatt gatt = mBleService.getGatt();
                    BluetoothGattService service = null;
                    if ((service = (gatt.getService(UUID.fromString(BleGattAttribute.SERVICE_1_CONFIG)))) != null) {
                        gatt.setCharacteristicNotification(service.getCharacteristic(UUID.fromString(BleGattAttribute.CHARACTERISTIC_READ)), true);
                    }
                    break;
                case BluetoothHandleStatus.DISCONNECTED:
                    bluetoothStatus = BluetoothStatus.IDEL;
                    break;
                default:
                    break;
            }
        }
    };

    public BluetoothController(Context context) throws NullPointerException {
        this.context = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.mBluetoothAdapter == null) {
            bluetoothStatus = BluetoothStatus.NOT_EXIST;
            throwMissingAdapterException();
        }

        mBleService = new BleService();
        mHandler = bluetoothStatusHandler;
        this.registerActionFound();
    }

    public BluetoothStatus getBluetoothStatus() {
        return bluetoothStatus;
    }

    public boolean bindGattService() {
        Intent gattServiceIntent = new Intent(this.context, BleService.class);
        return this.context.bindService(gattServiceIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindGattService() {
        this.context.unbindService(this.serviceConnection);
    }

    public void connectBleDevice(final String address) {
        if (mBleService.getGatt() == null) {
            this.serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (!(service instanceof BleService)) {
                        mBleService = ((BleService.LocalBinder) service).getService();
                    }

                    if (mBleService != null) {
                        if (mBleService.connect(address, context)) {
                            bluetoothStatus = BluetoothStatus.GATT_CONNECTED;
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    bluetoothStatus = BluetoothStatus.IDEL;
                    unRegisterGattUpdateReceiver();
                    unbindGattService();
                    mBleService = null;
                }
            };

            registerGattUpdateReceiver();
            bindGattService();
            mBleService.connect(address, this.context);
        } else {
            mBleService.disconnect();
        }
    }

    public boolean isBluetoothEnabled() {
        return this.mBluetoothAdapter.isEnabled();
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return this.mBluetoothAdapter.getBondedDevices();
    }

    public void sendBroadcast(final Intent intent) {
        this.context.sendBroadcast(intent);
    }

    public void sendMessage() {
        mHandler.obtainMessage().sendToTarget();
    }

    public void sendMessage(int what) {
        mHandler.obtainMessage(what).sendToTarget();
    }

    public void sendMessage(int what, Object obj) {
        mHandler.obtainMessage(what, obj).sendToTarget();
    }

    public void sendMessage(int what, int arg1, int arg2) {
        mHandler.obtainMessage(what, arg1, arg2).sendToTarget();
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
    }

    public void startDiscovery() {
        Toast.makeText(this.context, "Discovery started", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Discovery start");
        this.registerDiscovery();
        this.mBluetoothAdapter.startDiscovery();
        bluetoothStatus = BluetoothStatus.DISCOVERING;
    }

    public void stopDiscovery() {
        Toast.makeText(this.context, "Discovery Finished", Toast.LENGTH_SHORT).show();
        this.unregisterDiscovery();
        this.mBluetoothAdapter.cancelDiscovery();
        bluetoothStatus = BluetoothStatus.IDEL;
    }

    public void startScan() {
        mBleService.startScan();
    }

    public void stopScan() {
        mBleService.stopScan();
    }

    private void registerActionFound() {
        this.context.registerReceiver(BluetoothReceiver.FOUND_RECEIVER, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    private void registerDiscovery() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.context.registerReceiver(BluetoothReceiver.DISCOVERY_RECEIVER, filter);
    }

    private void unregisterDiscovery() {
        this.context.unregisterReceiver(BluetoothReceiver.DISCOVERY_RECEIVER);
    }

    private void throwMissingAdapterException() throws NullPointerException {
        throw new NullPointerException("Missing Bluetooth Adapter!");
    }

    private void registerGattUpdateReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAction.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothAction.ACTION_GATT_DISCONNECTED);

        this.context.registerReceiver(BluetoothReceiver.GATT_UPDATE_RECEIVER, intentFilter);
    }

    private void unRegisterGattUpdateReceiver() {
        this.context.unregisterReceiver(BluetoothReceiver.GATT_UPDATE_RECEIVER);
    }
}
