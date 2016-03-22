package com.example.dilab.sampledilabapplication.Sample;

import java.util.ArrayList;
import java.util.Random;

public class SampleClassification {
    private SampleCentroidClassifier centroidClassifier;
    private SampleCategoryNamingConverter cNameConverter;

    public SampleClassification(String path, String file){
        //centroidClassifier = new SampleCentroidClassifier();
        cNameConverter = new SampleCategoryNamingConverter(2);
    }

    public String classify(String rawData){
        Random rn = new Random();
        int rNum = rn.nextInt(12) + 1;

        int resultCategoryID = allCategoriesID.get(rNum);
        String resultCategory = allCategories.get(rNum);

        return cNameConverter.convert(resultCategory);
    }

    public SampleCentroidClassifier getCentroidClassifier(){
        return centroidClassifier;
    }



    public static ArrayList<String> allCategories = new ArrayList<String>();
    public static ArrayList<Integer> allCategoriesID = new ArrayList<Integer>();
    public static void initialize(){

        allCategories.add("Top/컴퓨터/멀티미디어/음악및오디오");
        allCategories.add("Top/비즈니스/건설및유지관리/재료및소모품");
        allCategories.add("Top/건강/직업");
        allCategories.add("Top/비즈니스/금융서비스/은행서비스/신용조합");
        allCategories.add("Top/뉴스");
        allCategories.add("Top/예술/음악/스타일");
        allCategories.add("Top/뉴스/대학및대학교");
        allCategories.add("Top/쇼핑/간행물/책");
        allCategories.add("Top/사회/계보");
        allCategories.add("Top/사회/죽음");
        allCategories.add("Top/휴양/오토바이");
        allCategories.add("Top/스포츠/배구");
        allCategories.add("Top/게임/롤플레잉");
        allCategoriesID.add(63228);
        allCategoriesID.add(52687);
        allCategoriesID.add(87930);
        allCategoriesID.add(54768);
        allCategoriesID.add(91537);
        allCategoriesID.add(30396);
        allCategoriesID.add(91576);
        allCategoriesID.add(448824);
        allCategoriesID.add(453304);
        allCategoriesID.add(451152);
        allCategoriesID.add(96222);
        allCategoriesID.add(496782);
        allCategoriesID.add(70140);
    }
}
