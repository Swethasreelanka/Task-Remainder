package com.example.task_remainder.Model;

import java.util.ArrayList;
import java.util.List;

public class ToDoModel {
    private int id;
    private String task;
    private String deadline;
    private List<String> reminders;
    private int status; // Added status field

    // Default Constructor
    public ToDoModel() {
        this.reminders = new ArrayList<>();
    }

    // Parameterized Constructor
    public ToDoModel(int id, String task, String deadline, List<String> reminders, int status) {
        this.id = id;
        this.task = task;
        this.deadline = deadline;
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public List<String> getReminders() {
        return reminders;
    }

    public void setReminders(List<String> reminders) {
        this.reminders = reminders;
    }

    public void addReminder(String reminder) {
        reminders.add(reminder);
    }

    public void removeReminder(String reminder) {
        reminders.remove(reminder);
    }

    // New methods for status
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
