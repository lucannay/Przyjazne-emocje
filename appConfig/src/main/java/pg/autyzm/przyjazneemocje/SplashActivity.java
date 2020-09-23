package pg.autyzm.przyjazneemocje;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import pg.autyzm.przyjazneemocje.lib.SqliteManager;

public class SplashActivity extends Activity {

    public static final String CURRENT_LANG = "KEY_CURRENT_LANG";

    public SqliteManager sqlm;
    protected Locale myLocale;
    Intent refresh;

    public Timer timer;
    public int userInteractionTimeout = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sqlm = SqliteManager.getInstance(this);
        setLocale(sqlm.getCurrentLang());
        setContentView(R.layout.activity_splash);

//        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_splash);
//        layout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                startActivity(refresh);
//            }
//
//        });

    }

    public void setLocale(final String localeName) {
        myLocale = new Locale(localeName);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        sqlm.updateCurrentLang(localeName);

        refresh = new Intent(this, ActivityChoice.class);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                if (userInteractionTimeout == 10) {
                    userInteractionTimeout = 0;
                    refresh.putExtra(CURRENT_LANG, localeName);
                    startActivity(refresh);
                }
                userInteractionTimeout++;
            }
        }, 0, 1000);

    }

    @Override
    public void onUserInteraction() {
        // TODO Auto-generated method stub
        super.onUserInteraction();
        userInteractionTimeout=10;
    }

    public void onBackPressed() {
        finish();
        System.exit(0);
    }

}
