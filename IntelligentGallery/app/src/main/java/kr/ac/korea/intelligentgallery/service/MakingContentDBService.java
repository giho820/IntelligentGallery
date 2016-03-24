package kr.ac.korea.intelligentgallery.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import kr.ac.korea.intelligentgallery.asynctask.ClassifyingUsingAsyncTask;
import kr.ac.korea.intelligentgallery.database.DatabaseHelper;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;

public class MakingContentDBService extends Service {
    private DatabaseHelper databaseHelper;

    public MakingContentDBService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DebugUtil.showDebug("MakingContentDbService, onCreate()");
        databaseHelper = DatabaseHelper.getInstacnce(this);

        Integer ImageFilesCnt = FileUtil.getAllImageFilecount(this);
        Integer imageFilesHavingGpsInfo = FileUtil.getAllImageFilesThatHaveGPSInfoCount(this);
        // Asynctask를 통한 분류 진행
        // Asynchronous thread
//        new ClassifyingUsingAsyncTask(this).execute(ImageFilesCnt);
        new ClassifyingUsingAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageFilesHavingGpsInfo);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugUtil.showDebug("MakingContentDbService, onDestroy()");
    }



}


