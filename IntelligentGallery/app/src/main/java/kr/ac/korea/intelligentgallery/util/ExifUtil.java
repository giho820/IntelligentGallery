package kr.ac.korea.intelligentgallery.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by kiho on 2016. 3. 11..
 */
public class ExifUtil {
    // http://sylvana.net/jpegcrop/exif_orientation.html
    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        try {
            int orientation = getExifOrientation(src);

            if (orientation == 1) {
                return bitmap;
            }

            Matrix matrix = new Matrix();
            switch (orientation) {
                case 2:
                    matrix.setScale(-1, 1);
                    break;
                case 3:
                    matrix.setRotate(180);
                    break;
                case 4:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case 5:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case 6:
                    matrix.setRotate(90);
                    break;
                case 7:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case 8:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static int getExifOrientation(String src) throws IOException {
        int orientation = 1;

        try {
            /**
             * if your are targeting only api level >= 5
             * ExifInterface exif = new ExifInterface(src);
             * orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
             */
            if (Build.VERSION.SDK_INT >= 5) {
                Class<?> exifClass = Class.forName("android.media.ExifInterface");
                Constructor<?> exifConstructor = exifClass.getConstructor(new Class[]{String.class});
                Object exifInstance = exifConstructor.newInstance(new Object[]{src});
                Method getAttributeInt = exifClass.getMethod("getAttributeInt", new Class[]{String.class, int.class});
                Field tagOrientationField = exifClass.getField("TAG_ORIENTATION");
                String tagOrientation = (String) tagOrientationField.get(null);
                orientation = (Integer) getAttributeInt.invoke(exifInstance, new Object[]{tagOrientation, 1});
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return orientation;
    }


    public static int getOrientation(Context context, Uri photoUri) {
    /* it's on the external media. */
        if(photoUri == null) {
            DebugUtil.showDebug("ExifUtil, getOrigentation photoUri:: null");
            return 0;
        }

        Cursor cursor = context.getContentResolver().query(photoUri, new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    //    public static boolean setOrientation(Uri fileUri, int orientation, Context context) {
    public static boolean setOrientation(Uri fileUri, String filepath, int orientation, Context context) {
        ExifInterface exif = null;
        int ori = orientation % 360;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            DebugUtil.showDebug("ExifUtil, setOrientation ::cannot read exif");
            e.printStackTrace();
        }
        if (exif != null) {
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ori));
            DebugUtil.showDebug("ExifUtil, setOrientation::" + String.valueOf(ori));

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.ORIENTATION, ori);
            int rowsUpdated = context.getContentResolver().update(fileUri, values, null, null);
            DebugUtil.showDebug("ExifUtil, setOrientation, rowsUpdated::" + rowsUpdated);
            return rowsUpdated > 0;
        } else {
            DebugUtil.showDebug("ExifUtil, exif is null");
            return false;
        }
    }

    public static float[] getGPSinfo(String filepath, Context context) {
        ExifInterface exif = null;
        float[] gpsInfo = new float[2];

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            DebugUtil.showDebug("ExifUtil, getGPSinfo ::cannot read exif");
            e.printStackTrace();
        }
        if (exif != null) {
            exif.getLatLong(gpsInfo);
//            DebugUtil.showToast(context, "ExifUtil, getGPSinfo::(" + gpsInfo[0] + "," + gpsInfo[1] + ")");
        } else {
            DebugUtil.showDebug("ExifUtil, exif is null");
            return null;
        }
        return gpsInfo;
    }
}
