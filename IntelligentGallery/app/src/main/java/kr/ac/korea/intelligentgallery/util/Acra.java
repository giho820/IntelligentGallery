package kr.ac.korea.intelligentgallery.util;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import kr.ac.korea.intelligentgallery.R;

/**
 * Created by AirFactory on 16. 1. 22..
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
public class Acra extends android.app.Application {

    public static Acra context;

    public Acra() {
        context = this;
    }

    public static Acra getContext() {
        return context;
    }

    public void onCreate() {
        super.onCreate();

        //주석 해제 시 에러내용 이메일로 전송
        ACRA.init(this);

        DebugUtil.showDebug("Acra, onCreate()");
    }
}
