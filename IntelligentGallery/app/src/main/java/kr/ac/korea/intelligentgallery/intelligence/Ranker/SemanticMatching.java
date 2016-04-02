package kr.ac.korea.intelligentgallery.intelligence.Ranker;

import android.content.Context;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.intelligence.Sample.DBAdapter.GraphDBAdapter;
import kr.ac.korea.intelligentgallery.intelligence.Sample.Model.ContentScoreData;

public class SemanticMatching {

        private GraphDBAdapter m_graph;

        private WPPR wppr;

        ///////////////////////
        ///// Constructor /////
        ///////////////////////
        public SemanticMatching(Context context) {
            String path = "/data/data/" + context.getPackageName() + "/files/";
            this.m_graph = new GraphDBAdapter();
            this.m_graph.openDB(path + "sigmaSimilarity030.db");
        }

        ///////////////////////
        /////// SETTER // /////
        ///////////////////////


        protected void setWPPR(WPPR wppr) {
            this.wppr = wppr;
        }
        ///////////////////////
        /////// GETTER // /////
        ///////////////////////

        ///////////////////////
        ////// Function  //////
        ///////////////////////

        /**
         * ??
         *
         * @param   arrContent2CategoryScore   검색어에 대하여 반환된 카테고리-score 값들
         * @return  각 콘텐츠에 대한 콘텐츠ID - Score값
         * @since   Sigma1.0
         */
        public ContentScoreData[] getRelevantContents(ArrayList<SampleScoreData> arrContent2CategoryScore) {
            LinkedHashMap<Integer, ContentScoreData> a_mapScores = new LinkedHashMap<Integer, ContentScoreData>();
            try {
                if (arrContent2CategoryScore == null || arrContent2CategoryScore.size() == 0)
                    return a_mapScores.values().toArray(new ContentScoreData[0]);
                ArrayList<ContentScoreData> contentInvertedIndexCount = null;
                for (int i = 0; i < arrContent2CategoryScore.size(); i++) {
                    // Category에 대한 가장 유사한 N개의 카테고리 가져옴
                    SampleScoreData[] b_categoryScores = this.m_graph.getTopNRelevanceCategory(arrContent2CategoryScore.get(i).getID(), 5);

                    for (int j = 0; j < b_categoryScores.length; j++) {
                        int categoryID_content = b_categoryScores[j].getID();
                        // categoryID에 속하는 모든 inverted index content 값을 가져옴
                        contentInvertedIndexCount = DatabaseCRUD.getContentScoreData(categoryID_content);
                        for (ContentScoreData contentScoreData: contentInvertedIndexCount) {
                            a_mapScores.put(contentScoreData.getContentsID(), contentScoreData);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                return a_mapScores.values().toArray(new ContentScoreData[0]);
            }
        }


        public void calculateFinalScore(ContentScoreData[] a_aContentsScoreDatas, double a_dblAlpha) {
            for (int i = 0; i < a_aContentsScoreDatas.length; i++) {
                a_aContentsScoreDatas[i].calculateFinalScores(a_dblAlpha);
                System.out.println(a_aContentsScoreDatas[i].getFinalScore());
            }
        }
    }
