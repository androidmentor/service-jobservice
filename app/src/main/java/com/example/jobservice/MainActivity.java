package com.example.jobservice;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String WORK_DURATION_KEY =
            BuildConfig.APPLICATION_ID + ".WORK_DURATION_KEY";

    private ComponentName mServiceComponent;
    private int mJobId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServiceComponent = new ComponentName(this, SimpleJobService.class);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(this, SimpleJobService.class);
            startService(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, SimpleJobService.class));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void scheduleJob(View v) {
        JobInfo.Builder builder = new JobInfo.Builder(mJobId++, mServiceComponent);
        builder.setMinimumLatency(10 * 1000);
        builder.setOverrideDeadline(60 * 1000);
        //builder.setPeriodic(5 * 1000);

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);

        PersistableBundle extras = new PersistableBundle();
        extras.putLong(WORK_DURATION_KEY, 5 * 1000);
        builder.setExtras(extras);

        // Schedule job
        Log.d(TAG, "Scheduling job ~ id:" + mJobId);
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = tm.schedule(builder.build());
        if (result <= 0) {
            Log.d(TAG, "Failed to schedule");
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void finishJob(View v) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
        if (allPendingJobs.size() > 0) {
            // Finish the last one
            int jobId = allPendingJobs.get(0).getId();
            jobScheduler.cancel(jobId);
            Toast.makeText(MainActivity.this, "Cancelled a job: " + jobId,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "No jobs to cancel",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void cancelAllJobs(View v) {
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();
        Toast.makeText(MainActivity.this, "Canceled all jobs", Toast.LENGTH_SHORT).show();
    }
}
