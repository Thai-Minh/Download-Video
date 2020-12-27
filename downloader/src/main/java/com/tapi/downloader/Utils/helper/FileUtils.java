package com.tapi.downloader.Utils.helper;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static File create(String folder, String fileName){
        File file = new File(
                address(folder, fileName));
        Log.d("---------------------------------------", "Create file address: " + address(folder, fileName));
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static void forceCreate(String folder, String fileName){
        File dirs = new File(folder);
        dirs.mkdirs();

        File file = new File(
                address(folder, fileName));

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void delete(String folder, String fileName){
        File file = new File(
                address(folder, fileName));
        boolean delete = file.delete();
    }

    public static long size(String folder, String fileName){
        File file = new File(
                address(folder, fileName));
        return file.length();
    }

    public static FileOutputStream getOutputStream(String folder, String fileName){
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(
                    address(folder, fileName));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileOut;
    }

    public static FileInputStream getInputStream(String folder, String fileName){
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(
                    address(folder, fileName));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileIn;
    }

    public static String address(String folder, String file) {
        return folder + "/" + file;
    }

    public static int deleteFileTemp(String save_address) {
        File directory = new File(save_address);
        ArrayList<String> strings = new ArrayList<>();
        File[] files = directory.listFiles();
//        if (files != null && files.length != 0) {
//            for (int i = 0; i < files.length; i++) {
//                if (!files[i].getName().contains(".")) {
//                    strings.add(files[i].getAbsolutePath());
//                }
//            }
//
//        }
//        for (String s : strings) {
//            File file = new File(s);
//            if (file.exists()) {
//                boolean delete = file.delete();
//            }
//        }
        return files.length;
    }
}