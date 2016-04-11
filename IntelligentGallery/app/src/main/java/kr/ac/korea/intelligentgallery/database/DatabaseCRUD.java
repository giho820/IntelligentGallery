package kr.ac.korea.intelligentgallery.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dilab.sampledilabapplication.Sample.Models.SampleScoreData;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import kr.ac.korea.intelligentgallery.act.GalleryAct;
import kr.ac.korea.intelligentgallery.data.Category;
import kr.ac.korea.intelligentgallery.data.ImageFile;
import kr.ac.korea.intelligentgallery.database.util.DatabaseConstantUtil;
import kr.ac.korea.intelligentgallery.intelligence.Sample.Model.ContentScoreData;
import kr.ac.korea.intelligentgallery.util.DebugUtil;

public class DatabaseCRUD {

    private static SQLiteDatabase sqLiteDatabase;
    private static Cursor cursor;
    private static String keyword;

    public static boolean checkTable(DatabaseHelper databaseHelper, SQLiteDatabase sqLiteDatabase, String tableName) {

        boolean flag = true;

        if (sqLiteDatabase == null) {
            DebugUtil.showDebug("SQLiteDatabase is null");
            flag = false;
            return flag;
        }

        String sql_check_whether_table_exist_or_not = "SELECT count(*) as check_table FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(sql_check_whether_table_exist_or_not, null);
        cursor.moveToFirst();

        DebugUtil.showDebug("cursor count : " + cursor.getInt(cursor.getColumnIndex("check_table")));
        if (cursor.getInt(cursor.getColumnIndex("check_table")) != 1) {
            flag = false;
        }
        DebugUtil.showDebug("flag : " + flag);

        return flag;

    }

    public static void execRawQuery(String _query) {
        DatabaseHelper.sqLiteDatabase.execSQL(_query);
    }

    public static void deleteSpecificIdQuery(Integer id) {
        String deleteQuery = "delete from " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + " where " + DatabaseConstantUtil.COLUMN_DID + "=" + id + ";";
        execRawQuery(deleteQuery);
        DebugUtil.showDebug("DatabaseCRUD, deleteSpecificIdQuery, " + deleteQuery);
    }

    //카테고리 아이디별 대표이미지 검색하는 쿼리(Rank가 0으로 되어있는 이미지 id)
    public static Integer queryForCategoryRepresentImageIdUsingCID(Integer cID) {
        Integer result = null;
        cursor = DatabaseHelper.sqLiteDatabase.rawQuery("select " + DatabaseConstantUtil.COLUMN_DID + " from " +
                DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + " where " + DatabaseConstantUtil.COLUMN_CATEGORY_ID + " = " + cID + " and " +
                DatabaseConstantUtil.COLUMN_RANK + " = 0 order by " + DatabaseConstantUtil.COLUMN_DID + " desc limit 1;", null);

        while (cursor != null && cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    /**
     * 카테고리 아이디별 대표이미지 검색하는 쿼리(앨범 내에 있는 이미지 파일들 내부에서)
     */
    public static Integer queryForCategoryRepresentImageIdUsingCIDinCategroyFragInAlbum(Integer cID, ArrayList<ImageFile> imagesInFolder) {
        Integer result = null;

        for (ImageFile imageFile : imagesInFolder) {
            cursor = DatabaseHelper.sqLiteDatabase.rawQuery("select " + DatabaseConstantUtil.COLUMN_DID + " from " +
                    DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + " where " + DatabaseConstantUtil.COLUMN_CATEGORY_ID + " = " + cID + " and " +
                    DatabaseConstantUtil.COLUMN_DID + "=" + imageFile.getId()
                    + " order by " + DatabaseConstantUtil.COLUMN_SCORE + " desc limit 1;", null);

            if (cursor == null) {
                return 0;
            }
            while (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }
            cursor.close();
        }

        return result;
    }


    public static String selectQuery() {

        String result = "";

        //역순으로 검색되도록 변경
        cursor = DatabaseHelper.sqLiteDatabase.rawQuery("select * from " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME, null);
        if (cursor == null)
            return null;

        while (cursor.moveToNext()) {
            result += cursor.getInt(0)
                    + ". "
                    + cursor.getString(1)
                    + " | "
                    + cursor.getString(2)
                    + " | "
                    + cursor.getString(3)
                    + " | "
                    + cursor.getString(4)
                    + "\n";
        }

        cursor.close();
        return result;
    }

    public static ArrayList<Integer> getImagesIdsInInvertedIndexDb() {
        ArrayList<Integer> result = new ArrayList<>();

        cursor = DatabaseHelper.sqLiteDatabase.rawQuery("select distinct " + DatabaseConstantUtil.COLUMN_DID + " from " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME
                + " where " + DatabaseConstantUtil.COLUMN_RANK + "=0", null);

//        cursor.moveToFirst();
        while (cursor != null && cursor.moveToNext()) {
//            if (cursor.getCount() >0 && result.size() > cursor.getPosition())
            if (cursor.getCount() > 0)
                result.add(cursor.getInt(0));
        }

        cursor.close();

        return result;
    }

    public static ArrayList<Integer> dIDsInsideCategoryFragInAlbumUsingCId(Integer cID) {
        ArrayList<Integer> integers = new ArrayList<>();
        String selectQuery =
                "SELECT distinct " + DatabaseConstantUtil.COLUMN_DID + " FROM " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + " where " + DatabaseConstantUtil.COLUMN_CATEGORY_ID + "=" + cID + " and " + DatabaseConstantUtil.COLUMN_RANK + "=0;";

        cursor = DatabaseHelper.sqLiteDatabase.rawQuery("PRAGMA case_sensitive_like = 'TRUE' ", null);
        cursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectQuery, null);

        if (cursor == null)
            return null;

        if (cursor.getCount() <= 0)
            return null;

        while (cursor.moveToNext()) {
            int dID = cursor.getInt(0);
//            DebugUtil.showDebug("dIDsInsideCategoryFragInAlbumUsingCId, dID : " + dID);
            integers.add(dID);
        }
        cursor.close();
        return integers;
    }


    //카테고리 데이터베이스에 존재하는 카테고리 아이디들의 목록을 만든다
    public static ArrayList<Integer> selectCategoryList() {
        //대표적인 카테고리 아이디를 검색하는 쿼리
        String selectSql = "SELECT DISTINCT " + DatabaseConstantUtil.COLUMN_CATEGORY_ID + " FROM " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + ";";
        ArrayList<Integer> list = new ArrayList<>();

        cursor = DatabaseHelper.sqLiteDatabase.rawQuery("PRAGMA case_sensitive_like = 'TRUE' ", null);
        cursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectSql, null);

        while (cursor != null && cursor.moveToNext()) {
            int categoryID;
            categoryID = cursor.getInt(0);
            list.add(categoryID);
        }
        cursor.close();
        return list;
    }

