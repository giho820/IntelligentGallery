package kr.ac.korea.intelligentgallery.util;

/**
 * Created by airfactory on 2015. 12. 10..
 */
public class ConstantUtil {

    //for MainAct
    public static final int MAINACT_REQUESTCODE_FOR_CAMERA_INTENT = 0;

    public static final int GALLERYACT_REQUESTCODE_FOR_IMAGES_REFRESH = 1;
    public static final int GALLERYACT_REQUESTCODE_FOR_MOVEACT = 2;
    public static final int GALLERYACT_REQUESTCODE_FOR_COPYACT = 3;
    public static final int GALLERYACT_REQUESTCODE_FOR_SELECT_COVER_IMAGE = 4;


    public interface KEY_SHARED_PREFERENCE {
        String CHECK_DATABASE = "CHECK_DATABASE";
        String CATEGORY_CLASSIFIED = "CATEGORY_CLASSIFIED";
    }

}
