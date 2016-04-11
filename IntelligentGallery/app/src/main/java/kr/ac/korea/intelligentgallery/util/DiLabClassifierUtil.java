package kr.ac.korea.intelligentgallery.util;

import android.content.Context;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;
import com.example.dilab.sampledilabapplication.Sample.SampleCategoryNamingConverter;
import com.example.dilab.sampledilabapplication.Sample.SampleCentroidClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleDatabaseInitializer;
import com.example.dilab.sampledilabapplication.Sample.SampleMNClassifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.database.util.DatabaseConstantUtil;
import kr.ac.korea.intelligentgallery.intelligence.Ranker.SemanticMatching;

public class DiLabClassifierUtil {
    public static SampleCentroidClassifier centroidClassifier;
    public static SampleMNClassifier mnClassifier;
    public static SampleCategoryNamingConverter cNameConverter;
    public static SampleDatabaseInitializer initializer;
    public static SemanticMatching semanticMatching;
    public static Integer K;

    public static void init(Context context){
        DiLabClassifierUtil.initializer = new SampleDatabaseInitializer(context);
        DiLabClassifierUtil.cNameConverter = new SampleCategoryNamingConverter(2);
        DiLabClassifierUtil.mnClassifier = new SampleMNClassifier(3, 2);
        DiLabClassifierUtil.centroidClassifier = SampleCentroidClassifier.getClassifier(DiLabClassifierUtil.initializer.getTargetPath(), "sigmaBase030.db");
        DiLabClassifierUtil.semanticMatching = new SemanticMatching(context);
        DiLabClassifierUtil.K = 5;
    }

    public static LinkedHashMap<Integer, String> classifySpecificImageFile(String inputText, Integer _DATA) {

        ArrayList<SampleScoreData> categoryList;
        ArrayList<String> categoriesArrayList = new ArrayList<>();
        ArrayList<Integer> categoryIDsArrayList = new ArrayList<>();

        String textViewString = "";
        int rank = 1;

        categoryList = centroidClassifier.topK(K, inputText);
        String rawQuery = "insert or ignore into " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + "(" + DatabaseConstantUtil.COLUMN_AUTO_INCREMENT_KEY + ", " + DatabaseConstantUtil.COLUMN_DID + ", " + DatabaseConstantUtil.COLUMN_CATEGORY_ID + ", " + DatabaseConstantUtil.COLUMN_RANK + ", " + DatabaseConstantUtil.COLUMN_SCORE + ") values ";

        DiLabClassifierUtil.mnClassifier = new SampleMNClassifier(3, 2);

        for (SampleScoreData scoreData : categoryList) {
            int cID = scoreData.getID();
            double score = scoreData.getScore();
            String cNameOriginal = centroidClassifier.getCategoryName(cID);
            String cName = cNameConverter.convert(cNameOriginal);

            categoriesArrayList.add(cName);
            categoryIDsArrayList.add(cID);

            //inserting DB
            textViewString = textViewString + "\n" + "id : " + _DATA + "\n categoryId : " + cID + "\n rank : " + rank + "\n categoryName : " + cNameOriginal + " (" + cName + ") : \n score: " + score;

            rawQuery += "(null, '" + _DATA + "', " + cID + ", " + rank + ", " + score+"),";
            rank++;
        }

        //MNClassifer 적용하여 rank 0에 넣기 / score를 1.0으로 저장할 예정
        LinkedHashMap<Integer, String> top0Info = new LinkedHashMap<>();
        String[] categories = new String[categoriesArrayList.size()];
        for (int i = 0; i < categoriesArrayList.size(); i++) {
            categories[i] = categoriesArrayList.get(i);
        }
        String top0Name = DiLabClassifierUtil.mnClassifier.classifying(categories);
        Integer top0ID = categoryIDsArrayList.get(0);
        top0Info.put(top0ID, top0Name);
        DebugUtil.showDebug("DiLabClassifierUtil, top0ID::" + top0ID +", ::top0Name::" + top0Name);

        if (categories != null && categories.length >= 0) {
            for (int i = categories.length - 1; i >= 0; i--) {
                DebugUtil.showDebug("DiLabClassifierUtil, i::" + i +", ::categories[i]::" + categories[i]);
                if(!TextUtil.isNull(top0Name)){
                    if (categories[i].contains(top0Name)) {
                        top0ID = categoryIDsArrayList.get(i);
                        DebugUtil.showDebug("DiLabClassifierUtil, top0ID::" + top0ID +", ::top0Name::" + top0Name);
                    }
                }
            }
        }
        rawQuery += "(null, '" + _DATA + "', " + top0ID + ", " + 0 + ", " + 1.0 +");";

        DebugUtil.showDebug("DiLabClassifierUtil, classifyViewItems() rawQuery : " + rawQuery);
        DatabaseCRUD.execRawQuery(rawQuery);

        return top0Info;
    }

    public static void deleteUselessDB(Context context){
        // 외부에서 사진을 지웠을 때 같은 상황
        // 만일 외부에서 사진을 지워서 디비의 개수가 쿼리에 있는 분류해야할 사진의 개수보다 많다면
        ArrayList<Integer> dbImages = new ArrayList<>();
        ArrayList<ImageFile> mediaImages = new ArrayList<>();
        ArrayList<Integer> uselessDids = new ArrayList<>();
        dbImages = DatabaseCRUD.getImagesIdsInInvertedIndexDb();
        mediaImages = FileUtil.getImagesHavingGPSInfo(context);
        uselessDids = dbImages;
        int dbImageCnt = dbImages.size();
        int mediaImageCnt = mediaImages.size();

        DebugUtil.showDebug("zzz", "ClassifyingUsingAsyncTask, ", "" + dbImageCnt);
        DebugUtil.showDebug("zzz", "ClassifyingUsingAsyncTask, ", "" + mediaImageCnt);
        if (mediaImageCnt < dbImageCnt) {
            DebugUtil.showDebug("zzz, 외부에서 사진을 지워서 디비에 불필요한 이미지가 저장된 상태");

            for (int i = dbImageCnt -1; i >= 0; i--) {
                DebugUtil.showDebug("zzz, dbImages.get("+i + ") :: " + dbImages.get(i) + "===============");
                for(int j = 0; j < mediaImageCnt; j ++) {
                    DebugUtil.showDebug("zzz, mediaImages.get("+j + ").getId :: " + mediaImages.get(j).getId());
                    if(dbImages.get(i).equals(mediaImages.get(j).getId()) ){
                        DebugUtil.showDebug("zzz. did 같음");
                        uselessDids.remove(i);
                        break;
                    }
                }
            }

        }
        for(Integer uselessImageId : uselessDids){
            DebugUtil.showDebug("zzz, uselessImage 지워야하는 이미지 :: " + uselessImageId);
            DatabaseCRUD.deleteSpecificIdQuery(uselessImageId);
        }
    }
}
