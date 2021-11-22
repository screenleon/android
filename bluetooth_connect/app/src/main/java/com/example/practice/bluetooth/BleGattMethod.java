package com.example.practice.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.example.practice.constants.bluetooth.BleGattAttribute;

import java.util.UUID;

public class BleGattMethod {
    private static final String TAG = BleGattMethod.class.getSimpleName();

    public static boolean DiscoverServices(final BluetoothGatt gatt) {
        return gatt.discoverServices();
    }

    private static boolean isCharacteristicWriteable(final BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    public static boolean SetBleStop(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "setBleStop");
        UartDeviceCmd deviceCmd = new UartDeviceCmd();

        return writeBle(gatt, characteristic, deviceCmd.stopCommand().build());
    }

    public static boolean SetBleTime(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "setBleTime");
        UartDeviceCmd deviceCmd = new UartDeviceCmd();

        return writeBle(gatt, characteristic, deviceCmd.setTimeCommand().build());
    }

    private static boolean writeBle(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] command) {
        Log.d(TAG, "writeBle");
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        characteristic.setValue(command);

        return gatt.writeCharacteristic(characteristic);
    }

    public static boolean readCharacteristic(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) throws NullPointerException {
        return gatt.readCharacteristic(characteristic);
    }

    public static boolean setCharacteristicNotification(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final boolean enable) {
        Log.d(TAG, "setCharacteristicNotification");
        boolean setCharacteristicNotification = gatt.setCharacteristicNotification(characteristic, enable);

        if (!setCharacteristicNotification) {
            Log.d(TAG, "setCharacteristicNotification false");
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BleGattAttribute.CHARACTERISTIC_CLIENT_CONFIG));
        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        return gatt.writeDescriptor(descriptor);
    }
}
