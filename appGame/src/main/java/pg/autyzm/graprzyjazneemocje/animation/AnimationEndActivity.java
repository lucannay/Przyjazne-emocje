package pg.autyzm.graprzyjazneemocje.animation;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import pg.autyzm.graprzyjazneemocje.R;
import pg.autyzm.przyjazneemocje.lib.entities.Level;


public class AnimationEndActivity extends Activity implements Animation.AnimationListener {

    protected ImageView animImage;
    protected Animation anim;
    private Level level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

            randomAward();

            anim.setAnimationListener(this);
            animImage.startAnimation(anim);
        MediaPlayer ring= MediaPlayer.create(AnimationEndActivity.this,R.raw.fanfare3);
        ring.start();

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    public void onAnimationEnd(Animation animation) {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


    protected void randomAward() {
        setContentView(R.layout.activity_anim_end);

        int offset = -300;
        int butterflyimages[] = {R.id.butterfly1_image, R.id.image2, R.id.image3, R.id.image4, R.id.butterfly5_image};

        for (int image : butterflyimages) {
            animImage = (ImageView) findViewById(image);
            anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.spiralend);
            anim.setStartOffset(offset += 300);
            animImage.startAnimation(anim);
        }
        animImage = (ImageView) findViewById(R.id.sun_image);
        anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.sun);
        animImage.startAnimation(anim);
    }

}
