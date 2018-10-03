package e.sergeev.oleg.agent1c2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcast extends BroadcastReceiver {
    private static final String TAG = "AgentServiceLogs";
    public BootBroadcast() {
    }

    //private static final long interval = 5 * 1000;

    @Override
    public void onReceive(Context context, Intent intentIn) {
        StringBuilder msgStr = new StringBuilder();
        String string = intentIn.getAction();
        Log.i(TAG, "BootBroadCast onRecerve");
        if(string.contains("setAlarm")){
            startAgentServiceWithPreferrnce(context, string, intentIn);
        }else /*if (string.contains("android.intent.action.TIMEZONE_CHANGED"))*/{
            context.stopService(new Intent(context, AgentService.class));
            context.startService(new Intent(context,AgentService.class));
        }
    }
    private void startAgentServiceWithPreferrnce(Context context, String string, Intent intentIn){
        Log.i(TAG, "BootBroatCast onRecerve, action : " + string);

        long startTime = intentIn.getLongExtra("startTime", 0);
        long interval = intentIn.getLongExtra("interval", 0);

        Intent intentTo = new Intent(context, AgentService.class);
        intentTo.putExtra("interval", interval);
        intentTo.putExtra("startTime", startTime);
        context.startService(intentTo);
    }
}
