package kr.ac.korea.intelligentgallery.database.util;


public class DatabaseConstantUtil {

    public static final String DATABASE_SQLITE_NAME = "IntelligentGallery.sqlite"; //큰 의미 없음
    public static final String DATABASE_DB_NAME = "IntelligentGallery.db";
    public static final String PAKAGE_NAME = "kr.ac.korea.intelligentgallery";
    public static final String DATABASE_PATH = "/data/data/" + PAKAGE_NAME + "/databases/" + DATABASE_DB_NAME;

    //inverted index table for category
    public static final String TABLE_INTELLIGENT_GALLERY_NAME = "INTELLIGENT_GALLERY";
    public static final String COLUMN_AUTO_INCREMENT_KEY = "IDX";
    public static final String COLUMN_DID = "ID";
    public static final String COLUMN_CATEGORY_ID = "CID";
    public static final String COLUMN_RANK = "RANK";
    public static final String COLUMN_SCORE = "SCORE";

    //컨텐츠 앨범 테이블
    public static final String TABLE_INTELLIGENT_GALLERY_CONTENT_ALBUM_NAME = "intelligent_gallery_content_album_table";
    public static final String COLUMN_AUTO_INCREMENT_KEY_CONTENT_ALBUM = "idx";
    public static final String COLUMN_ALBUM_ID = "id";
    public static final String COLUMN_ALBUM_CONTENTS_COUNT = "count";

    //컨텐츠가 담긴 테이블, id, path, longitude, latitude, album_id

    public static final String CREATE_INTELLIGENT_GALLERY_TABLE = "create table " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME + "(" +
            DatabaseConstantUtil.COLUMN_AUTO_INCREMENT_KEY + " integer primary key autoincrement Not null UNIQUE, " +
            DatabaseConstantUtil.COLUMN_DID + " text Not null, " +
            DatabaseConstantUtil.COLUMN_CATEGORY_ID + " Integer Not null, " +
            DatabaseConstantUtil.COLUMN_RANK + " integer Not null, " +
            DatabaseConstantUtil.COLUMN_SCORE + " double Not null, " +
            "unique (" +DatabaseConstantUtil.COLUMN_DID +", "+ DatabaseConstantUtil.COLUMN_RANK + ")" +
            ");";

    public static final String CREATE_INTELLIGENT_GALLERY_CONTENT_ALBUM_TABLE = "create table " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_CONTENT_ALBUM_NAME + "(" +
            DatabaseConstantUtil.COLUMN_AUTO_INCREMENT_KEY_CONTENT_ALBUM + " integer primary key autoincrement Not null UNIQUE, " +
            DatabaseConstantUtil.COLUMN_ALBUM_ID + " text Not null, " +
            DatabaseConstantUtil.COLUMN_ALBUM_CONTENTS_COUNT + " Integer Not null" +
            ");";

    public static int DATABASE_VERSION = 4;


}