    /**
     * 카테고리 데이터베이스에 존재하는 카테고리 아이디들의 목록을 만든다
     *
     * @return
     */
    public static ArrayList<Category> selectCategoryFragInAlbumCategoryList(ArrayList<ImageFile> imageFiles) {
        String TAG = "selectCategoryFragInAlbumCategoryList";
        ArrayList<ImageFile> list = imageFiles;
        ArrayList<Category> categoryArrayList = new ArrayList<>();
        if (list == null) {
            return null;
        }
//        DebugUtil.showDebug(TAG + "list::size::" + list.size());
        for (ImageFile ifs : list) {
//            DebugUtil.showDebug(TAG + "ifs::" + ifs.getId());

            //대표적인 카테고리 아이디를 검색하는 쿼리
            String selectSql = "SELECT " + DatabaseConstantUtil.COLUMN_CATEGORY_ID +
                    " FROM " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME
                    + " where " + DatabaseConstantUtil.COLUMN_DID + "=" + ifs.getId()
                    + " and " + DatabaseConstantUtil.COLUMN_RANK + "=0";
            Cursor cursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectSql, null);

//            if (cursor == null) {
//                cursor.close();
//                return null;
//            }
//            if (cursor.getCount() <= 0) {
//                cursor.close();
//                return null;
//            }

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                Category category = new Category();
                category.setcID(cursor.getInt(0));
                category.setCoverImageId(ifs.getId());
                ArrayList<ImageFile> tempImageFiles = new ArrayList<>();
                tempImageFiles.add(ifs);
                category.setContainingImages(tempImageFiles);
//            while (cursor.moveToNext()) {
//                int categoryID = cursor.getInt(0);
//                cIDsInImageList.add(categoryID);
//            }
                categoryArrayList.add(category);
            }
            cursor.close();

        }

        return categoryArrayList;
    }

    public static ArrayList<ImageFile> getViewItemsWithSpecificCId(Integer clickedcId) {
        ArrayList<ImageFile> viewItemsWithSpecificCidImageFile = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + " where " + DatabaseConstantUtil.COLUMN_RANK + "=0 and " + DatabaseConstantUtil.COLUMN_CATEGORY_ID + "=" + clickedcId + " order by " + DatabaseConstantUtil.COLUMN_DID + " desc ;";

        Cursor cursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectQuery, null);

        while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
            ImageFile imageFile = new ImageFile();
            imageFile.setIsDirectory(false);
            imageFile.setId(cursor.getInt(1));
