package com.example.dilab.sampledilabapplication.Sample;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;

import java.util.ArrayList;
import java.util.Random;


public class SampleCentroidClassifier {

    private int numDoc = 0;
    private SQLiteDatabase sqliteDB = null;

    private SampleCentroidClassifier() {
    }

    public static SampleCentroidClassifier getClassifier(String filePath, String fileName) {

        if (SampleClassification.allCategoriesID == null || SampleClassification.allCategoriesID.size() < 13)
            SampleClassification.initialize();

        SampleCentroidClassifier classifier = null;
        Cursor result = null;

        try {
            SQLiteDatabase ex = SQLiteDatabase.openDatabase(filePath + fileName, (SQLiteDatabase.CursorFactory)null, 1);
            String[] columns = new String[]{"df"};
            String selection = "term=?";
            String[] selectionArgs = new String[]{"number_of_training_document"};
            result = ex.query("vocabulary", columns, selection, selectionArgs, (String)null, (String)null, (String)null);
            if(!result.moveToFirst()) {
                return null;
            }

            int numDoc = result.getInt(0);
            classifier = new SampleCentroidClassifier();
            classifier.numDoc = numDoc;
            classifier.sqliteDB = ex;

        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return new SampleCentroidClassifier();
    }

    public ArrayList<SampleScoreData> topK(int k, String query) {
        ArrayList listTopK = new ArrayList();
        ArrayList<Integer> listOrder = new ArrayList<Integer>();
        int topK = 0;

        for (int i = 0; i < k; i++) {
            Random rn = new Random();
            int rNum = rn.nextInt(12) + 1;
            /////

            if (SampleClassification.allCategoriesID == null || SampleClassification.allCategoriesID.size() < 13)
                SampleClassification.initialize();

            int cid = SampleClassification.allCategoriesID.get(rNum);
            double score = rn.nextDouble() * .8;
            SampleScoreData scoreData = new SampleScoreData(cid, score);
            listTopK.add(scoreData);
        }
        listTopK = topKSort(k, listTopK);
        try {
            Thread.sleep(500);
        } catch (Exception e) {

        }
        return listTopK;
    }

    private ArrayList<SampleScoreData> topKSort(int k, ArrayList<SampleScoreData> listTopK) {
        System.out.println(listTopK);
        boolean isChanging = false;
        for (int i = 1; i < k; i++) {
            if (listTopK.get(i).getScore() > listTopK.get(i - 1).getScore()) {
                isChanging = true;
                SampleScoreData tmpScoreData = listTopK.get(i);
                listTopK.set(i, listTopK.get(i - 1));
                listTopK.set(i - 1, tmpScoreData);
            }
        }
        if (isChanging) {
            listTopK = topKSort(k, listTopK);
        }
        return listTopK;
    }

    public String getCategoryName(int cid) {

        int num = SampleClassification.allCategoriesID.indexOf(cid);
        return SampleClassification.allCategories.get(num);
    }
}
