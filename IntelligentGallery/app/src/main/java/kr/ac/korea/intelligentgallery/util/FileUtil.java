package kr.ac.korea.intelligentgallery.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import kr.ac.korea.intelligentgallery.act.FolderCategoryAct;
import kr.ac.korea.intelligentgallery.data.Album;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.DatabaseCRUD;
import kr.ac.korea.intelligentgallery.database.DatabaseHelper;
import kr.ac.korea.intelligentgallery.database.util.DatabaseConstantUtil;

/**
 * Created by kiho on 2015. 12. 16..
 */
public class FileUtil {

    //미디어스토리지에서 쿼리로 커서 값 가져올 때 쓰는 함수
    static String getColumeValue(Cursor cursor, String cname) {
        String value = cursor.getString(cursor.getColumnIndex(cname)) + "\n";
        return value;
    }


    public static String getImagePath(Context context, Uri uri) {
        String path = "";
        if (context == null) {
            DebugUtil.showDebug("FileUtil, context is null");
            return path;
        }

        if (uri != null) {
            String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
            }
        }
        return path;
    }

    //특정 이미지 uri의 상위 폴더에 해당하는 BucketID를 가져오는 함수
    public static String getBucketIdFromImage(Context context, Uri uri) {
        String bucketId = "";
        if (context == null) {
            DebugUtil.showDebug("FileUtil, context is null");
            return bucketId;
        }
        String[] projection = {MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
            cursor.close();
        }
        return bucketId;
    }


    // 특정 아이디를 가진 이미지의 buketID(상위 폴더의 아이디)를 가져오는 쿼리

    public static String queryForBucketIdOfImage(Context context, Integer imageID) {
        ContentResolver mCr;
        String bucketId = "";
        // 미디어 쿼리 사용해서 앨범 정보 가지고 오기
        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.BUCKET_ID};
        String orderBy = MediaStore.Images.Media.DEFAULT_SORT_ORDER; //폴더의 갯수가 정렬 기준, 지금은 앨범 알파벳, 가나다순 정렬
        Cursor cursor = mCr.query(uri, projection, null, null, orderBy);

        while (cursor.moveToNext()) {
            //앨범 아이디
            bucketId = getColumeValue(cursor, MediaStore.Images.Media.BUCKET_ID);
        }
        cursor.close();
        return bucketId;
    }


    public static Integer getAllImageFilecount(Context context) {
        //미디어 스토리지 이용하는 방법
        ContentResolver mCr;
        Integer result = 0;

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projectionForAlbumImage = {MediaStore.Images.ImageColumns.DATA};
        Cursor cursor = mCr.query(uri, projectionForAlbumImage, null, null, null);
        while (cursor.moveToNext()) {
            result = cursor.getCount();
        }
        cursor.close();
        DebugUtil.showDebug("전체 이미지 파일 갯수  : " + result);

        return result;
    }

    public static Integer getAllImageFilesThatHaveGPSInfoCount(Context context) {
        //미디어 스토리지 이용하는 방법
        ContentResolver mCr;
        Integer result = 0;

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projectionForAlbumImage = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.LATITUDE, MediaStore.Images.ImageColumns.LONGITUDE};
        String select = MediaStore.Images.ImageColumns.LATITUDE + " is not null and " + MediaStore.Images.ImageColumns.LONGITUDE + " is not null";
        Cursor cursor = mCr.query(uri, projectionForAlbumImage, select, null, null);
        while (cursor.moveToNext()) {
            result = cursor.getCount();
            DebugUtil.showDebug("위도 경도 정보가 있는 이미지 전체::" + getColumeValue(cursor, projectionForAlbumImage[1]));
        }
        cursor.close();
        DebugUtil.showDebug("위도 경도를 가진 파일의 전체 개수  : " + result);

        return result;
    }

    public static void updateMediaStorageQuery(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    //분류가 되어야하는데 아직 분류가 안 된 것들을 찾아내는 함수

    public static ArrayList<ImageFile> getImagesHavingGPSInfoButNotInInvertedIndex(Context context) {
        ContentResolver mCr;
        ArrayList<ImageFile> images = new ArrayList<>();
        ArrayList<ImageFile> imagesNotInInvertedIndexDb = new ArrayList<>();

        String selectSql = "SELECT " + DatabaseConstantUtil.COLUMN_DID +
                " FROM " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME +
                " WHERE " + DatabaseConstantUtil.COLUMN_RANK + "=0";
        Cursor subQueryCursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectSql, null);

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.ImageColumns.LATITUDE + " is not null and " + MediaStore.Images.ImageColumns.LONGITUDE + " is not null";
        Cursor cursor = mCr.query(uri, projection, selection, null, null);

        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            ImageFile imageFile = new ImageFile();

            Integer viewItemID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            imageFile.setId(viewItemID);
//            DebugUtil.showDebug(FolderCategoryAct.ttttt + ", 위도 경도 있는 애들 :: " + viewItemID);

            String imageFileUriPath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID));
            imageFile.setPath(imageFileUriPath);

            images.add(imageFile);
        }
        cursor.close();


        imagesNotInInvertedIndexDb.addAll(images);

        for (int i = imagesNotInInvertedIndexDb.size() - 1; i >= 0; i--) {
//            DebugUtil.showDebug(FolderCategoryAct.ttttt + "images having gps info :: " + imagesNotInInvertedIndexDb.get(i).getId() + "======");

            while (subQueryCursor != null && subQueryCursor.moveToNext()) {
                int did = subQueryCursor.getInt(0);
//                DebugUtil.showDebug(FolderCategoryAct.ttttt + ", inverted 에 있는 did:: " + did);
                if (imagesNotInInvertedIndexDb.get(i).getId() == did) {
//                    DebugUtil.showDebug(FolderCategoryAct.ttttt + ", 이미 분류가 완료된 did :: " + did);
                    if (imagesNotInInvertedIndexDb != null && imagesNotInInvertedIndexDb.size() > i)
                        imagesNotInInvertedIndexDb.remove(i);
                    break;
                }
            }
        }
        subQueryCursor.close();