//            imageFile.setPath(cursor.getString(1));// 수정 됨;;(건내줄 때 반드시 변경할 것)
            imageFile.setCategoryId(cursor.getInt(2));
            imageFile.setIsChecked(false);
            imageFile.setRank(cursor.getInt(3));
            imageFile.setScore(cursor.getFloat(4));
            imageFile.setRecentImageFile(cursor.getString(1));
            viewItemsWithSpecificCidImageFile.add(imageFile);
        }

        cursor.close();

        return viewItemsWithSpecificCidImageFile;
    }

    public static ArrayList<LinkedHashMap> getContentScoreDataArray(ArrayList<Integer> cIDs) {
        ArrayList<LinkedHashMap> maps = new ArrayList<>();

        ArrayList<ContentScoreData> ContentScoreDatas = new ArrayList<>();

        for (Integer cID : cIDs) {
            String selectSql = "SELECT *" + " FROM " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME
                    + " where " + DatabaseConstantUtil.COLUMN_CATEGORY_ID + "=" + cID + ";";
            cursor = DatabaseHelper.sqLiteDatabase.rawQuery("PRAGMA case_sensitive_like = 'TRUE' ", null);
            cursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectSql, null);

            if (cursor == null)
                return null;

            if (cursor.getCount() <= 0)
                return null;

            LinkedHashMap<Integer, ContentScoreData> a_mapScores = new LinkedHashMap<Integer, ContentScoreData>();
            while (cursor.moveToNext()) {
                ContentScoreData ContentScoreData = new ContentScoreData();
                int dID = cursor.getInt(1);
                ContentScoreData.setContentsID(dID);
                ContentScoreData.setGraphScore(cursor.getDouble(4));
                ContentScoreDatas.add(ContentScoreData);
                a_mapScores.put(cID, ContentScoreData);
//                DebugUtil.showDebug("DatabaseCRUD, getContentScoreDataArray(), a_mapScores : " + a_mapScores.get(cID).getContentsID() + ", " + a_mapScores.get(cID).getGraphScore());
            }
            cursor.close();

            maps.add(a_mapScores);
        }
        return maps;
    }

    public static ArrayList<ContentScoreData> getContentScoreData(int cID) {
        ArrayList<ContentScoreData> ContentScoreDatas = new ArrayList<>();
        String selectSql = "SELECT * FROM " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME
                + " where " + DatabaseConstantUtil.COLUMN_CATEGORY_ID + "=" + cID + " AND " + DatabaseConstantUtil.COLUMN_RANK + " != 0 ORDER BY " + DatabaseConstantUtil.COLUMN_SCORE + " DESC;";
        cursor = DatabaseHelper.sqLiteDatabase.rawQuery("PRAGMA case_sensitive_like = 'TRUE' ", null);
        cursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectSql, null);

        LinkedHashMap<Integer, ContentScoreData> a_mapScores = new LinkedHashMap<Integer, ContentScoreData>();
        while (cursor != null && cursor.moveToNext()) {
            ContentScoreData ContentScoreData = new ContentScoreData();
            int dID = cursor.getInt(1);
            ContentScoreData.setContentsID(dID);
            ContentScoreData.setGraphScore(cursor.getDouble(4));
            ContentScoreDatas.add(ContentScoreData);
            a_mapScores.put(cID, ContentScoreData);
        }
        cursor.close();
        return ContentScoreDatas;
    }


    public static ArrayList<SampleScoreData> getScoreDatasUsingDidThatSizeIsK(Integer currentImageFileImageId, Integer k) {
        ArrayList<SampleScoreData> scoreDatas = new ArrayList<>();
        String selectSql = "select * from " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME
                + " where " + DatabaseConstantUtil.COLUMN_DID + "=" + currentImageFileImageId
                + " and " + DatabaseConstantUtil.COLUMN_RANK +" != 0";

        cursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectSql, null);

        if(cursor.getCount() == 0l) {
            DebugUtil.showDebug(GalleryAct.correctTopk +" DatabaseCRUD, getScoreDatasUsingDidThatSizeIsK, 이건 db에 없어서 쿼리 결과 없음");
        }
        while (cursor != null && cursor.moveToNext()) {//&& cursor.getCount() == k
            int categoryId = cursor.getInt(2);
            double score = cursor.getDouble(4);
            SampleScoreData sampleScoreData = new SampleScoreData(categoryId, score);
            DebugUtil.showDebug(GalleryAct.correctTopk + ", cid:: " + sampleScoreData.getID() + " 's score:: " + sampleScoreData.getScore());
            scoreDatas.add(sampleScoreData);
        }
        return scoreDatas;
    }

    public static Integer getCategoryIdUsingImageId(Integer did) {
        Integer cid = 0;
        String selectSql = "select "+ DatabaseConstantUtil.COLUMN_CATEGORY_ID +" from " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME
                + " where " + DatabaseConstantUtil.COLUMN_DID + "=" + did
                + " and " + DatabaseConstantUtil.COLUMN_RANK + "=0";

        cursor = DatabaseHelper.sqLiteDatabase.rawQuery(selectSql, null);

        if(cursor.getCount() == 0l) {
            DebugUtil.showDebug("DatabaseCRUD,  getCategoryIdUsingImageId(), 분류가 되어있지 않은 이미지");
        }
        while (cursor != null && cursor.moveToNext()) {//&& cursor.getCount() == k
            cid = cursor.getInt(0);
            DebugUtil.showDebug("DatabaseCRUD,  getCategoryIdUsingImageId(), 현재 이미지의 cid :: " + cid);
        }
        return cid;
    }
}
