package com.example.dilab.sampledilabapplication.Sample;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class SampleFileCopyUtils {
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    public SampleFileCopyUtils() {
    }

    public static void saveFile(InputStream inputStream, String filePath) {
        int ind = filePath.lastIndexOf("/");
        if(ind > 0) {
            String ex = filePath.substring(0, ind);
            makeFolder(ex);
        }

        try {
            File ex1 = new File(filePath);
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            if(ex1.exists()) {
                ex1.delete();
            }

            ex1.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(ex1));
            boolean read = true;
            byte[] buffer = new byte[1024];

            int read1;
            while((read1 = bis.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read1);
            }

            bos.flush();
            bos.close();
            bis.close();
        } catch (IOException var8) {
            var8.printStackTrace();
        }

    }

    public static void makeFolder(String folderPath) {
        File folder = new File(folderPath);
        if(!folder.exists()) {
            folder.mkdirs();
        }

    }
}
