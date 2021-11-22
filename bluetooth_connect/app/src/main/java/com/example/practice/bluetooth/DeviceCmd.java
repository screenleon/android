package com.example.practice.bluetooth;

public class DeviceCmd {
    /**
     * Translate int array to byte array
     * @param  data     int[]
     * @return byte[]
     */
    protected byte[] intArrayToByteArray(int[] data) {
        byte[] output = new byte[data.length];
        for (int index = 0; index < data.length; index++) {
            output[index] = (byte) data[index];
        }

        return output;
    }

    /**
     * Get array's checksum
     * @param  intBuffer   int[]
     * @return int
     */
    protected int getChecksum(int[] intBuffer) {
        int sum = 0;
        for (int data : intBuffer) {
            sum = (sum + data) & 0xff;
        }

        return 256 - sum;
    }
}
