package com.tapi.downloader.database.elements;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tapi.downloader.database.constants.TASKS;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Majid Golshadi on 4/10/2014.
 * <p>
 * "CREATE TABLE "+ TABLES.TASKS + " ("
 * + TASKS.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 * + TASKS.COLUMN_NAME + " CHAR( 128 ) NOT NULL, "
 * + TASKS.COLUMN_SIZE + " INTEGER, "
 * + TASKS.COLUMN_STATE + " INT( 3 ), "
 * + TASKS.COLUMN_URL + " CHAR( 256 ), "
 * + TASKS.COLUMN_PERCENT + " INT( 3 ), "
 * + TASKS.COLUMN_CHUNKS + " BOOLEAN, "
 * + TASKS.COLUMN_NOTIFY + " BOOLEAN, "
 * + TASKS.COLUMN_SAVE_ADDRESS + " CHAR( 256 ),"
 * + TASKS.COLUMN_EXTENSION + " CHAR( 32 )"
 * + " ); "
 */
public class Task implements Serializable {

    private static final String TAG = "Task";

    public int id;
    public long idMerge;
    public int videoTaskId;
    public int audioTaskId;
    public String name;
    public long size;
    public long commonSize;
    public int state;
    public int commonState;
    public String url;
    public int percent;
    public int percentCommon;
    public int chunks;
    public boolean notify;
    public boolean resumable;
    public String save_address;
    public String extension;
    public boolean priority;
    public String thumbnailUrl;
    public long duration;
    public long completeTime;
    public String idServer;

    public Task() {
        this.id = 0;
        this.idMerge = -1;
        this.videoTaskId = 0;
        this.audioTaskId = 0;
        this.name = null;
        this.size = 0;
        this.commonSize = 0;
        this.state = 0;
        this.commonState = 0;
        this.url = null;
        this.thumbnailUrl = null; // new
        this.duration = 0; // new
        this.completeTime = 0; // new
        this.percent = 0;
        this.chunks = 0;
        this.notify = true;
        this.resumable = true;
        this.save_address = null;
        this.extension = null;
        this.priority = false;  // low priority
        this.idServer = null;
    }

    public Task(long size, String name, String url, String thumbnailUrl, long duration,
                int state, int chunks, String sdCardFolderAddress,
                boolean priority, String idServer) {
        this.id = 0;
        this.videoTaskId = 0;
        this.audioTaskId = 0;
        this.name = name;
        this.size = size;
        this.state = state;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.completeTime = 0;
        this.percent = 0;
        this.chunks = chunks;
        this.notify = true;
        this.resumable = true;
        this.save_address = sdCardFolderAddress;
        this.extension = "";
        this.priority = priority;
        this.idServer = idServer;
        this.idMerge = -1;
    }

    public ContentValues convertToContentValues() {
        ContentValues contentValues = new ContentValues();

        if (id != 0)
            contentValues.put(TASKS.COLUMN_ID, id);

        contentValues.put(TASKS.COLUMN_VIDEO_TASK_ID, videoTaskId);
        contentValues.put(TASKS.COLUMN_AUDIO_TASK_ID, audioTaskId);
        contentValues.put(TASKS.COLUMN_NAME, name);
        contentValues.put(TASKS.COLUMN_SIZE, size);
        contentValues.put(TASKS.COLUMN_COMMON_SIZE, commonSize);
        contentValues.put(TASKS.COLUMN_STATE, state);
        contentValues.put(TASKS.COLUMN_COMMON_STATE, commonState);
        contentValues.put(TASKS.COLUMN_URL, url);
        contentValues.put(TASKS.COLUMN_THUMBNAIL_URL, thumbnailUrl);
        contentValues.put(TASKS.COLUMN_DURATION, duration);
        contentValues.put(TASKS.COLUMN_COMPLETE_TIME, completeTime);
        contentValues.put(TASKS.COLUMN_PERCENT, percent);
        contentValues.put(TASKS.COLUMN_CHUNKS, chunks);
        contentValues.put(TASKS.COLUMN_NOTIFY, notify);
        contentValues.put(TASKS.COLUMN_RESUMABLE, resumable);
        contentValues.put(TASKS.COLUMN_SAVE_ADDRESS, save_address);
        contentValues.put(TASKS.COLUMN_EXTENSION, extension);
        contentValues.put(TASKS.COLUMN_PRIORITY, priority);
        contentValues.put(TASKS.COLUMN_SERVER_ID, idServer);
        contentValues.put(TASKS.COLUMN_PERCENT_COMMON, percentCommon);

//        Log.e(TAG, "convertToContentValues: "+ percentCommon);

        return contentValues;
    }

    public void cursorToTask(Cursor cr) {
        id = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_ID));
        videoTaskId = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_VIDEO_TASK_ID));
        audioTaskId = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_AUDIO_TASK_ID));
        name = cr.getString(
                cr.getColumnIndex(TASKS.COLUMN_NAME));
        size = cr.getLong(
                cr.getColumnIndex(TASKS.COLUMN_SIZE));
        commonSize = cr.getLong(
                cr.getColumnIndex(TASKS.COLUMN_COMMON_SIZE));
        state = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_STATE));
        commonState = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_COMMON_SIZE));
        url = cr.getString(
                cr.getColumnIndex(TASKS.COLUMN_URL));
        thumbnailUrl = cr.getString(
                cr.getColumnIndex(TASKS.COLUMN_THUMBNAIL_URL));
        duration = cr.getLong(
                cr.getColumnIndex(TASKS.COLUMN_DURATION));
        completeTime = cr.getLong(
                cr.getColumnIndex(TASKS.COLUMN_COMPLETE_TIME));
        percent = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_PERCENT));
        chunks = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_CHUNKS));
        notify = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_NOTIFY)) > 0;
        resumable = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_RESUMABLE)) > 0;
        save_address = cr.getString(
                cr.getColumnIndex(TASKS.COLUMN_SAVE_ADDRESS));
        extension = cr.getString(
                cr.getColumnIndex(TASKS.COLUMN_EXTENSION));
        priority = cr.getInt(
                cr.getColumnIndex(TASKS.COLUMN_PRIORITY)) > 0;
        idServer = cr.getString(cr.getColumnIndex(TASKS.COLUMN_SERVER_ID));

        percentCommon = cr.getInt(cr.getColumnIndex(TASKS.COLUMN_PERCENT_COMMON));
    }

    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id)
                    .put("videoTaskId", videoTaskId)
                    .put("audioTaskId", audioTaskId)
                    .put("name", name)
                    .put("size", size)
                    .put("state", state)
                    .put("url", url)
                    .put("thumbnail_url", thumbnailUrl)
                    .put("duration", duration)
                    .put("completeTime", completeTime)
                    .put("percent", percent)
                    .put("chunks", chunks)
                    .put("notify", notify)
                    .put("resumable", resumable)
                    .put("save_address", save_address)
                    .put("extension", extension)
                    .put("priority", priority);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public String getFullName() {
        return extension.isEmpty() ? name : name.concat(".").concat(extension);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Task)
            return ((Task) obj).id == id;

        return super.equals(obj);
    }

    public static final Comparator<Task> SORT_ID = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            if (o2.id > o1.id) {
                return 1;
            } else if (o2.id < o1.id) {
                return -1;
            } else {
                return 0;
            }
        }
    };
}
