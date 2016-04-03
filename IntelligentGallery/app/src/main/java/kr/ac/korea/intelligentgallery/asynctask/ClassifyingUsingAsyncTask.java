package kr.ac.korea.intelligentgallery.asynctask;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.foursquare.Foursquare;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.ExifUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;
import kr.ac.korea.intelligentgallery.util.SharedPreUtil;

/**
 * Created by kiho on 2016. 3. 25..
 */
public class ClassifyingUsingAsyncTask extends AsyncTask<Integer, String, Integer> {

    private Context mContext;

    public ClassifyingUsingAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        DebugUtil.showDebug("ClassifyingUsingAsyncTask, onPreExecute() ");

//        //디비 저장 전 테이블에 내용이 있으면 지울 것(분류 중에 어플을 강제로 시작할 경우에만 실행 됨)
//        String deleteQuery = "delete from " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + ";";
//        DebugUtil.showDebug(deleteQuery);
//        DatabaseCRUD.execRawQuery(deleteQuery);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        DebugUtil.showDebug("ClassifyingUsingAsyncTask, doInBackground ");

        final int taskCnt = params[0];
        ContentResolver mCr;
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        if (SharedPreUtil.getInstance().getBooleanPreference(SharedPreUtil.IS_NOT_FIRST_TIME_TO_START_APP) == false) {//최초실행이면
            DebugUtil.showDebug("ClassifyingUsingAsyncTask, doInBackground, getBooleanPreference(SharedPreUtil.IS_NOT_FIRST_TIME_TO_START_APP) == false, 최초실행");

            // 미디어 쿼리 사용해서 앨범 정보 가지고 오기
            mCr = mContext.getContentResolver();
            String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA};
            String select = MediaStore.Images.ImageColumns.LATITUDE + " is not null and " +  MediaStore.Images.ImageColumns.LONGITUDE + " is not null and " +
                    MediaStore.Images.ImageColumns.LATITUDE + " != 0 and " +  MediaStore.Images.ImageColumns.LONGITUDE + " != 0";
//            Cursor cursor = mCr.query(uri, projection, null, null, null);//test
            Cursor cursor = mCr.query(uri, projection, select, null, null);

            if (cursor == null) {
                return 0;
            }
            while (cursor.moveToNext()) {

                Integer imageId = cursor.getInt(cursor.getColumnIndex(projection[0]));
                String imageFileUriPath = FileUtil.getImagePath(mContext, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + imageId));
                String imagePath = cursor.getString(cursor.getColumnIndex(projection[1]));
                String imageName = FileUtil.getFileNameFromPath(imagePath);
                DebugUtil.showDebug("ClassifyingUsingAsyncTask(), doInBackground, ImageId() : " + imageId + ", ImageName : " + imageName);

                //특정 한 개의 이미지에 대해서 K 개의 카테고리를 생성하여 db에 insert하는 프로세스를 진행한다
                // 해당 사진의 위도-경도값 얻기
                float[] gpsInfo = ExifUtil.getGPSinfo(imageFileUriPath, mContext);
                String classificationQuery = Foursquare.getQueryFromLocation("" + gpsInfo[0], "" + gpsInfo[1]);

                LinkedHashMap<Integer, String> rank0CategoryInfoAboutImage = DiLabClassifierUtil.classifySpecificImageFile(classificationQuery, imageId);//분류에 필요한 키워드의 경우 사진앱은 임의의 문자열

                //이미지 한 개를 분류할 때마다 MainAct에 분류했음을 알려주는 브로드캐스트를 send
                Intent intent = new Intent("android.intent.action.classifying");
                ImageFile imageFile = new ImageFile();
                imageFile.setId(imageId);
                imageFile.setName(imageName);
                imageFile.setCategoryId((Integer) rank0CategoryInfoAboutImage.keySet().toArray()[0]);
                imageFile.setCategoryName((String) rank0CategoryInfoAboutImage.values().toArray()[0]);

                imageFile.setPath(imageFileUriPath);
                imageFile.setRecentImageFileID(imageId);
                intent.putExtra("newImageFile", imageFile);

                mContext.sendBroadcast(intent);
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

    @Override
    protected void onPostExecute(Integer result) {
        DebugUtil.showDebug("MakingContentDBService, ClassifyingUsingAsyncTask, ClassifyingUsingAsyncTask(), onPostExecute ");
    }
}