//        DebugUtil.showDebug(FolderCategoryAct.ttttt + ", 분류가 안된 did 개수 :: " + imagesNotInInvertedIndexDb.size());

        for (ImageFile imgfile : imagesNotInInvertedIndexDb) {
            DebugUtil.showDebug(FolderCategoryAct.ttttt + ", 분류해야하는 아이디:: " + imgfile.getId());
        }

        return imagesNotInInvertedIndexDb;
    }


//    public static ArrayList<ImageFile> getImagesHavingGPSInfoButNotInInvertedIndex(Context context) {
//        ContentResolver mCr;
//        ArrayList<ImageFile> images = new ArrayList<>();
//        ArrayList<ImageFile> imagesNotInInvertedIndexDb = new ArrayList<>();
//
//        String selectSql = "SELECT " + DatabaseConstantUtil.COLUMN_DID +
//                " FROM " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME +
//                " WHERE " + DatabaseConstantUtil.COLUMN_RANK + "=0";
//        Cursor subQueryCursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectSql, null);
//
//        mCr = context.getContentResolver();
//        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        String[] projection = {MediaStore.Images.Media._ID};
//        String selection = MediaStore.Images.ImageColumns.LATITUDE + " is not null and " + MediaStore.Images.ImageColumns.LONGITUDE + " is not null";
//        Cursor cursor = mCr.query(uri, projection, selection, null, null);
//
//        cursor.moveToFirst();
//        while (cursor.moveToNext()) {
//            ImageFile imageFile = new ImageFile();
//
//            Integer viewItemID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//            imageFile.setId(viewItemID);
////            DebugUtil.showDebug(FolderCategoryAct.ttttt + ", 위도 경도 있는 애들 :: " + viewItemID);
//
//            String imageFileUriPath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID));
//            imageFile.setPath(imageFileUriPath);
//
//            images.add(imageFile);
//        }
//        cursor.close();
//
//
//        imagesNotInInvertedIndexDb.addAll(images);
//
//        for (int i = imagesNotInInvertedIndexDb.size() - 1; i >= 0; i--) {
////            DebugUtil.showDebug(FolderCategoryAct.ttttt + "images having gps info :: " + imagesNotInInvertedIndexDb.get(i).getId() + "======");
//
//            while (subQueryCursor != null && subQueryCursor.moveToNext()) {
//                int did = subQueryCursor.getInt(0);
////                DebugUtil.showDebug(FolderCategoryAct.ttttt + ", inverted 에 있는 did:: " + did);
//                if (imagesNotInInvertedIndexDb.get(i).getId() == did) {
////                    DebugUtil.showDebug(FolderCategoryAct.ttttt + ", 이미 분류가 완료된 did :: " + did);
//                    if (imagesNotInInvertedIndexDb != null && imagesNotInInvertedIndexDb.size() > i)
//                        imagesNotInInvertedIndexDb.remove(i);
//                    break;
//                }
//            }
//        }
//        subQueryCursor.close();
//
////        DebugUtil.showDebug(FolderCategoryAct.ttttt + ", 분류가 안된 did 개수 :: " + imagesNotInInvertedIndexDb.size());
//
//        for (ImageFile imgfile : imagesNotInInvertedIndexDb) {
//            DebugUtil.showDebug(FolderCategoryAct.ttttt + ", 분류해야하는 아이디:: " + imgfile.getId());
//        }
//
//        return imagesNotInInvertedIndexDb;
//    }

    /**
     * 미디어 스토리지에 쿼리를 사용하여 앨범 가져오기
     *
     * @param context
     * @return
     */
    public static ArrayList<Album> getAlbums(Context context, String _orderBy) {
        ContentResolver mCr;
        ArrayList<Album> albums = new ArrayList<>();

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
        //폴더의 갯수가 정렬 기준
//        String orderBy = MediaStore.Images.Media.DEFAULT_SORT_ORDER; // 앨범 알파벳, 가나다순 정렬
//        String orderBy = MediaStore.Images.Media.DATE_ADDED + " desc"; // 추가된 시간 순
//        String orderBy = MediaStore.Images.Media.DATE_TAKEN + " desc"; // 찍은 시간 순
//        String orderBy = MediaStore.Images.Media.DATA; //경로 순
//        String orderBy = MediaStore.Images.Media.SIZE; //사이즈 순
//        String orderBy = MediaStore.Images.Media.BUCKET_DISPLAY_NAME; //가나다 순
        String orderBy = _orderBy;

        Cursor cursor = mCr.query(uri, projection, null, null, orderBy);

        ArrayList<String> ids = new ArrayList<String>();
        while (cursor.moveToNext()) {
            Album album = new Album();

            //앨범 아이디
            String albumId = getColumeValue(cursor, MediaStore.Images.Media.BUCKET_ID);
            album.setId(albumId);

            //앨범 이름
            String albumName = getColumeValue(cursor, MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            album.setName(albumName);

            //앨범의 경로
            String imageFilePath = getColumeValue(cursor, MediaStore.Images.Media.DATA);
            String albumPath = new File(imageFilePath).getParentFile().getPath();
            album.setPath(albumPath);

            if (!ids.contains(album.getId())) {

                //쿼리 결과로 테이블에서 나온 순서가 맨 앞에 있는 것이 앨범 커버로 저장 됨
                Integer albumCoverId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                album.setCoverID(albumCoverId);

                String albumCoverImagePath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + albumCoverId));
                album.setCoverImagePath(albumCoverImagePath);

                album.count = 1;

                albums.add(album);
                ids.add(album.getId());
            } else {
                albums.get(ids.indexOf(album.getId())).count++;
            }
        }
        cursor.close();
        return albums;
    }

    /**
     * 미디어 스토리지에 쿼리를 사용하여 앨범 가져오기
     *
     * @param context
     * @return
     */
    public static Album getAlbumsInSepecficLocation(Context context, String path) {
        Album album = new Album();

        File newFile = new File(path);
        String fileName = "";
        if (newFile.exists()) {
            fileName = newFile.getName();
            DebugUtil.showDebug("FileUtil, getAlbumsInSepecficLocation::" + fileName);
        }

        ContentResolver mCr;
        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media.DATA + " like '" + path + "%' and " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " like '" + fileName + "'";
        String orderBy = MediaStore.Images.Media.DEFAULT_SORT_ORDER; //폴더의 갯수가 정렬 기준, 지금은 앨범 알파벳, 가나다순 정렬
        Cursor cursor = mCr.query(uri, projection, selection, null, orderBy);

        while (cursor.moveToNext()) {

            //앨범 아이디
            String albumId = getColumeValue(cursor, MediaStore.Images.Media.BUCKET_ID);
            album.setId(albumId);

            //앨범 이름
            String albumName = getColumeValue(cursor, MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            album.setName(albumName);

            //앨범의 경로
            String imageFilePath = getColumeValue(cursor, MediaStore.Images.Media.DATA);
            String albumPath = new File(imageFilePath).getParentFile().getPath();
            album.setPath(albumPath);

        }
        cursor.close();
        return album;
    }


    public static int getImagesCount(Context context, Album album) {
        ContentResolver mCr;
        ArrayList<ImageFile> images = new ArrayList<>();

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String albumID = album.getId();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.BUCKET_ID + "=" + albumID;
        String orderBy = MediaStore.Images.Media.DATE_TAKEN; //이미지가 찍힌 날짜 순서 정렬
        Cursor cursor = mCr.query(uri, projection, selection, null, orderBy + " desc");

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public static int getAlbumCoverImageIds(Context context, Album album) {
        ContentResolver mCr;
        int result = 0;

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String albumID = album.getId();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.BUCKET_ID + "=" + albumID;
        String orderBy = SharedPreUtil.getInstance().getSharedPrefs().getString(SharedPreUtil.FOLDER_CATEGORY_ORDER_BY, MediaStore.Images.Media.DATE_TAKEN);
        Cursor cursor = mCr.query(uri, projection, selection, null, orderBy);
        while (cursor != null && cursor.moveToNext()) {

            result = cursor.getInt(0);
        }
        cursor.close();

        return result;
    }

    public static ArrayList<ImageFile> getImages(Context context, Album album) {
        ContentResolver mCr;
        ArrayList<ImageFile> images = new ArrayList<>();

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String albumID = album.getId();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.BUCKET_ID + "=" + albumID;
        String orderBy = MediaStore.Images.Media.DATE_TAKEN; //이미지가 찍힌 날짜 순서 정렬
//        Cursor cursor = mCr.query(uri, projection, selection, null, orderBy + " desc" + " limit 0, 30");
        Cursor cursor = mCr.query(uri, projection, selection, null, FolderCategoryAct.imageOrderby + " limit 0, 30");

        while (cursor.moveToNext()) {
            ImageFile imageFile = new ImageFile();

            Integer viewItemID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            imageFile.setId(viewItemID);

            String imageFileUriPath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID));
            imageFile.setPath(imageFileUriPath);

            images.add(imageFile);
        }
        cursor.close();

        return images;
    }

    public static ArrayList<ImageFile> getImages(Context context, Album album, int start, int limit) {
        ContentResolver mCr;
        ArrayList<ImageFile> images = new ArrayList<>();

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String albumID = album.getId();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.BUCKET_ID + "=" + albumID;
//        String orderBy = MediaStore.Images.Media.DATE_TAKEN + " desc"; //이미지가 찍힌 날짜 순서 정렬
        Cursor cursor = mCr.query(uri, projection, selection, null, FolderCategoryAct.imageOrderby + " limit " + start + ", " + limit);

        while (cursor.moveToNext()) {
            ImageFile imageFile = new ImageFile();

            Integer viewItemID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            imageFile.setId(viewItemID);

            String imageFileUriPath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID));
            imageFile.setPath(imageFileUriPath);

            images.add(imageFile);
        }
        cursor.close();

        return images;
    }

    public static ArrayList<ImageFile> getImagesHavingGPSInfoInSpecificAlbum(Context context, Album album) {
        ContentResolver mCr;
        ArrayList<ImageFile> images = new ArrayList<>();

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String albumID = album.getId();
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.BUCKET_ID + "=" + albumID + " and " + MediaStore.Images.ImageColumns.LATITUDE + " is not null and " + MediaStore.Images.ImageColumns.LONGITUDE + " is not null";
        String orderBy = MediaStore.Images.Media.DATE_TAKEN; //이미지가 찍힌 날짜 순서 정렬
//        Cursor cursor = mCr.query(uri, projection, selection, null, orderBy + " desc" + " limit 0, 30");
        Cursor cursor = mCr.query(uri, projection, selection, null, FolderCategoryAct.imageOrderby + " limit 0, 30");

        while (cursor.moveToNext()) {
            ImageFile imageFile = new ImageFile();

            Integer viewItemID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            imageFile.setId(viewItemID);

            String imageFileUriPath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID));
            imageFile.setPath(imageFileUriPath);

            images.add(imageFile);
        }
        cursor.close();

        return images;
    }


    public static ImageFile getSpecificImageInfo(Context context, Integer id) {
        ImageFile imageFile = new ImageFile();
        ContentResolver mCr = context.getContentResolver();
        Uri uri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + id);
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.ORIENTATION, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.DATE_TAKEN};
        String orderBy = MediaStore.Images.Media.DATE_TAKEN; //이미지가 찍힌 날짜 순서 정렬
        Cursor cursor = mCr.query(uri, projection, null, null, orderBy + " desc");

        while (cursor.moveToNext()) {

            Integer viewItemID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            imageFile.setId(viewItemID);

            String imageFileUriPath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID));
            imageFile.setPath(imageFileUriPath);

            //이미지 사이즈
            String imageSize = getColumeValue(cursor, MediaStore.Images.Media.SIZE);
            imageFile.setSize(imageSize);

            //이미지 촬영 시간
            String date_taken = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
            imageFile.setDate_taken(date_taken);

            //이미지 생성 시간
            String date_added = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
            imageFile.setDate_added(date_added);

            //이미지 수정 시간
            String date_modified = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
            imageFile.setDate_modified(date_modified);

            //이미지 회전 각도
            Integer orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
            imageFile.setOrientation(orientation);

            String albumCoverImagePath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)));
            imageFile.setRecentImageFile(albumCoverImagePath);

        }
        cursor.close();

        return imageFile;
    }

    //카테고리가 같은 dIDs들 중에서 특정 경로의 did의 개수를 가져온다
    public static Integer getCountOfCategoryInCategoryFragInAlbum(Context context, Integer cID, ArrayList<Integer> dIDsInsideCategoryFragInAlbum, Album album) {
        int count = 0;
        String bucketIdAlbum = album.getId();

        ContentResolver mCr;
        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.BUCKET_ID};
        String select = MediaStore.Images.Media.BUCKET_ID + "=" + bucketIdAlbum;
        Cursor cursor = mCr.query(uri, projection, select, null, null);

        while (cursor.moveToNext()) {

            for (Integer dIDsInsideSameCId : dIDsInsideCategoryFragInAlbum) {
                if (dIDsInsideSameCId == cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))) {
                    count++;
                    break;
                }
            }
            //앨범 아이디
            String id = getColumeValue(cursor, MediaStore.Images.Media._ID);
            String name = getColumeValue(cursor, MediaStore.Images.Media.TITLE);
