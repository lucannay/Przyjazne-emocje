package pg.autyzm.przyjazneemocje;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pg.autyzm.przyjazneemocje.adapter.CustomList;
import pg.autyzm.przyjazneemocje.adapter.ILevelListCallback;
import pg.autyzm.przyjazneemocje.adapter.LevelItem;
import pg.autyzm.przyjazneemocje.camera.MainCameraActivity;
import pg.autyzm.przyjazneemocje.configuration.LevelConfigurationActivity;
import pg.autyzm.przyjazneemocje.configuration.LevelValidator;
import pg.autyzm.przyjazneemocje.lib.SqliteManager;
import pg.autyzm.przyjazneemocje.lib.entities.Level;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static pg.autyzm.przyjazneemocje.lib.SqliteManager.getInstance;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE_CAMERA = 1000;
    private final List<LevelItem> levelList = new ArrayList<>();


    private static MainActivity appContext;
    public SqliteManager sqlm;
    protected Locale myLocale;
    String currentLanguage = null;

    ArrayList<String> list;
    ArrayList<Boolean> active_list;
    String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    private CustomList adapter;


    public static MainActivity getAppContext() {
        return appContext;
    }

    private boolean hideDefaultValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setTitle(R.string.app_name);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));


        initAdapter();
        initShowHideDefaultsLevelButton();

        File createMainDir = new File(root + "FriendlyEmotions" + File.separator);

        if (!createMainDir.exists())
            createMainDir.mkdir();

        sqlm = getInstance(this);

        currentLanguage = getIntent().getStringExtra(SplashActivity.CURRENT_LANG);


        updateLevelList();
        //generate list

        sqlm.cleanTable("photos"); //TODO not clean and add, but only update
        sqlm.cleanTable("videos");


        String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

        File createDir = new File(root + "FriendlyEmotions/Photos" + File.separator);
        //TODO jak katalog nie istnieje, to przepisujemy zdjęcia z resourcow do nowotworzonego katalogu
        if (!createDir.exists()) {
            createDir.mkdir();

            Field[] drawables = pg.autyzm.przyjazneemocje.R.drawable.class.getFields();
            for (Field f : drawables) {
                try {
                    if (ifConstainsEmotionName(f.getName())) {
                        extractFromDrawable(f, "Photos", ".jpg", Bitmap.CompressFormat.JPEG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

       /* File createDirV = new File(root + "FriendlyEmotions/Videos" + File.separator);
        if (!createDirV.exists()) {
            createDirV.mkdir();

            Field[] raw = pg.autyzm.przyjazneemocje.R.raw.class.getFields();
            for (Field f : raw) {
                try {
                    extractFromDrawable(f, "Videos", ".mp4", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }*/

        //TODO bezwarunkowe przepisanie zdjęć z katalogu do bazy danych
        if (new File(root + "FriendlyEmotions/Photos").list() != null) {

            for (String fileName : new File(root + "FriendlyEmotions/Photos").list()) {

                try {
                    int resID = getResources().getIdentifier(fileName, "drawable", getPackageName());
                    String[] photoSeg = fileName.split("_");
                    sqlm.addPhoto(resID, photoSeg[0] + "_" + photoSeg[1], fileName);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        sqlm.AddDefaultLevelsIfNeeded();

        Button smile = (Button) findViewById(R.id.uruchomSmileButton);
        smile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("pg.smile");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(MainActivity.this, "There is no package available in android", Toast.LENGTH_LONG).show();
                }

            }
        });

        Button play = (Button) findViewById(R.id.game);
        play.setBackgroundResource(R.drawable.game2);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (levelList.size() < 1) {
                    Toast.makeText(MainActivity.this, R.string.activate, Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("pg.autyzm.graprzyjazneemocje");
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        System.exit(0);

                    } else {
                        Toast.makeText(MainActivity.this, "There is no package available in android", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void initShowHideDefaultsLevelButton() {
        SwitchCompat switchCompat = findViewById(R.id.deafultLevelsBtn);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && levelList.size() > 4) {
                    int id_active = 0;
                    int id_last = 0;
                    boolean learn_mode = true;
                    for (LevelItem levelItem : levelList) {
                        if (levelItem.isLearnMode() || levelItem.isTestMode()) {
                            id_active = levelItem.getLevelId();
                            learn_mode = levelItem.isLearnMode();
                        }
                        id_last = levelItem.getLevelId();
                    }
                    if (id_active < 4)
                        updateActiveState(id_last, learn_mode);

                }
                hideDefaultValues = isChecked;

                updateLevelList();
            }
        });
    }

    private void initAdapter() {
        adapter = new pg.autyzm.przyjazneemocje.adapter.CustomList(levelList, new ILevelListCallback() {
            @Override
            public void editLevel(LevelItem level) {
                openLevelConfigActivity(level.getLevelId());
            }

            @Override
            public void removeLevel(LevelItem level) {
                doclick(level);
            }

            int id = 0;

            public void doclick(LevelItem level) {
                DialogHandler appdialog = new DialogHandler();
                String information = "";
                id = level.getLevelId();


                appdialog.Confirm(MainActivity.this, getResources().getString(R.string.level_removal), getResources().getString(R.string.confirm_removal),
                        getResources().getString(R.string.cancel_removal), getResources().getString(R.string.ok_removal), aproc(), bproc());
            }

            public Runnable aproc() {
                return new Runnable() {
                    public void run() {
                        deleteLevel(id);
                        if (levelList.size() == 1) {
                            SwitchCompat switchCompat = findViewById(R.id.deafultLevelsBtn);
                            switchCompat.setChecked(false);
                        } else {
                            updateLevelList();
                        }


                        Log.d("Test", "This from A proc");
                    }
                };
            }

            public Runnable bproc() {
                return new Runnable() {
                    public void run() {
                        Log.d("Test", "This from B proc");

                    }
                };
            }


            @Override
            public void setLevelActive(LevelItem level, boolean isChecked, boolean isLearnMode) {
                Log.d("Testo", " ------------------------------------------------------");
                updateActiveState(level.getLevelId(), isLearnMode);
            }
        });

        ListView lView = findViewById(R.id.list);
        lView.setAdapter(adapter);
    }


    private void updateActiveState(int levelId, boolean learnMode) {
        for (LevelItem levelItem : levelList) {
            int id = levelItem.getLevelId();
            Cursor cur2 = sqlm.giveLevel(id);
            Cursor cur3 = sqlm.givePhotosInLevel(id);
            Cursor cur4 = sqlm.giveEmotionsInLevel(id);

            Level l = new Level(cur2, cur3, cur4);
            l.setLevelActive(levelId == id);
            levelItem.setActive(l.isLevelActive());
            l.setLearnMode(levelId == id && learnMode);
            l.setTestMode(levelId == id && !learnMode);
            levelItem.setLearnMode(l.isLearnMode());
            levelItem.setTestMode(l.isTestMode());
            // Log.d("Testo - UpdateActivity ", "Name levelItem: " + levelItem.getName() + ", learnMode: " + levelItem.isLearnMode() + ", testMode: " + levelItem.isTestMode());
            //  Log.d("Testo - UpdateActivity ", "Name level: " + l.getName() + ", learnMode: " + l.isLearnMode() + ", testMode: " + l.isTestMode());
            sqlm.saveLevelToDatabase(l, true);
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteLevel(int levelId) {
        sqlm.delete("levels", "id", String.valueOf(levelId));
        sqlm.delete("levels_photos", "levelid", String.valueOf(levelId));
        sqlm.delete("levels_emotions", "levelid", String.valueOf(levelId));
    }

    private void openLevelConfigActivity(int levelId) {
        Intent intent = new Intent(MainActivity.this, LevelConfigurationActivity.class);

        Bundle b = new Bundle();
        b.putInt("key", levelId);
        b.putBoolean("edit", true);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void onBackPressed() {
        finish();
    }


    // napisuje, bo chce, by po dodaniu poziomu lista poziomow w main activity automatycznie sie odswiezala - Pawel
    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        updateLevelList();
    }

    public boolean ifConstainsEmotionName(String inputString) {
        Cursor cur = sqlm.giveAllEmotions();
        while (cur.moveToNext()) {
            String emotion = cur.getString(1);
            if (inputString.contains(emotion))
                return true;
        }
        return false;
    }


    public void sendMessage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, LevelConfigurationActivity.class);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = "String";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);

    }

    /*public void updateLevelList(){

        Cursor cur = sqlm.giveAllLevels();
        list = new ArrayList<String>();
        active_list = new ArrayList<Boolean>();

        while(cur.moveToNext())
        {
            String name = cur.getString(cur.getColumnIndex("name"));
            String levelId = cur.getInt(0) + " " + name;
            //String levelId = "Level " + cur.getInt(0);

            int active = cur.getInt(cur.getColumnIndex("is_level_active"));
            boolean isLearnMode = (active != 0);
            active_list.add(isLearnMode);
            list.add(levelId);

        }

        //instantiate custom adapter
        CustomList adapter = new CustomList(list, active_list, this);

        //handle listview and assign adapter
        ListView lView = (ListView) findViewById(R.id.list);
        lView.setAdapter(adapter);

    }*/


    private void extractFromDrawable(Field field, String dir, String fileExt, Bitmap.CompressFormat format) throws IOException {
//TODO zapisuje prawdopodbnie do katalogu ?? zmiana rozdzielczości
        String emotName = field.getName();
        int resID = getResources().getIdentifier(emotName, "drawable", getPackageName());
        String path = root + "FriendlyEmotions/" + dir + File.separator;
        File file = new File(path, emotName + fileExt);
        FileOutputStream outStream = new FileOutputStream(file);

        if (format != null) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), resID);
            bm.compress(format, 100, outStream);
            outStream.flush();
            outStream.close();

        } else {
            resID = getResources().getIdentifier(emotName, "raw", getPackageName());
            InputStream in = getResources().openRawResource(resID);
            byte[] buff = new byte[1024];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    outStream.write(buff, 0, read);
                }
            } finally {
                in.close();
                outStream.close();
            }
        }
    }

    public void updateLevelList() {
        levelList.clear();
        Cursor cur = sqlm.giveAllLevels();
        int lpDisplay = 1;
        if (cur.moveToFirst()) {
            do {
                int levelId = cur.getInt(0);
                String name = cur.getString(cur.getColumnIndex("name"));
                boolean is_default = cur.getInt(cur.getColumnIndex("is_default")) == 1;
                String displayName = "";
                if (name.contains("::")) {
                    if (sqlm.getCurrentLang().startsWith("pl")) {
                        displayName += name.split("::")[0];
                    } else {
                        displayName += name.split("::")[1];
                    }
                } else {
                    displayName += name;
                }

                int isLearnMode = cur.getInt(cur.getColumnIndex("is_learn_mode"));
                int isTestMode = cur.getInt(cur.getColumnIndex("is_test_mode"));
                boolean isDefault = false;
                if (levelId < 5) isDefault = true;

                if (!isDefault || !hideDefaultValues) {

                    displayName = lpDisplay++ + ". " + displayName;
                    levelList.add(new LevelItem(levelId, displayName, (isLearnMode != 0), (isTestMode != 0), isDefault));
                }
            }

            while (cur.moveToNext());
        }

        cur.close();
       /* if (hideDefaultValues) {
            levelList.remove(0);
            levelList.remove(0);
            levelList.remove(0);
            levelList.remove(0);
            levelList.remove(0);
        }*/
        boolean is_any_active = false;
        int id = 0;
        for (LevelItem levelItem : levelList) {
            if (levelItem.isLearnMode() || levelItem.isTestMode())
                is_any_active = true;
            id = levelItem.getLevelId();
        }
        if (!is_any_active && id != 0) {
            updateActiveState(id, true);
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQ_CODE_CAMERA == requestCode && resultCode == Activity.RESULT_OK) {
            recreate();
        }
    }
}

