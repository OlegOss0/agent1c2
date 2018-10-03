package e.sergeev.oleg.agent1c2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RequestActivity extends AppCompatActivity {
    private static final int REGUEST_CODE_PERMISSION = 123;
    private static final int REGUEST_CODE_GPS_ENABLE= 12;

    TextView tvMessage;
    Button btnSettings;
    static Thread thread;
    static int numberTread;
    private static final String APP_SETTINGS = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
    private static final String LOC_SETTINGS = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

    private static final String TAG = "agent1c_2";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.i(TAG, "onCreate RA");
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        String extra = i.getStringExtra("extra");

        switch (extra) {
            case "permissions":
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //requestPermissions(permissions, REGUEST_CODE_PERMISSION);
                    setContentView(R.layout.request_activity);
                    tvMessage = (TextView) findViewById(R.id.tvMessage);
                    tvMessage.setText("Для работы приложения \"Балмико Активные продажи\" нет необходимых разрешений.\nПерейдите в настройки приложения и включите все разрешения");
                    btnSettings = (Button) findViewById(R.id.btnSettings);
                    btnSettings.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    });
                }break;
            case "!gps":
                int api = Build.VERSION.SDK_INT;
                String message = "";
                if(api <= 17){
                    message = "Для работы приложения \"Балмико Активные продажи\" необходимо включить в настройках " +
                            "\"Использовать GPS\" .";
                }else if(api >= 21 & api < 23){
                    message = "Для работы приложения \"Балмико Активные продажи\" необходимо включить в настройках" +
                            " \" Геоданные \" \n " +
                            " \" Режим обнаружения -> GPS,Wi-Fi и мобильные сети\".";
                }else if(api >= 23){
                    message = "Для работы приложения \"Активные продажи\" необходимо включить в настройках " +
                            "\" Геолокация \" Режим \"По всем источникам\".";
                }else {
                    message = "Для работы приложения \"Балмико Активные продажи\" необходимо включить \"Геоданные\" \n" +
                            " \"Метод обнаружения\" -> \"Высокая точность\" .";
                }
                setContentView(R.layout.request_activity);
                tvMessage = (TextView) findViewById(R.id.tvMessage);
                tvMessage.setText(message);
                btnSettings = (Button) findViewById(R.id.btnSettings);
                btnSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openLocationsSettings();
                    }
                });
                break;

        }
    }

    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(APP_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, REGUEST_CODE_PERMISSION);

    }

    public void openLocationsSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(appSettingsIntent, REGUEST_CODE_GPS_ENABLE);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGUEST_CODE_PERMISSION) {
            Log.i(TAG, "onActivityResult RA");
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(this, AgentService.class);
                startService(i);
                finish();
            } else {
                tvMessage.setText("В настройках не были включены необходимые разрешения. Работа приложения невозможна. Вернитесь в настройки");
            }
            return;
        }else if(requestCode == REGUEST_CODE_GPS_ENABLE){
            String gpsONstr = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            boolean gpsON = (gpsONstr.contains("network,gps")) | (gpsONstr.contains("gps,network"));
            if(!gpsON){
                tvMessage.setText("В настройках не была включена геолокация. Работа приложения невозможна. Вернитесь в настройки");
            }else{
                Intent i = new Intent(this, AgentService.class);
                startService(i);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onPause() {
        Log.i(TAG, "onPause RA");
        thread = new Thread(new StartService());
        thread.start();
        /*Intent startServiceIntent = new Intent(getApplicationContext(),AgentService.class);
        startService(startServiceIntent);
        Log.i(TAG, "onStartService intent on RequestActivity onPause");*/
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy RA");
        if (thread != null){
            Thread dummy = thread;
            thread = null;
            dummy.interrupt();
            Log.i(TAG, "in onDestroy RA thead  StartService interrupt");
        }
        Intent startServiceIntent = new Intent(getApplicationContext(), AgentService.class);
        startService(startServiceIntent);
        Log.i(TAG, "onStartService intent on RequestActivity onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop RA");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume RA");
        if (thread != null){
            Thread dummy = thread;
            thread = null;
            dummy.interrupt();
            Log.i(TAG, "in onResume RA thead  StartService interrupt");
        }
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        Log.i(TAG, "onPostResume RA");
        super.onPostResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {
            case REGUEST_CODE_PERMISSION:

                for (int res : grantResults) {
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed) {
            //user granted all permissions we can perform our task.
            Intent startServiceIntent = new Intent(getApplicationContext(), AgentService.class);
            startService(startServiceIntent);
            finish();
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (String perm : permissions) {
                    if (shouldShowRequestPermissionRationale(perm)) {
                        Toast.makeText(this, perm + "denied.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }

        }
    }

    private class StartService implements Runnable{
        private int thread = numberTread ++;
        @Override
        public void run() {
            synchronized (this){
                try {
                    Log.i(TAG, "RA sleep thread " + thread);
                    Thread.sleep(3 * 60 * 1000);
                    //если пользователь свернул свернул\закрыл настройки, запуск сервиса через 3 мин.
                    Intent startServiceIntent = new Intent(getApplicationContext(), AgentService.class);
                    startService(startServiceIntent);
                    Log.i(TAG, "onStartService intent on RA after sleep");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        public int getThread(){
            return thread;
        }
    }
}
