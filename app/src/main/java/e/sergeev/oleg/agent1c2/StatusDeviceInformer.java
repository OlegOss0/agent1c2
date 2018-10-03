package e.sergeev.oleg.agent1c2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import java.util.TimeZone;

public class StatusDeviceInformer {
    Context context;

    public StatusDeviceInformer(Context contextM){
        this.context = contextM;
    }


    protected String getBatteryLevel() {
        int batteryPct;
        String res = "";

        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPct = (int) ((level / (float) scale) * 100.0f);
            res = Integer.toString(batteryPct);

        } catch (Exception e) {
            res = "-100";
        }
        return res;
    }

    protected String getModelDevice() {
        String res = "";
        try{
            res = Build.BRAND + " " + Build.MODEL;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return res;
    }

    protected String getChargingStatus(){
        String res = "";
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if(status == 2){
            res = "1";
        }
        else{
            res = "0";
        }
        return res;
    }

    protected String getTimeZone(){
        String timeZone = TimeZone.getDefault().getID();
        return timeZone;
    }
}

