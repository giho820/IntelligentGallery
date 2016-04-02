package kr.ac.korea.intelligentgallery.intelligence.Ranker;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;

import java.util.ArrayList;


/**
 * An abstract class used to refer wPPR values for calculating GraphScore.
 * @author okcomputer
 *
 */
public abstract class WPPR {
	public abstract ArrayList<SampleScoreData> getSortedListScore(int cidJ);
	
	public abstract double getScore(int cidI, int cidJ);
	
	public ArrayList<SampleScoreData> topK(int k, int cidJ) {
		ArrayList<SampleScoreData> listScore = this.getSortedListScore(cidJ);
		if (k >= listScore.size()) {
			return listScore;
		} else {
			ArrayList<SampleScoreData> listTopK = new ArrayList<SampleScoreData>();
			for (int index = 0; index < k; index++) {
				listTopK.add(listScore.get(index));
			}
			listScore.clear();
			return listTopK;
		}
	}
	
	public static String getFileNameDB() {
		return String.format(WPPR.FILE_DB_WPPR_F, TOP_K);
	}
	
	public static String getFileNameMap() {
		return String.format(WPPR.FILE_MAP_CID_CID_WPPR_F, WPPR.TOP_K);
	}
	
	public static final double	SIM_THRESHOLD		= 0.1;
	public static final double	DAMPING_FACTOR		= 0.15;
	public static final int		NUM_ITERATION		= 100;
	public static final int		TOP_K				= 100;
	public static final String FILE_MAP_CID_CID_WPPR	= "cid_cid_wppr.map";
	public static final String FILE_MAP_CID_CID_WPPR_F	= "cid_cid_wppr_top%d.map";
	public static final String FILE_DB_WPPR			= "wppr.db";
	public static final String FILE_DB_WPPR_F			= "wppr_top%d.db";
}