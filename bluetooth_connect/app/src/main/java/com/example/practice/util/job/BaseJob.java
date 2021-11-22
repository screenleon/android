package com.example.practice.util.job;

public class BaseJob implements IBaseJob{
    private Enum<?> action;
    private Object[] args;

    public BaseJob(Enum<?> action, Object[] args) {
        this.action = action;
        this.args = args;
    }

    @Override
    public Enum<?> getAction() {
        return this.action;
    }

    @Override
    public Object[] getArgs() {
        return this.args;
    }

    @Override
    public boolean runTask() { return false; }
}
