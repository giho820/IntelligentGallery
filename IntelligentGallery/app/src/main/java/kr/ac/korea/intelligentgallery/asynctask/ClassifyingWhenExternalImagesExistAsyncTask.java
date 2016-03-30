package kr.ac.korea.intelligentgallery.asynctask;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.example.dilab.sampledilabapplication.Sample.SampleCategoryNamingConverter;
import com.example.dilab.sampledilabapplication.Sample.SampleCentroidClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleClassification;
import com.example.dilab.sampledilabapplication.Sample.SampleDatabaseInitializer;
import com.example.dilab.sampledilabapplication.Sample.SampleMNClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleResourceInitializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.util.DebugUtil;
import kr.ac.korea.intelligentgallery.util.DiLabClassifierUtil;
import kr.ac.korea.intelligentgallery.util.FileUtil;

/**
 * Created by kiho on 2016. 3. 25..
 */
public class ClassifyingWhenExternalImagesExistAsyncTask extends AsyncTask<Integer, String, Integer> {

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
    protected Integer doInBackground(Integer... params) {
        final int taskCnt = params[0];
        DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask(), doInBackground ");

        //여기서 분류기 초기화
        DiLabClassifierUtil.initializer = new SampleDatabaseInitializer(mContext);
        DiLabClassifierUtil.luceneKoInitializer = new SampleResourceInitializer();
        DiLabClassifierUtil.luceneKoInitializer.initialize(mContext);
        DiLabClassifierUtil.cNameConverter = new SampleCategoryNamingConverter(2);
        DiLabClassifierUtil.mnClassifier = new SampleMNClassifier(3, 2);
        DiLabClassifierUtil.centroidClassifier = SampleCentroidClassifier.getClassifier(DiLabClassifierUtil.initializer.getTargetPath(), "sigmaBase030.db");

        SampleClassification.initialize();
        DiLabClassifierUtil.K = 5;

        ImagesHavingGpsInfoButNotInInvertedIndexDB = FileUtil.getImagesHavingGPSInfoNotInInvertedIndex(mContext);
        if (ImagesHavingGpsInfoButNotInInvertedIndexDB != null && ImagesHavingGpsInfoButNotInInvertedIndexDB.size() > 0) {

            for (ImageFile imageFileNotInInvertedIndexDb : ImagesHavingGpsInfoButNotInInvertedIndexDB) {
                Integer imageId = imageFileNotInInvertedIndexDb.getId();
                String imagePath = imageFileNotInInvertedIndexDb.getPath();
                String imageFileUriPath = FileUtil.getImagePath(mContext, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + imageId));
                String imageName = FileUtil.getFileNameFromPath(imagePath);
                DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask(), doInBackground, ImageId() : " + imageId + ", ImageName : " + imageName);


                //특정 한 개의 이미지에 대해서 K 개의 카테고리를 생성하여 db에 insert하는 프로세스를 진행한다
                ArrayList<Integer> ids = new ArrayList<>();
                ids = DatabaseCRUD.getImagesIdsInInvertedIndexDb();
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.contains(imageId)) {
                        DebugUtil.showDebug("이미 있음 분류 필요없음:: " + imageId);
                        break;
                    } else {
                        DebugUtil.showDebug("디비에 없음 분류해야함:: " + imageId);

                        DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask(), " + imageName + " inserted 됨 ");
                        LinkedHashMap<Integer, String> rank0CategoryInfoAboutImage = DiLabClassifierUtil.classifySpecificImageFile("helloworld", imageId);//분류에 필요한 키워드의 경우 사진앱은 임의의 문자열
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
                }


            }
        }


        //작업이 끝나고 작업된 개수를 리턴. onPostExecute()함수의 인수가 됨
        return taskCnt;
    }

    //onProgressUpdate() 함수는 publishProgress() 함수로 넘겨준 데이터들을 받아옴
    @Override
    protected void onProgressUpdate(String... progress) {
        DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask(), onProgressUpdate ");
    }

    //onPostExecute() 함수는 doInBackground() 함수가 종료되면 실행됨
    @Override
    protected void onPostExecute(Integer result) {
        DebugUtil.showDebug("ClassifyingWhenExternalImagesExistAsyncTask(), onPostExecute ");
    }
}