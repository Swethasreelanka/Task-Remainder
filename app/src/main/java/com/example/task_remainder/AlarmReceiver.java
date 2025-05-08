package com.example.task_remainder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.task_remainder.Utils.DatabaseHandler;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("taskId", -1);

        DatabaseHandler db = new DatabaseHandler(context);
        db.openDatabase();

        if (db.getTaskStatus(taskId) == -1) { // Task does not exist
            return; // Stop execution
        }

        String taskName = intent.getStringExtra("taskName");

        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra("taskName", taskName);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }

}
