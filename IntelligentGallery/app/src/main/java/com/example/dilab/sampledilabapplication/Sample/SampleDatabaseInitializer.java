package com.example.dilab.sampledilabapplication.Sample;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by shysi on 2016-01-04.
 */
public class SampleDatabaseInitializer {

    private final String targetPath;
    private final Context context;

    private static final String DATABASE_ASSET_PATH = "db";
    private static final String DICTIONARY_ASSET_PATH = "morph/dic";
    private static final String STOPWORDS_ASSET_PATH = "morph/properties";
    private static final String[] ASSET_PATHS = new String[]{"db", "morph/dic", "morph/properties"};


    public SampleDatabaseInitializer(Context context){
        this.context = context;
        this.targetPath  = "/data/data/" + context.getPackageName() + "/files/";
        this.initialize();
        SampleClassification.initialize();
    }

    private void initialize() {
        AssetManager am = this.context.getAssets();
        try {
            String[] e = ASSET_PATHS;
            int len$ = e.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                String assetPath = e[i$];
                String[] arr$ = am.list(assetPath);
                int len$1 = arr$.length;

                for(int i$1 = 0; i$1 < len$1; ++i$1) {
                    String fileName = arr$[i$1];
                    InputStream fileInputStream = am.open(assetPath + "/" + fileName);
                    SampleFileCopyUtils.saveFile(fileInputStream, this.targetPath + fileName);
                }
            }

        } catch (IOException var11) {
            throw new NullPointerException(var11.getMessage());
        }
    }

    public String getTargetPath(){
        return this.targetPath;
    }
}
