package e.sergeev.oleg.agent1c2;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    final ArrayList<String> gpsdata = new ArrayList<String>();
    DataBaseHelper dataBaseHelper;
    ExchangeHelper exchangeHelper;
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button buttonStartService = (Button)findViewById(R.id.buttonStartService);
        buttonStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                context.startService(new Intent(context, AgentService.class));
            }
        });

        exchangeHelper = new ExchangeHelper(this);
        dataBaseHelper = new DataBaseHelper(this,DataBaseHelper.DATABASE_NAME, null, 1);

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,gpsdata);
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);

        TextView textView = (TextView)findViewById(R.id.textViewVersion);
        String res = "";
        try {
            PackageManager pm = getPackageManager();
            res= pm.getPackageInfo("e.sergeev.oleg.agent1c2",0).versionName;
        } catch (PackageManager.NameNotFoundException ex)
        {
            res = "0";
        }
        textView.setText("Версия:" + res);

    }

    public void onClickRead(View view) {
        SQLiteDatabase sdb;
        sdb = dataBaseHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sdb.query(DataBaseHelper.DATABASE_TABLE, new String[] {DataBaseHelper.DEVICE_DATE_COLUMN,
                        DataBaseHelper.GPS_ON_COLUMN,DataBaseHelper.GPS_DATE_COLUMN,DataBaseHelper.GPS_LATITUDE_COLUMN,DataBaseHelper.GPS_LONGITUDE_COLUMN, DataBaseHelper.BATTERY_LEVEL, DataBaseHelper.CHARGING_STATUS},
                null, null,
                null, null, null);

        Date deviceDate,gpsDate;
        int gpsOn;
        double latitude,longitude;
        int bateryLevel, chargingStat;


        gpsdata.clear();

        String text = "";

        Calendar calendar = Calendar.getInstance();

        while (cursor.moveToNext()) {

            gpsOn      = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.GPS_ON_COLUMN));
            latitude   = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.GPS_LATITUDE_COLUMN));
            longitude  = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.GPS_LONGITUDE_COLUMN));
            bateryLevel = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.BATTERY_LEVEL));
            chargingStat = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.CHARGING_STATUS));

            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataBaseHelper.DEVICE_DATE_COLUMN)));
            deviceDate = calendar.getTime();
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataBaseHelper.GPS_DATE_COLUMN)));
            gpsDate = calendar.getTime();

            text = "Дата: " + deviceDate + "; GPS: " + gpsOn + "; По спутнику: " + gpsDate + "; lat:" + latitude + "; long:" + longitude +
                    "; ,батарея:" + bateryLevel + "; заряжается:" + chargingStatToString(chargingStat);
            gpsdata.add(0, text);
        }

        arrayAdapter.notifyDataSetChanged();
        cursor.close();
        sdb.close();
    }

    private String chargingStatToString(int chargingStat) {
        String str = "";
        if (chargingStat == 0){
            str = "нет";
        }
        if (chargingStat == 1){
            str = "да";
        }
        else{
        }
        return str;
    }

    public void onClickClear(View view) {
        AlertDialog.Builder a = new AlertDialog.Builder(this);
        a.setMessage("База данных будет очищена, продолжить?");
        a.setCancelable(true);
        a.setPositiveButton("ДА", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SQLiteDatabase sdb;
                sdb = dataBaseHelper.getWritableDatabase();
                sdb.delete(DataBaseHelper.DATABASE_TABLE, null, new String[]{});
                sdb.close();
            }
        });
        a.setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        a.create();
        a.show();
    }

    public void onClickNew(View view) {

        String text = "";
        gpsdata.clear();
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(500);
        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            text = "Process " + rsi.process + " with component "
                    + rsi.service.getClassName();
            if (text.contains("sergeev")) {
                gpsdata.add(0, text);
            }
        }
        arrayAdapter.notifyDataSetChanged();

    }
    /*public void onClickGetApps(View viwe){
        exchangeHelper.getInstalledApps();
    }*/

    /*public void onClickGetApps(View view) {
        exchangeHelper.getInstalledApps();
    }*/

    public void onClickViewMap(View view) {

        /*Intent intent = new Intent("android.intent.action.start_mapActivity");
        startActivity(intent);*/
    }



}
