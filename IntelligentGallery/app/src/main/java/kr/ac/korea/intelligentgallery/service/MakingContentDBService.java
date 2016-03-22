package kr.ac.korea.intelligentgallery.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.MediaStore;

import com.example.dilab.sampledilabapplication.Sample.SampleCategoryNamingConverter;
import com.example.dilab.sampledilabapplication.Sample.SampleCentroidClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleClassification;
import com.example.dilab.sampledilabapplication.Sample.SampleDatabaseInitializer;
import com.example.dilab.sampledilabapplication.Sample.SampleMNClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleResourceInitializer;

import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.database.DatabaseHelper;
import kr.ac.korea.intelligentgallery.database.util.DatabaseConstantUtil;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.SharedPreUtil;

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

    public class ClassifyingUsingAsyncTask extends AsyncTask<Integer, String, Integer> {

        private Context mContext;

        public ClassifyingUsingAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DebugUtil.showDebug("MakingContentDBService, ClassifyingUsingAsyncTask, onPreExecute() ");

            //디비 저장 전 테이블에 내용이 있으면 지울 것(분류 중에 어플을 강제로 시작할 경우에만 실행 됨)
            String deleteQuery = "delete from " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_CONTENT_ALBUM_NAME + ";";
            DebugUtil.showDebug(deleteQuery);
            DatabaseCRUD.execRawQuery(deleteQuery);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            final int taskCnt = params[0];
            DebugUtil.showDebug("MakingContentDBService, ClassifyingUsingAsyncTask, ClassifyingUsingAsyncTask(), doInBackground ");

            if (SharedPreUtil.getInstance().getBooleanPreference(SharedPreUtil.IS_NOT_FIRST_TIME_TO_START_APP) == false) {//최초실행이면
                DebugUtil.showDebug("MakingContentDBService, ClassifyingUsingAsyncTask, ClassifyingUsingAsyncTask(), doInBackground, getBooleanPreference(SharedPreUtil.IS_NOT_FIRST_TIME_TO_START_APP) == false, 최초실행");

                String deleteQuery = "delete from " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_CONTENT_ALBUM_NAME + ";";
                DebugUtil.showDebug(deleteQuery);
                DatabaseCRUD.execRawQuery(deleteQuery);

                ContentResolver mCr;

                // 미디어 쿼리 사용해서 앨범 정보 가지고 오기
                mCr = MakingContentDBService.this.getContentResolver();
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA};
                String select = MediaStore.Images.ImageColumns.LATITUDE + " is not null and " +  MediaStore.Images.ImageColumns.LONGITUDE + " is not null";
                Cursor cursor = mCr.query(uri, projection, select, null, null);
                if (cursor == null) {
                    return 0;
                }
                while (cursor.moveToNext()) {

                    Integer imageId = cursor.getInt(cursor.getColumnIndex(projection[0]));
                    String imagePath = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String imageName = FileUtil.getFileNameFromPath(imagePath);
                    DebugUtil.showDebug("MakingContentDBService, ClassifyingUsingAsyncTask, ClassifyingUsingAsyncTask(), doInBackground, ImageId() : " + imageId + ", ImageName : " + imageName);

                    //여기서 분류기 초기화
                    DiLabClassifierUtil.initializer = new SampleDatabaseInitializer(MakingContentDBService.this);
                    DiLabClassifierUtil.luceneKoInitializer = new SampleResourceInitializer();
                    DiLabClassifierUtil.luceneKoInitializer.initialize(MakingContentDBService.this);
                    DiLabClassifierUtil.cNameConverter = new SampleCategoryNamingConverter(2);
                    DiLabClassifierUtil.mnClassifier = new SampleMNClassifier(3, 2);
                    DiLabClassifierUtil.centroidClassifier = SampleCentroidClassifier.getClassifier(DiLabClassifierUtil.initializer.getTargetPath(), "sigmaBase030.db");

                    SampleClassification.initialize();
                    DiLabClassifierUtil.K = 5;

                    DebugUtil.showDebug("MakingContentDBService, " + imageName + " inserted 됨 ");
                    //특정 한 개의 이미지에 대해서 K 개의 카테고리를 생성하여 db에 insert하는 프로세스를 진행한다
                    LinkedHashMap<Integer, String> rank0CategoryInfoAboutImage = DiLabClassifierUtil.classifySpecificImageFile("helloworld", imageId);//분류에 필요한 키워드의 경우 사진앱은 임의의 문자열

                    Intent intent = new Intent("android.intent.action.classifying");
                    ImageFile imageFile = new ImageFile();
                    imageFile.setId(imageId);
                    imageFile.setName(imageName);
                    imageFile.setCategoryId((Integer) rank0CategoryInfoAboutImage.keySet().toArray()[0]);
                    imageFile.setCategoryName((String) rank0CategoryInfoAboutImage.values().toArray()[0]);
                    String imageFileUriPath = FileUtil.getImagePath(MakingContentDBService.this, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + imageId));
                    imageFile.setPath(imageFileUriPath);
                    imageFile.setRecentImageFileID(imageId);
                    intent.putExtra("newImageFile", imageFile);

                    sendBroadcast(intent);

                }
                cursor.close();

                //최초의 디비 생성이 끝나고나서 진행, false이면 최초 실행이라는 뜻
                SharedPreUtil.getInstance().putPreference(SharedPreUtil.IS_NOT_FIRST_TIME_TO_START_APP, true);
            } else {//최초 실행이 아닐 때
                DebugUtil.showDebug("MakingContentDBService, ClassifyingUsingAsyncTask, ClassifyingUsingAsyncTask(), doInBackground, getBooleanPreference(SharedPreUtil.IS_NOT_FIRST_TIME_TO_START_APP) == true, 최초실행 아님");
            }
            //작업이 끝나고 작업된 개수를 리턴. onPostExecute()함수의 인수가 됨
            return taskCnt;
        }

        //onProgressUpdate() 함수는 publishProgress() 함수로 넘겨준 데이터들을 받아옴
        @Override
        protected void onProgressUpdate(String... progress) {
            DebugUtil.showDebug("MakingContentDBService, ClassifyingUsingAsyncTask, ClassifyingUsingAsyncTask(), onProgressUpdate ");
//            if (progress[0].equals("progress")) {
//                mDlg.setProgress(Integer.parseInt(progress[1]));
//                mDlg.setMessage(progress[2]);
//            } else if (progress[0].equals("max")) {
//                mDlg.setMax(Integer.parseInt(progress[1]));
//            }
        }

        //onPostExecute() 함수는 doInBackground() 함수가 종료되면 실행됨
        @Override
        protected void onPostExecute(Integer result) {
            DebugUtil.showDebug("MakingContentDBService, ClassifyingUsingAsyncTask, ClassifyingUsingAsyncTask(), onPostExecute ");
//            mDlg.dismiss();
//            DebugUtil.showToast(mContext, Integer.toString(result) + "개의 작업 완료");
//
//            MainAct 이동
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    hideLoading();
//                    MoveActUtil.chageActivity(IntroAct.this, MainAct.class, 0, R.anim.fade_in, true, true);
//                }
//            }, 0);
            //콜백 메소드 등록

        }
    }

}


