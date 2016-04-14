package kr.ac.korea.intelligentgallery.database.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kr.ac.korea.intelligentgallery.util.DebugUtil;


public class DatabaseUtil {

    public static boolean checkDatabase() {
        SQLiteDatabase checkDatabase = null;

        try {
            String path = DatabaseConstantUtil.DATABASE_PATH;
            checkDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException sqlE) {
            DebugUtil.showDebug("checkDatabase() : " + sqlE.getMessage());
        }

        if (checkDatabase != null)
            checkDatabase.close();

        return checkDatabase != null ? true : false;
    }

    public static void copyDataBase(Context context) throws IOException {
        DebugUtil.showDebug("copyDataBase");

        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DatabaseConstantUtil.DATABASE_SQLITE_NAME);

        // Path to the just created empty db
        String outFileName = DatabaseConstantUtil.DATABASE_PATH;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public static void copyToDataBase(Context context) throws IOException {
        DebugUtil.showDebug("copyDataBase");

        //Open your local db as the input stream
        String root = Environment.getExternalStorageDirectory().toString();

//        InputStream myInput = new InputStream().;

        // Path to the just created empty db
//        String outFileName = DatabaseConstantUtil.DATABASE_PATH;
//
//        //Open the empty db as the output stream
//        OutputStream myOutput = new FileOutputStream(outFileName);
//
//        //transfer bytes from the inputfile to the outputfile
//        byte[] buffer = new byte[1024];
//        int length;
//        while ((length = myInput.read(buffer)) > 0) {
//            myOutput.write(buffer, 0, length);
//        }
//
//        //Close the streams
//        myOutput.flush();
//        myOutput.close();
//        myInput.close();

    }

    public static boolean checkTable() {
        boolean flag = false;

        return flag;
    }
}
