package e.sergeev.oleg.agent1c2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GPSWriter1C {
    private static final long CHECK_DEVICE_TIMEOUT = 10 * 60 * 1000; //раз в 10 мин.
    private static long lastCheckDeviceTime;
    final String TAG = "agent1c";
    private NotificationManager nm;
    private Boolean gpsExists, gpsON, wifiOn, gsmOn;
    private Location location;
    private double longitude, latitude;
    private long mtime;
    private long currentTime;
    private Date gpsDate, devDate;
    static Date lastDevDate;
    private static int bateryLevel, chargingStat;
    static String timeZone;
    private DataBaseHelper dataBaseHelper;
    private StatusDeviceInformer statusDeviceInformer;
    //LastCoordinatesByWiFI lastCoordinatesByWiFI;


    public void GPSNotify(Context context) {

        dataBaseHelper = new DataBaseHelper(context, DataBaseHelper.DATABASE_NAME, null, 1);
        SQLiteDatabase sdb;
        sdb = dataBaseHelper.getWritableDatabase();
        ContentValues values = getCurrentGPS(context);
        sdb.insert(DataBaseHelper.DATABASE_TABLE, null, values);
        sdb.close();

        // Формиируем оповещение или запись координат //
        boolean packageInstalled = false;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> l = pm.getInstalledApplications(0);
        for (int j = 0; j < l.size(); j++) {
            ApplicationInfo info = l.get(j);
            if (info.processName.equals(PackageStarterService.APP_NAME)) {
                packageInstalled = true;
            }
        }

        Intent scheduledIntent;

        if (packageInstalled) {
            scheduledIntent = context.getPackageManager().getLaunchIntentForPackage(PackageStarterService.APP_NAME);
        } else {
            scheduledIntent = new Intent(context, SettingsActivity.class);
        }

        scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, scheduledIntent, 0);

        nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Resources res = context.getResources();

        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.icon));
        builder.setTicker("Агент 1С активен");
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(false);
        builder.setContentTitle("Агент 1С активен");
        CharSequence date = DateFormat.format("hh:mm:ss", System.currentTimeMillis());


        String contentText = (String) date;

        if (((int) values.get(DataBaseHelper.GPS_ON_COLUMN)) == 1) {
            contentText = contentText + "; GPS ON";
        } else {
            contentText = contentText + "; GPS OFF";
        }

        builder.setContentText(contentText);
        Notification n = builder.build();
        nm.notify(1, n);
    }

    public ContentValues getCurrentGPS(final Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        PackageManager pm = context.getPackageManager();
        statusDeviceInformer = new StatusDeviceInformer(context);
        //lastCoordinatesByWiFI = new LastCoordinatesByWiFI();


        gpsExists = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        gpsON = false;
        wifiOn = false;


        // Получаем текущие координаты //
        if (gpsExists) {
            String gpsONstr = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            gpsON = (gpsONstr.contains("network,gps")) | (gpsONstr.contains("gps,network"));
            wifiOn = wifi.isWifiEnabled();
            gsmOn = tm.hasIccCard();

            if (gpsON) {
                if(getLocationsFromGPS(context)) {
                    //Toast.makeText(context, "По GPS получены", Toast.LENGTH_LONG).show();
                }else if(wifiOn | gsmOn){
                    if(getLocationsFromNetwork(context)){
                        //Toast.makeText(context, "По Network  получены" , Toast.LENGTH_LONG).show();
                    }
                }

            }
        }
        checkDeviceInfo();
        return fillValues();
    }

    private void checkDeviceInfo() {
        currentTime = System.currentTimeMillis();
        if ((currentTime - lastCheckDeviceTime) > CHECK_DEVICE_TIMEOUT) {
            lastCheckDeviceTime = currentTime;
            bateryLevel = Integer.parseInt(statusDeviceInformer.getBatteryLevel());
            chargingStat = Integer.parseInt(statusDeviceInformer.getChargingStatus());
        }
    }


    private ContentValues fillValues() {
        ContentValues values = new ContentValues();
        if (gpsExists) {
            values.put(DataBaseHelper.GPS_AVAILABLE_COLUMN, 1);
        } else {
            values.put(DataBaseHelper.GPS_AVAILABLE_COLUMN, 0);
        }

        if (gpsON) {
            values.put(DataBaseHelper.GPS_ON_COLUMN, 1);
            if (location == null) {
                values.put(DataBaseHelper.GPS_DATE_COLUMN, (long) 0);
                values.put(DataBaseHelper.GPS_LATITUDE_COLUMN, (double) 0);
                values.put(DataBaseHelper.GPS_LONGITUDE_COLUMN, (double) 0);
            } else {
                values.put(DataBaseHelper.GPS_DATE_COLUMN, mtime);
                values.put(DataBaseHelper.GPS_LATITUDE_COLUMN, latitude);
                values.put(DataBaseHelper.GPS_LONGITUDE_COLUMN, longitude);
            }
        } else {
            values.put(DataBaseHelper.GPS_ON_COLUMN, 0);
        }
        values.put(DataBaseHelper.BATTERY_LEVEL, bateryLevel);
        values.put(DataBaseHelper.CHARGING_STATUS, chargingStat);
        values.put(DataBaseHelper.DEVICE_DATE_COLUMN, System.currentTimeMillis());
        return values;
    }

    private boolean getLocationsFromNetwork(final Context context) {
        boolean locationsIsGet = false;
        String NetWorkProvider = LocationManager.NETWORK_PROVIDER;
        LocationManager NetworklocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            location = NetworklocationManager.getLastKnownLocation(NetWorkProvider);
        } catch (SecurityException s) {
            location = null;
        }
        if (location != null) {
            if(location.getAccuracy() > 50){
                longitude = 0.0;
                latitude = 0.0;
            }else{
                longitude = formatDouble(location.getLongitude(), 4);
                latitude = formatDouble(location.getLatitude(), 4);
                locationsIsGet = true;
                mtime = System.currentTimeMillis();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                gpsDate = calendar.getTime();
            }
        }
        return locationsIsGet;
    }

    private boolean getLocationsFromGPS(final Context context) {
        boolean locationsIsGet = false;
        String gpsProvider = LocationManager.GPS_PROVIDER;
        LocationManager GPSlocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            location = GPSlocationManager.getLastKnownLocation(gpsProvider);
        } catch (SecurityException s) {
            location = null;
        }

        //если координаты получены
        if (location != null) {
            if(location.getAccuracy() > 35){
                longitude = 0.0;
                latitude = 0.0;
            } else {
                locationsIsGet = true;
                longitude = formatDouble(location.getLongitude(), 4);
                latitude = formatDouble(location.getLatitude(), 4);
                mtime = location.getTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(mtime);
                gpsDate = calendar.getTime();
            }
        }
        return locationsIsGet;
    }


    //It isn't used in the working version, only for tests of removal of coordinates
    //
    /*static private class LastCoordinatesByWiFI {
        static private double lastLongitude = (double) 0;
        static private double lastLatitude = (double) 0;
        static private double lastAltitude = (double) 0;

        public LastCoordinatesByWiFI() {

        }

        public void eraseCoordinates() {
            lastLatitude = (double) 0;
            lastLongitude = (double) 0;
            lastAltitude = (double) 0;
        }

        public boolean coordinatesIsEmpty() {
            if (lastLatitude == (double) 0 & lastLongitude == (double) 0) {
                return true;
            }
            return false;
        }


        public double getLastLongitude() {
            return lastLongitude;
        }

        public double getLastLatitude() {
            return lastLatitude;
        }

        public double getLastAltitude() {
            return lastAltitude;
        }

        public void setCoordinates(double longitude, double latitude, double altitude) {
            LastCoordinatesByWiFI.lastLongitude = longitude;
            LastCoordinatesByWiFI.lastLatitude = latitude;
            LastCoordinatesByWiFI.lastAltitude = altitude;
        }

        public boolean isReceivedCoordinatesCurrent(double recLongitude, double recLatitude) {
            boolean isCurrent = true;
            double inaccuracy = 0.0016;
            double resCompareLong = Math.abs(lastLongitude - recLongitude);
            double resCompareLat = Math.abs(lastLatitude - recLatitude);

            if ((resCompareLat < inaccuracy) || (resCompareLong < inaccuracy)) {
                isCurrent = false;
            } else {
                lastLongitude = recLongitude;
                lastLatitude = recLatitude;
            }
            return isCurrent;
        }
    }*/
    private double formatDouble(double d, int dz)
    {
        double dd=Math.pow(10,dz);
        return Math.round(d*dd)/dd;
    }

}
