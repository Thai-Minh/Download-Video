package com.tapi.downloader.report;

import com.tapi.downloader.Utils.helper.FileUtils;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Chunk;
import com.tapi.downloader.database.elements.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Majid Golshadi on 4/10/2014.
 */
public class ReportStructure {

    public int id;
    public int videoTaskId;
    public int audioTaskId;
    public String name;
    public int state;
    public String url;
    public String thumbnailUrl;
    public long duration;
    public long fileSize;
    public boolean resumable;
    public String type;
    public int chunks;
    public double percent;
    public long downloadLength;
    public String saveAddress;
    public boolean priority;

    public long setDownloadLength(long downloadedLength){
        return downloadLength += downloadedLength;
    }

    public long getTotalSize(){
        return fileSize;
    }

    public boolean isResumable(){
        return resumable;
    }

    public ReportStructure setObjectValues(Task task, List<Chunk> taskChunks){
        this.id = task.id;
        this.videoTaskId = task.videoTaskId;
        this.audioTaskId = task.audioTaskId;
        this.name = task.name;
        this.state = task.state;
        this.resumable = task.resumable;
        this.url = task.url;
        this.thumbnailUrl = task.thumbnailUrl;
        this.duration = task.duration;
        this.fileSize = task.size;
        this.type = task.extension;
        this.chunks = task.chunks;
        this.priority = task.priority;
        this.saveAddress = task.save_address+"/"+task.name+"."+task.extension;

        this.percent = calculatePercent(task, taskChunks);

        return this;
    }

    /** calculate download percent from compare chunks size with real file size **/
    private double calculatePercent(Task task, List<Chunk> chunks){
    	// initialize report
    	double report = 0;
    	
    	// if download not completed we have chunks 
    	if (task.state != TaskStates.DOWNLOAD_FINISHED) {
	        int sum = 0;    
	        for (Chunk chunk : chunks){
	            this.downloadLength += FileUtils.size(task.save_address, String.valueOf(chunk.id));
	        }
	
	        if (task.size > 0) {
	            report = ((float)downloadLength / task.size * 100);
	        }   
    	} else {
    		this.downloadLength = task.size;
    		report = 100;
    	}
    	
    	return report;
    }


    public JSONObject toJsonObject(){
        JSONObject json = new JSONObject();
        try {
            return json.put("token", String.valueOf(id))
                    .put("videoTaskId", videoTaskId)
                    .put("audioTaskId", audioTaskId)
                    .put("name", name)
                    .put("state", state)
                    .put("resumable", resumable)
                    .put("fileSize", fileSize)
                    .put("url", url)
                    .put("thumbnailUrl", thumbnailUrl)
                    .put("duration", duration)
                    .put("type", type)
                    .put("chunks", chunks)
                    .put("percent", percent)
                    .put("downloadLength", downloadLength)
                    .put("saveAddress", saveAddress)
                    .put("priority", priority);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
