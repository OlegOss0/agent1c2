package e.sergeev.oleg.agent1c2;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DataBaseHelper extends SQLiteOpenHelper implements BaseColumns {
    public static final String DATABASE_NAME    = "agent1Cdata.db";
    public static final String DATABASE_TABLE   = "gpsdata";
    public static final String GPS_AVAILABLE_COLUMN  = "gps_available";
    public static final String GPS_ON_COLUMN         = "gps_on";
    public static final String GPS_DATE_COLUMN       = "gps_date";
    public static final String DEVICE_DATE_COLUMN    = "device_date";
    public static final String GPS_LATITUDE_COLUMN   = "latitude";
    public static final String GPS_LONGITUDE_COLUMN  = "longitude";
    //
    public static final String BATTERY_LEVEL         = "battery_level";
    public static final String CHARGING_STATUS       = "charging_status";
    public static final String TIME_ZONE             = "timeZone";
    private static final int    DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " ("
            + BaseColumns._ID + " integer primary key autoincrement, "
            + GPS_AVAILABLE_COLUMN  + " integer, "
            + GPS_ON_COLUMN + " integer, "
            + DEVICE_DATE_COLUMN + " numeric, "
            + GPS_DATE_COLUMN + " numeric, "
            + GPS_LATITUDE_COLUMN + " real, "
            + GPS_LONGITUDE_COLUMN + " real, "
            + BATTERY_LEVEL + " integer, "
            + CHARGING_STATUS + " integer );";



    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
        //dataBaseStatic = new DataBaseStatic(this);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);
    }

    public String getGPS (int count) throws JSONException {
        SQLiteDatabase sdb;
        sdb = getReadableDatabase();
        Cursor cursor;
        cursor = sdb.query(DataBaseHelper.DATABASE_TABLE, new String[] {BaseColumns._ID,DataBaseHelper.DEVICE_DATE_COLUMN,
                        DataBaseHelper.GPS_ON_COLUMN,DataBaseHelper.GPS_DATE_COLUMN,
                        DataBaseHelper.GPS_LATITUDE_COLUMN,DataBaseHelper.GPS_LONGITUDE_COLUMN, DataBaseHelper.BATTERY_LEVEL,
                        DataBaseHelper.CHARGING_STATUS},
                null, null,
                null, null, null);
        int i = 0;

        int gpsOn,id,bateryLevel, chargingStat;
        String timeZone = "";

        double latitude,longitude;
        Date deviceDate,gpsDate;
        Calendar calendar = Calendar.getInstance();

        String res = "";

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        JSONException j;

        while (cursor.moveToNext() && (i < count)) {
            i++;

            id         = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            gpsOn      = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.GPS_ON_COLUMN));
            latitude   = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.GPS_LATITUDE_COLUMN));
            longitude  = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.GPS_LONGITUDE_COLUMN));
            // TODO: 12.10.2016
            bateryLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.BATTERY_LEVEL));
            chargingStat = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.CHARGING_STATUS));
            //timeZone = cursor.getString(cursor.getColumnIndex(DataBaseHelper.TIME_ZONE));

            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataBaseHelper.DEVICE_DATE_COLUMN)));
            deviceDate = calendar.getTime();
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataBaseHelper.GPS_DATE_COLUMN)));
            gpsDate = calendar.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

            jsonObject = new JSONObject();

            jsonObject.put("id"         ,id);
            jsonObject.put("gpsOn"      ,gpsOn);
            jsonObject.put("latitude"   ,latitude);
            jsonObject.put("longitude"  ,longitude);
            jsonObject.put("deviceDate" ,formatter.format(deviceDate));
            jsonObject.put("gpsDate"    ,formatter.format(gpsDate));
            jsonObject.put("bateryLevel",bateryLevel);
            jsonObject.put("chargingStat",chargingStat);
            //jsonObject.put("timeZone"   , timeZone);
            jsonArray.put(jsonObject);

        }

        cursor.close();

        sdb.close();

        res = jsonArray.toString();

        return res;
    }

    public MyGeoPoint getLastMyPosition (){
        MyGeoPoint myGeoPoint = new MyGeoPoint();
        SQLiteDatabase sdb;
        sdb = getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        Cursor cursor;
        cursor = sdb.query(DataBaseHelper.DATABASE_TABLE, new String[] {BaseColumns._ID,DataBaseHelper.DEVICE_DATE_COLUMN,
                        DataBaseHelper.GPS_ON_COLUMN,DataBaseHelper.GPS_DATE_COLUMN,
                        DataBaseHelper.GPS_LATITUDE_COLUMN,DataBaseHelper.GPS_LONGITUDE_COLUMN},
                null, null,
                null, null, null);

        while (cursor.moveToNext()) {
            double lat = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.GPS_LATITUDE_COLUMN));
            double longi = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.GPS_LONGITUDE_COLUMN));
            if( lat != 0 && longi != 0){
                myGeoPoint.setLat(lat);
                myGeoPoint.setLongi(longi);
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataBaseHelper.DEVICE_DATE_COLUMN)));
                myGeoPoint.setDeviceDate(calendar.getTime());
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataBaseHelper.GPS_DATE_COLUMN)));
                myGeoPoint.setGpsDate(calendar.getTime());
                break;
            }
        }
        cursor.close();
        sdb.close();
        return myGeoPoint;
    }

    public ArrayList<MyGeoPoint> getMyTreck(){
        int i = 0;
        ArrayList<MyGeoPoint> treck = new ArrayList();
        SQLiteDatabase sdb;
        sdb = getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        Cursor cursor;
        cursor = sdb.query(DataBaseHelper.DATABASE_TABLE, new String[] {BaseColumns._ID,DataBaseHelper.DEVICE_DATE_COLUMN,
                        DataBaseHelper.GPS_ON_COLUMN,DataBaseHelper.GPS_DATE_COLUMN,
                        DataBaseHelper.GPS_LATITUDE_COLUMN,DataBaseHelper.GPS_LONGITUDE_COLUMN},
                null, null,
                null, null, null);

        while (cursor.moveToNext()) {
            double lat = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.GPS_LATITUDE_COLUMN));
            double longi = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.GPS_LONGITUDE_COLUMN));
            if( lat != 0 && longi != 0){
                MyGeoPoint myGeoPoint = new MyGeoPoint();
                myGeoPoint.setLat(lat);
                myGeoPoint.setLongi(longi);
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataBaseHelper.DEVICE_DATE_COLUMN)));
                myGeoPoint.setDeviceDate(calendar.getTime());
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataBaseHelper.GPS_DATE_COLUMN)));
                myGeoPoint.setGpsDate(calendar.getTime());
                treck.add(i++, myGeoPoint);
            }
        }
        cursor.close();
        sdb.close();
        return treck;
    }

    public String deleteGPSbyID (String jsonString) {

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            SQLiteDatabase sdb;
            sdb = getWritableDatabase();

            int i,l;
            String curId;
            l = jsonArray.length();

            //String sqlText = "DELETE FROM " + DATABASE_TABLE + " WHERE " + BaseColumns._ID;

            for (i = 0; i<l; i++) {
                curId = jsonArray.getString(i);
                sdb.delete(DATABASE_TABLE,BaseColumns._ID + "=" + curId,null);
            }

            sdb.close();

        } catch (JSONException j) {
            return "FAIL";
        }

        return "OK";
    }
}
