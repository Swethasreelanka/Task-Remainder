package com.example.task_remainder;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class TaskDetailsDialog extends DialogFragment {

    public static final String TAG = "TaskDetailsDialog";

    private String task, deadline;
    private ArrayList<String> reminders;

    public TaskDetailsDialog(String task, String deadline, ArrayList<String> reminders) {
        this.task = task;
        this.deadline = deadline;
        this.reminders = reminders;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_details, null);
        dialog.setContentView(view);

        TextView taskTextView = view.findViewById(R.id.taskDetailsText);
        TextView deadlineTextView = view.findViewById(R.id.taskDetailsDeadline);
        TextView remindersTextView = view.findViewById(R.id.taskDetailsReminders);

        taskTextView.setText("Title: " + task);
        deadlineTextView.setText("Deadline: " + deadline);

        // Show reminders if available
        if (reminders != null && !reminders.isEmpty()) {
            StringBuilder remindersText = new StringBuilder("Reminders:\n\n");
            for (String reminder : reminders) {
                remindersText.append("- ").append(reminder).append("\n\n");
            }
            remindersTextView.setText(remindersText.toString());
        } else {
            remindersTextView.setText("No reminders set.");
        }

        return dialog;
    }
}
