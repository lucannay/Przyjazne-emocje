package pg.autyzm.graprzyjazneemocje;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//reward and hint activities displayed after correct answer
public class RewardAndHintActivity extends Activity implements ISounds {
    MediaPlayer ring;
    String speakerText;
    private int chosenColor = generateRandomColor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_reward);


        LinearLayout rewardAndHintLayout = (LinearLayout) findViewById(R.id.activity_reward);
        rewardAndHintLayout.setBackgroundColor(chosenColor);
        int time = 3000;

        Intent intentReward = getIntent();
        Intent intentHint = getIntent();
        String fileName = intentReward.getStringExtra("fileName");
        String emotion = intentReward.getStringExtra("emotion");

        boolean hintMode = intentHint.getBooleanExtra("hintMode", true);

        int praiseBinary = intentReward.getIntExtra("praises", 0);

        List<String> praiseList = new ArrayList<String>();
        String[] praiseTab = getPraises(praiseBinary);
        for (String element : praiseTab) {
            if (element != null && element.length() > 0) {
                praiseList.add(element);
            }
        }

        int max = praiseList.size();
        int position = (int) Math.floor(Math.random() * max);

        String commandPraise = "";
        if (praiseList.size() > 0) {
            commandPraise = praiseList.get(position);
        }

        speakerText = commandPraise + ", " + emotion;

        if (hintMode) {
            rewardAndHintLayout.setBackgroundColor(getResources().getColor(R.color.background_center));
            fileName = intentHint.getStringExtra("fileName");
            emotion = intentHint.getStringExtra("emotion");
            speakerText = emotion;
            time = 2000;
        }


        File rewardOrHintFile = new File(fileName);

        Bitmap bitmap = BitmapFactory.decodeFile(rewardOrHintFile.getAbsolutePath());

        TextView rewardOrHintText = (TextView) findViewById(R.id.rewardOrHintText);
        ImageView correctPhoto = (ImageView) findViewById(R.id.correctAnswer);

        rewardOrHintText.setText(emotion);
        Animation zoom = AnimationUtils.loadAnimation(RewardAndHintActivity.this, R.anim.zoom);
        correctPhoto.setImageBitmap(bitmap);
        if (hintMode) {
            ring = MediaPlayer.create(RewardAndHintActivity.this, soundsToApply(emotion));
            ring.start();
        }
        if (!hintMode) {
            if (commandPraise != "")
                ring = MediaPlayer.create(RewardAndHintActivity.this, soundsToApply(commandPraise));
            else
                ring = MediaPlayer.create(RewardAndHintActivity.this, soundsToApply(emotion));
            ring.start();
            while (ring.isPlaying()) {

            }
            correctPhoto.startAnimation(zoom);

            ring = MediaPlayer.create(RewardAndHintActivity.this, soundsToApply(emotion));
            ring.start();
        }


        hintMode = false;


        new CountDownTimer(time, 1) {

            public void onTick(long millisUntilFinished) {


            }

            public void onFinish() {

                finish();


            }
        }.start();


    }

    public String[] getPraises(int praiseBinary) {
        String[] praises = getResources().getStringArray(R.array.praise_array);
        String[] result = new String[5];
        int praisePositionBinary = 1;
        int i = 0;
        for (String praise : praises) {
            if ((praisePositionBinary & praiseBinary) == praisePositionBinary) {
                result[i] = praise;
                i++;
            }
            praisePositionBinary = praisePositionBinary * 2;


        }

        return result;

    }

    @Override
    public void finish() {
        super.finish();


    }

    public int generateRandomColor() {
        final Random mRandom = new Random(System.currentTimeMillis());

        final int baseColor = Color.WHITE;
        Random rnd = new Random();

        final int baseRed = Color.red(baseColor);
        final int baseGreen = Color.green(baseColor);
        final int baseBlue = Color.blue(baseColor);

        final int red = (baseRed + mRandom.nextInt(256)) / 2;
        final int green = (baseGreen + mRandom.nextInt(256)) / 2;
        final int blue = (baseBlue + mRandom.nextInt(256)) / 2;


        return Color.rgb(red, green, blue);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void mySleep(int duration) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, duration);
    }

    public void ringWait() {
        while (ring.isPlaying()) {
            mySleep(5000);
        }
    }

    @Override
    public int soundsToApply(String commandText) {
        switch (commandText) {
            case "dobrze":
            case "good":
                return R.raw.good;
            case "wspaniale":
            case "excellent":
                return R.raw.excellent;
            case "super":
                return R.raw.sup2;
            case "ekstra":
            case "extra":
                return R.raw.extra;

            case "świetnie":
            case "great":
                return R.raw.great;

            case "smutna":
                return R.raw.sad_female;
            case "smutny":
            case "sad":
                return R.raw.sad_male;
            case "wesoła":

                return R.raw.happy_female;
            case "wesoły":
            case "happy":
                return R.raw.happy_male;
            case "zła":
                return R.raw.angry_female;
            case "zły":
            case "angry":
                return R.raw.angry_male;
            case "znudzona":
                return R.raw.bored_female;
            case "znudzony":
            case "bored":
                return R.raw.bored_male;
            case "zdziwiona":
                return R.raw.surprised_female;
            case "zdziwiony":
            case "surprised":
                return R.raw.surprised_male;
            case "przestraszona":
                return R.raw.scared_female;
            //TODO ZMIENIĆ NA FEMALE
            case "przestraszony":
            case "scared":
                return R.raw.scared_male;

            default:
                return -1;

        }
    }
}
