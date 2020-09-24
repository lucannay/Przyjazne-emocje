package pg.autyzm.graprzyjazneemocje.animation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import java.util.Random;

import pg.autyzm.graprzyjazneemocje.R;
import pg.autyzm.graprzyjazneemocje.api.managers.AnimationBuilder;

public class AnimationActivity extends Activity implements Animation.AnimationListener {

    protected Animation anim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        createView();
        anim.setAnimationListener(this);
        Random rnd = new Random();

        //colour of reward/hint and animations view
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        while (color == Color.WHITE){
            color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }


        RelativeLayout myLayout = (RelativeLayout) findViewById(R.id.activity_menu);
        myLayout.setBackgroundColor(color);
    }

    protected void createView() {

        anim = new AnimationBuilder(this).prepareAndReturnRandomAward();

    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
