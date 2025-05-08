package com.example.task_remainder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.task_remainder.Model.ToDoModel;
import com.example.task_remainder.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private EditText newTaskText;
    private TextView deadlineTextView, addReminderTextView;
    private Button newTaskSaveButton, addReminderButton;
    private LinearLayout reminderContainer;
    private Calendar deadlineCalendar;

    private DatabaseHandler db;
    private String selectedDeadline = "";
    private List<String> remindersList = new ArrayList<>();

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View parent = (View) view.getParent();
        if (parent != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO); // Ensures it expands properly
        }
        newTaskText = view.findViewById(R.id.newTaskText);
        deadlineTextView = view.findViewById(R.id.deadlineTextView);
        addReminderTextView = view.findViewById(R.id.addReminderTextView);
        newTaskSaveButton = view.findViewById(R.id.newTaskButton);
        addReminderButton = view.findViewById(R.id.addReminderButton);
        reminderContainer = view.findViewById(R.id.reminderContainer);

        boolean isUpdate = false;
        final Bundle bundle = getArguments();

        if (bundle != null) {
            isUpdate = true;
            newTaskText.setText(bundle.getString("task"));
            selectedDeadline = bundle.getString("deadline", "");
            remindersList = bundle.getStringArrayList("reminders");

            if (selectedDeadline != null && !selectedDeadline.isEmpty()) {
                deadlineTextView.setText("Deadline: " + selectedDeadline);
            }

            if (remindersList != null && !remindersList.isEmpty()) {
                for (String reminder : remindersList) {
                    addReminderView(reminder);
                }
            }
        }

        db = new DatabaseHandler(getActivity());
        db.openDatabase();

        deadlineTextView.setOnClickListener(v -> showDatePickerForDeadline());
        addReminderButton.setOnClickListener(v -> showDateTimePickerForReminder());

        final boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(v -> {
            String text = newTaskText.getText().toString();
            int taskId;

            if (finalIsUpdate) {
                taskId = bundle.getInt("id");
                db.updateTask(taskId, text, selectedDeadline);
                db.updateReminders(taskId, remindersList);
            } else {
                ToDoModel task = new ToDoModel();
                task.setTask(text);
                task.setDeadline(selectedDeadline);
                task.setReminders(remindersList);
                db.insertTask(task);
                taskId = db.getLastInsertedTaskId(); // Fetch the new task ID
            }

            // Schedule alarms after saving the task
            for (String reminder : remindersList) {
                scheduleAlarm(reminder, text, taskId);
            }

            dismiss(); // Dismiss the dialog after scheduling alarms
        });

    }

    private void updateDeadlineCalendar() {
        String[] dateParts = selectedDeadline.split("/");
        deadlineCalendar = Calendar.getInstance();
        deadlineCalendar.set(Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[0])+1);
    }

    private void showDatePickerForDeadline() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.HaloGreenDatePicker,
                (view, year, month, dayOfMonth) -> {
                    selectedDeadline = dayOfMonth + "/" + (month + 1) + "/" + year;
                    deadlineTextView.setText("Deadline: " + selectedDeadline);
                    updateDeadlineCalendar();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setOnShowListener(dialog -> {
            datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
        });
        datePickerDialog.show();
    }

    private void showDateTimePickerForReminder() {
        if (selectedDeadline.isEmpty()) {
            return; // Prevent reminder addition if there's no deadline
        }

        if (deadlineCalendar == null) {
            updateDeadlineCalendar(); // Ensure deadlineCalendar is set
        }

        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.HaloGreenDatePicker,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    // Ensure the selected reminder is BEFORE OR ON the deadline
                    if (selectedDate.after(deadlineCalendar)) {
                        deadlineTextView.setError("Reminder must be before the deadline!");
                        return; // Prevent invalid selection
                    }

                    TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), R.style.HaloGreenDatePicker,
                            (timeView, hourOfDay, minute) -> {
                                String reminder = dayOfMonth + "/" + (month + 1) + "/" + year + " " + hourOfDay + ":" + minute;
                                remindersList.add(reminder);
                                addReminderView(reminder);
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

                    timePickerDialog.setOnShowListener(dialog -> {
                        timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                        timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                    });
                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setOnShowListener(dialog -> {
            datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
        });
        datePickerDialog.show();
    }


    private void addReminderView(String reminderText) {
        LinearLayout reminderLayout = new LinearLayout(requireContext());
        reminderLayout.setOrientation(LinearLayout.HORIZONTAL);
        reminderLayout.setPadding(10, 5, 10, 5);

        TextView reminderTextView = new TextView(requireContext());
        reminderTextView.setText(reminderText);
        reminderTextView.setTextColor(Color.BLACK);
        reminderTextView.setTextSize(16);
        reminderTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button deleteButton = new Button(requireContext());
        deleteButton.setText("X");
        deleteButton.setTextColor(Color.RED);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        deleteButton.setOnClickListener(v -> {
            remindersList.remove(reminderText); // Remove from list
            reminderContainer.removeView(reminderLayout); // Remove from UI
        });

        reminderLayout.addView(reminderTextView);
        reminderLayout.addView(deleteButton);
        reminderContainer.addView(reminderLayout);
    }

    private void scheduleAlarm(String reminderTime, String taskName, int taskId) {
        // Check if task is already marked as completed
        if (db.getTaskStatus(taskId) == 1) return;

        // Generate a unique request code using taskId and reminder time hash
        int requestCode = (taskId * 1000) + reminderTime.hashCode();

        String[] dateTime = reminderTime.split(" ");
        String[] dateParts = dateTime[0].split("/");
        String[] timeParts = dateTime[1].split(":");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        intent.putExtra("taskName", taskName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListener)
            ((DialogCloseListener) activity).handleDialogClose(dialog);
    }
}