//            count = cursor.getCount();
        }
        cursor.close();

        return count;
    }

    //카테고리가 같은 dIDs들 중에서 특정 경로의 did를 가져온다
    public static ArrayList<Integer> getDidsCategoryInCategoryFragInAlbum(Context context, Integer cID, ArrayList<Integer> dIDsInsideCategoryFragInAlbum, Album album) {
        String bucketIdAlbum = album.getId();
        ArrayList<Integer> resultDids = new ArrayList<>();

        ContentResolver mCr;
        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.BUCKET_ID};
        String select = MediaStore.Images.Media.BUCKET_ID + "=" + bucketIdAlbum;
        Cursor cursor = mCr.query(uri, projection, select, null, null);

        while (cursor.moveToNext()) {

            for (Integer dIDsInsideSameCId : dIDsInsideCategoryFragInAlbum) {
                if (dIDsInsideSameCId == cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))) {
                    resultDids.add(dIDsInsideSameCId);
                    break;
                }

            }
        }
        cursor.close();

        return resultDids;
    }

    public static ArrayList<ImageFile> getImagesUsingDids(Context context, ArrayList<Integer> dIDsInSameCategoryAndSameDir, Album album) {
        String bucketIdAlbum = album.getId();

        //미디어 스토리지 이용하는 방법
        ContentResolver mCr;
        ArrayList<ImageFile> images = new ArrayList<>();

        // 미디어 쿼리 사용해서 특정 경로에 있는 이미지들 가지고 오기
        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {"*"};
        String select = MediaStore.Images.Media.BUCKET_ID + "=" + bucketIdAlbum;
        String orderBy = MediaStore.Images.Media.DATE_TAKEN; //이미지가 찍힌 날짜 순서대로 정렬할 것
        Cursor cursor = mCr.query(uri, projection, select, null, null);


        while (cursor.moveToNext()) {

            for (Integer dIDsInsideSameCId : dIDsInSameCategoryAndSameDir) {
                if (dIDsInsideSameCId == cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))) {

                    ImageFile imageFile = new ImageFile();

                    //이미지 아이디
                    Integer viewItemID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    imageFile.setId(viewItemID);

                    //이미지 이름
                    String albumName = getColumeValue(cursor, MediaStore.Images.Media.DISPLAY_NAME);
                    imageFile.setName(albumName);

                    //이미지의 경로
                    String imageFilePath = getColumeValue(cursor, MediaStore.Images.Media.DATA);
                    String imageFileUriPath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + viewItemID));
                    imageFile.setPath(imageFileUriPath);


                    //이미지 사이즈
                    String imageSize = getColumeValue(cursor, MediaStore.Images.Media.SIZE);
                    imageFile.setSize(imageSize);

                    //이미지 촬영 시간
                    String date_taken = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                    imageFile.setDate_taken(date_taken);

                    //이미지 생성 시간
                    String date_added = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    imageFile.setDate_added(date_added);

                    //이미지 수정 시간
                    String date_modified = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
                    imageFile.setDate_modified(date_modified);

                    //이미지 회전 각도
                    Integer orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                    imageFile.setOrientation(orientation);


