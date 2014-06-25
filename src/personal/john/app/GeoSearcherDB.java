
package personal.john.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GeoSearcherDB {
    GeoOpenHelper dbGeoOpenHelper;

    /* DB */
    static final String DB_NAME = "GeoSearcherDB.db";

    static final String DB_TABLE = "GeoTable";

    static final String DB_HOTELID = "HotelID";

    static final String DB_ARRIVED = "Arrived";

    static final String DB_MEMO = "memo";

    static final int DB_VERSION = 1;

    static final String CREATE_TABLE = "create table if not exists " + DB_TABLE + "(" + DB_HOTELID
            + " text primary key, " + DB_ARRIVED + "," + DB_MEMO + ")";

    private SQLiteDatabase mDb;

    public GeoSearcherDB(Context context) {
        dbGeoOpenHelper = new GeoOpenHelper(context);
    }

    public void openGeoSearcherDB() {
        try {
            mDb = dbGeoOpenHelper.getWritableDatabase();
        } catch (SQLException e) {
            Log.e("app", e.toString());
            mDb = dbGeoOpenHelper.getReadableDatabase();
        }
    }

    public void closeGeoSearcherDB() {
        mDb.close();
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return mDb;
    }

    // 既訪・未訪書き込みメソッド
    public void writeArrivedData(String strHotelID, int iArraived) {
        ContentValues values = new ContentValues();
        // 書き込みデータ作成
        values.put(DB_HOTELID, strHotelID);
        values.put(DB_ARRIVED, iArraived);

        // データ書き込み（Update）
        int iRet = mDb.update(DB_TABLE, values, DB_HOTELID + "=" + strHotelID, null);
        if (iRet == 0) {
            mDb.insert(DB_TABLE, null, values);
        }
    }

    // 既訪・未訪読み込みメソッド
    public int readArrivedData(String strHotelID) {
        final int RET_ARRIVED = 1;
        final int RET_NOT_ARRIVED = 0;
        // カーソル作成
        Cursor cursor = mDb.query(DB_TABLE, new String[] {
                DB_HOTELID, DB_ARRIVED
        }, DB_HOTELID + "=" + strHotelID, null, null, null, null);

        // ヒットレコードが 0件の場合
        if (cursor.getCount() <= 0) {
            cursor.close();
            return RET_NOT_ARRIVED;
        }

        cursor.moveToFirst();

        // ヒットレコードがある場合
        int iRet = cursor.getInt(1);
        cursor.close();

        if (iRet == RET_NOT_ARRIVED)
            return RET_NOT_ARRIVED;

        return RET_ARRIVED;
    }

    // メモ書き込みメソッド
    public void writeMemoData(String strHotelID, String strMemo) {
        ContentValues values = new ContentValues();
        // 書き込みデータ作成
        values.put(DB_MEMO, strMemo);

        // データ書き込み（Update）
        int iRet = mDb.update(DB_TABLE, values, DB_HOTELID + "=" + strHotelID, null);
        if (iRet == 0) {
            mDb.insert(DB_TABLE, null, values);
        }
    }

    // メモ読み込みメソッド
    public String readMemoData(String strHotelID) {
        // カーソル作成
        Cursor cursor = mDb.query(DB_TABLE, new String[] {
                DB_HOTELID, DB_MEMO
        }, DB_HOTELID + "=" + strHotelID, null, null, null, null);

        // ヒットレコードが 0件の場合
        if (cursor.getCount() <= 0) {
            cursor.close();
            return "";
        }

        cursor.moveToFirst();

        // ヒットレコードがある場合
        String strRet = cursor.getString(1);
        cursor.close();

        return strRet;
    }

    public SQLiteDatabase getGSDB() {
        return mDb;
    }

    public static class GeoOpenHelper extends SQLiteOpenHelper {

        public GeoOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(CREATE_TABLE);
        }
    }
}
