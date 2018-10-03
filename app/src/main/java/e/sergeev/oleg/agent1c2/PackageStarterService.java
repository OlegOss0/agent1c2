package e.sergeev.oleg.agent1c2;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.List;

public class PackageStarterService extends Service {
    public static final String APP_NAME = "evgeniy.v.bystrov.torgpred";
    private long interval = 5 * 60 * 1000; // Проверку осуществляем раз в 5 минут //
    public PackageStarterService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (false) { // Пока  отключим сервис //

            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    List<ActivityManager.RunningAppProcessInfo> rs = activityManager.getRunningAppProcesses();
                    String t = "NOT ACTIVE";
                    for (int i = 0; i < rs.size(); i++) {
                        ActivityManager.RunningAppProcessInfo rsi = rs.get(i);
                        if (rsi.processName.contains(APP_NAME)) {
                            t = "ACTIVE";
                        }
                    }
                    if (t.equals("NOT ACTIVE")) {
                        PackageManager pm = getPackageManager();
                        List<ApplicationInfo> l = pm.getInstalledApplications(0);
                        for (int j = 0; j < l.size(); j++) {
                            ApplicationInfo info = l.get(j);
                            if (info.processName.equals(APP_NAME)) {
                                Intent BrowserIntent = pm.getLaunchIntentForPackage(APP_NAME);
                                startActivity(BrowserIntent);
                            }
                        }
                    }
                    handler.postDelayed(this, interval);
                }
            };
            handler.postDelayed(r, 1000);
        };


        return Service.START_STICKY;

    }

    @Override
    public void onCreate() {
        Toast.makeText(this,"Запущен стартер 1С",Toast.LENGTH_LONG).show();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
