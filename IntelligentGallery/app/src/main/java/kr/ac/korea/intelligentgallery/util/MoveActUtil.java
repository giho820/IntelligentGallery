package kr.ac.korea.intelligentgallery.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.ac.korea.intelligentgallery.act.MainAct;

/**
 * Created by kiho on 2015. 9. 9..
 */
public class MoveActUtil {

    /**
     * @param preAct
     * @param intent
     * @param isFinish
     */
    public static void moveActivity(Activity preAct, Intent intent, int enterAniRes,
                                    int exitAniRes, boolean isFinish, boolean isAnimation) {
        preAct.startActivity(intent);
        preAct.overridePendingTransition(0, 0);
        if (isAnimation)
            preAct.overridePendingTransition(enterAniRes, exitAniRes);
        if (isFinish)
        preAct.finish();

    }

    /**
     * @param preAct
     * @param afterAct
     * @param enterAniRes
     * @param exitAniRes
     * @param isFinish
     */
    public static void chageActivity(Activity preAct, Class<?> afterAct, int enterAniRes,
                                     int exitAniRes, boolean isFinish, boolean isAnimation) {
        Intent intent = null;
        intent = new Intent(preAct, afterAct);
        preAct.startActivity(intent);
        if (isAnimation)
            preAct.overridePendingTransition(enterAniRes, exitAniRes);
        if (isFinish)
            preAct.finish();

    }

    /**
     * 업데이트
     *
     * @param packageName
     * @param activity
     */
    public static void setupApp(String packageName, Activity activity) {

        Uri marketUri = Uri.parse("market://details?id=" + packageName);
        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(marketUri);
        activity.startActivity(marketIntent);
    }

    public static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    public static void updateIconBadge(Context context, int notiCnt) {
        Intent badgeIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        badgeIntent.putExtra("badge_count", notiCnt);
        badgeIntent.putExtra("badge_count_package_name", context.getPackageName());
        badgeIntent.putExtra("badge_count_class_name", getLauncherClassName(context));
        DebugUtil.showDebug(getLauncherClassName(context));
        context.sendBroadcast(badgeIntent);
    }

    public static void cameraIntent(Activity activity, int requestCode){
        DebugUtil.showDebug("MoveActUtil, cameraIntent");
        Uri mImageCaptureUri=null;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Date now = new Date();
        SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_kkmmssss");
        String uri = "/DCIM/Camera/IG_" + String.valueOf(fileNameFormat.format(now)) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), uri));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        DebugUtil.showDebug(MainAct.currentMission, "MoveActUtil.java, cameraIntent()", String.valueOf(mImageCaptureUri));

        activity.startActivityForResult(intent, requestCode);
    }



}
