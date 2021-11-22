package com.example.practice.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.example.practice.enums.bluetooth.BleActionJob;
import com.example.practice.util.job.BaseJob;

public class BleJob extends BaseJob {
    public BleJob(BleActionJob action, Object[] args) {
        super(action, args);
    }

    @Override
    public BleActionJob getAction() {
        return (BleActionJob) super.getAction();
    }

    @Override
    public boolean runTask() throws IllegalStateException {
        boolean result = false;
        Object[] args = this.getArgs();
        switch (this.getAction()) {
            case BLE_DISCOVER_SERVICES:
                result = BleGattMethod.DiscoverServices((BluetoothGatt) args[0]);
                break;
            case BLE_STOP:
                result = BleGattMethod.SetBleStop((BluetoothGatt) args[0], (BluetoothGattCharacteristic) args[1]);
                break;
            case BLE_SET_TIME:
                result = BleGattMethod.SetBleTime((BluetoothGatt) args[0], (BluetoothGattCharacteristic) args[1]);
                break;
            case BLE_SET_CHARACTERISTIC_NOTIFICATION:
                result = BleGattMethod.setCharacteristicNotification((BluetoothGatt) args[0], (BluetoothGattCharacteristic) args[1], (boolean) args[2]);
                break;
            case BLE_READ_CHARACTERISTIC:
                result = BleGattMethod.readCharacteristic((BluetoothGatt) args[0], (BluetoothGattCharacteristic) args[1]);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this.getAction());
        }

        return result;
    }
}
