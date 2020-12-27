package com.tapi.downloader.core.chunkWorker;

import android.util.Log;

import com.tapi.downloader.Utils.helper.FileUtils;
import com.tapi.downloader.database.elements.Chunk;
import com.tapi.downloader.database.elements.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Random;

/**
 * Created by Majid Golshadi on 4/14/2014.
 */
public class AsyncWorker extends Thread {

    private final int BUFFER_SIZE = 1024;

    private final Task task;
    private final Chunk chunk;
    private final Moderator observer;
    private byte[] buffer;
    private ConnectionWatchDog watchDog;

    int i = 0;
    private boolean stop = false;
    private boolean isClear = false;

    private int current = new Random().nextInt(1000);

    public AsyncWorker(Task task, Chunk chunk, Moderator moderator) {
        buffer = new byte[BUFFER_SIZE];

        this.task = task;
        this.chunk = chunk;
        this.observer = moderator;

        String fullName = task.getFullName();
        setName(current + ".A-" + fullName.substring(fullName.length() - 9, fullName.length() - 4));
    }


    @Override
	public void run() {
        try {
        	
            URL url = new URL(task.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            	// Avoid timeout exception which usually occurs in low network
            connection.setConnectTimeout(0);
            connection.setReadTimeout(0);
            if (chunk.end != 0) // support unresumable links
                connection.setRequestProperty("Range", "bytes=" + chunk.begin + "-" + chunk.end);

            connection.connect();


            File cf = new File(FileUtils.address(task.save_address, String.valueOf(chunk.id)));
            // Check response code first to avoid error stream
            int status = connection.getResponseCode();
            InputStream remoteFileIn;
            if (status == 416)
                remoteFileIn = connection.getErrorStream();
            else
                remoteFileIn = connection.getInputStream();

            FileOutputStream chunkFile = new FileOutputStream(cf, true);

            int len = 0;
            // set watchDoger to stop thread after 1sec if no connection lost
            watchDog = new ConnectionWatchDog(5000, this);
            watchDog.start();
            while (!this.isInterrupted() &&
                    (len = remoteFileIn.read(buffer)) > 0) {
                if (i < 60) {
                    Log.e("phi.hd", "Read --------" + current + "-------");
                    i++;
                }
                watchDog.reset();
                chunkFile.write(buffer, 0, len);
                process(len);
            }
            chunkFile.flush();
            chunkFile.close();
            if (isClear) {
                boolean delete = cf.delete();
            }
            watchDog.interrupt();
            connection.disconnect();

            if (!this.isInterrupted()) {
                observer.rebuild(chunk);
            }


        }catch (SocketTimeoutException e) {
        	e.printStackTrace();
        	
        	observer.connectionLost(task.id);
        	puaseRelatedTask();
        	
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

    public void interrupt(boolean isClear) {
        super.interrupt();
        this.isClear = isClear;
        this.stop = true;
        Log.e("thinh.pt", "interrupt: stop: " + this.stop + " interrupt: " + isInterrupted());
    }

    @Override
    public boolean isInterrupted() {
        return stop || super.isInterrupted();
    }

    private void process(int read) {
        observer.process(chunk.task_id, read);
    }
    
    private void puaseRelatedTask()	{
    	observer.pause(task.id);
    }
    
    private boolean flag = true;
    public void connectionTimeOut(){
    	if (flag) {
    		watchDog.interrupt();
    		flag = false;
    		observer.connectionLost(task.id);
        	puaseRelatedTask();
		}
    	
    }

}
