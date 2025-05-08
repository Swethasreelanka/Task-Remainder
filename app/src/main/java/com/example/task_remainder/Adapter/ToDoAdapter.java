package com.example.task_remainder.Adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task_remainder.AddNewTask;
import com.example.task_remainder.AlarmReceiver;
import com.example.task_remainder.MainActivity;
import com.example.task_remainder.Model.ToDoModel;
import com.example.task_remainder.R;
import com.example.task_remainder.TaskDetailsDialog;
import com.example.task_remainder.Utils.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> todoList;
    private DatabaseHandler db;
    private MainActivity activity;

    public ToDoAdapter(DatabaseHandler db, MainActivity activity) {
        this.db = db;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        db.openDatabase();
        final ToDoModel item = todoList.get(position);

        // Set task details
        holder.taskText.setText(item.getTask());
        holder.deadlineText.setText("Deadline: " + item.getDeadline());

        // Prevent multiple calls to onCheckedChangeListener when setting the state
        holder.taskCheckBox.setOnCheckedChangeListener(null);

        // Ensure CheckBox reflects the correct status
        holder.taskCheckBox.setChecked(item.getStatus() == 1);

        // Add listener to update the database when checkbox is clicked
        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.updateStatus(item.getId(), isChecked ? 1 : 0);
            item.setStatus(isChecked ? 1 : 0); // Update the local object state

            if (isChecked) {
                cancelAlarm(holder.itemView.getContext(), item.getId()); // Cancel alarms if task is marked complete
            }
        });

        // Show full task details when clicked
        holder.itemView.setOnClickListener(v -> {
            TaskDetailsDialog dialog = new TaskDetailsDialog(
                    item.getTask(),
                    item.getDeadline(),
                    new ArrayList<>(item.getReminders())
            );
            dialog.show(activity.getSupportFragmentManager(), TaskDetailsDialog.TAG);
        });
    }


    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);

        // Cancel alarms before deleting the task
        cancelAlarm(activity, item.getId());

        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        ToDoModel item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        bundle.putString("deadline", item.getDeadline());
        bundle.putStringArrayList("reminders", new ArrayList<>(item.getReminders()));

        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCheckBox;
        TextView taskText, deadlineText;

        ViewHolder(View view) {
            super(view);
            taskCheckBox = view.findViewById(R.id.todoCheckBox);
            taskText = view.findViewById(R.id.taskText);  // Added taskText
            deadlineText = view.findViewById(R.id.deadlineTextView);
        }
    }

    private void cancelAlarm(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Retrieve all reminders for the task
        List<String> reminders = db.getRemindersForTask(taskId);

        for (String reminder : reminders) {
            int requestCode = (taskId * 1000) + reminder.hashCode();

            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent); // Cancel the alarm
            }
        }
    }
}
