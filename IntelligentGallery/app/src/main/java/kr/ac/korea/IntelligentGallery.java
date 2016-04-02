package kr.ac.korea;

import android.app.Application;

import com.example.dilab.sampledilabapplication.Sample.SampleResourceInitializer;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import kr.ac.korea.intelligentgallery.R;
import kr.ac.korea.intelligentgallery.util.DebugUtil;

/**
 * Created by preparkha on 15. 6. 15..
 */
@ReportsCrashes(
//        formKey = "",
        resToastText = R.string.crash_toast_text,
        mode = ReportingInteractionMode.DIALOG,
        logcatArguments = { "-t", "2000", "-v", "time" },
        resDialogIcon = android.R.drawable.ic_dialog_info,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogText = R.string.crash_dialog_text,
        mailTo = "giho820@airrabbit.com")
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

        SampleResourceInitializer resourceInitializer = new SampleResourceInitializer();
        resourceInitializer.initialize(getApplicationContext());

//        ACRA.init(this);
        DebugUtil.showDebug("KorThaiDicApplication onCreate()");
    }

}
