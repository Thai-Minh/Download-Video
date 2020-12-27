package com.tapi.downloader.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tapi.downloader.Utils.helper.FileUtils;
import com.tapi.downloader.Utils.helper.SqlString;
import com.tapi.downloader.core.enums.QueueSort;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.constants.CHUNKS;
import com.tapi.downloader.database.constants.TABLES;
import com.tapi.downloader.database.constants.TASKS;
import com.tapi.downloader.database.elements.Chunk;
import com.tapi.downloader.database.elements.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Majid Golshadi on 4/10/2014.
 */
public class TasksDataSource {
    private static final String TAG = "TasksDataSource";

    private SQLiteDatabase database;
    private final ArrayList<Task> tasks = new ArrayList<>();

    public void openDatabase(DatabaseHelper dbHelper) {
        database = dbHelper.getWritableDatabase();
        synchronized (tasks) {
            tasks.clear();
            tasks.addAll(getAllTaskFromDB());
        }
    }

    public long insertTask(Task task) {
        long id = database
                .insert(TABLES.TASKS, null, task.convertToContentValues());

        if (id != -1) {
            synchronized (tasks) {
                tasks.add(task);
            }
        }
        return id;
    }

    public boolean update(Task task) {
        int affectedRow = database
                .update(TABLES.TASKS, task.convertToContentValues(), TASKS.COLUMN_ID + "=" + task.id, null);

        if (affectedRow != 0) {
            synchronized (tasks) {
                int index = tasks.indexOf(task);
                Task old = tasks.remove(index);
                task.percent = old.percent;
                tasks.add(index, task);
            }
            return true;
        }

        return false;
    }

    public List<Task> getTasksInState(int state) {
        List<Task> taskResults = new ArrayList<Task>();
        synchronized (tasks) {
            for (Task task : tasks) {
                if (task.state == state)
                    taskResults.add(task);
            }
        }

        return taskResults;
    }

    // New
    private ArrayList<Task> getAllTaskFromDB() {
        ArrayList<Task> tasks = new ArrayList<>();
        Cursor cr = database.query(TABLES.TASKS, null, null, null, null, null, null);

        if (cr != null) {
            cr.moveToFirst();

            while (!cr.isAfterLast()) {
                Task task = new Task();
                List<Chunk> chunks = chunksRelatedTask(task.id);
                task.percent = calculatePercent(task, chunks);
                task.cursorToTask(cr);
                tasks.add(task);
                cr.moveToNext();
            }

            cr.close();
        }

        for (Task task: tasks) {
            if(task.audioTaskId != 0) {
                long audioLength=0;
                Task audio = getTaskInfo(tasks, task.audioTaskId);
                long videoLength = ((task.percent * task.size) / 100);
                if (audio != null) {
                    audioLength = ((audio.percent * audio.size) / 100);
                    task.percentCommon = (int) ((float) (videoLength + audioLength) / (task.size + audio.size) * 100);
                }else {
                    task.percentCommon = (int) ((float) (videoLength) / (task.size) * 100);
                }
            }
        }

        return tasks;
    }

    public List<Chunk> chunksRelatedTask(int taskID){
        List<Chunk> chunks = new ArrayList<Chunk>();
        String query = "SELECT * FROM "+ TABLES.CHUNKS+" WHERE "+ CHUNKS.COLUMN_TASK_ID+" == "+taskID;
        Cursor cr = database.rawQuery(query, null);

        if (cr.moveToFirst()){
            do{
                Chunk chunk = new Chunk(taskID);
                chunk.cursorToChunk(cr);
                chunks.add(chunk);
            } while (cr.moveToNext());
        }

        cr.close();
        return chunks;
    }

    private int calculatePercent(Task task, List<Chunk> chunks){
        // initialize report
        double report = 0;

        // if download not completed we have chunks
        if (task.state != TaskStates.DOWNLOAD_FINISHED) {
            int sum = 0;
            for (Chunk chunk : chunks){
                sum += FileUtils.size(task.save_address, String.valueOf(chunk.id));
            }

            if (task.size > 0) {
                report = ((float)sum / task.size * 100);
            }
        } else {
            report = 100;
        }
        return (int) report;
    }

