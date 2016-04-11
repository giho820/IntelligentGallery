package kr.ac.korea.intelligentgallery.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

/**
 * Created by kiho on 2016. 4. 7..
 */
public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mMs;
    private File mFile;

    public SingleMediaScanner(Context context, File f) {
        mFile = f;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        DebugUtil.showDebug("스캔하기", "SingleMediaScanner, onMediaScannerConnected()", "연결됨");
        mMs.scanFile(mFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        DebugUtil.showDebug("스캔하기", "SingleMediaScanner, onScanCompleted()", "path::" + path + ", uri::" + uri.toString());
        mMs.disconnect();
    }

}
