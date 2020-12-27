package com.tapi.downloadsocialvideo.function.downloader;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Level;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.downloader.core.DownloadManagerPro;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloader.report.listener.DownloadManagerListener;
import com.tapi.downloadsocialvideo.function.downloader.util.DownloaderUtils;
import com.tapi.downloadsocialvideo.service.NotificationDLManager;
import com.tapi.downloadsocialvideo.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class DownloadManagerImpl implements DownloadManagerListener, IDownloadManager {
    private static final String TAG = "DownloadManagerImpl";
    public static final String DOWNLOAD_DIRECTORY = "Downloads/Download/";

    private Context mContext;

    private static final String PATH_FOLDER = Environment.getExternalStorageDirectory() + File.separator + DOWNLOAD_DIRECTORY;
    private static final int MAX_CHUNKS = 4;
    private static final boolean OVERWRITE = false;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private ArrayList<OnDownloadListener> onDownloadListeners = new ArrayList<>();
    private DownloadManagerPro downloadManager;

    private NotificationDLManager mNotificationDLManager;

    private ArrayList<Task> mTasks;

    public DownloadManagerImpl(Context context) {
        mContext = context;
        mNotificationDLManager = new NotificationDLManager(context);
        createFolderDownload();
        Config.setLogLevel(Level.AV_LOG_INFO);
        // init
        downloadManager = new DownloadManagerPro(context,this);
        downloadManager.init(DOWNLOAD_DIRECTORY, MAX_CHUNKS, this);
        mTasks = downloadManager.getDownloadTasks();
    }

    private void createFolderDownload() {
        File folder = new File(PATH_FOLDER);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
    }

    @Override
    public int startDownload(Video video, DownloadLink downloadLink) {
        String audioLink = downloadLink.getAudioLink();
        if (!TextUtils.isEmpty(audioLink)) { // multi task
            return startVideoWithAudio(video, downloadLink);
        } else { // normal task
            return startNormal(video, downloadLink);
        }
    }

    private int startNormal(Video video, DownloadLink downloadLink) {
        int resolution = downloadLink.getResolution();
        String saveName = Utils.getSaveNameVideo(video, resolution);
        int downloadId = downloadManager.addTask(saveName, downloadLink.getLink(), video.getThumbnail(),
                video.getDuration(), OVERWRITE, false,video.getIdVideo());

        try {
            downloadManager.startDownload(downloadId);

            postOnDownloadTaskListChange();

            // Show notification
            showNotification(saveName, downloadId);
        } catch (IOException e) { //  task is in downloading state
            e.printStackTrace();
            Log.d(TAG, "startDownload: " + e.toString());
        }
        return downloadId;
    }

    private int startVideoWithAudio(Video video, DownloadLink downloadLink) {
        int resolution = downloadLink.getResolution();
        String saveName = Utils.getSaveNameVideo(video, resolution);
        int downloadId = downloadManager.addTask(saveName,
                downloadLink.getLink(), downloadLink.getAudioLink(), video.getThumbnail(), video.getDuration(), OVERWRITE, false,video.getIdVideo());

        Task downloadTask = downloadManager.getTaskInfo(downloadId);
        int audioTaskId = downloadTask.audioTaskId;
        try {
            downloadManager.startDownload(downloadId); // video task
            downloadManager.startDownload(audioTaskId); // audio task

            // Show notification
            showNotification(saveName, downloadId);

            postOnDownloadTaskListChange();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "startDownload multi: " + e.toString());
        }
        return downloadId;
    }

    @Override
    public void pauseDownload(long downloadId) {
        pauseOrCancelDownload(downloadId, false);
    }

    public void pauseOrCancelDownload(long downloadId, boolean cancel) {
        Task downloadTask = downloadManager.getTaskInfo((int) downloadId);
        int audioTaskId = downloadTask.audioTaskId;
        if (audioTaskId != 0) {
            Task audioTask = downloadManager.getTaskInfo(audioTaskId);
            if (audioTask != null && audioTask.state != TaskStates.DOWNLOAD_FINISHED && audioTask.state != TaskStates.END) {
                Log.e("phi.hd", "pauseDownload audioTaskId: " + audioTaskId);
                if (cancel)
                    downloadManager.cancelDownload(audioTaskId);
                else
                    downloadManager.pauseDownload(audioTaskId);
            }
        }

        if (downloadTask.state != TaskStates.DOWNLOAD_FINISHED && downloadTask.state != TaskStates.END) {
            Log.e("phi.hd", "pauseDownload videoTaskId: " + downloadId);
            if (cancel)
                downloadManager.cancelDownload((int) downloadId);
            else
                downloadManager.pauseDownload((int) downloadId);
        }
    }

    @Override
    public void resumeDownload(long downloadId) {
        Task task = downloadManager.getTaskInfo((int) downloadId);
        int audioTaskId = task.audioTaskId;
        if (audioTaskId != 0) {
            resumeDownloadWithId(audioTaskId);
        }

        resumeDownloadWithId(downloadId);
    }

    private void resumeDownloadWithId(long downloadId) {
        try {
            downloadManager.startDownload((int) downloadId);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "resumeDownload: " + e.toString());
        }
    }

    @Override
    public void cancelDownload(long downloadId) {
        Log.e("phi.hd", "cancelDownload: " + downloadId);
        pauseOrCancelDownload(downloadId, true);
        deleteDownload(downloadId);
    }

    @Override
    public boolean deleteDownload(long downloadId) {
        Task downloadTask = downloadManager.getTaskInfo((int) downloadId);
        int audioTaskId = downloadTask.audioTaskId;
        Log.e(TAG, "deleteDownload audioTaskId: " + audioTaskId);
        if (audioTaskId != 0) {
            boolean delete = downloadManager.delete(audioTaskId, true);
            Log.e(TAG, "deleteDownload audio: "+ delete);
            if (downloadTask.idMerge != -1) {
                FFmpeg.cancel(downloadTask.idMerge);
            }
        }

        boolean delete = downloadManager.delete((int) downloadId, true);
        Log.e(TAG, "deleteDownload delete: " + delete);
        updateDeleteState(downloadTask);
        return delete;
    }

    private void updateDeleteState(Task downloadTask) {
        postOnDownloadTaskListChange();
        cancelNotification(downloadTask);
    }

    @Override
    public void addOnDownloadListener(OnDownloadListener onDownloadListener) {
        if (onDownloadListener != null && !onDownloadListeners.contains(onDownloadListener)) {
            onDownloadListeners.add(onDownloadListener);
            onDownloadListener.onDownloadTaskListChange(mTasks);
        }
    }

    @Override
    public void removeOnDownloadListener(OnDownloadListener onDownloadListener) {
        onDownloadListeners.remove(onDownloadListener);
    }

    public void onClearAllListener() {
        onDownloadListeners.clear();
        Log.e(TAG, "onClearAllListener: " + onDownloadListeners.size());
    }

    @Override
    public void onDownloadStateChanged(Task task) {
        if (task != null && mTasks.contains(task)) {
            int audioTaskId = task.audioTaskId;
            int videoTaskId = task.videoTaskId;
            if (audioTaskId == 0 && videoTaskId == 0) { // normal task
                task.commonState = -1;
                task.commonSize = -1;
                postOnDownloadStateChange(task);

                // Update notification
                updateStateNotification(task);

                // Cancel notification
                if (task.state == TaskStates.END) {
                    cancelNotification(task);
                    DownloaderUtils.addFileToMediaStore(mContext, task.name, task.save_address.concat("/".concat(task.getFullName())));
                }
            } else { // multi task audio + video
                Task refTask, commonTask;
                if (audioTaskId != 0) { // video task
                    refTask = downloadManager.getTaskInfo(audioTaskId);
                    commonTask = task;
                } else { // audio task
                    refTask = downloadManager.getTaskInfo(videoTaskId);
                    commonTask = refTask;
                }
                if (refTask != null) {
                    int commonState = getCommonState(task.state, refTask.state);
                    commonTask.commonState = commonState;
                    downloadManager.updateTask(commonTask); // update db

                    // Cancel notification and Merge file
                    if (commonState == TaskStates.END) {
                        commonTask.commonState = TaskStates.MERGE;
                        downloadManager.updateTask(commonTask);
                        postOnDownloadStateChange(commonTask);
                        updateStateNotification(commonTask);
//                    cancelNotification(commonTask);
                        if (commonTask.extension.contains("mp4")) {
                            mergeFile(commonTask);
                        } else {
                            mergeFileFFmpeg(commonTask);
                        }
                    } else {
                        postOnDownloadStateChange(commonTask);

                        // Update notification
                        updateStateNotification(commonTask);
                    }
                }

            }
        }
    }

    public void mergeFileFFmpeg(Task commonTask) {
        String audioPath = changeTypeFile(commonTask);
        convertAudioAndMerge(commonTask, audioPath);
    }

    private String changeTypeFile(Task commonTask) {
        Task audioTask = downloadManager.getTaskInfo(commonTask.audioTaskId);
        File from = new File(PATH_FOLDER, audioTask.getFullName());
        String reTypeTo = audioTask.name.concat("." + (audioTask.extension = "aac"));
        File to = new File(PATH_FOLDER, reTypeTo.replaceAll(" ", ""));
        boolean b = from.renameTo(to);
        if (b) {
            downloadManager.updateTask(commonTask);
        }
        return to.getAbsolutePath().replaceAll(" ", "");
    }

    @Override
    public void onDownloadProcess(long taskId, double percent, long downloadedLength) {
        Task downloadTask = downloadManager.getTaskInfo((int) taskId);
        if (downloadTask != null) {
            int audioTaskId = downloadTask.audioTaskId;
            int videoTaskId = downloadTask.videoTaskId;
            if (audioTaskId == 0 && videoTaskId == 0) { // normal task
                downloadTask.percentCommon = -1;
                postOnDownloadProgressChange(downloadTask);

                Log.e(TAG, "onDownloadProcess: "+downloadTask.percentCommon );
                downloadManager.updateTask(downloadTask);
                // Update notification
                updateProgressNotification(downloadTask);
            } else {
                long refFileDownloaded;
                Task refTask, commonTask;
                if (audioTaskId != 0) { // video task
                    refTask = downloadManager.getTaskInfo(audioTaskId);
                    commonTask = downloadTask;
                    Log.d("phi,hd", "Video (" + downloadTask.percent + ", " + downloadTask.size + ") , Audio (" + refTask.percent + ", " + refTask.size + ")");
                } else { // audio task
                    refTask = downloadManager.getTaskInfo(videoTaskId);
                    commonTask = refTask;
                    Log.d("phi,hd", "Video (" + refTask.percent + ", " + refTask.size + ") , Audio (" + downloadTask.percent + ", " + downloadTask.size + ")");
                }

                refFileDownloaded = ((refTask.percent * refTask.size) / 100);

                int commonPercent = (int) ((float) (downloadedLength + refFileDownloaded) / (refTask.size + downloadTask.size) * 100);
                commonTask.percentCommon = commonPercent;
                Log.e(TAG, "onDownloadProcess: "+commonTask.percentCommon);
                downloadManager.updateTask(commonTask);
                Log.d("phi,hd", "------- percentCommon " + commonPercent);

                postOnDownloadProgressChange(commonTask);

                // Update notification
                updateProgressNotification(commonTask);
            }
        }
    }

    public void mergeFile(final Task commonTask) {
        int videoTaskId = commonTask.id;
        int audioTaskId = commonTask.audioTaskId;
        Task videoTask = downloadManager.getTaskInfo(videoTaskId);
        Task audioTask = downloadManager.getTaskInfo(audioTaskId);
        String videoPath = videoTask.save_address.concat(File.separator).concat(videoTask.name).concat(".").concat(videoTask.extension);
        String audioPath = audioTask.save_address.concat(File.separator).concat(audioTask.name).concat(".").concat(audioTask.extension);

        String changeVideoPath = changeVideoMp4Path(videoPath, videoTask);
        String changeAudioPath = changeAudioMp4Path(audioPath, audioTask);
        String outputPath = PATH_FOLDER.concat(videoTask.name.replaceAll(" ", "").concat(String.valueOf(System.currentTimeMillis())).concat("." + videoTask.extension));
        commonTask.idMerge = FFmpeg.executeAsync("-y -i " + changeVideoPath + " " + "-i " + changeAudioPath + " " + "-c:v copy -c:a copy " + outputPath, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    commonTask.commonSize = renameOutputVideo(commonTask, outputPath);
                    commonTask.state = TaskStates.END;
                    downloadManager.updateTask(commonTask);
                    cancelNotification(commonTask);
                    postOnDownloadStateChange(commonTask);
                    downloadManager.mergeVideoSuccess(commonTask);
                    DownloaderUtils.addFileToMediaStore(mContext, commonTask.name, videoPath);
                    deleteFile(changeAudioPath);
                    deleteFile(changeVideoPath);
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    deleteFile(audioPath);
                    deleteFile(videoPath);
                    deleteFile(changeAudioPath);
                    deleteFile(changeVideoPath);
                } else {
                    Config.printLastCommandOutput(Log.ERROR);
                }
            }
        });
    }

    private String changeAudioMp4Path(String audioPath, Task audioTask) {
        File fileAudio = new File(audioPath);
        File fileChangeAudio = new File(PATH_FOLDER, audioTask.name.replaceAll(" ", "").concat("." + audioTask.extension));
        if (fileAudio.exists()) {
            boolean b = fileAudio.renameTo(fileChangeAudio);
            if (b)
                return fileChangeAudio.getAbsolutePath();
        } else if (fileChangeAudio.exists()) {
            return fileChangeAudio.getAbsolutePath();
        }
        return audioPath;
    }

    private String changeVideoMp4Path(String videoPath, Task task) {
        File fileVideo = new File(videoPath);
        File fileChange = new File(PATH_FOLDER, task.name.replace(" ", "").concat("." + (task.extension)));
        if (fileVideo.exists()) {
            boolean b = fileVideo.renameTo(fileChange);
            if (b)
                return fileChange.getAbsolutePath();
        } else if (fileChange.exists()) {
            return fileChange.getAbsolutePath();
        }
        return videoPath;
    }

    private int getCommonState(int stateOne, int stateTwo) {
        return Math.min(stateOne, stateTwo);
    }

    public int getNumberTaskProgress() {
        List<Task> downloadTasks = downloadManager.getDownloadTasks();
        int count = 0;
        for (Task downloadTask : downloadTasks) {
            int downloadState = downloadTask.state;
            if (downloadTask.videoTaskId == 0 && downloadState != TaskStates.END)
                count++;
        }
        return count;
    }

    // Show notification or notify start foreground service
    public void showNotification(String title, int notificationId) {
        mNotificationDLManager.showNotification(title, notificationId);
    }

    // Cancel notification or notify stop foreground service
    private void cancelNotification(Task downloadTask) {
        mNotificationDLManager.cancelNotification(downloadTask.id);
    }

    private void updateProgressNotification(Task downloadTask) {
        int progress = downloadTask.percentCommon > -1 ? downloadTask.percentCommon : downloadTask.percent;
        String progressStr = String.format(Locale.US, "%d%%", progress);
        mNotificationDLManager.updateNotification(downloadTask.id, downloadTask.getFullName(), progressStr);
    }

    private void updateStateNotification(Task downloadTask) {
        int state = downloadTask.commonState > -1 ? downloadTask.commonState : downloadTask.state;
        mNotificationDLManager.updateNotification(downloadTask.id, downloadTask.getFullName(), DownloaderUtils.getStateString(state));
    }

    private void postOnDownloadTaskListChange() {
        Log.d("postOnDownloadTaskList", "postOnDownloadTaskListChange: " + mTasks.size());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnDownloadListener listener : onDownloadListeners)
                    listener.onDownloadTaskListChange(mTasks);
            }
        });
    }

    private void postOnDownloadStateChange(final Task downloadTask) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("postOnDownloadState", "postOnDownloadStateChange: " + downloadTask.id + ", state: " + downloadTask.state);
                for (OnDownloadListener listener : onDownloadListeners)
                    listener.onDownloadStateChange(downloadTask);
            }
        });
    }

    private void postOnDownloadProgressChange(final Task downloadTask) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG, "onDownloadProcess: " + downloadTask.percent);
                for (OnDownloadListener listener : onDownloadListeners)
                    listener.onDownloadProgressChange(downloadTask);
            }
        });
    }


    public void convertAudioAndMerge(Task task, String audioPath) {
        File audioFile = new File(audioPath);
        if (audioFile.exists()) {
            String oggPath = PATH_FOLDER + task.name.replaceAll(" ", "").concat("." + (task.extension = "ogg"));
            deleteFile(oggPath);
            task.idMerge = FFmpeg.executeAsync("-i " + audioPath + " " + oggPath, new ExecuteCallback() {
                @Override
                public void apply(long executionId, int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        mergeVideo(audioPath, oggPath, task);
                        Log.e(Config.TAG, "Command execution completed successfully.");
                    } else if (returnCode == RETURN_CODE_CANCEL) {
                        deleteFile(PATH_FOLDER + task.name.concat("." + (task.extension = "webm")));
                        deleteFile(oggPath);
                        deleteFile(audioPath);
                        Log.e(Config.TAG, "Command execution cancelled by user.");
                    } else {
                        Log.e(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", returnCode));
                        Config.printLastCommandOutput(Log.INFO);
                    }
                }
            });
        }
    }

    public void mergeVideo(String audioPath, String oggPath, Task task) {
        String videopath = renameVideoFile(task);
        if (videopath != null) {
            String outputPath = PATH_FOLDER + task.name.replaceAll(" ", "").concat(String.valueOf(System.currentTimeMillis())).concat("." + (task.extension = "webm"));
            task.idMerge = FFmpeg.executeAsync("-i " + oggPath + " -i " + videopath + " -codec copy -shortest " + outputPath, new ExecuteCallback() {
                @Override
                public void apply(long executionId, int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        task.commonSize = renameOutputVideo(task, outputPath);
                        task.state = TaskStates.END;
                        downloadManager.updateTask(task);
                        cancelNotification(task);
                        postOnDownloadStateChange(task);
                        downloadManager.mergeVideoSuccess(task);
                        DownloaderUtils.addFileToMediaStore(mContext, task.name, PATH_FOLDER.concat(task.getFullName()));
                        deleteFile(audioPath);
                        deleteFile(oggPath);
                        deleteFile(videopath);
                        Log.e(Config.TAG, "mergeVideo Command execution completed successfully.");
                    } else if (returnCode == RETURN_CODE_CANCEL) {
                        deleteFile(outputPath);
                        deleteFile(oggPath);
                        deleteFile(videopath);
                        deleteFile(audioPath);
                        Log.e(Config.TAG, "mergeVideo Command execution cancelled by user.");
                    } else {
                        Log.e(Config.TAG, String.format("mergeVideo Command execution failed with rc=%d and the output below.", returnCode));
                        Config.printLastCommandOutput(Log.INFO);
                    }
                }
            });
        }
    }

    public String renameVideoFile(Task task) {
        boolean b = false;
        File from = new File(PATH_FOLDER, task.name.concat("." + (task.extension = "webm")));
        File to = new File(PATH_FOLDER, task.name.replace(" ", "").concat("." + task.extension));
        if (from.exists()) {
            b = from.renameTo(to);
        }
        return b ? to.getAbsolutePath() : null;
    }

    private long renameOutputVideo(Task task, String outputPath) {
        File fromVideo = new File(outputPath);
        File toVideo = new File(PATH_FOLDER, task.getFullName());
        boolean b = fromVideo.renameTo(toVideo);
        return fromVideo.length();
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean delete = file.delete();
        }
    }
}
