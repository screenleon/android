package com.example.practice.util.job;

public interface IBaseJob {
    Enum<?> getAction();
    Object[] getArgs();
    boolean runTask();
}
