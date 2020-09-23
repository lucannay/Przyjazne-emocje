package pg.autyzm.graprzyjazneemocje;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

public class Blank extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.blank);

        new CountDownTimer(500,500) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                finish();
            }
        }.start();
    }


}