//            String albumCoverImagePath = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + albumCoverId));
//            imageFile.setCoverImagePath(albumCoverImagePath);


                    images.add(imageFile);

                    break;
                }
            }
        }
        cursor.close();

        return images;
    }

    /**
     * 카테고리 아이디별 이미지 검색하는 쿼리(앨범 내에 있는 이미지 파일들 내부에서)
     */
    public static ArrayList<ImageFile> getImagesCategoryInCategoryFragInAlbum(Context context, Integer cID, ArrayList<Integer> dIDsInsideCategoryFragInAlbum, Album album) {
        String bucketIdAlbum = album.getId();
        ArrayList<ImageFile> imageFiles = new ArrayList<>();

        ContentResolver mCr;
        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.BUCKET_ID};
        String select = MediaStore.Images.Media.BUCKET_ID + "=" + bucketIdAlbum;
        Cursor cursor = mCr.query(uri, projection, select, null, null);

        while (cursor.moveToNext()) {
            for (Integer dIDsInsideSameCId : dIDsInsideCategoryFragInAlbum) {
                if (dIDsInsideSameCId == cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))) {
                    ImageFile imageFile1 = new ImageFile();
                    imageFile1.setId(dIDsInsideSameCId);
                    String imageFileUri = FileUtil.getImagePath(context, Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))));
                    imageFile1.setPath(imageFileUri);
                    imageFiles.add(imageFile1);
                    break;
                }

            }
        }
        cursor.close();

        return imageFiles;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    /**
     * Gets the last image id from the media store
     *
     * @return
     */
    public static int getLastImageId(Context context) {
        ContentResolver mCr;
        mCr = context.getContentResolver();
        final String[] imageColumns = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
        if (imageCursor.moveToFirst()) {
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            String fullPath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            DebugUtil.showDebug("getLastImageId::id " + id);
            DebugUtil.showDebug("getLastImageId::path " + fullPath);
            imageCursor.close();
            return id;
        } else {
            return 0;
        }
    }

    /**
     * 이미지의 고유한 아이디를 받아 해당 이미지의 경로를 만들어내는 함수
     * 이미지 로더를 이용할 때는 result를 사용하면 나오지 않음에 주의
     *
     * @param context
     * @param _ID
     * @return 해당 이미지 경로
     */
    public static String getImagePathUsingImageUniqueID(Context context, Integer _ID) {
        String result = new String();

        //미디어 스토리지 이용하는 방법
        ContentResolver mCr = context.getContentResolver();
        Uri uri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + _ID);

        String[] projection = {MediaStore.Images.ImageColumns.DATA};
