package kr.ac.korea.intelligentgallery.intelligence.Sample.Model;

public class ContentScoreData {
    private int m_contentsID;
    private double m_dblKeywordScore;
    private double m_dblGraphScore;
    private double m_dblFinalScore;

    public ContentScoreData(){
        this.m_contentsID = 0;
        this.m_dblFinalScore = 0;
        this.m_dblGraphScore = 0;
        this.m_dblKeywordScore = 0;
    }


    public int getContentsID() {
        return m_contentsID;
    }
    public void setContentsID(int a_contentsID) {
        this.m_contentsID = a_contentsID;
    }
    public void setKeywordScore(double a_dblKeywordScore){
        this.m_dblKeywordScore = a_dblKeywordScore;
    }
    public double getKeywordScore(){
        return this.m_dblKeywordScore;
    }
    public void addKeywordScore(double a_dblKeywordScore){
        this.m_dblKeywordScore += a_dblKeywordScore;
    }
    public void setGraphScore(double a_dblGraphScore){
        this.m_dblGraphScore = a_dblGraphScore;
    }
    public double getGraphScore(){
        return this.m_dblGraphScore;
    }
    public void addGraphScore(double a_dblGraphScore){
        this.m_dblGraphScore += a_dblGraphScore;
    }
    public double calculateFinalScores(double a_dblAlpha){
        this.m_dblFinalScore = (1.0f - a_dblAlpha) * this.m_dblKeywordScore + a_dblAlpha * this.m_dblGraphScore;
        return this.m_dblFinalScore;
    }
    public double getFinalScore(){
        return this.m_dblFinalScore;
    }

}

