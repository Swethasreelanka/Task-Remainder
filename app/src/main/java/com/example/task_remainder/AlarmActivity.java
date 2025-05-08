package com.example.task_remainder;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AlarmActivity extends Activity {
    private MediaPlayer mediaPlayer;
    private String taskName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        TextView taskTextView = findViewById(R.id.taskTextView);
        Button stopAlarmButton = findViewById(R.id.stopAlarmButton);

        taskName = getIntent().getStringExtra("taskName");
        taskTextView.setText(taskName);

        // Play alarm sound
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        stopAlarmButton.setOnClickListener(v -> stopAlarm());
    }

    private void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        finish(); // Close the alarm UI
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
