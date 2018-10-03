package e.sergeev.oleg.agent1c2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ExchangeActivity extends AppCompatActivity {
    public static final String ACTION_MESSAGE       = "EXCHANGE_ACTION";
    public static final String ACTION_PARAMETER_STR = "ACTION_PARAMETER_STR";
    ExchangeHelper exchangeHelper;
    Intent curIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager packageManager = getPackageManager();

        curIntent = getIntent();

        if (curIntent != null) {

            String actionMessage = curIntent.getStringExtra(ACTION_MESSAGE);

            if(actionMessage != null) {

                Intent intent = new Intent();

                exchangeHelper = new ExchangeHelper(this);

                String res = exchangeHelper.exchange("/" + actionMessage,curIntent.getStringExtra(ACTION_PARAMETER_STR));
                intent.putExtra("RESULT", res);
                setResult(RESULT_OK,intent);

            } else {
                setResult(RESULT_CANCELED);
            }

        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }
}