//        String selection = MediaStore.Images.Media._ID + "=" + _ID;
        Cursor cursor = mCr.query(uri, projection, null, null, null);

        while (cursor.moveToNext()) {
            result = getColumeValue(cursor, projection[0]);
            DebugUtil.showDebug("FileUtil, getImagePathUsingImageUniqueID, result::" + result);
        }
        cursor.close();

        return result;
    }



    //
    public static ArrayList<ImageFile> createViewItemsHavingPath(Context context, ArrayList<ImageFile> items) {
        ArrayList<ImageFile> itemsWithNoPath = items;
        ArrayList<ImageFile> lists = new ArrayList<>();

        //미디어 스토리지 이용하는 방법
        ContentResolver mCr;
        String result = "";

        mCr = context.getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projectionForAlbumImage = {MediaStore.Images.ImageColumns.DATA};

        if (itemsWithNoPath == null) {
            return null;
        }

        for (ImageFile vi : itemsWithNoPath) {
            String selection = MediaStore.Images.ImageColumns._ID + "= " + vi.getId();
            Cursor cursor = mCr.query(uri, projectionForAlbumImage, selection, null, null);
            while (cursor.moveToNext()) {
                result = cursor.getString(cursor.getColumnIndex(projectionForAlbumImage[0]));
                DebugUtil.showDebug("FileUtil, createViewItemsHavingPath() result : " + result);
                vi.setPath(result);
//                ImageFile imageFile = new ImageFile(result, false);
//                imageFile.setId(vi.getId());
//                lists.add(imageFile);
            }
            cursor.close();
        }
        return itemsWithNoPath;
    }


    public static Integer itemsCountInsideCategory(Integer clickedcId) {
        int count;

        if (DatabaseCRUD.getViewItemsWithSpecificCId(clickedcId) == null) {
            return 0;
        } else {
            count = DatabaseCRUD.getViewItemsWithSpecificCId(clickedcId).size();
        }
        return count;
    }


    /**
     * 파일 경로로부터 파일의 이름만 가져오는 메소드
     *
     * @param path
     * @return
     */
    public static String getFileNameFromPath(String path) {
        File temp = new File(path);
        return temp.getName();
    }

    /**
     * 파일 필터
     */
    public static class ImageFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            } else if (isImageFile(file.getAbsolutePath())) {
                return true;
            }
            return false;
        }
    }

    /**
     * 어떤 폴더 내에서 마지막에 있는 이미지의 경로를 리턴한다.
     */
    public static String getLatestImagesFilePath(String parentFile) {
        File temp = new File(parentFile);
        String lastImagePath = "";

        if (!temp.exists()) {
            return "";
        }

        if (temp.isDirectory()) {

            File[] childFiles = temp.listFiles();

            for (File childFile : childFiles) {
                if (childFile.isFile()) {

                    if (FileUtil.isImageFile(childFile.getPath())) {
                        lastImagePath = childFile.getPath();
                    }
                }
            }

//            lastImagePath = childFiles[childFiles.length-1].getPath();
//            DebugUtil.showDebug("FileUtil, getLatestImagesFilePath, lastImagePath : " + lastImagePath);
        }

        return lastImagePath;
    }

    /**
     * 특정 확장자를 가진 파일인지를 확인하는 메소드
     *
     * @param filePath
     * @return .jpg, .png, .bmp, .gif로 끝나면 true, 아니면 false
     */
    public static boolean isImageFile(String filePath) {
        if (filePath.endsWith(".jpg") || filePath.endsWith(".png") || filePath.endsWith(".bmp") || filePath.endsWith(".gif")
                || filePath.endsWith(".JPG") || filePath.endsWith(".PNG") || filePath.endsWith(".BMP") || filePath.endsWith(".GIF"))
        // Add other formats as desired
        {
            return true;
        }
        return false;
    }


    /**
     * 상위 폴더의 경로를 반환하는 함수
     */
    public static String getParentPath(String path) {
        File cFile = new File(path);
        File parentFile = cFile.getParentFile();
        return parentFile.getAbsolutePath();
    }


    /**
     * 파일을 복사하는 메소드
     */
    public static void copyFile(String src, String dest) {
        long fsize = 0;
        try {
            File newFile = new File(src);
            String fileName = newFile.getName();
            DebugUtil.showDebug("FileUtil, fileName ::" + fileName);

            FileInputStream fin = new FileInputStream(src);
            FileOutputStream fout = new FileOutputStream(dest + "/" + fileName);

            FileChannel inc = fin.getChannel();
            FileChannel outc = fout.getChannel();

            //복사할 file size
            fsize = inc.size();

            inc.transferTo(0, fsize, outc);

            inc.close();
            outc.close();
            fin.close();
            fout.close();
            //입출력을 위한 버퍼를 할당한다.
            ByteBuffer buf = ByteBuffer.allocateDirect(1024);


            while (true) {
                if (inc.read(buf) == -1)
                    break;
                buf.flip();
                outc.write(buf);
                buf.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void moveFile(String src, String destFolderPath) {

        try {
            File afile = new File(src);

            if (afile.renameTo(new File(destFolderPath + "/" + afile.getName()))) {
                System.out.println("File is moved successful!");
            } else {
                System.out.println("File is failed to move!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 파일 및 폴더 삭제함수
     */
    public static void removeDir(Context context, String path) {
        String mRootPath = path;
        final Context ctx = context;

        File file = new File(mRootPath);
        File[] childFileList = file.listFiles();
        if (childFileList != null) {
            for (File childFile : childFileList) {
                final File chlFile = childFile;
                if (childFile.isDirectory()) {
                    removeDir(context, childFile.getAbsolutePath());    //하위 디렉토리
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            deleteImage(ctx, chlFile.getPath());
                            chlFile.delete();    //하위 파일
                        }
                    }, 0);

                }
            }
            file.delete();    //root 삭제
        } else { //널 인 경우
            removeFile(mRootPath);
        }
    }

    public static void removeFile(String path) {
        String removeFilePath = path;

        File file = new File(removeFilePath);
        if (file != null) {
            file.delete();
        }
    }


    public static void deleteImage(Context context, String file_path) {
        final Context con = context;
        final File fdelete = new File(file_path);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.e("-->", "file Deleted :" + file_path);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        // Set up the projection (we only need the ID)
                        String[] projection = {MediaStore.Images.Media._ID};

                        // Match on the file path
                        String selection = MediaStore.Images.Media.DATA + " = ?";
                        String[] selectionArgs = new String[]{fdelete.getAbsolutePath()};

                        // Query for the ID of the media matching the file path
                        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        ContentResolver contentResolver = con.getContentResolver();
                        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
                        if (c.moveToFirst()) {
                            // We found the ID. Deleting the item via the content provider will also remove the file
                            int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                            contentResolver.delete(deleteUri, null, null);

                            DatabaseCRUD.deleteSpecificIdQuery(id);
                            DebugUtil.showDebug("FileUtil, deleteImage, deleting image:: " + id);

                        } else {
                            // File not found in media store DB
                        }
                        c.close();
                    }
                });


                callBroadCast(context);
            } else {
                Log.e("-->", "file not Deleted :" + file_path);
            }
        }
    }

    public static void callBroadCast(Context context) {
        if (context == null) {
            DebugUtil.showDebug("설마 널? ");
            return;
        }
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.e("-->", " < 14");
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

        }
    }

    public static ArrayList<Integer> findImagesInSearchResultAct(Context context, String partial_file_name) {
        final ArrayList<Integer> results = new ArrayList<>();

        final Context con = context;
        final ContentResolver mCr = context.getContentResolver();
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE};
        final String selection = MediaStore.Images.Media.TITLE + " like '%" + partial_file_name.toString() + "%'";
