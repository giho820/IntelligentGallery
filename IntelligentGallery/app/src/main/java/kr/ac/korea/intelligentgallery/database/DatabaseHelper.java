package kr.ac.korea.intelligentgallery.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import kr.ac.korea.intelligentgallery.database.util.DatabaseConstantUtil;
import kr.ac.korea.intelligentgallery.database.util.DatabaseUtil;
import kr.ac.korea.intelligentgallery.util.DebugUtil;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper databaseHelper;
    public static SQLiteDatabase sqLiteDatabase;
    private static Context context;
    private static Cursor cursor;
    /**
     * 이 부분은 SQLiteOpenHelper 의 constructor 에 대응되게 만들어놓은 것임.
     *
     * @param context
     */
    private DatabaseHelper(Context context) {
//        super(context, DatabaseConstantUtil.DATABASE_DB_NAME, null, SharedPreUtil.getInstance().getIntPreference(KorThaiDicConstantUtil.KEY_SHARED_PREFERENCE.CHECK_DATABASE));
        super(context, DatabaseConstantUtil.DATABASE_DB_NAME, null, DatabaseConstantUtil.DATABASE_VERSION);
//        DebugUtil.showDebug("constructor : " + DatabaseConstantUtil.DATABASE_DB_NAME + " / " + DatabaseConstantUtil.DATABASE_VERSION + " / " + SharedPreUtil.getInstance().getIntPreference(ConstantUtil.KEY_SHARED_PREFERENCE.CHECK_DATABASE));
    }

    public static synchronized DatabaseHelper getInstacnce(Context context) {
        DebugUtil.showDebug("DatabaseHelper, getInstance()");

        databaseHelper = new DatabaseHelper(context);

        DatabaseHelper.context = context;
        DatabaseHelper.sqLiteDatabase = databaseHelper.getReadableDatabase();
        return databaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DebugUtil.showDebug("DatabaseHelper onCreate()");

        if (!DatabaseUtil.checkDatabase()) {
            DebugUtil.showDebug("Database is not existed");
        } else {
            DebugUtil.showDebug("Database is existed");
        }


        db.beginTransaction();
        try {
            db.execSQL(DatabaseConstantUtil.CREATE_INTELLIGENT_GALLERY_TABLE);
            DebugUtil.showDebug(DatabaseConstantUtil.CREATE_INTELLIGENT_GALLERY_TABLE);
            db.setTransactionSuccessful();

        } catch (Exception err) {
            DebugUtil.showDebug(err.toString());
        }
        db.endTransaction();

        db.beginTransaction();
        try {
            db.execSQL(DatabaseConstantUtil.CREATE_INTELLIGENT_GALLERY_CONTENT_ALBUM_TABLE);
            DebugUtil.showDebug(DatabaseConstantUtil.CREATE_INTELLIGENT_GALLERY_CONTENT_ALBUM_TABLE + " 테이블 생성됨");
            db.setTransactionSuccessful();

        } catch (Exception err) {
            DebugUtil.showDebug(err.toString());
        }
        db.endTransaction();


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DebugUtil.showDebug("DatabaseHelper onUpgrade()");
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstantUtil.TABLE_INTELLIGENT_GALLERY_CONTENT_ALBUM_NAME);

        onCreate(db);

    }

    @Override
    public synchronized void close() {

        if (sqLiteDatabase != null)
            sqLiteDatabase.close();

        super.close();
    }


}
