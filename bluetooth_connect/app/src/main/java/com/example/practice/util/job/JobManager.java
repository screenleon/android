package com.example.practice.util.job;

import android.util.Log;

import com.example.practice.enums.JobManagerStatus;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobManager {
    private static final String TAG = JobManager.class.getSimpleName();
    private ScheduledExecutorService executorService;
    private JobManagerStatus status;

    private static ArrayList<BaseJob> jobs = new ArrayList<>();
    private static final int IDLE_PERIOD = 5;

    private Runnable jobRunnable = new Runnable() {
        @Override
        public void run() {
            runJob();
        }
    };

    public JobManager() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.status = JobManagerStatus.IDLE;
    }

    public boolean isJobEmpty() {
        if (jobs.size() > 0) {
            return false;
        }

        return true;
    }

    public boolean addJob(BaseJob job) {
        return jobs.add(job);
    }

    private void runJob() {
        Log.d(TAG, "runJob");

        if (!this.isJobEmpty()) {
            BaseJob job = jobs.get(0);
            job.runTask();
            jobs.remove(job);
        } else {
            this.executorService.schedule(jobRunnable, IDLE_PERIOD, TimeUnit.SECONDS);
        }
    }

    public void startRunJob() {
        if (this.executorService == null) {
            this.executorService = Executors.newSingleThreadScheduledExecutor();
        }

        this.status = JobManagerStatus.WORKING;
        this.executorService.submit(jobRunnable);
    }

    public JobManagerStatus getStatus() {
        return this.status;
    }

    public void jobFinish() {
        this.executorService.submit(jobRunnable);
    }

    public void stopRunJob() {
        this.executorService.shutdown();
        jobs.clear();
        this.status = JobManagerStatus.IDLE;
    }
}
