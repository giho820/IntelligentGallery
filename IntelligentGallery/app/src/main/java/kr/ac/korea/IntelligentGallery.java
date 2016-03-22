package kr.ac.korea;

import android.app.Application;

import org.acra.ACRA;

import kr.ac.korea.intelligentgallery.util.DebugUtil;

/**
 * Created by preparkha on 15. 6. 15..
 */
public class IntelligentGallery extends Application {

    private static IntelligentGallery context;

    public IntelligentGallery() {
        super();
        this.context = this;
        DebugUtil.showDebug("context: " + this.toString());
    }

    public static IntelligentGallery getContext() {
        return context;
    }


    @Override
    public void onCreate() {
        super.onCreate();


        ACRA.init(this);
        DebugUtil.showDebug("KorThaiDicApplication onCreate()");
    }

}
