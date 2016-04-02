package kr.ac.korea.intelligentgallery.util;

/**
 * Created by kiho on 2016. 1. 19..
 */

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kr.ac.korea.intelligentgallery.R;

/**
 * Image 처리에 관련된 기능들을 모아놓은 유틸리티 클래스.
 *
 * @author : nexturbo
 * @create : 2012.4.24
 */
public class ImageUtil {

    public static DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.ic_stub) //로딩 전 이미지
            .showImageOnLoading(R.color.c_ff222222)
            .resetViewBeforeLoading(true)
            .showImageForEmptyUri(R.drawable.ic_empty)
            .showImageOnFail(R.color.c_ff222222)
            .cacheInMemory(true)
//                    .cacheOnDisc(false) //deprecated
            .cacheOnDisk(true)
            .considerExifParams(false)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.EXACTLY)
            .displayer(new FadeInBitmapDisplayer(0))
            .build();

    public static ImageLoaderConfiguration intelligentGalleryGlobalImageLoaderConfiguration(Context context) {
        DisplayImageOptions options;
        ImageLoaderConfiguration config;

        /**info : https://github.com/nostra13/Android-Universal-Image-Loader/wiki/Useful-Info
         http://m.blog.naver.com/d_onepiece/100210301983
         http://d2.naver.com/helloworld/429368 */

        options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.ic_stub) //로딩 전 이미지
                .showImageOnLoading(R.color.c_ff222222)
                .resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.color.c_ff222222)
                .cacheInMemory(true)
//                    .cacheOnDisc(false) //deprecated
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(0))
                .build();

        config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
//                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
//                .memoryCacheSize(20 * 1024 * 1024) // 20 Mb
                .denyCacheImageMultipleSizesInMemory()
                .defaultDisplayImageOptions(options)
//                .diskCacheSize(20 * 1024 * 1024) //20 Mb
                .threadPoolSize(5)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
//                .discCacheFileNameGenerator(new Md5FileNameGenerator())

                .memoryCacheExtraOptions(480, 800)
                .diskCacheExtraOptions(480, 800, null)
//                .diskCacheExtraOptions(ImageUtil.getDeviceWidth(context), ImageUtil.getDeviceHeight(context), null)
//                    .writeDebugLogs() // Remove for release app
                .build();

        return config;
    }

    /**
     * 비트맵의 모서리를 라운드 처리 한 후 Bitmap을 리턴
     *
     * @param bitmap bitmap handle
     * @return Bitmap
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 10;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();
        bitmap = output;

        return bitmap;
    }


    /**
     * 폰의 가로 사이즈를 구함
     *
     * @param mContext
     * @return
     */
    public static int getDeviceWidth(Context mContext) {
        int width = 0;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        } else {
            width = display.getWidth();  // Deprecated
        }
        return width;
    }

    /**
     * 폰의 세로 사이즈를 구함
     *
     * @param mContext
     * @return
     */
    public static int getDeviceHeight(Context mContext) {
        int height = 0;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12) {
            Point size = new Point();
            display.getSize(size);
            height = size.y;
        } else {
            height = display.getHeight();  // Deprecated
        }
        return height;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    /**
     * 지정한 패스의 파일을 화면 크기에 맞게 읽어서 Bitmap을 리턴
     *
     * @param context     application context
     * @param imgFilePath bitmap file path
     * @return Bitmap
     * @throws IOException
     */
    public static Bitmap loadBackgroundBitmap(Context context, String imgFilePath) {
        File file = new File(imgFilePath);
        if (file.exists() == false) {
            return null;
        }

        // 폰의 화면 사이즈를 구한다.
        Display display = ((WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        // 읽어들일 이미지의 사이즈를 구한다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgFilePath, options);

        // 화면 사이즈에 가장 근접하는 이미지의 스케일 팩터를 구한다.
        // 스케일 팩터는 이미지 손실을 최소화하기 위해 짝수로 한다.
        float widthScale = options.outWidth / displayWidth;
        float heightScale = options.outHeight / displayHeight;
        float scale = widthScale > heightScale ? widthScale : heightScale;

        if (scale >= 8)
            options.inSampleSize = 8;
        else if (scale >= 6)
            options.inSampleSize = 6;
        else if (scale >= 4)
            options.inSampleSize = 4;
        else if (scale >= 2)
            options.inSampleSize = 2;
        else
            options.inSampleSize = 1;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imgFilePath, options);
    }

    /**
     * 지정한 패스의 파일을 EXIF 정보에 맞춰 회전시키기
     *
     * @param bitmap bitmap handle
     * @return Bitmap
     */
    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), m, true);
                if (bitmap != b2) {
                    bitmap.recycle();
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }

        return bitmap;
    }

    /**
     * 사진을 찍거나 화면을 캡쳐할때 저장 메소드
     * 첫번째 인자값으로는 비트맵 이미지 두번째 인자값으로는 저장할 경로
     *
     * @param bitmap
     * @param strFilePath
     */

    public static void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {
        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 이미지 회전 함수
    public static Bitmap rotateImage(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    // jpeg 형식 사진의 Exif태그 정보를 가져오는 함수
    public static String getExifTag(ExifInterface exif, String tag) {
        String attribute = exif.getAttribute(tag);

        return (null != attribute ? attribute : "");
    }

    //해당 파일의 Exif정보를 보여준다
    //추후 다이얼로그로 보기 좋게 표시해주도록 한다
    public static String getExifInfoOfSelectedPicture(String path) {
        String result = "";
        try {
            ExifInterface exif = new ExifInterface(path);
            StringBuilder builder = new StringBuilder();
            DebugUtil.showDebug("FolderFrag, onCreate(), getExifInfoOfSelectedPicture() : " + path);

            File file = new File(path);
            if (file != null && file.exists()) {
                builder.append("이름 : " + file.getName() + "\n");
                builder.append("경로 : " + file.getAbsolutePath() + "\n");
            }

            builder.append("Date & Time: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_DATETIME) + "\n");
            builder.append("Flash: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_FLASH) + "\n");
            builder.append("Focal Length: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_FOCAL_LENGTH) + "\n");
            builder.append("GPS Datestamp: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_FLASH) + "\n");
            builder.append("GPS Latitude: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_GPS_LATITUDE) + "\n");
            builder.append("GPS Latitude Ref: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_GPS_LATITUDE_REF) + "\n");
            builder.append("GPS Longitude: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_GPS_LONGITUDE) + "\n");
            builder.append("GPS Longitude Ref: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_GPS_LONGITUDE_REF) + "\n");
            builder.append("GPS Processing Method: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_GPS_PROCESSING_METHOD) + "\n");
            builder.append("GPS Timestamp: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_GPS_TIMESTAMP) + "\n");
            builder.append("Image Length: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_IMAGE_LENGTH) + "\n");
            builder.append("Image Width: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_IMAGE_WIDTH) + "\n");
            builder.append("Camera Make: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_MAKE) + "\n");
            builder.append("Camera Model: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_MODEL) + "\n");
            builder.append("Camera Orientation: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_ORIENTATION) + "\n");
            builder.append("Camera White Balance: " + ImageUtil.getExifTag(exif, ExifInterface.TAG_WHITE_BALANCE) + "\n");

            result = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static Bitmap scaleImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        int MAX_IMAGE_DIMENSION = 600;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        String type = context.getContentResolver().getType(photoUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.equals("image/png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    //이미지 전처리 메소드 생성
    //어떠한 크기의 이미지가 소스로 들어오더라도 outofmemory exception 을 발생시키지 않음
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    //Scaled Down된 버전의 이미지 메모리에 로딩하기
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}