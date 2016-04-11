package kr.ac.korea.intelligentgallery.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import kr.ac.korea.intelligentgallery.util.DebugUtil;

/**
 * Created by kiho on 2016. 4. 7..
 */

//풀 미디어 스캔을 할때 이미 미디어 스캐닝중인지 확인
public class MediaScannerBroadcastReceiver extends BroadcastReceiver {
    public static boolean mMedaiScanning = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)){
            DebugUtil.showDebug("MediaScannerBroadcastReceiver, onReceive(), 풀 스캐닝 시작됨");
            mMedaiScanning = true;
        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)){
            DebugUtil.showDebug("MediaScannerBroadcastReceiver, onReceive(), 풀 스캐닝 완료됨");
            mMedaiScanning = false;
        }
    }
}