package com.example.dilab.sampledilabapplication.Sample.Models;

public class SampleScoreData implements Comparable<SampleScoreData> {
    private int id; // categotyId
    private double score;

    public SampleScoreData(int id, double score) {
        this.id = id;
        this.score = score;
    }

    public int getID() {
        return this.id;
    }

    public double getScore() {
        return this.score;
    }

    public int compareTo(SampleScoreData other) {
        return this.score > other.score?-1:(this.score < other.score?1:0);
    }
}
