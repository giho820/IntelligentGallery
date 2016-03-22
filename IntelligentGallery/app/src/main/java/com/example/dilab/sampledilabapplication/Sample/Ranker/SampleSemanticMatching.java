package com.example.dilab.sampledilabapplication.Sample.Ranker;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleContentScoreData;
import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;
import com.example.dilab.sampledilabapplication.Sample.SampleClassification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.util.DebugUtil;

//
//import android.content.Context;
//
//import com.example.dilab.sampledilabapplication.Sample.Models.SampleContentScoreData;
//import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.Random;
//
//import com.example.dilab.sampledilabapplication.Sample.Models.SampleContentScoreData;
//import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;
//import com.example.dilab.sampledilabapplication.Sample.SampleClassification;
//import us.korea.intelligentmail.database.ClassificationDataSource;
//import us.korea.intelligentmail.model.ClassificationModel;
//
//
public class SampleSemanticMatching {

    //    private ClassificationDataSource classificationDataSource;
//
//    ///////////////////////
//    ///// Constructor /////
//    ///////////////////////
    public SampleSemanticMatching() {

    }
//    public boolean init(Context context){
//        String path = "/data/data/" + context.getPackageName() + "/files/";
//        classificationDataSource = new ClassificationDataSource(context);
//        return true;
//    }
//
//
//    ///////////////////////
//    /////// SETTER // /////
//    ///////////////////////
//
//
//    ///////////////////////
//    /////// GETTER // /////
//    ///////////////////////
//
//    ///////////////////////
//    ////// Function  //////
//    ///////////////////////
//

    /**
     * @param arrContent2CategoryScore 검색어에 대하여 반환된 카테고리-score 값들
     * @return 각 콘텐츠에 대한 콘텐츠ID - Score값
     */
    public static ArrayList<LinkedHashMap> getRelevantContents(ArrayList<SampleScoreData> arrContent2CategoryScore) {
        ArrayList<LinkedHashMap> maps = new ArrayList<>();
        LinkedHashMap<Integer, SampleContentScoreData> a_mapScores = new LinkedHashMap<Integer, SampleContentScoreData>();
        ArrayList<Integer> cIDs = new ArrayList<>();

        try {
            if (arrContent2CategoryScore == null || arrContent2CategoryScore.size() == 0)
//                return a_mapScores.values().toArray(new SampleContentScoreData[0]);
                return null;

            for(SampleScoreData sampleScoreData : arrContent2CategoryScore) {
                Random rn = new Random();
                int rNum = rn.nextInt(12) + 1;
                int cID = SampleClassification.allCategoriesID.get(rNum);
                cIDs.add(cID);
            }
            maps = DatabaseCRUD.getContentScoreDataArray(cIDs);
            DebugUtil.showDebug("SampleSemanticMatching, getRelevantContents(), maps.size() : " + maps.size());
        } catch (Exception e) {
        } finally {
            return maps;
        }
    }

    public void calculateFinalScore(SampleContentScoreData[] a_aContentsScoreDatas, double a_dblAlpha) {
        for (int i = 0; i < a_aContentsScoreDatas.length; i++) {
            a_aContentsScoreDatas[i].calculateFinalScores(a_dblAlpha);
            System.out.println(a_aContentsScoreDatas[i].getFinalScore());
        }
    }
}
