package kr.ac.korea.intelligentgallery.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

/**
 * Created by kiho on 2016. 4. 7..
 */
public class MediaScanFile extends MediaScannerConnection {
    private static final String TAG = MediaScanFile.class.getSimpleName();
    private static MediaScanFile mSingleton = null;
    private static String mPath = null;

    public static MediaScanFile getInstance(Context context, String path){
        if(mSingleton == null){
            mPath = path;
            mSingleton = new MediaScanFile(context, mScanClient);
            mSingleton.connect();
        }
        return mSingleton;
    }

    private MediaScanFile(Context context, MediaScannerConnectionClient client) {
        super(context, client);
        // TODO Auto-generated constructor stub
    }

    private static MediaScannerConnectionClient mScanClient = new MediaScannerConnectionClient() {

        @Override
        public void onScanCompleted(String path, Uri uri) {
            Log.i(TAG, "onScanCompleted:" + path + ", " + uri.toString());
            mSingleton.disconnect();
            mSingleton = null;
        }

        @Override
        public void onMediaScannerConnected() {
            mSingleton.scanFile(mPath, null);

        }
    };

}