    public ArrayList<Task> getAllTasks() {
        return tasks;
    }

//    public List<Task> getUnnotifiedCompleted() {
//        List<Task> completedTasks = new ArrayList<Task>();
//
//        // SQLite does not have a separate Boolean storage class. Instead, Boolean values are stored as integers 0 (false) and 1 (true).
//        String query = "SELECT * FROM " + TABLES.TASKS + " WHERE " + TASKS.COLUMN_NOTIFY + " != " + SqlString.Int(1);
//        Cursor cr = database.rawQuery(query, null);
//
//        if (cr != null) {
//            cr.moveToFirst();
//
//            while (!cr.isAfterLast()) {
//                Task task = new Task();
//                task.cursorToTask(cr);
//                completedTasks.add(task);
//
//                cr.moveToNext();
//            }
//
//            cr.close();
//        }
//
//        return completedTasks;
//    }

    public List<Task> getUnCompletedTasks(int sortType) {
        List<Task> unCompleted = new ArrayList<Task>();
        String query = "SELECT * FROM " + TABLES.TASKS
                + " WHERE " + TASKS.COLUMN_STATE + "!=" + SqlString.Int(TaskStates.END);
        switch (sortType) {
            case QueueSort.HighPriority:
                query += " AND " + TASKS.COLUMN_PRIORITY + "=" + SqlString.Int(1);
                break;
            case QueueSort.LowPriority:
                query += " AND " + TASKS.COLUMN_PRIORITY + "=" + SqlString.Int(0);
                break;
            case QueueSort.oldestFirst:
                query += " ORDER BY " + TASKS.COLUMN_ID + " ASC";
                break;
            case QueueSort.earlierFirst:
                query += " ORDER BY " + TASKS.COLUMN_ID + " DESC";
                break;
            case QueueSort.HighToLowPriority:
                query += " ORDER BY " + TASKS.COLUMN_PRIORITY + " ASC";
                break;
            case QueueSort.LowToHighPriority:
                query += " ORDER BY " + TASKS.COLUMN_PRIORITY + " DESC";
                break;

        }

        Cursor cr = database.rawQuery(query, null);

        if (cr != null) {
            cr.moveToFirst();

            while (!cr.isAfterLast()) {
                Task task = new Task();
                task.cursorToTask(cr);
                unCompleted.add(task);

                cr.moveToNext();
            }

            cr.close();
        }

        return unCompleted;
    }

    private Task getTaskInfo(ArrayList<Task> tasks, int id) {
        for (Task task: tasks) {
            if (task.id == id)
                return task;
        }

        return null;
    }

    public Task getTaskInfo(int id) {
        synchronized (tasks) {
            return getTaskInfo(tasks, id);
        }
    }

    public Task getTaskInfoWithName(String name) {
        Cursor cr = database.rawQuery("SELECT * FROM " + TABLES.TASKS + " WHERE " + TASKS.COLUMN_NAME + "=?", new String[]{name});

        Task task = new Task();
        if (cr != null && cr.moveToFirst()) {
            task.cursorToTask(cr);
            cr.close();
        }

        return task;
    }

    public boolean delete(int taskID) {
        int affectedRow = database
                .delete(TABLES.TASKS, TASKS.COLUMN_ID + "=" + SqlString.Int(taskID), null);

        if (affectedRow != 0) {
            Task task = getTaskById(taskID);
            if (task != null) {
                synchronized (tasks) {
                    tasks.remove(task);
                }
            }
            return true;
        }

        return false;
    }

    // new
    private Task getTaskById(int taskId) {
        synchronized (tasks){
            for (Task task : tasks)
                if (task.id == taskId)
                    return task;
        }
        return null;
    }

    public boolean containsTask(String name) {
        boolean result = false;
        Cursor cr = database.rawQuery("SELECT * FROM " + TABLES.TASKS + " WHERE " + TASKS.COLUMN_NAME + "=?", new String[]{name});

        if (cr != null && cr.getCount() != 0) {
            result = true;
            cr.close();
        }

        return result;
    }

    public boolean checkUnNotifiedTasks() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASKS.COLUMN_NOTIFY, 1);
        int affectedRows = database.update(TABLES.TASKS, contentValues, TASKS.COLUMN_NOTIFY + "=" + SqlString.Int(0), null);

        return affectedRows > 0;
    }

    public void close() {
        database.close();
    }
}
