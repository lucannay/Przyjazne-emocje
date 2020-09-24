package pg.autyzm.graprzyjazneemocje;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import pg.autyzm.graprzyjazneemocje.animation.AnimationActivity;
import pg.autyzm.graprzyjazneemocje.animation.AnimationEndActivity;
import pg.autyzm.przyjazneemocje.lib.SqliteManager;
import pg.autyzm.przyjazneemocje.lib.entities.Level;

import static pg.autyzm.przyjazneemocje.lib.SqliteManager.getInstance;


public class MainActivity extends Activity implements View.OnClickListener, ISounds {

    static boolean is_playing = false;
    static boolean clicked = false;
    static int repeat = 1;
    public Speaker speaker;
    int sublevelsLeft;
    List<Integer> sublevelsList;
    boolean onlyEmotion;
    int whichTry;
    List<String> photosWithEmotionSelected;
    List<String> photosWithRestOfEmotions;
    List<String> photosToUseInSublevel;
    String goodAnswer;
    String commandWithoutEmotion;
    boolean hints = false;
    boolean onePicDisplayed;
    Cursor cur0;
    Cursor videoCursor;
    SqliteManager sqlm;
    int wrongAnswers;
    int rightAnswers;
    int wrongAnswersSublevel;
    int rightAnswersSublevel;
    int timeout;
    String commandText;
    String emotion;
    boolean animationEnds = true;
    Level level;
    ImageView image_selected = null;
    CountDownTimer timer;
    int height;
    LinearLayout linearLayout1;
    LinearLayout.LayoutParams lp;
    int j = 0;
    boolean videos = false;
    private int listSize;
    private SubLevelMode subLevelMode;
    private int attempt;

    public SubLevelMode getSubLevelMode() {
        return subLevelMode;
    }

