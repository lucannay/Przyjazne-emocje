package pg.autyzm.przyjazneemocje.configuration;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pg.autyzm.przyjazneemocje.DialogHandler;
import pg.autyzm.przyjazneemocje.R;
import pg.autyzm.przyjazneemocje.lib.SqliteManager;
import pg.autyzm.przyjazneemocje.lib.entities.Level;

//validations before saving the configuration view

public class LevelValidator extends AppCompatActivity {


    Level validatedLevel;
    int womanPhotos, manPhotos;
    int mode;

    Context currentContext;
    List<Integer> photoIds = new ArrayList<>();
    SqliteManager sqlm = SqliteManager.getInstance(this);

    public LevelValidator(Level l, Object obj) {
        validatedLevel = l;
        currentContext = (Context) obj;
    }


    public boolean validateLevel() {

        if (validatedLevel.getName().length() == 0) {
            Toast.makeText(currentContext, R.string.level_name_validation_too_short, Toast.LENGTH_LONG).show();
            return false;
        }
        if (validatedLevel.getName().length() > 55) {
            Toast.makeText(currentContext, R.string.level_name_validation_too_long, Toast.LENGTH_LONG).show();
            return false;
        }
        if (validatedLevel.getPhotosOrVideosIdList().isEmpty()) {
            Toast.makeText(currentContext, R.string.empty_photos_learn_mode, Toast.LENGTH_LONG).show();
            return false;
        }

        if (!everyEmotionHasAtLestOnePhoto()) {
            return false;
        }
        if (!photosOfBothSexesChosen()) {
            return false;
        }
        if (validatedLevel.getCommandTypesAsNumber() == 0) {
            Toast.makeText(currentContext, R.string.commandWarning, Toast.LENGTH_LONG).show();
            return false;
        } else
            return true;
    }

    public boolean photosOfBothSexesChosen() {
        SqliteManager sqlm = SqliteManager.getInstance(this);

        boolean differentSexes = validatedLevel.isOptionDifferentSexes();
        for (mode = 0; mode < 2; mode++) {

            if (mode == 0) photoIds = validatedLevel.getPhotosOrVideosIdList();
            if (mode == 1) photoIds = validatedLevel.getPhotosOrVideosIdListInTest();
            for (int photoId : photoIds) {
                Cursor cursorPhotoId = sqlm.givePhotoWithId(photoId);
                cursorPhotoId.moveToFirst();
                String photoName = cursorPhotoId.getString(cursorPhotoId.getColumnIndex("name"));

                if (photoName.contains("_woman")) {
                    womanPhotos++;
                }
                if (photoName.contains("_man")) {
                    manPhotos++;
                }

            }

            if ((differentSexes) && (womanPhotos == 0 || manPhotos == 0)) {
                if (mode == 0)
                    Toast.makeText(currentContext, R.string.both_sexes_material, Toast.LENGTH_LONG).show();
                if (mode == 1)
                    Toast.makeText(currentContext, R.string.both_sexes_test, Toast.LENGTH_LONG).show();
                mode++;
                return false;
            }
        }
        return true;
    }

