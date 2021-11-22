package com.example.practice.bluetooth;

import com.example.practice.enums.units.Scale;

public interface IDeviceCmd {
    IDeviceCmd stopCommand();
    IDeviceCmd setTimeCommand(boolean[] reminders, boolean ambient, Scale scale, boolean enable24Hr, boolean alarm);
    byte[] build();
}