    public void setSubLevelMode(SubLevelMode subLevelMode) {
        this.subLevelMode = subLevelMode;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sqlm = getInstance(this);

        sqlm.getReadableDatabase();


        cur0 = sqlm.giveAllLevels();


        videoCursor = sqlm.giveAllVideos(); //TODO: Change to "giveVideosInLevel(int levelId)
        videoCursor.moveToFirst();

        if (!videos) {
            setContentView(R.layout.activity_main);


        } else {
            Intent i = new Intent(this, VideoWelcomeActivity.class);
            startActivity(i);
            setContentView(R.layout.activity_videos);
        }

        final ImageButton speakerButton = (ImageButton) findViewById(R.id.matchEmotionsSpeakerButton);

        findNextActiveLevel();

        generateView(photosToUseInSublevel);

        if (!videos) {
            if (!level.isShouldQuestionBeReadAloud()) {
                findViewById(R.id.matchEmotionsSpeakerButton).setVisibility(View.GONE);
            } else {
                speaker = Speaker.getInstance(MainActivity.this);

                speakerButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        speakerButton.setClickable(false);
                        speakerButton.setEnabled(false);
                        complexSoundsToApply(commandWithoutEmotion, emotion, onlyEmotion);

                        while (is_playing) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    speakerButton.setClickable(false);
                                    speakerButton.setEnabled(false);
                                }
                            }, 200);
                        }

                        speakerButton.setClickable(true);
                        speakerButton.setEnabled(true);
                    }
                });

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer_cancel();
        finish();
        System.exit(0);
    }

    public boolean findNextActiveLevel() {

        if (sublevelsLeft > 0) {
            wrongAnswersSublevel = 0;
            generateSublevel(sublevelsList.get(sublevelsLeft - 1));
            return true;
        }

        while (cur0.moveToNext()) {
            int levelId = cur0.getInt(cur0.getColumnIndex("id"));

            Cursor cur2 = sqlm.giveLevel(levelId);
            Cursor cur3 = sqlm.givePhotosInLevel(levelId);
            Cursor cur4 = sqlm.giveEmotionsInLevel(levelId);

            level = new Level(cur2, cur3, cur4);
            if (level.isLearnMode() || level.isTestMode()) {
                return loadLevel(level);
            } else {
                continue;
            }
        }
        return false;
    }

    boolean loadLevel(Level level) {
        wrongAnswersSublevel = 0;
        rightAnswersSublevel = 0;
        timeout = 0;

        int photosPerLvL = 0;


        if (level.getPhotosOrVideosFlag().equals("videos")) {
            videos = true;
        } else {
            videos = false;
        }

        photosPerLvL = level.getPhotosOrVideosShowedForOneQuestion();
        level.incrementEmotionIdsForGame();

        // tworzymy tablice do permutowania
        if (!videos)
            sublevelsLeft = level.getEmotions().size() * level.getSublevelsPerEachEmotion();
        else
            sublevelsLeft = videoCursor.getCount();

        sublevelsList = new ArrayList<Integer>();

        for (int i = 0; i < level.getEmotions().size(); i++) {
            for (int j = 0; j < level.getSublevelsPerEachEmotion(); j++) {
                long rand = Math.round(Math.random());
                int female = level.getEmotions().get(i);
                int male = level.getEmotions().get(i) + 6;
                Cursor curEmotionFemale = sqlm.giveEmotionName(female);
                Cursor curEmotionMale = sqlm.giveEmotionName(male);
                if (rand == 0) {
                    if (curEmotionFemale.getCount() != 0)
                        sublevelsList.add(female);
                    else
                        sublevelsList.add(male);
                } else {
                    if (curEmotionFemale.getCount() != 0)
                        sublevelsList.add(male);
                    else
                        sublevelsList.add(male);
                }

            }
        }

        java.util.Collections.shuffle(sublevelsList);
        generateSublevel(sublevelsList.get(sublevelsLeft - 1));

        return true;
    }

    void generateSublevel(int emotionIndexInList) {
        attempt = 0;
        whichTry = 0;
        subLevelMode = SubLevelMode.NO_WRONG_ANSWER;
        Cursor emotionCur = sqlm.giveEmotionName(emotionIndexInList);

        emotionCur.moveToFirst();
        String selectedEmotionName = emotionCur.getString(emotionCur.getColumnIndex("emotion"));

        photosWithEmotionSelected = new ArrayList<String>();
        photosWithRestOfEmotions = new ArrayList<String>();
        photosToUseInSublevel = new ArrayList<String>();

        if (level.isTestMode()) {
            selectedPhotosForGame(level.getPhotosOrVideosIdListInTest(), selectedEmotionName);
        } else {
            selectedPhotosForGame(level.getPhotosOrVideosIdList(), selectedEmotionName);
        }

        goodAnswer = selectPhotoWithSelectedEmotion();
        boolean differentSexes = level.isOptionDifferentSexes();
        if (differentSexes) {
            selectPhotoWithNotSelectedEmotions(level.getPhotosOrVideosShowedForOneQuestion(), "_");
        } else {
            String sex = (goodAnswer.contains("_man")) ? "_man" : "woman";
            selectPhotoWithNotSelectedEmotions(level.getPhotosOrVideosShowedForOneQuestion(), sex);
        }
        // we connect correct picture with the rest of pictures (that will be displayed in the sublevel)
        photosToUseInSublevel.add(goodAnswer);

        java.util.Collections.shuffle(photosToUseInSublevel);


        if (level.isLearnMode()) {
            startTimer(level);
        } else if (level.isTestMode()) {

            Intent intent = new Intent(MainActivity.this, Blank.class);
            intent.putExtra("right", goodAnswer);
            startActivity(intent);
            TestHandler(200);
            StartTimerForTest(level);
        }


    }

    void selectedPhotosForGame(List<Integer> photos, String selectedEmotionName) {

        for (int e : photos) {
            Cursor curEmotion = sqlm.givePhotoWithId(e);
            Cursor curSelectedEmotion = sqlm.givePhotosWithEmotion(selectedEmotionName);


            if (curEmotion.getCount() != 0) {
                curEmotion.moveToFirst();
                String photoEmotionName = curEmotion.getString(curEmotion.getColumnIndex("emotion"));
                String photoName = curEmotion.getString(curEmotion.getColumnIndex("name"));

                if (photoEmotionName.equals(selectedEmotionName)) {
                    photosWithEmotionSelected.add(photoName);
                } else {
                    if (photoEmotionName.contains(selectedEmotionName.substring(0, 4))) {

                    } else
                        photosWithRestOfEmotions.add(photoName);
                }


            }
            curEmotion.close();
            curSelectedEmotion.close();


        }
        if (photosWithEmotionSelected.size() == 0) {
            if (selectedEmotionName.contains("woman"))
                selectedEmotionName = selectedEmotionName.replace("woman", "man");
            else if (selectedEmotionName.contains("_man"))
                selectedEmotionName = selectedEmotionName.replace("man", "woman");

            photosWithRestOfEmotions.clear();
            selectedPhotosForGame(photos, selectedEmotionName);

        }


    }

    public void generateView(List<String> photosList) {

        int trials = level.getAllSublevelsInLevelAmount();
        TextView numberOfTries2 = (TextView) findViewById(R.id.numberOfTries2);
        numberOfTries2.setText(getString(R.string.commands) + (trials - sublevelsLeft + 1) + "/" + trials);

        String rightEmotion = goodAnswer.replace(".jpg", "").replaceAll("[0-9.]", "").replaceAll("_r_", "").replaceAll("_e_", "");

        if (!videos) {
            TextView txt = (TextView) findViewById(R.id.rightEmotion);

            String rightEmotionLang = getResources().getString(getResources().getIdentifier("emotion_" + rightEmotion, "string", getPackageName()));

            if (level.getQuestionType().equals(Level.Question.SHOW_WHERE_IS_EMOTION_NAME)) {
                final int commandTypes = level.getCommandTypesAsNumber();
                ArrayList<String> commandsSelected = new ArrayList<>();

                if ((commandTypes & CommandTypeValue(CommandType.TOUCH)) == CommandTypeValue(CommandType.TOUCH)) {

                    commandsSelected.add(getResources().getString(R.string.touch_where));

                }
                //checkbox:1
                if ((commandTypes & CommandTypeValue(CommandType.SHOW)) == CommandTypeValue(CommandType.SHOW)) {
                    commandsSelected.add(getResources().getString(R.string.show_where));
                }

                //checkbox: 4
                if ((commandTypes & CommandTypeValue(CommandType.POINT)) == CommandTypeValue(CommandType.POINT)) {
                    commandsSelected.add(getResources().getString(R.string.point_where));

                }

                //checkbox:2
                if ((commandTypes & CommandTypeValue(CommandType.SELECT)) == CommandTypeValue(CommandType.SELECT)) {
                    commandsSelected.add(getResources().getString(R.string.select_where));


                }
                //checkbox: 16
                if ((commandTypes & CommandTypeValue(CommandType.FIND)) == CommandTypeValue(CommandType.FIND)) {
                    commandsSelected.add(getResources().getString(R.string.find_where));
                }


                int size = commandsSelected.size();
                String[] commandsToChoose = new String[size];
                for (int i = 0; i < size; i++) {
                    commandsToChoose[i] = commandsSelected.get(i);
                }

                commandWithoutEmotion = commandsToChoose[(int) Math.floor(Math.random() * (size))];
                emotion = rightEmotionLang;
                commandText = commandWithoutEmotion + " " + emotion;
                onlyEmotion = false;


            } else if (level.getQuestionType().equals(Level.Question.SHOW_EMOTION_NAME)) {
                commandWithoutEmotion = getResources().getString(R.string.show);
                emotion = rightEmotionLang;
                commandText = commandWithoutEmotion + " " + emotion;
                onlyEmotion = false;
            } else if (level.getQuestionType().equals(Level.Question.EMOTION_NAME)) {
                commandWithoutEmotion = "";
                emotion = rightEmotionLang;
                commandText = emotion;
                onlyEmotion = true;

            }

            txt.setText(commandText);

        }

        linearLayout1 = (LinearLayout) findViewById(R.id.imageGallery);
        linearLayout1.setGravity(Gravity.CENTER);
        linearLayout1.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        linearLayout1.removeAllViews();
        listSize = photosList.size();

        for (
                String photoName : photosList) {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            File fileOut = new File(root + "FriendlyEmotions/Photos" + File.separator + photoName);
            try {
                final ImageView image = new ImageView(MainActivity.this);

                setLayoutMargins(image, 45 / listSize, 45 / listSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 790 / listSize, getResources().getDisplayMetrics()));

                if (photoName.contains(rightEmotion)) {
                    image.setId(1);
                } else {
                    image.setId(0);
                }

                image.setOnClickListener(this);
                final Bitmap captureBmp = Media.getBitmap(getContentResolver(), Uri.fromFile(fileOut));
                image.setImageBitmap(captureBmp);
                linearLayout1.addView(image);
            } catch (IOException e) {
                System.out.println("IO Exception " + photoName);
            }
        }
    }

    public void onClick(View v) {
        clicked = true;
        if (level.isTestMode()) {
            onClickTestMode(v, false);
        } else {
            onClickLearnMode(v);

        }
    }

    public void onClickTestMode(View v, boolean endTimer) {
        TextView numberOfTries = (TextView) findViewById(R.id.numberOfTries);
        timer_cancel();
        clear_efects_on_all_images();

        if (v.getId() == 1) {

            rightAnswersSublevel++;
            wrongAnswersSublevel = 0;
            numberOfTries.setText(getString(R.string.tries) + (wrongAnswersSublevel + 1) + "/" + level.getNumberOfTriesInTest());

            if (getAttempt() == 0)
                rightAnswers++;
            nextLevelOrEnd();
            wrongAnswersSublevel = 0;
        } else {
            Intent intent = new Intent(MainActivity.this, Blank.class);
            startActivity(intent);
            wrongAnswers++;
            wrongAnswersSublevel++;
            setAttempt(1);
            if (endTimer) {
                timeout++;
            }
            if (wrongAnswersSublevel >= level.getNumberOfTriesInTest()) {
                numberOfTries.setVisibility(View.VISIBLE);
                wrongAnswersSublevel = 0;
                numberOfTries.setText(getString(R.string.tries) + (wrongAnswersSublevel + 1) + "/" + level.getNumberOfTriesInTest());
                nextLevelOrEnd();
            } else if (!endTimer) {
                numberOfTries.setVisibility(View.VISIBLE);
                numberOfTries.setText(getString(R.string.tries) + (wrongAnswersSublevel + 1) + "/" + level.getNumberOfTriesInTest());

                TestHandler(200);
                timer_start();
            } else {
                numberOfTries.setVisibility(View.VISIBLE);
                numberOfTries.setText(getString(R.string.tries) + (wrongAnswersSublevel + 1) + "/" + level.getNumberOfTriesInTest());
                TestHandler(200);
                timer_start();
            }
        }

    }

    public void onClickLearnMode(View v) {
        timer.cancel();
        Intent intent = new Intent(MainActivity.this, RewardAndHintActivity.class);
        if (v.getId() == 1) {
            whichTry = 4;
            switch (subLevelMode) {
                case NO_WRONG_ANSWER:
                    subLevelMode = SubLevelMode.FIRST_CORRECT;
                    clicked = false;
                    break;
                case AFTER_WRONG_ANSWER:
                    subLevelMode = SubLevelMode.AFTER_WRONG_ANSWER_1_CORRECT;
                    clicked = false;
                    break;
                case AFTER_WRONG_ANSWER_1_CORRECT:
                    subLevelMode = SubLevelMode.AFTER_WRONG_ANSWER_2_CORRECT;
                    clicked = false;
                    break;
                case AFTER_WRONG_ANSWER_2_CORRECT:
                    subLevelMode = SubLevelMode.AFTER_WRONG_ANSWER_3_CORRECT;
                    break;

            }

            if (getAttempt() == 0) {
                rightAnswers++;
                attempt = 1;
                if (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER_2_CORRECT) {
                    //TODO ogarnąć po co ten if WARTOŚĆ PIERWOTNA - AFTER 1 CORRECT
                    clear_efects_on_all_images();
                    timer_cancel();
                    setLayoutMargins(image_selected, 45 / listSize, 45 / listSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 790 / listSize, getResources().getDisplayMetrics()));
                    attempt = 2;
                }
            }
            rightAnswersSublevel++;

            timer_cancel();
            clear_efects_on_all_images();


            boolean correctness = true;

            if ((subLevelMode == SubLevelMode.FIRST_CORRECT) || (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER_3_CORRECT)) {
                sublevelsLeft--;
                if (!hints) {
                    mySleep(100);
                    startRewardActivity();
                } else {
                    mySleep(100);
                    startHintActivity();
                    if (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER_3_CORRECT || subLevelMode == SubLevelMode.FIRST_CORRECT) {
                        if (!findNextActiveLevel()) {
                            mySleep(200);
                            startEndActivity(true);
                        } else {
                            clear_efects_on_all_images();
                            generateView(photosToUseInSublevel);
                        }


                    }

                    if (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER_2_CORRECT)
                        timer_start();


                }


            } else if (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER_1_CORRECT) {

                mySleep(100);
                timer_cancel();
                startHintActivity();


            } else if (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER_2_CORRECT) {
                timer_cancel();
                if (!hints) {
                    mySleep(100);
                    startRewardActivity();
                } else {
                    mySleep(100);
                    startHintActivity();

                    timer_start();

                }


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        clear_efects_on_all_images();
                        reorder_image();
                    }
                }, 1200);
            }

            if (sublevelsLeft == 0) {
                correctness = checkCorrectness();
            }

            if (correctness && level.isLearnMode()) {

            } else {
                startEndActivity(false);
            }

        } else {
            attempt = 1;


            subLevelMode = SubLevelMode.AFTER_WRONG_ANSWER;
            wrongAnswers++;
            wrongAnswersSublevel++;
            if (whichTry != 3) {
                timeout--;
                timer_finish();
            }
            whichTry = 3;
        }
    }

    void nextLevelOrEnd() {
        sublevelsLeft--;
        wrongAnswersSublevel = 0;
        timer_cancel();

        if (!findNextActiveLevel()) {
            mySleep(600);
            startEndActivity(true);
        } else {
            if (level.isTestMode()) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reorder_image();
                        generateView(photosToUseInSublevel);
                    }
                }, 500);
            } else {
                generateView(photosToUseInSublevel);

            }
        }

    }

    boolean checkCorrectness() {
        if (wrongAnswersSublevel > level.getAmountOfAllowedTriesForEachEmotion()) {
            return false;
        }
        return true;
    }

    String selectPhotoWithSelectedEmotion() {
        Random rand = new Random();
        int photoWithSelectedEmotionIndex = rand.nextInt(photosWithEmotionSelected.size());
        String name = photosWithEmotionSelected.get(photoWithSelectedEmotionIndex);

        return name;

    }

    void selectPhotoWithNotSelectedEmotions(int howMany, String sex) {


        List<Integer> emotions = level.getEmotions();
        List<Integer> emotionsLeft = new ArrayList<>();


        if ((howMany - 1) < emotions.size()) {
            Collections.shuffle(emotions);
            for (int a = howMany - 1; a < emotions.size(); a++) {
                emotionsLeft.add(emotions.get(a));
            }
        }
        j = 0;
        for (int i = 0; i < howMany - 1; i++) {
            int size = photosForEmotion(emotions.get(i), sex).size();
            int random = (int) Math.round(Math.random() * (size - 1));
            if (size > 0) {
                photosToUseInSublevel.add(photosForEmotion(emotions.get(i), sex).get(random));
            } else if (size == 0) {
                if (emotionsLeft.size() > j) {
                    for (int k = j; k < emotionsLeft.size(); k++) {
                        size = photosForEmotion(emotionsLeft.get(k), sex).size();
                        if (size > 0) {
                            int rand = (int) Math.round(Math.random() * (size - 1));
                            photosToUseInSublevel.add(photosForEmotion(emotionsLeft.get(k), sex).get(rand));
                            break;
                        }
                    }
                    j++;
                }
            }
        }
    }

    List<String> photosForEmotion(int emotion, String sex) {
        String emotionName = "";
        switch (emotion) {
            case 1:
                emotionName = "happy";
                break;
            case 2:
                emotionName = "sad";
                break;
            case 3:
                emotionName = "surprised";
                break;
            case 4:
                emotionName = "angry";
                break;
            case 5:
                emotionName = "scared";
                break;
            case 6:
                emotionName = "bored";
                break;
        }
        List<String> photosWithChosenEmotion = new ArrayList<>();

        for (int i = 0; i < photosWithRestOfEmotions.size(); i++) {
            if (photosWithRestOfEmotions.get(i).contains(emotionName) && photosWithRestOfEmotions.get(i).contains(sex))
                photosWithChosenEmotion.add(photosWithRestOfEmotions.get(i));
        }

        return photosWithChosenEmotion;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Different_activity df;

        if (requestCode < 1) {
            df = Different_activity.NO_ACTIVITY;
        } else {
            df = Different_activity.values()[requestCode];
        }


        switch (df) {
            case START_ANIMATION:
                timer_cancel();
                startAnimationActivity();
                break;
            case END_ANIMATION:
                animationEnds = true;
                timer_cancel();
                if (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER_3_CORRECT || subLevelMode == SubLevelMode.FIRST_CORRECT) {
                    if (!findNextActiveLevel()) {

                        startEndActivity(true);
                    } else {
                        clear_efects_on_all_images();
                        generateView(photosToUseInSublevel);
                    }


                }

                if (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER_2_CORRECT)
                    timer_start();

                break;
            case GENERATE_SUBLEVEL:


                java.util.Collections.shuffle(sublevelsList);
                if (!videos)
                    sublevelsLeft = level.getEmotions().size() * level.getSublevelsPerEachEmotion();
                else
                    sublevelsLeft = videoCursor.getCount();

                wrongAnswersSublevel = 0;
                rightAnswersSublevel = 0;
                timeout = 0;
                generateSublevel(sublevelsList.get(sublevelsLeft - 1));

                break;

            case START_RESULTS:
                timer_finish();
                timeout--;
                startResults();

                break;
            case HINT_END:
                clear_efects_on_all_images();
                timer_start();
        }
    }

    private void startHintActivity() {

        hints = false;

        if (speaker == null) {
            speaker = Speaker.getInstance(MainActivity.this);
        }

        timer_cancel();
        selected_image_unzoom();
        String rightEmotion = goodAnswer.replace(".jpg", "").replaceAll("[0-9.]", "").replaceAll("_r_", "").replaceAll("_e_", "");
        String emotion = getResources().getString(getResources().getIdentifier("emotion_" + rightEmotion, "string", getPackageName()));
        Intent intentHint = new Intent(MainActivity.this, RewardAndHintActivity.class);
        intentHint.putExtra("hintMode", true);
        intentHint.putExtra("emotion", emotion);
        String photoName = "/" + getGoodAnswer().replace(".jpg", "");

        ///ADDING A PHOTO TO AN INTENT - VERSION WITHOUT DRAWABLES

        String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        final File path = new File(root + "FriendlyEmotions/Photos" + File.separator);
        String fileName = (path.toString() + photoName + ".jpg");

        intentHint.putExtra("fileName", fileName);
        startActivityForResult(intentHint, Different_activity.HINT_END.value);


    }

    private void startRewardActivity() {
        hints = false;
        if (speaker == null) {
            speaker = Speaker.getInstance(MainActivity.this);
        }

        Intent intentReward = new Intent(MainActivity.this, RewardAndHintActivity.class);

        String rightEmotion = goodAnswer.replace(".jpg", "").replaceAll("[0-9.]", "").replaceAll("_r_", "").replaceAll("_e_", "");
        String emotion = getResources().getString(getResources().getIdentifier("emotion_" + rightEmotion, "string", getPackageName()));
        intentReward.putExtra("emotion", emotion);
        intentReward.putExtra("hintMode", false);
        String photoName = "/" + getGoodAnswer().replace(".jpg", "");

        ///ADDING A PHOTO TO AN INTENT - VERSION WITHOUT DRAWABLES

        String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        final File path = new File(root + "FriendlyEmotions/Photos" + File.separator);
        String fileName = (path.toString() + photoName + ".jpg");

        intentReward.putExtra("fileName", fileName);

        //DZIAŁA DLA DRAWABLES:
        int id = getResources().getIdentifier("pg.autyzm.graprzyjazneemocje:drawable/" + photoName, null, null);
        intentReward.putExtra("photoId", id);

        ///WYPOWIADANE POCHWAŁY
        int praises = level.getPraisesBinary();
        intentReward.putExtra("praises", praises);

        startActivityForResult(intentReward, Different_activity.START_ANIMATION.value);

    }

    public String getGoodAnswer() {
        return goodAnswer;
    }

    private void startAnimationActivity() {
        if (speaker == null) {
            speaker = Speaker.getInstance(MainActivity.this);
        }
        Intent intent = getIntent();
        int currentStrokeColor = intent.getIntExtra("color", 0);
        Intent i = new Intent(MainActivity.this, AnimationActivity.class);
        i.putExtra("color", currentStrokeColor);
        startActivityForResult(i, Different_activity.END_ANIMATION.value);
    }


    private void startEndActivity(boolean pass) {
        if (level.isLearnMode())
            passLevel();
        else {

            startResults();
        }
    }

    public void StartTimerForTest(final Level l) {

        timer = new CountDownTimer(l.getTimeLimitInTest() * 1000, 1000) {


            public void onTick(long millisUntilFinished) {
                if (subLevelMode == SubLevelMode.AFTER_WRONG_ANSWER) {
                }
            }

            public void onFinish() {


                View badAnswer = new View(getApplicationContext());
                badAnswer.setId(0);
                onClickTestMode(badAnswer, true);

                whichTry = 3;

            }
        }.start();
    }

    public void image_grey_out(ImageView image, boolean set) {
        ColorMatrix matrix = new ColorMatrix();
        if (set)
            matrix.setSaturation((float) 0.1);
        else
            matrix.setSaturation((float) 1);


        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        image.setColorFilter(filter);
    }

    public void image_frame(ImageView image, boolean set) {

        int border;
        if (set) border = 15;
        else {
            border = 0;
        }

        image.setPadding(border, border, border, border);
        image.setBackgroundColor(getResources().getColor(R.color.frame));
    }

    public void image_zoom(ImageView image) {
        Animation zooming;
        zooming = AnimationUtils.loadAnimation(this, R.anim.zoom);
        zooming.scaleCurrentDuration(1.05f);
        image.hasOverlappingRendering();
        setLayoutMargins(image, 45 / listSize + 10, 45 / listSize + 10, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 790 / listSize, getResources().getDisplayMetrics()));
        image.startAnimation(zooming);

        image_selected = image;

    }

    public void selected_image_unzoom() {
        Animation unzooming;

        if (image_selected != null) {
            setLayoutMargins(image_selected, 45 / listSize, 45 / listSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 790 / listSize, getResources().getDisplayMetrics()));
            unzooming = AnimationUtils.loadAnimation(this, R.anim.unzoom);
            unzooming.scaleCurrentDuration(0);
            image_selected.startAnimation(unzooming);

        }


    }


    public void clear_efects_on_all_images() {


        LinearLayout imagesLinear = (LinearLayout) findViewById(R.id.imageGallery);

        selected_image_unzoom();

        final int childcount = imagesLinear.getChildCount();
        for (int i = 0; i < childcount; i++) {
            ImageView image = (ImageView) imagesLinear.getChildAt(i);
            image_grey_out(image, false);
            image_frame(image, false);
        }


    }

    public void reorder_image() {
        long choose = Math.round(Math.random());
        boolean reorder = true;
        if (choose == 0) {
            swapRight();

        }
        if (choose == 1) {
            swapLeft();

        }
    }

    private void swapRight() {
        int size = photosToUseInSublevel.size();
        for (int i = 0; i < size - 1; i++) {

            Collections.swap(photosToUseInSublevel, i, i + 1);
        }
        generateView(photosToUseInSublevel);
    }

    private void swapLeft() {
        int size = photosToUseInSublevel.size();
        for (int i = size - 1; i > 0; i--) {
            Collections.swap(photosToUseInSublevel, i, i - 1);
        }
        generateView(photosToUseInSublevel);
    }

    private void timer_start() {
        timer.start();
    }

    private void timer_cancel() {
        timer.cancel();
    }

    private void timer_finish() {
        timer.onFinish();
    }


    private void startTimer(final Level l) {
        if (l.getTimeLimit() != 1) {
            final int hintTypes = l.getHintTypesAsNumber();
            final Context currentContext = this;

            timer = new CountDownTimer(level.isTestMode() ? l.getTimeLimitInTest() * 1000 : l.getTimeLimit() * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    hints = true;
                    LinearLayout imagesLinear = (LinearLayout) findViewById(R.id.imageGallery);

                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation((float) 0.1);
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    if (whichTry != 3) {
                        final int childcount = imagesLinear.getChildCount();
                        for (int i = 0; i < childcount; i++) {
                            final ImageView image = (ImageView) imagesLinear.getChildAt(i);
                            if (l.isLearnMode()) {

                                //FRAME,ENLARGE,MOVE,GREY_OUT
                                if (image.getId() != 1) {
                                    if ((hintTypes & HintTypeValue(HintType.GREY_OUT)) == HintTypeValue(HintType.GREY_OUT)) {
                                        image_grey_out(image, true);
                                    }
                                } else {

                                    if ((hintTypes & HintTypeValue(HintType.FRAME)) == HintTypeValue(HintType.FRAME)) {
                                        image_frame(image, true);
                                    }
                                    if ((hintTypes & HintTypeValue(HintType.MOVE)) == HintTypeValue(HintType.MOVE)) {
                                        Animation shake = AnimationUtils.loadAnimation(currentContext, R.anim.shake);
                                        image.startAnimation(shake);

                                    }
                                    if ((hintTypes & HintTypeValue(HintType.ENLARGE)) == HintTypeValue(HintType.ENLARGE)) {
                                        image_zoom(image);
                                    }
                                }

                            }
                        }
                        timeout++;
                    }
                }
            }.start();

        }
    }

    @Override
    public int soundsToApply(String commandText) {

        MediaPlayer ring;

        switch (commandText) {

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
            case "przestraszony":
            case "scared":
                return R.raw.scared_male;

            default:
                return -1;


        }


    }

    public boolean complexSoundsToApply(String commandText, String emotion, boolean onlyEmotion) {

        MediaPlayer ring = null;
        ImageButton speakerButton = (ImageButton) findViewById(R.id.matchEmotionsSpeakerButton);

        if (onlyEmotion && !clicked) {
            ring = MediaPlayer.create(MainActivity.this, soundsToApply(emotion));
            ring.start();

        } else {
            switch (commandText) {
                case "Pokaż gdzie jest":
                case "Show which is":
                    ring = MediaPlayer.create(MainActivity.this, R.raw.show_where_is);
                    ring.start();
                    while (ring.isPlaying()) {
                        is_playing = true;
                    }
                    is_playing = false;
                    if (clicked)
                        ring.stop();
                    break;
                case "Dotknij gdzie jest":
                case "Touch which is":
                    ring = MediaPlayer.create(MainActivity.this, R.raw.touch_where_is);
                    ring.start();
                    while (ring.isPlaying()) {
                        is_playing = true;
                    }
                    is_playing = false;
                    if (clicked)
                        ring.stop();
                    break;
                case "Wskaż gdzie jest":
                case "Point which is":
                    ring = MediaPlayer.create(MainActivity.this, R.raw.point_where_is);
                    ring.start();
                    while (ring.isPlaying()) {
                        is_playing = true;
                    }
                    is_playing = false;
                    if (clicked)
                        ring.stop();
                    break;
                case "Znajdź gdzie jest":
                case "Find which is":
                    ring = MediaPlayer.create(MainActivity.this, R.raw.find_where_is);
                    ring.start();
                    while (ring.isPlaying()) {
                        is_playing = true;
                    }
                    is_playing = false;
                    if (clicked)
                        ring.stop();
                    break;
                case "Wybierz gdzie jest":
                case "Select which is":
                    ring = MediaPlayer.create(MainActivity.this, R.raw.select_where_is);
                    ring.start();
                    while (ring.isPlaying()) {
                        is_playing = true;
                    }
                    is_playing = false;
                    if (clicked)
                        ring.stop();
                    break;
                case "Pokaż":
                case "Show":
                    ring = MediaPlayer.create(MainActivity.this, R.raw.show);
                    ring.start();
                    while (ring.isPlaying()) {
                        is_playing = true;
                    }
                    is_playing = false;
                    if (clicked)
                        ring.stop();
                    break;

            }
        }
        switch (emotion) {
            case "wesoły":
            case "happy":
                ring = MediaPlayer.create(MainActivity.this, R.raw.happy_male);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "wesoła":
                ring = MediaPlayer.create(MainActivity.this, R.raw.happy_female);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "smutny":
            case "sad":
                ring = MediaPlayer.create(MainActivity.this, R.raw.sad_male);
                ring.start();
                if (clicked)
                    ring.stop();
                break;
            case "smutna":
                ring = MediaPlayer.create(MainActivity.this, R.raw.sad_female);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "zły":
            case "angry":
                ring = MediaPlayer.create(MainActivity.this, R.raw.angry_male);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "zła":
                ring = MediaPlayer.create(MainActivity.this, R.raw.angry_female);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "znudzony":
            case "bored":
                ring = MediaPlayer.create(MainActivity.this, R.raw.bored_male);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "znudzona":
                ring = MediaPlayer.create(MainActivity.this, R.raw.bored_female);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "przestraszony":
            case "scared":
                ring = MediaPlayer.create(MainActivity.this, R.raw.scared_male);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "przestraszona":

                ring = MediaPlayer.create(MainActivity.this, R.raw.scared_female);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "zdziwiony":
            case "surprised":
                ring = MediaPlayer.create(MainActivity.this, R.raw.surprised_male);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;
            case "zdziwiona":
                ring = MediaPlayer.create(MainActivity.this, R.raw.surprised_female);
                ring.start();
                while (ring.isPlaying()) {
                    is_playing = true;
                }
                is_playing = false;
                if (clicked)
                    ring.stop();
                break;

        }

        return is_playing;
    }

    public int CommandTypeValue(CommandType command) {
        if (command == CommandType.SHOW)
            return 1;
        if (command == CommandType.SELECT)
            return 2;
        if (command == CommandType.POINT)
            return 2 * 2;
        if (command == CommandType.TOUCH)
            return 2 * 2 * 2;
        if (command == CommandType.FIND)
            return 2 * 2 * 2 * 2;
        return 1;
    }

    public int HintTypeValue(HintType hint) {
        if (hint == HintType.FRAME)
            return 1;
        if (hint == HintType.ENLARGE)
            return 2;
        if (hint == HintType.MOVE)
            return 2 * 2;
        if (hint == HintType.GREY_OUT)
            return 2 * 2 * 2;
        return 2 * 2 * 2;
    }

    @Override
    public void onBackPressed() {
    }

    public void setLayoutMargins(ImageView image, int leftMargin, int rightMargin, int height) {
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50 - (150 / listSize), getResources().getDisplayMetrics());

        lp = new LinearLayout.LayoutParams(height, height);
        lp.setMargins(leftMargin, 10, rightMargin, margin);

        onePicDisplayed = (photosToUseInSublevel.size() == 1);
        if (onePicDisplayed) {
            lp = new LinearLayout.LayoutParams(420, 420);

            lp.setMargins(30, 50, 30, 30);
        }
        lp.gravity = Gravity.CENTER;
        image.setLayoutParams(lp);


    }

    private void passLevel() {
        MediaPlayer ring = MediaPlayer.create(MainActivity.this, R.raw.fanfare3);
        ring.start();

        Intent i = new Intent(this, AnimationEndActivity.class);
        startActivityForResult(i, Different_activity.START_RESULTS.value);

    }

    private void startResults() {
        Intent in = new Intent(this, EndActivity.class);
        //in.putExtra("PASS", pass);
        in.putExtra("WRONG", wrongAnswers);
        in.putExtra("RIGHT", rightAnswers);
        in.putExtra("TIMEOUT", timeout);
        in.putExtra("LANG", sqlm.getCurrentLang());

        //startActivityForResult(in, Different_activity.GENERATE_SUBLEVEL.value);
        startActivity(in);

    }

    public void mySleep(int duration) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                timer_cancel();
                clear_efects_on_all_images();

            }
        }, duration);
    }

    public void TestHandler(int duration) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reorder_image();
            }
        }, duration);
    }

    public void shuffling() {
        String rightEmotion = goodAnswer.replace(".jpg", "").replaceAll("[0-9.]", "").replaceAll("_r_", "").replaceAll("_e_", "");
        reorder_image();
        linearLayout1 = (LinearLayout) findViewById(R.id.imageGallery);
        linearLayout1.setGravity(Gravity.CENTER);
        linearLayout1.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        linearLayout1.removeAllViews();
        for (
                String photoName : photosToUseInSublevel) {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            File fileOut = new File(root + "FriendlyEmotions/Photos" + File.separator + photoName);
            try {
                final ImageView image = new ImageView(MainActivity.this);
                listSize = photosToUseInSublevel.size();

                setLayoutMargins(image, 45 / listSize, 45 / listSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 790 / listSize, getResources().getDisplayMetrics()));
                if (photoName.contains(rightEmotion)) {
                    image.setId(1);
                } else {
                    image.setId(0);
                }

                image.setOnClickListener(this);
                final Bitmap captureBmp = Media.getBitmap(getContentResolver(), Uri.fromFile(fileOut));
                image.setImageBitmap(captureBmp);
                linearLayout1.addView(image);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public enum HintType {
        FRAME, ENLARGE, MOVE, GREY_OUT
    }

    public enum CommandType {
        SHOW, SELECT, POINT, TOUCH, FIND
    }

    public enum SubLevelMode {
        NO_WRONG_ANSWER,
        AFTER_WRONG_ANSWER,
        FIRST_CORRECT,
        AFTER_WRONG_ANSWER_1_CORRECT,  //zalivzony 1 piziom ale z błędem
        AFTER_WRONG_ANSWER_2_CORRECT, //zaliczony drugi
        AFTER_WRONG_ANSWER_3_CORRECT
    }

    public enum Different_activity {
        NO_ACTIVITY(0),
        START_ANIMATION(1),
        END_ANIMATION(2),
        GENERATE_SUBLEVEL(3),
        START_RESULTS(4),
        HINT_END(5);
        private final int value;

        private Different_activity(int value) {
            this.value = value;
        }
    }
}