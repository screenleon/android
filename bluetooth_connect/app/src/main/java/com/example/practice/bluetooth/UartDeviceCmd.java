package com.example.practice.bluetooth;

import com.example.practice.enums.units.Scale;

import java.util.Calendar;

public class UartDeviceCmd extends DeviceCmd implements IDeviceCmd{
    private CurrentWork currentWork = CurrentWork.NONE;

    private static int[] SET_TIME_COMMAND = {
            0x1A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    private final static int[] STOP_COMMAND = {
            0x1A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    private enum CurrentWork {
        NONE, SET_TIME, STOP
    }

    /**
     * Build the given command
     * @throws IllegalStateException Unset Cmd setting
     * @return byte[]
     */
    @Override
    public byte[] build() throws IllegalStateException {
        if (this.currentWork == CurrentWork.NONE) {
            throw new IllegalStateException("Unknown State. Must set the command first!!");
        }

        int[] command = new int[4 + SET_TIME_COMMAND.length + 1 + 2];
        command[0] = 0x00;
        command[1] = (SET_TIME_COMMAND.length + 1 + 2) & 0xFF;
        command[2] = 0x00;
        command[3] = 0x00;
        command[4 + SET_TIME_COMMAND.length + 1] = 0x00;
        command[4 + SET_TIME_COMMAND.length + 2] = 0x00;

        switch (this.currentWork) {
            case STOP:
                System.arraycopy(STOP_COMMAND, 0, command, 4, STOP_COMMAND.length);
                command[4 + SET_TIME_COMMAND.length] = this.getChecksum(STOP_COMMAND);
                break;
            case SET_TIME:
                System.arraycopy(SET_TIME_COMMAND, 0, command, 4, SET_TIME_COMMAND.length);
                command[4 + SET_TIME_COMMAND.length] = this.getChecksum(SET_TIME_COMMAND);
                break;
        }

        StringBuilder sb = new StringBuilder();
        for (int c : command) {
            sb.append(String.format("%02x ", c));
        }


        // Reset status
        this.currentWork = CurrentWork.NONE;
        return this.intArrayToByteArray(command);
    }

    /**
     * Set Stop command
     * @return this
     */
    @Override
    public UartDeviceCmd stopCommand() {
        this.currentWork = CurrentWork.STOP;

        return this;
    }

    /**
     * Set Time by default
     * @return this
     */
    public UartDeviceCmd setTimeCommand() {
        boolean[] reminders = {};
        return this.setTimeCommand(reminders, false, Scale.CELSIUS, false, false);
    }

    /**
     * Set Time command
     * @param reminders Now reminder max size is 4
     * @param ambient Environment temperature
     * @param scale Scale unit
     * @param enable24Hr 24hr or 12hr
     * @param alarm Alarm on or off
     * @throws IllegalArgumentException Reminder's size larger than 4
     * @return this
     */
    @Override
    public UartDeviceCmd setTimeCommand(boolean[] reminders, boolean ambient, Scale scale, boolean enable24Hr, boolean alarm) throws IllegalArgumentException {
        if (reminders.length > 4) {
            throw new IllegalArgumentException("Give Reminder's array large than 4");
        }

        this.currentWork = CurrentWork.SET_TIME;

        Calendar now = Calendar.getInstance();
        SET_TIME_COMMAND[1] = now.get(Calendar.YEAR) - 1999;
        SET_TIME_COMMAND[2] = now.get(Calendar.MONTH) + 1;
        SET_TIME_COMMAND[3] = now.get(Calendar.DAY_OF_MONTH);
        SET_TIME_COMMAND[4] = now.get(Calendar.HOUR_OF_DAY);
        SET_TIME_COMMAND[5] = now.get(Calendar.MINUTE);
        SET_TIME_COMMAND[6] = now.get(Calendar.SECOND);

        SET_TIME_COMMAND[7] = 0x00;

        return this;
    }
}
