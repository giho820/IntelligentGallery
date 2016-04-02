package kr.ac.korea.intelligentgallery.util;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;
import com.example.dilab.sampledilabapplication.Sample.SampleCategoryNamingConverter;
import com.example.dilab.sampledilabapplication.Sample.SampleCentroidClassifier;
import com.example.dilab.sampledilabapplication.Sample.SampleDatabaseInitializer;
import com.example.dilab.sampledilabapplication.Sample.SampleMNClassifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
        String[] categories = new String[categoriesArrayList.size()];
        for (int i = 0; i < categoriesArrayList.size(); i++) {
            categories[i] = categoriesArrayList.get(i);
        }

        String top0Name = DiLabClassifierUtil.mnClassifier.classifying(categories);
        Integer top0ID = categoryIDsArrayList.get(0);
        LinkedHashMap<Integer, String> top0Info = new LinkedHashMap<>();
        top0Info.put(top0ID, top0Name);

        if (!TextUtil.isNull(top0Name) && categories != null && categories.length >= 0) {
            for (int i = categories.length - 1; i >= 0; i--) {
                if (categories[i].contains(top0Name)) {
                    top0ID = categoryIDsArrayList.get(i);
                }
            }
        }
        rawQuery += "(null, '" + _DATA + "', " + top0ID + ", " + 0 + ", " + 1.0 +");";

        DebugUtil.showDebug("DiLabClassifierUtil, classifyViewItems() rawQuery : " + rawQuery);
        DatabaseCRUD.execRawQuery(rawQuery);

        return top0Info;
    }
}
