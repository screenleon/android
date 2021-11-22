package com.example.practice.bluetooth;

import android.bluetooth.BluetoothGatt;

public interface IBleGattMethod {
    boolean discoverServices(final BluetoothGatt gatt);
}
