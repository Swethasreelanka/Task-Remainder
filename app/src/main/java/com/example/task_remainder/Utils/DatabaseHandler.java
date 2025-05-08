package com.example.task_remainder.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.task_remainder.Model.ToDoModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int VERSION = 2; // Incremented the version number
    private static final String NAME = "toDoListDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    private static final String DEADLINE = "deadline";
    private static final String REMINDERS = "reminders"; // Store reminders as a comma-separated string

    private static final String CREATE_TODO_TABLE =
            "CREATE TABLE " + TODO_TABLE + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TASK + " TEXT, " +
                    DEADLINE + " TEXT, " +
                    REMINDERS + " TEXT, " +
                    STATUS + " INTEGER)";

    private SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) { // Handle upgrades properly
            db.execSQL("ALTER TABLE " + TODO_TABLE + " ADD COLUMN " + DEADLINE + " TEXT");
            db.execSQL("ALTER TABLE " + TODO_TABLE + " ADD COLUMN " + REMINDERS + " TEXT");
        }
    }

    public void openDatabase() {
        db = this.getWritableDatabase();
    }

    // Insert a new task with deadline and reminders
    public void insertTask(ToDoModel task) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(DEADLINE, task.getDeadline());
        cv.put(REMINDERS, String.join(",", task.getReminders())); // Convert list to string
        cv.put(STATUS, 0);
        db.insert(TODO_TABLE, null, cv);
    }

    @SuppressLint("Range")
    public List<ToDoModel> getAllTasks() {
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = db.query(TODO_TABLE, null, null, null, null, null, null);

        if (cur != null && cur.moveToFirst()) {
            do {
                ToDoModel task = new ToDoModel();
                task.setId(cur.getInt(cur.getColumnIndex(ID)));
                task.setTask(cur.getString(cur.getColumnIndex(TASK)));
                task.setDeadline(cur.getString(cur.getColumnIndex(DEADLINE)));

                String remindersStr = cur.getString(cur.getColumnIndex(REMINDERS));
                List<String> reminders = (remindersStr != null) ? Arrays.asList(remindersStr.split(",")) : new ArrayList<>();
                task.setReminders(reminders);

                task.setStatus(cur.getInt(cur.getColumnIndex(STATUS))); // Ensure status is set properly

                taskList.add(task);
            } while (cur.moveToNext());
        }

        if (cur != null) cur.close();
        return taskList;
    }


    // Update task name and deadline
    public void updateTask(int id, String task, String deadline) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        cv.put(DEADLINE, deadline);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[]{String.valueOf(id)});
    }

    // Update task status
    public void updateStatus(int id, int status) {
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)});
    }


    // Update task reminders
    public void updateReminders(int id, List<String> reminders) {
        ContentValues cv = new ContentValues();
        cv.put(REMINDERS, String.join(",", reminders)); // Convert list to string
        db.update(TODO_TABLE, cv, ID + "= ?", new String[]{String.valueOf(id)});
    }

    // Delete task
    public void deleteTask(int id) {
        db.delete(TODO_TABLE, ID + "= ?", new String[]{String.valueOf(id)});
    }

    public int getTaskStatus(int taskId) {
        Cursor cursor = db.rawQuery("SELECT status FROM " + TODO_TABLE + " WHERE id=?", new String[]{String.valueOf(taskId)});
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(0);
            cursor.close();
            return status;
        }
        cursor.close();
        return 0; // Default to incomplete
    }

    public int getLastInsertedTaskId() {
        Cursor cursor = db.rawQuery("SELECT id FROM " + TODO_TABLE + " ORDER BY id DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return -1; // Return -1 if no tasks exist
    }

    public List<String> getRemindersForTask(int taskId) {
        List<String> reminders = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + REMINDERS + " FROM " + TODO_TABLE + " WHERE " + ID + "=?", new String[]{String.valueOf(taskId)});

        if (cursor.moveToFirst()) {
            String remindersStr = cursor.getString(0);
            if (remindersStr != null && !remindersStr.isEmpty()) {
                reminders = Arrays.asList(remindersStr.split(",")); // Convert comma-separated string to list
            }
        }
        cursor.close();
        return reminders;
    }
}
