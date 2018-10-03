package e.sergeev.oleg.agent1c2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class ExchangeHelper {
    Context context;

    public ExchangeHelper(Context mContext) {
        context = mContext;
    }

    public String exchange (String func, String par) {
        String result = "";

        if (func.equals("/READ_GPS")) {
            result = readGPS(Integer.parseInt(par));
        }

        if (func.equals("/DELETEGPSBUID")) {
            result = deleteGPSByID(par);
        }

        if (func.equals("/GETCURRENTGPS")) {
            result = readCurrentGPS();
        }

        if (func.equals("/GETVERSION")) {
            result = getVersion();
        }

        if (func.equals("/STARTSERVICE")) {
            startService();
        }

        if (func.equals("/APPLYSETTINGS")) {
            SetParameters(par);
            startService();
        }

        if (func.equals("/GETNETWOKSTATE")) {
            result = getNetworkState();
        }
        //Производитель и модель устройства
        if (func.equals("/GETMODELDEVICE")){
            result = getModelDevice();
        }
        //Уровень заряда батареи
        if (func.equals("/GETBATTERYLEVEL")){
            result = getBatteryLevel();
        }
        //Список установленных приложений
        if (func.equals("/GETINSTALLEDAPP")){
            result = getInstalledApps();
        }
//        //Мощность сигнала сети
//        if (func.equals("/GETSIGNALLEVEL")){
//            result = getSignalLevel();
//        }

        return  result;
    }


    private String getBatteryLevel() {
        int batteryPct;
        String res = "";
        try{
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPct = (int) ((level/(float) scale)* 100.0f);
            res = Integer.toString(batteryPct);

        }catch (Exception e){
            // TODO: 04.07.2017 Log it!
            res = "Don't accese to battery lavel info";
        }
        return res;
    }

    private String getModelDevice() {
        String res = "";
        try{
            res = Build.BRAND + " " + Build.MODEL;;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return res;
    }

    public String readGPS (int count) {
        String res = "";
        int i;

        DataBaseHelper dataBaseHelper = new DataBaseHelper(context,DataBaseHelper.DATABASE_NAME, null, 1);

        try {
            res = dataBaseHelper.getGPS(count);
        } catch (JSONException j) {
            res = "";
        }
        return res;
    }

    private String deleteGPSByID (String jsonString)  {

        DataBaseHelper dataBaseHelper = new DataBaseHelper(context ,DataBaseHelper.DATABASE_NAME, null, 1);
        String res = dataBaseHelper.deleteGPSbyID(jsonString);
        return res;
    }

    private String readCurrentGPS () {
        Object d;
        long t;
        String res = "";
        GPSWriter1C gpsWriter1C = new GPSWriter1C();
        ContentValues values    = gpsWriter1C.getCurrentGPS(context);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", 0);
            jsonObject.put("gpsOn"     ,values.getAsInteger(DataBaseHelper.GPS_ON_COLUMN));

            if (values.containsKey(DataBaseHelper.GPS_LATITUDE_COLUMN)) {
                jsonObject.put("latitude"  ,values.getAsDouble(DataBaseHelper.GPS_LATITUDE_COLUMN));
            } else {
                jsonObject.put("latitude"  ,0);
            }

            if (values.containsKey(DataBaseHelper.GPS_LONGITUDE_COLUMN)) {
                jsonObject.put("longitude"  ,values.getAsDouble(DataBaseHelper.GPS_LONGITUDE_COLUMN));
            } else {
                jsonObject.put("longitude"  ,0);
            }
            // TODO: 12.10.2016
            if (values.containsKey(DataBaseHelper.BATTERY_LEVEL)) {
                jsonObject.put("bateryLevel"  ,values.getAsInteger(DataBaseHelper.BATTERY_LEVEL));
            } else {
                jsonObject.put("bateryLevel"  ,0);
            }

            if (values.containsKey(DataBaseHelper.CHARGING_STATUS)) {
                jsonObject.put("chargingStat"  ,values.getAsInteger(DataBaseHelper.CHARGING_STATUS));
            } else {
                jsonObject.put("chargingStat"  ,0);
            }

            if (values.containsKey(DataBaseHelper.TIME_ZONE)) {
                jsonObject.put("timeZone"  ,values.getAsString(DataBaseHelper.TIME_ZONE));
            } else {
                jsonObject.put("timeZone"  , "");
            }

            if (values.containsKey(DataBaseHelper.DEVICE_DATE_COLUMN)) {
                calendar.setTimeInMillis(values.getAsLong(DataBaseHelper.DEVICE_DATE_COLUMN));
                jsonObject.put("deviceDate"  ,formatter.format(calendar.getTime()));
            } else {
                calendar.setTimeInMillis(0);
                jsonObject.put("deviceDate"  ,formatter.format(calendar.getTime()));
            }

            if (values.containsKey(DataBaseHelper.GPS_DATE_COLUMN)) {
                calendar.setTimeInMillis(values.getAsLong(DataBaseHelper.GPS_DATE_COLUMN));
                jsonObject.put("gpsDate"  ,formatter.format(calendar.getTime()));
            } else {
                calendar.setTimeInMillis(0);
                jsonObject.put("gpsDate"  ,formatter.format(calendar.getTime()));
            }

            res = jsonObject.toString();
        } catch (JSONException j)
        {
            res = "FAIL";
        }
        return res;
    }

    private String getVersion () {
        String res = "";
        try {
            PackageManager pm = context.getPackageManager();
            res= pm.getPackageInfo("evgeniy.v.bystrov.agent1c",0).versionName;
        } catch (PackageManager.NameNotFoundException ex)
        {
            res = "0";
        }
        return  res;
    }

    private String SetParameters(String jsonString) {

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            SharedPreferences mSettings = context.getSharedPreferences(AgentService.APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putLong(AgentService.APP_PREFERENCES_INTERVAL,jsonObject.getLong(AgentService.APP_PREFERENCES_INTERVAL));
            editor.putLong(AgentService.APP_PREFERENCES_STARTTIME,jsonObject.getLong(AgentService.APP_PREFERENCES_STARTTIME));
            editor.putLong(AgentService.APP_PREFERENCES_ENDTIME,jsonObject.getLong(AgentService.APP_PREFERENCES_ENDTIME));
            editor.apply();
        } catch (JSONException j) {
            return "FAIL";
        }
        return "OK";
    }

    private void startService () {
        context.stopService(new Intent(context, AgentService.class));
        context.startService(new Intent(context, AgentService.class));
    }


    private String getNetworkState () {
        String res = "";
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            res = "ON";
        } else {
            res = "OFF";
        }
        return res;
    }

    public String getInstalledApps() {
        PackageManager pm = context.getPackageManager();
        HashSet<String> appsName = new HashSet<>();
        List<ApplicationInfo> list = new ArrayList<>(pm.getInstalledApplications(PackageManager.GET_META_DATA));

        for(ApplicationInfo info : list) {
            try{
                if(pm.getLaunchIntentForPackage(info.packageName) != null) {
                    if(((info.flags & ApplicationInfo.FLAG_SYSTEM) != 1)){
                        String name = info.loadLabel(pm).toString();
                        appsName.add(name);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return appsName.toString();
    }
}
