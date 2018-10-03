package e.sergeev.oleg.agent1c2;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AgentService extends Service {
    public static final String APP_PREFERENCES           = "agent1CPreferences";
    public static final String APP_PREFERENCES_INTERVAL  = "interval";
    public static final String APP_PREFERENCES_STARTTIME = "starttime";
    public static final String APP_PREFERENCES_ENDTIME   = "endtime";
    public static final String APP_PREFERENCES_CHECKGPS = "checkGps";

    public static final String TAG = "agent1c";

    private final Handler handler = new Handler();
    DataBaseHelper dataBaseHelper;
    private long interval;
    private GPSWriter1C writer1C;
    private AgentHTTPServer agentHTTPServer;
    private PendingIntent pendingIntent;
    private LocationManager locationManager;
    private long starttime,endtime;
    private SharedPreferences mSettings;
    private Calendar calendar;
    private Date today;
    private final Runnable r = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "AS started to work");
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            today = calendar.getTime();
            if(checkPermissions()){
                handler.removeCallbacks(r);
                Log.i(TAG, "AS removeCallbaks in cheсked perms...");
            }else if(checkGPSenable()){
                handler.removeCallbacks(r);
                Log.i(TAG, "AS removeCallbaks in cheсked gps...");
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    reguestIgnoreBatteryOptomisation(getApplicationContext());
                }
                writer1C = new GPSWriter1C();
                writer1C.GPSNotify(getApplicationContext());
                Log.i(TAG, "AS handler will start in 15 seconds...");
                handler.postDelayed(this, interval);
            }
        }

        private void reguestIgnoreBatteryOptomisation(final Context context) {
            Intent i = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!pm.isIgnoringBatteryOptimizations(packageName)){
                    i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    i.setData(Uri.parse("package:" + packageName));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        }

        public void quit(){
            handler.post(new QuitLooper());
        }
        class QuitLooper implements Runnable{

            @Override
            public void run() {
                try {
                    Looper.myLooper().wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    };

    private boolean checkPermissions() {
        boolean grand;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(this, RequestActivity.class);
            i.putExtra("extra", "permissions");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            grand = true;
        }else{
            grand = false;
        }
        return grand;
    }
    private boolean checkGPSenable() {
        boolean check = false;
        String gpsONstr = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        boolean gpsON = (gpsONstr.contains("network,gps")) | (gpsONstr.contains("gps,network"));
        if(!gpsON){
            check = true;
            Intent i = new Intent(this, RequestActivity.class);
            i.putExtra("extra", "!gps");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        return check;
    }


    public AgentService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand AS");
        SQLiteDatabase sdb;
        Context context = this;

        // Прочитаем настройки //
        if (mSettings.contains(APP_PREFERENCES)) {
            interval = mSettings.getLong(APP_PREFERENCES_INTERVAL, 5 * 1000);
        } else {
            interval = 15 * 1000;
        }

        interval = 15 * 1000;

        if (mSettings.contains(APP_PREFERENCES_STARTTIME)) {
            starttime = mSettings.getLong(APP_PREFERENCES_STARTTIME, 8 * 60 * 60 * 1000);
        } else {
            starttime = 8 * 60 * 60 * 1000;
        }

        if (mSettings.contains(APP_PREFERENCES_ENDTIME)) {
            endtime = mSettings.getLong(APP_PREFERENCES_ENDTIME, 8 * 60 * 60 * 1000);
        } else {
            endtime = 19 * 60 * 60 * 1000;
        }

        String s = " запись каждые " + (interval / 1000) + " сек. ";

        SimpleDateFormat f = new SimpleDateFormat("HH:mm");

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(starttime);
        s += " с " + f.format(calendar.getTime());
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(endtime);
        s += " по " + f.format(calendar.getTime());

        // Инициализируем БД //
        dataBaseHelper = new DataBaseHelper(this, DataBaseHelper.DATABASE_NAME, null, 1);
        //dataBaseStatic = new DataBaseStatic(dataBaseHelper); нужно для map_activity
        sdb = dataBaseHelper.getReadableDatabase();
        sdb.close();

        /*//проверка разрешений
        checkPermissions();
        //проверка снятия координат по всем источникам
        checkGPSenable();
        //проверка отключения экономии батареи
        reguestIgnoreBatteryOptomisation(context);*/

        Intent myIntent = new Intent(context, AlarmReceiverIN.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        handler.removeCallbacks(r);
        handler.postDelayed(r, interval);

        // Инициализируем поиск локации //
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 30, pendingIntent);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 30, pendingIntent);
            Toast.makeText(this, "Запущен агент 1С", Toast.LENGTH_LONG).show();


        } catch (SecurityException sec) {
            Toast toast = Toast.makeText(getApplicationContext(), "Агент 1С не запущен!", Toast.LENGTH_LONG);
            toast.show();
        }

        agentHTTPServer = new AgentHTTPServer(getApplicationContext());
        try {
            agentHTTPServer.start();
            Toast toast = Toast.makeText(getApplicationContext(), "HTTP сервер 1С запущен! ", Toast.LENGTH_LONG);
            toast.show();
        } catch (IOException ex) {
            //Toast toast = Toast.makeText(getApplicationContext(), "HTTP сервер 1С не запущен! " + ex.getMessage(), Toast.LENGTH_LONG);
            //toast.show();
        }

        return Service.START_STICKY;
    }



    @Override
    public void onDestroy() {
        locationManager.removeUpdates(pendingIntent);
        agentHTTPServer.stop();
        handler.removeCallbacks(r);
        Log.i(TAG, "onDestroy AS");
        super.onDestroy();
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class AlarmReceiverIN extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive AS");
            //     writer1C = new GPSWriter1C(); // ТК координаты и так записываются раз в 5 сек - вызывает слишком частую запись //
            //     writer1C.GPSNotify(context);
        }
    }

}