//        final String[] selectionArgs = new String[]{partial_file_name};

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Cursor c = mCr.query(uri, projection, selection, null, null);
                DebugUtil.showDebug("[search] c.toString()::" + c.toString());
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    results.add(id);
                    DebugUtil.showDebug("[search] found, id:::::::" + id + ", size::" + results.size());
                }
//                if (c.moveToFirst()) {
//                    // We found the ID. Deleting the item via the content provider will also remove the file
//                    int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
//                    DebugUtil.showDebug("[search] found, id:::::::"+id );
//                } else {
//                    // File not found in media store DB
//                    DebugUtil.showDebug("[search] findImagesInSearchResultAct, search not matched");
//                }
                c.close();
            }
        }, 1000);
        return results;
    }

    public static String viewColumnInfoOfSpecificImageFile(Context context, Integer id, String columnName) {
        String result = "";
        final Context con = context;
        final ContentResolver mCr = context.getContentResolver();
        final Uri uri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + id);
        final String[] projection = {columnName};

        Cursor c = mCr.query(uri, projection, null, null, null);
        while (c != null && c.moveToNext()) {
            String data = c.getString(c.getColumnIndexOrThrow(projection[0])).toString();
//            DebugUtil.showDebug("FileUtil, viewColumnInfoOfSpecificImageFile(), image :: " + id + "'s path :: " + data);
            result = "FileUtil, viewColumnInfoOfSpecificImageFile(), image :: " + id + "'s path :: " + data;
        }
        return result;
    }

    public static String viewIdOfSpecificImageFileUsingPath(Context context, String path, String columnName){
        String result = "";
        final Context con = context;
        final ContentResolver mCr = context.getContentResolver();
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String selection = MediaStore.Images.Media.DATA + " = '" + path +"'";
        final String[] projection = {columnName};

        Cursor c = mCr.query(uri, projection, selection, null, null);
        while (c != null && c.moveToNext()) {
            String data = c.getString(c.getColumnIndexOrThrow(projection[0])).toString();
            result += "FileUtil, viewIdOfSpecificImageFileUsingPath(), image :: " + path + "'s "+ columnName+" :: " + data +"\n";
        }
        return result;
    }

    public static String viewColumnInfoOfSpecificAlbum(Context context, String buckedId) {
        String result = "";
        final Context con = context;
        final ContentResolver mCr = context.getContentResolver();
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        final String selection = MediaStore.Images.Media.BUCKET_ID + "=" + buckedId;
        Cursor c = mCr.query(uri, projection, selection, null, null);

        while (c != null && c.moveToNext()) {
            String data = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            result += "rename folder::FileUtil, viewColumnInfoOfSpecificAlbum(), buckedId :: " + buckedId + "'s data :: " + data + "\n";
        }
        return result;
    }

    public static String updateAlbumName(Context context, String buckedId, String newFilePath) {


        String result = "";
        final ContentResolver mCr = context.getContentResolver();
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        final String selection = MediaStore.Images.Media.BUCKET_ID + "=" + buckedId;
        Cursor c = mCr.query(uri, projection, selection, null, null);

        while (c != null && c.moveToNext()) {
            Integer did = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            String titie = c.getString(c.getColumnIndex(MediaStore.Images.Media.TITLE));
            String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            File file = new File(path);
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, newFilePath + "/" + file);
                DebugUtil.showDebug("rename folder::" + values.toString());
                mCr.update(Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + did), values, null, null);
            }

        }
        return result;
    }

}