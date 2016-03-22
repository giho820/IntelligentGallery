package com.example.dilab.sampledilabapplication.Sample;
        import java.util.Random;

/**
 * Created by shysi on 2016-01-04.
 */
public class SampleMNClassifier {

    private final int M;
    private final int N;

    public SampleMNClassifier(int m, int n) {
        this.M = m;
        this.N = n;
    }

    public String classifying(String... categories) {
        String result = null;
        Random rn = new Random();
        if(rn.nextBoolean() && categories.length > 0){
            result = categories[0];
        }
        return result;
    }


    public int getM() {
        return this.M;
    }

    public int getN() {
        return this.N;
    }
}
