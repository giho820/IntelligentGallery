package kr.ac.korea.intelligentgallery.asynctask;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.foursquare.Foursquare;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.ExifUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;

/**
 * Created by kiho on 2016. 3. 25..
 */
public class ClassifyingWhenExternalImagesExistAsyncTask extends AsyncTask<ArrayList<ImageFile>, String, ArrayList<ImageFile>> {

    private Context mContext;
    private ArrayList<ImageFile> ImagesHavingGpsInfoButNotInInvertedIndexDB;

    public ClassifyingWhenExternalImagesExistAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ImagesHavingGpsInfoButNotInInvertedIndexDB = new ArrayList<>();
        DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask, onPreExecute() ");
    }

    @Override
    protected ArrayList<ImageFile> doInBackground(ArrayList<ImageFile>... params) {
        final ArrayList<ImageFile> ImagesThatNeedToBeClassified = params[0];
        DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask(), doInBackground ");

        for (ImageFile imageFileNotInInvertedIndexDb : ImagesThatNeedToBeClassified) {
            Integer imageId = imageFileNotInInvertedIndexDb.getId();
            String imagePath = imageFileNotInInvertedIndexDb.getPath();
            String imageFileUriPath = FileUtil.getImagePath(mContext, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + imageId));
            String imageName = FileUtil.getFileNameFromPath(imagePath);
            DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask(), doInBackground, ImageId() : " + imageId + ", ImageName : " + imageName);

            //특정 한 개의 이미지에 대해서 K 개의 카테고리를 생성하여 db에 insert하는 프로세스를 진행한다
            DebugUtil.showDebug("디비에 없음 분류해야함:: " + imageId);

            // 해당 사진의 위도-경도값 얻기
            float[] gpsInfo = ExifUtil.getGPSinfo(imageFileUriPath, mContext);
            String classificationQuery = Foursquare.getQueryFromLocation("" + gpsInfo[0], "" + gpsInfo[1]);

            LinkedHashMap<Integer, String> rank0CategoryInfoAboutImage = DiLabClassifierUtil.classifySpecificImageFile(classificationQuery, imageId);//분류에 필요한 키워드의 경우 사진앱은 임의의 문자열
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
        return ImagesThatNeedToBeClassified;
    }

    //onPostExecute() 함수는 doInBackground() 함수가 종료되면 실행됨
    @Override
    protected void onPostExecute(ArrayList<ImageFile> result) {
        DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask(), onPostExecute 분류 완료된 이미지파일 개수 : " + result.size());
    }
}