    public boolean everyEmotionHasAtLestOnePhoto() {
        String emotionNameWithoutSex;
        LevelConfigurationActivity lca = new LevelConfigurationActivity();
        for (int emotion : validatedLevel.getEmotions()) {
            List<Integer> id_zd = new ArrayList<>();
            emotionNameWithoutSex = getEmotionNameinBaseLanguage2(emotion);
            for (int mode = 0; mode < 2; mode++) {
                if (mode == 0)
                    id_zd = selectedPhotosForGameCheck(validatedLevel.getPhotosOrVideosIdList(), emotionNameWithoutSex);
                else if ((mode == 1) && (!validatedLevel.isMaterialForTest()))
                    id_zd = selectedPhotosForGameCheck(validatedLevel.getPhotosOrVideosIdListInTest(), emotionNameWithoutSex);

                if (id_zd.size() < 1) {
                    if (mode == 0) {
                        Toast.makeText(currentContext, "Wybierz w zakładce MATERIAŁ conajmniej jedno zdjęcie dla emocji: " + (returnEmotionInPolish(emotionNameWithoutSex)), Toast.LENGTH_LONG).show();
                    } else if ((mode == 1) && (!validatedLevel.isMaterialForTest())) {
                        Toast.makeText(currentContext, "Wybierz w zakładce TEST conajmniej jedno zdjęcie dla emocji: " + returnEmotionInPolish(emotionNameWithoutSex), Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            }
        }

        return true;
    }


    public boolean numberOfPhotosSelected(int numberOfPhotosDisplayed, boolean differentSexes) {
        String emotionNameWithoutSex;
        int womanPhotos = 0, manPhotos = 0;
        int countedWoman;
        int countedMan;
        int mode = 0;
        if (!differentSexes) {
            List<Integer> id_zd = new ArrayList<>();
            for (int emotion : validatedLevel.getEmotions()) {
                countedMan = 0;
                countedWoman = 0;

                emotionNameWithoutSex = getEmotionNameinBaseLanguage2(emotion);
                //for (int mode = 0; mode < 2; mode++) {
                if (mode == 0) {
                    id_zd = selectedPhotosForGameCheck(validatedLevel.getPhotosOrVideosIdList(), emotionNameWithoutSex);
                } else if (mode == 1) {
                    id_zd = selectedPhotosForGameCheck(validatedLevel.getPhotosOrVideosIdListInTest(), emotionNameWithoutSex);
                }

                for (int i : id_zd) {
                    Cursor curEmotion = sqlm.givePhotoWithId(i);

                    if (curEmotion.getCount() != 0) {
                        curEmotion.moveToFirst();
                        String photoName = curEmotion.getString(curEmotion.getColumnIndex("name"));

                        if (countedWoman == 0 && photoName.contains("woman")) {
                            womanPhotos++;
                            countedWoman = 1;
                        }
                        if (countedMan == 0 && photoName.contains("_man")) {
                            manPhotos++;
                            countedMan = 1;
                        }
                    }
                    curEmotion.close();
                }
                //mode++;
            }

            if (womanPhotos < numberOfPhotosDisplayed && womanPhotos != 0) {
                //Toast.makeText(currentContext, R.string.chosen_amount + numberOfPhotosDisplayed + ". " + R.string.less_photos_than_chosen_amount_female, Toast.LENGTH_LONG).show();
                return false;
            } else if (manPhotos < numberOfPhotosDisplayed && manPhotos != 0) {
                // Toast.makeText(currentContext, R.string.chosen_amount + numberOfPhotosDisplayed + ". " + R.string.less_photos_than_chosen_amount_male, Toast.LENGTH_LONG).show();
                return false;

            }
        }

        return true;
    }


    List<Integer> selectedPhotosForGameCheck(List<Integer> photos, String emotionBaseNameWithoutSex) {
        List<Integer> photosForEmotionList = new ArrayList<>();
        for (int e : photos) {
            Cursor curEmotion = sqlm.givePhotoWithId(e);


            if (curEmotion.getCount() != 0) {
                curEmotion.moveToFirst();
                String photoEmotionName = curEmotion.getString(curEmotion.getColumnIndex("emotion"));
                String photoName = curEmotion.getString(curEmotion.getColumnIndex("name"));

                if (photoEmotionName.contains(emotionBaseNameWithoutSex)) {
                    photosForEmotionList.add(e);
                }
                curEmotion.close();
            }
        }

        return photosForEmotionList;
    }

    public String getEmotionNameinBaseLanguage2(int emotionNumber) {

        switch (emotionNumber) {
            case 0:
                return "happy";
            case 1:
                return "sad";
            case 2:
                return "surprised";
            case 3:
                return "angry";
            case 4:
                return "scared";
            case 5:
                return "bored";

        }
        return "zly numer emocji";
    }


    public String returnEmotionInPolish(String emotion) {
        switch (emotion) {
            case "happy":
                return "wesoły";
            case "sad":
                return "smutny";
            case "angry":
                return "zły";
            case "scared":
                return "przestraszony";
            case "surprised":
                return "zdziwiony";
            case "bored":
                return "znudzony";
            default:
                return "";
        }
    }

    public int femalePhotos(int mode) {
        String emotionNameWithoutSex;
        int womanPhotos = 0;
        int countedWoman;


        List<Integer> id_zd = new ArrayList<>();
        for (int emotion : validatedLevel.getEmotions()) {
            countedWoman = 0;

            emotionNameWithoutSex = getEmotionNameinBaseLanguage2(emotion);

            //for (int mode = 0; mode < 2; mode++) {
            if (mode == 0) {
                id_zd = selectedPhotosForGameCheck(validatedLevel.getPhotosOrVideosIdList(), emotionNameWithoutSex);
            } else if (mode == 1) {
                id_zd = selectedPhotosForGameCheck(validatedLevel.getPhotosOrVideosIdListInTest(), emotionNameWithoutSex);
            }

            for (int i : id_zd) {
                Cursor curEmotion = sqlm.givePhotoWithId(i);

                if (curEmotion.getCount() != 0) {
                    curEmotion.moveToFirst();
                    String photoName = curEmotion.getString(curEmotion.getColumnIndex("name"));

                    if (countedWoman == 0 && photoName.contains("woman")) {
                        womanPhotos++;
                        countedWoman = 1;
                    }
                }

                curEmotion.close();
            }


        }
        return womanPhotos;
    }

    public int malePhotos(int mode) {
        String emotionNameWithoutSex;
        int womanPhotos = 0, manPhotos = 0;
        int countedWoman;
        int countedMan;


        List<Integer> id_zd = new ArrayList<>();
        for (int emotion : validatedLevel.getEmotions()) {
            countedMan = 0;

            emotionNameWithoutSex = getEmotionNameinBaseLanguage2(emotion);
            //for (int mode = 0; mode < 2; mode++) {
            if (mode == 0) {
                id_zd = selectedPhotosForGameCheck(validatedLevel.getPhotosOrVideosIdList(), emotionNameWithoutSex);
            } else if (mode == 1) {
                id_zd = selectedPhotosForGameCheck(validatedLevel.getPhotosOrVideosIdListInTest(), emotionNameWithoutSex);
            }

            for (int i : id_zd) {
                Cursor curEmotion = sqlm.givePhotoWithId(i);

                if (curEmotion.getCount() != 0) {
                    curEmotion.moveToFirst();
                    String photoName = curEmotion.getString(curEmotion.getColumnIndex("name"));


                    if (countedMan == 0 && photoName.contains("_man")) {
                        manPhotos++;
                        countedMan = 1;
                    }
                }
                curEmotion.close();
            }

        }
        return manPhotos;
    }
}