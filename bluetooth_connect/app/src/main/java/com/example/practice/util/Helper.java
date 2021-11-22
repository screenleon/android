package com.example.practice.util;


import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Helper {
    public static boolean isArrayOfType(Object[] list, Class<?> testClass) throws NullPointerException {
        return list != null && list.getClass().getComponentType().isAssignableFrom(testClass);
    }

    public static <T> boolean isArrayListOfType(ArrayList<?> list, Class<T> testClass) throws NullPointerException {
        return list != null && list.size() > 0 && testClass.isInstance(list.get(0));
    }

    public static ArrayList<BluetoothDevice> MapBluetoothDevices2List(Map<String, BluetoothDevice> devicesMap) {
        ArrayList<BluetoothDevice> results = new ArrayList<>();
        if (devicesMap != null) {
            Set<Map.Entry<String, BluetoothDevice>> entrySet = devicesMap.entrySet();
            for (Map.Entry<String, BluetoothDevice> entry : entrySet) {
                BluetoothDevice device = entry.getValue();
                results.add(device);
            }
        }

        return results;
    }

    public static String getHexString(byte[] data) {
        if (data == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x ", b & 0xff));
        }

        return sb.toString();
    }
}
