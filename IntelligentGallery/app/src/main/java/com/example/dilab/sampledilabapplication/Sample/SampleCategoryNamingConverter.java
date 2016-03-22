package com.example.dilab.sampledilabapplication.Sample;

import java.util.StringTokenizer;


public class SampleCategoryNamingConverter {

    public static SampleConvertBalloonName cBName;
    private final int maximumLimitLevel;
    private final boolean useBalloonName;

    public SampleCategoryNamingConverter(int limit) {
        this.maximumLimitLevel = limit;
        this.useBalloonName = true;
    }

    public SampleCategoryNamingConverter(int limit, boolean useBalloonName) {
        this.maximumLimitLevel = limit;
        this.useBalloonName = useBalloonName;
    }

    public String convert(String original) {
        String result = null;
        String categoryName = original.replaceAll("Top", "");
        categoryName = categoryName.trim();
        cBName = new SampleConvertBalloonName();
        if(this.useBalloonName) {
            SampleConvertBalloonName var10000 = cBName;
            if(SampleConvertBalloonName.balloonName.containsKey(categoryName)) {
                var10000 = cBName;
                result = (String)SampleConvertBalloonName.balloonName.get(categoryName);
                return result;
            }
        }

        StringTokenizer st = new StringTokenizer(categoryName, "/");

        for(int level = 1; st.hasMoreTokens() && level <= this.maximumLimitLevel; ++level) {
            result = st.nextToken();
        }

        result = result.replaceAll("_", " ");
        return result;
    }
}
