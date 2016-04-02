package kr.ac.korea.intelligentgallery.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.util.DebugUtil;

/**
 * Created by kiho on 2016. 2. 14..
 */
public class UiUpdateBroadCastReceiver extends BroadcastReceiver {
    List<ImageFile> gridItems;
    public UiUpdateBroadCastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DebugUtil.showDebug("UiUpdateBroadCastReceiver, onReceive()");
        String name = intent.getAction();
        gridItems = (ArrayList<ImageFile>) intent.getSerializableExtra("gridItemsFromMakingContentDbService");

        if(name.equals("kr.ac.korea.intelligentgallery.service.MakingContentDBService")){
            DebugUtil.showDebug("정상적으로 값을 받았습니다.");
            if(gridItems!= null && gridItems.size() >=0 ) {
                for (ImageFile gridItem : gridItems){
                    DebugUtil.showDebug("UiUpdateBroadCastReceiver, onReceive() "+gridItem.getPath());
                }
            }
        }
    }

    @Override
    public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
    }
}
