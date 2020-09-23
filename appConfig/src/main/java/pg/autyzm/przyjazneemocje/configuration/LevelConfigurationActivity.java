package pg.autyzm.przyjazneemocje.configuration;

import java.lang.StringBuilder;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import pg.autyzm.przyjazneemocje.DialogHandler;
import pg.autyzm.przyjazneemocje.R;
import pg.autyzm.przyjazneemocje.View.CheckboxGridAdapter;
import pg.autyzm.przyjazneemocje.View.CheckboxGridBean;
import pg.autyzm.przyjazneemocje.View.CheckboxImageAdapter;
import pg.autyzm.przyjazneemocje.View.GridCheckboxImageBean;
import pg.autyzm.przyjazneemocje.adapter.LevelItem;
import pg.autyzm.przyjazneemocje.lib.SqliteManager;
import pg.autyzm.przyjazneemocje.lib.entities.Level;

import static pg.autyzm.przyjazneemocje.lib.SqliteManager.getInstance;


public class LevelConfigurationActivity extends AppCompatActivity {

    boolean duringInitiation = true;
    public static final int MATERIAL_FOR_TEST_DATA = 0;

    ArrayList<CheckboxGridBean> praiseList = new ArrayList<>();
    private Level level = Level.defaultLevel();
    int[] flag = {0};
    boolean checked = true;


    public int getCommandTypesAsNumber() {
        return commandTypesAsNumber;
    }

    public void setCommandTypesAsNumber(int commandTypesAsNumber) {
        this.commandTypesAsNumber = commandTypesAsNumber;
    }

    private int commandTypesAsNumber = 0;
    String currentEmotionName;
    int womanPhotos = 0, manPhotos = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //tworzenie nowego levelu
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.tab_view);
        //setTitle(R.string.app_name);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        createTabMaterial();
        createTabLearningWays();
        createTabConsolidation();
        createTabTest();
        createTabSave();
        loadLevelIfItIsEditionMode();

    }


    private void loadLevelIfItIsEditionMode() {
        duringInitiation = true;
        // Loaded level id retrieval

        Bundle b = getIntent().getExtras();
        int loadedLevelId = -1; // or other values
        if (b != null)
            loadedLevelId = b.getInt("key");

        if (loadedLevelId > 0) {
            loadLevelFromDatabaseAndInjectDataToGUI(loadedLevelId);
        } else {
            getLevel().setLearnMode(false);
            getLevel().setTestMode(false);
            getLevel().setMaterialForTest(true);
        }
        duringInitiation = false;
    }


    private void loadLevelFromDatabaseAndInjectDataToGUI(int loadedLevelId) {

        SqliteManager sqlm = getInstance(this);


        Cursor cur2 = sqlm.giveLevel(loadedLevelId);
        Cursor cur3 = sqlm.givePhotosInLevel(loadedLevelId);
        Cursor cur4 = sqlm.giveEmotionsInLevel(loadedLevelId);

        setLevel(new Level(cur2, cur3, cur4));


        // 1 Material
        EditText nrEmotions = (EditText) findViewById(R.id.nr_emotions);
        nrEmotions.setText(String.valueOf(getLevel().getEmotions().size()));

        // 2 panel

        TextView pvPerLevel = (TextView) findViewById(R.id.number_photos);
        pvPerLevel.setText(getLevel().getPhotosOrVideosShowedForOneQuestion() + "");

        TextView sublevels = (TextView) findViewById(R.id.number_try);
        sublevels.setText(getLevel().getSublevelsPerEachEmotion() + "");

        // question type

        Spinner spinner = (Spinner) findViewById(R.id.spinner_command);
        int spinnerPosition = 0;

        switch (getLevel().getQuestionType()) {
            case EMOTION_NAME:
                spinnerPosition = 0;
                break;
            case SHOW_WHERE_IS_EMOTION_NAME:
                spinnerPosition = 2;
                break;
            case SHOW_EMOTION_NAME:
                spinnerPosition = 1;
                break;
        }

        spinner.setSelection(spinnerPosition);

        //kinds of commands


        CheckBox commandChckBox;


        commandChckBox = (CheckBox) findViewById(R.id.show);


        if ((1 & getLevel().getCommandTypesAsNumber()) == 1) {
            commandChckBox.setChecked(true);
        } else {
            commandChckBox.setChecked(false);
        }

        commandChckBox = (CheckBox) findViewById(R.id.select);
        if ((2 & getLevel().getCommandTypesAsNumber()) == 2) {
            commandChckBox.setChecked(true);
        } else {
            commandChckBox.setChecked(false);
        }

        commandChckBox = (CheckBox) findViewById(R.id.point);
        if ((4 & getLevel().getCommandTypesAsNumber()) == 4) {
            commandChckBox.setChecked(true);
        } else {
            commandChckBox.setChecked(false);
        }


        commandChckBox = (CheckBox) findViewById(R.id.touch);
        if ((8 & getLevel().getCommandTypesAsNumber()) == 8) {
            commandChckBox.setChecked(true);
        } else {
            commandChckBox.setChecked(false);
        }

        commandChckBox = (CheckBox) findViewById(R.id.find);
        if ((16 & getLevel().getCommandTypesAsNumber()) == 16) {
            commandChckBox.setChecked(true);
        } else {
            commandChckBox.setChecked(false);
        }

        //one or different genders below command
        RadioButton gendersOption1 = (RadioButton) findViewById(R.id.plci_opcja1);

        RadioButton gendersOption2 = (RadioButton) findViewById(R.id.plci_opcja2);

        gendersOption1.setChecked(!(getLevel().isOptionDifferentSexes()));
        gendersOption2.setChecked(getLevel().isOptionDifferentSexes());

        // should questin be read aloud checkbox
        CheckBox isShouldQuestionBeReadAloud = (CheckBox) findViewById(R.id.checkBox);
        isShouldQuestionBeReadAloud.setChecked(getLevel().isShouldQuestionBeReadAloud());

        TextView secondsToHint = (TextView) findViewById(R.id.time);
        if (getLevel().getTimeLimit() > 1) {
            secondsToHint.setText(String.valueOf(getLevel().getTimeLimit()));
        }
        // hint types

        CheckBox checkBox;
        checkBox = (CheckBox) findViewById(R.id.obramuj);

        if ((1 & getLevel().getHintTypesAsNumber()) == 1) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        checkBox = (CheckBox) findViewById(R.id.powieksz);
        if ((2 & getLevel().getHintTypesAsNumber()) == 2) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }


        checkBox = (CheckBox) findViewById(R.id.porusz);
        if ((4 & getLevel().getHintTypesAsNumber()) == 4) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }


        checkBox = (CheckBox) findViewById(R.id.wyszarz);
        if ((8 & getLevel().getHintTypesAsNumber()) == 8) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }


        // 3 panel

        // praises

        int praisesBinary = level.getPraisesBinary();

        int praisePositionOfCheckbox = 1;
        for (Object praiseCheckbox : praiseList) {

            CheckboxGridBean checkboxGridBean = (CheckboxGridBean) praiseCheckbox;
            checkboxGridBean.setChecked(false);

            if ((praisesBinary & praisePositionOfCheckbox) == praisePositionOfCheckbox) {
                checkboxGridBean.setChecked(true);
            }
            praisePositionOfCheckbox *= 2;
        }

        updateGridPraise();

        // 4 panel
        CheckBox theSameMaterial = (CheckBox) findViewById(R.id.theSameMaterial);
        theSameMaterial.setChecked(getLevel().isMaterialForTest());

        EditText timeLimit = (EditText) findViewById(R.id.number_time_test);
        timeLimit.setText(getLevel().getTimeLimitInTest() + "");

        TextView numberOfTriesInTest = (TextView) findViewById(R.id.numberOfTriesLabel);
        numberOfTriesInTest.setText(getLevel().getNumberOfTriesInTest() + "");

        // panel 5
        EditText levelName = (EditText) findViewById(R.id.step_name);
        levelName.setText(getLevel().getName());
    }

    private void createTabSave() {
        createDefaultStepName();
        updateInfo();
    }

    private void createTabTest() {
        activateNumberTimeTest();
        activateNumberOfTriesInTest();

        CheckBox materialLikeLearn = (CheckBox) findViewById(R.id.theSameMaterial);
        materialLikeLearn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                level.setMaterialForTest(b);
                if (b) {
                    findViewById(R.id.materialForTestInfo).setVisibility(View.INVISIBLE);
                    findViewById(R.id.materialForTest).setVisibility(View.INVISIBLE);
                    findViewById(R.id.button_save).setVisibility(View.VISIBLE);
                    level.setPhotosOrVideosIdListInTest(level.getPhotosOrVideosIdList());
                    checked = false;
                } else {
                    findViewById(R.id.materialForTestInfo).setVisibility(View.VISIBLE);
                    findViewById(R.id.materialForTest).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_save).setVisibility(View.VISIBLE);
                    //" przed przepisaniem zdjec z materialow do testow W TEŚCIE" + level.getPhotosOrVideosIdListInTest() + " w materiale " + level.getPhotosOrVideosIdList());
                    if (!duringInitiation) {
                        level.setPhotosOrVideosIdListInTest(level.getPhotosOrVideosIdList());
                        //" po przepisaniu zdjec z materialow do testow W TEŚCIE" + level.getPhotosOrVideosIdListInTest() + " w materiale " + level.getPhotosOrVideosIdList());
                    }
                    checked = true;
                }
            }
        });

        findViewById(R.id.materialForTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LevelConfigurationActivity.this, MaterialForTestActivity.class);
                intent.putExtra("level", level);
                startActivityForResult(intent, MATERIAL_FOR_TEST_DATA);
            }
        });
    }

    private void createTabConsolidation() {
        createGridPraise();
        //activateAddPraiseButton();
    }

    private void createTabLearningWays() {
        createSpinnerCommand();
        activateNumberPhotos();
        activateNumberTry();
        activateNumberTime();
    }

    private void createTabMaterial() {
        createTabs();
        createListOfSpinners();
        activeNumberEmotionPlusMinus();
        SelectAllPicturesWithEmotionId(0);
        SelectAllPicturesWithEmotionId(1);
    }

    //informacje o kroku
    private void updateInfo() {
        gatherInfoFromGUI();
        Level level = getLevel();

        TextView LearnInfo = (TextView) findViewById(R.id.LearnInfo);
        StringBuilder learnInfo = new StringBuilder();
        learnInfo.append(("\n" + getString(R.string.label1_material) + " " + level.getAmountOfEmotions()));
        learnInfo.append("\n" + getString(R.string.label2_material) + " " + getEmotionsNameInLocalLang());
        learnInfo.append("\n" + getString(R.string.label1_1_learning_ways) + " " + level.getPhotosOrVideosShowedForOneQuestion());
        learnInfo.append("\n" + getString(R.string.label1_2_learning_ways) + " " + level.getSublevelsPerEachEmotion());
        learnInfo.append("\n" + getString(R.string.label2_1_learning_ways) + " " + getKindOfCommand(level.getQuestionType().toString()));
        learnInfo.append("\n" + getString(R.string.label1_5_1) + " " + yesOrNoLocalLanguage(level.isOptionDifferentSexes()));
        learnInfo.append("\n" + getString(R.string.label2_2_learning_ways) + " " + yesOrNoLocalLanguage(level.isShouldQuestionBeReadAloud()));
        learnInfo.append("\n" + getString(R.string.label2_4_learning_ways) + " " + level.getTimeLimit() + " " + getString(R.string.after_seconds));
        learnInfo.append("\n" + getString(R.string.label2_6_learning_ways) + " " + getHintName());
        learnInfo.append("\n" + getString(R.string.label1_consolidation) + " " + getPraises() + "\n");

        LearnInfo.setText(learnInfo);
        TextView TestInfo = (TextView) findViewById(R.id.TestInfo);
        StringBuilder testInfo = new StringBuilder();
        testInfo.append("\n" + getString(R.string.test_time_for_answer) + " " + level.getTimeLimitInTest() + " " + getString(R.string.seconds));
        testInfo.append("\n" + getString(R.string.test_tries) + " " + level.getNumberOfTriesInTest());
        TestInfo.setText(testInfo);
    }

    public String getPraises() {
        String[] praises = getResources().getStringArray(R.array.praise_array);
        int praisePositionBinary = 1;
        String result = "";
        int praiseBinary = getLevel().getPraisesBinary();
        for (String praise : praises) {
            if ((praisePositionBinary & praiseBinary) == praisePositionBinary) {
                result = result + praise + ", ";
            }
            praisePositionBinary = praisePositionBinary * 2;
        }
        if (result.length() == 0) return "-";

        result = result.substring(0, result.length() - 2);
        return result;
    }


    private String getKindOfCommand(String polecenie) {
        switch (polecenie) {
            case "EMOTION_NAME":
                return getResources().getStringArray(R.array.spinner_command)[0];
            case "SHOW_WHERE_IS_EMOTION_NAME":
                return getResources().getStringArray(R.array.spinner_command)[2];
            case "SHOW_EMOTION_NAME":
                return getResources().getStringArray(R.array.spinner_command)[1];
        }
        return "Polecenie nie zostało wybrane";
    }

    private String yesOrNoLocalLanguage(boolean word) {
        if (word)
            return this.getString(R.string.yes);
        else
            return this.getString(R.string.no);
    }

    private StringBuilder getEmotionsNameInLocalLang() {
        StringBuilder emotionName = new StringBuilder();
        for (int i : level.getEmotions()) {
            emotionName.append(getEmotionNameInLocalLanguage(i) + ", ");
        }
        if (emotionName.length() == 0) emotionName.append("Brak wybranych emocji");
        else emotionName.deleteCharAt(emotionName.length() - 2);
        return emotionName;
    }

    private void createDefaultStepName() {
        int number = 2;
        EditText editText = (EditText) findViewById(R.id.step_name);
        String name = "";
        for (int emotion : getLevel().getEmotions()) {
            name += getEmotionNameInLocalLanguage(emotion) + " ";
        }
        //name = name.substring(0,name.length()-1);
        if (does_name_exist(name)) {
            name += how_many_levels(name);
        }

        editText.setText(name);
    }

    private boolean does_name_exist(String name) {
        Bundle b = getIntent().getExtras();
        SqliteManager sqlm = getInstance(this);
        String trimmed = name.trim();
        if (sqlm.giveLevel(name) != null && sqlm.giveLevel(name).getCount() > 0) {
            if (sqlm.giveLevel(name).getCount() == 1 && (b.getBoolean("edit"))) {
                return false;
            } else
                return true;
        }
        return false;
    }

    private int how_many_levels(String name) {
        SqliteManager sqlm = getInstance(this);
        Cursor cursor = sqlm.giveAllLevels();
        String level_name;
        String compared_name = "";

        int max = 2;
        int value = 1;

        while (cursor.moveToNext()) {
            level_name = cursor.getString(cursor.getColumnIndex("name"));
            if (level_name.charAt(level_name.length() - 1) >= '0' && level_name.charAt(level_name.length() - 1) <= '9') {
                compared_name = level_name.substring(0, level_name.length() - 1);
                if (name.contains(compared_name) && (name.length() == compared_name.length() + 1) || name.length() == compared_name.length()) {
                    String new_name = level_name;
                    value = Integer.parseInt(level_name.substring(level_name.length() - 1)) + 1;
                    if (level_name.charAt(level_name.length() - 2) >= '0' && level_name.charAt(level_name.length() - 2) <= '9') {
                        value = Integer.parseInt(level_name.substring(level_name.length() - 2)) * 10 + Integer.parseInt(level_name.substring(level_name.length() - 1)) + 1;

                    }
                }
                if (value > max) max = value;
            }
        }

        return max;
    }

    /**
     * private void activateAddPraiseButton() {
     * <p>
     * Button button = (Button) findViewById(R.id.buttonAddPraise);
     * button.setOnClickListener(new View.OnClickListener() {
     *
     * @Override public void onClick(View view) {
     * final String newItem = ((TextView) findViewById(R.id.newPraise)).getText().toString();
     * praiseList.add(new CheckboxGridBean(newItem, true));
     * updateGridPraise();
     * }
     * });
     * }
     */

    private void createGridPraise() {
        String[] praises = getResources().getStringArray(R.array.praise_array);

        for (String praise : praises) {
            praiseList.add(new CheckboxGridBean(praise, true));
        }
        updateGridPraise();
    }

    private void updateGridPraise() {
        GridView gridView = (GridView) findViewById(R.id.gridWordPraise);
        CheckboxGridAdapter adapter = new CheckboxGridAdapter(praiseList, getApplicationContext());
        gridView.setAdapter(adapter);
    }

    private void activateNumberTimeTest() {
        activePlusMinus((EditText) findViewById(R.id.number_time_test), (Button) findViewById(R.id.button_minus_time_test), (Button) findViewById(R.id.button_plus_time_test));
    }

    private void activateNumberOfTriesInTest() {
        activePlusMinusTries((TextView) findViewById(R.id.numberOfTriesLabel), (Button) findViewById(R.id.numberOfTriesMinus), (Button) findViewById(R.id.numberOfTriesPlus));
    }

    private void activateNumberTime() {
        activePlusMinus((EditText) findViewById(R.id.time), (Button) findViewById(R.id.button_minus_time), (Button) findViewById(R.id.button_plus_time));
    }

    private void activateNumberTry() {
        activePlusMinusTries((EditText) findViewById(R.id.number_try), (Button) findViewById(R.id.button_minus_try), (Button) findViewById(R.id.button_plus_try));
    }

    private void activateNumberPhotos() {
        activePlusMinusDisplayedPhotos((EditText) findViewById(R.id.number_photos), (Button) findViewById(R.id.button_minus_photos), (Button) findViewById(R.id.button_plus_photos));
    }

    private void activePlusMinusDisplayedPhotos(final TextView textLabel, final Button minusButton, final Button plusButton) {
        minusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int newValue = Integer.parseInt(textLabel.getText().toString()) - 1;
                if (newValue > 0) {
                    textLabel.setText(Integer.toString(newValue));
                }
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int newValue = Integer.parseInt(textLabel.getText().toString()) + 1;
                if (newValue <= getLevel().getEmotions().size()) {
                    textLabel.setText(Integer.toString(newValue));
                }
            }
        });
    }

    private void activePlusMinus(final TextView textLabel, final Button minusButton, final Button plusButton) {
        minusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int newValue = Integer.parseInt(textLabel.getText().toString()) - 1;
                if (newValue > 2) {
                    textLabel.setText(Integer.toString(newValue));
                }
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int newValue = Integer.parseInt(textLabel.getText().toString()) + 1;

                textLabel.setText(Integer.toString(newValue));

            }
        });
    }

    private void activePlusMinusTries(final TextView textLabel, final Button minusButton, final Button plusButton) {
        minusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int newValue = Integer.parseInt(textLabel.getText().toString()) - 1;
                if (newValue > 0) {

                    textLabel.setText(Integer.toString(newValue));
                }
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int newValue = Integer.parseInt(textLabel.getText().toString()) + 1;

                textLabel.setText(Integer.toString(newValue));

            }
        });
    }

    private void createSpinnerCommand() {
        final Spinner spinner = (Spinner) findViewById(R.id.spinner_command);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_command, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void createListOfSpinners() {
        final ListView selectedEmotionsList = (ListView) findViewById(R.id.selected_emotions_list);

        CustomAdapter adapter = new CustomAdapter();
        selectedEmotionsList.setAdapter(adapter);
        level.getPhotosOrVideosIdList().clear();
    }

    private GridCheckboxImageBean[] getEmotionPhotos(String choosenEmotion) {
        SqliteManager sqlm = getInstance(this);
        Cursor cursor = sqlm.givePhotosWithEmotion(choosenEmotion);
        int n = cursor.getCount();
        Cursor cursorVid = sqlm.giveVideosWithEmotion(choosenEmotion);
        n = n + cursorVid.getCount();
        GridCheckboxImageBean tabPhotos[] = new GridCheckboxImageBean[n];

        while (cursorVid.moveToNext()) {

            tabPhotos[--n] = (new GridCheckboxImageBean(cursorVid.getString(3), cursorVid.getInt(1), getContentResolver(), cursorVid.getInt(0)));
        }

        while (cursor.moveToNext()) {

            tabPhotos[--n] = (new GridCheckboxImageBean(cursor.getString(3), cursor.getInt(1), getContentResolver(), cursor.getInt(0)));
        }
//TODO odczyt zdjęć z bazy danych dla uczonej emocji
        return tabPhotos;
    }

    public String getEmotionNameinBaseLanguage(int emotionNumber) {
        Configuration config = new Configuration(getBaseContext().getResources().getConfiguration());
        config.setLocale(Locale.ENGLISH);
        return getBaseContext().createConfigurationContext(config).getResources().getStringArray(R.array.emotions_array)[emotionNumber];
    }

    private int getAllEmotionsQuantity() {
        return getResources().getStringArray(R.array.emotions_array).length;
    }


    private String getEmotionNameInLocalLanguage(int emotionNumber) {
        Configuration config = new Configuration(getBaseContext().getResources().getConfiguration());
        return getBaseContext().createConfigurationContext(config).getResources().getStringArray(R.array.emotions_array)[emotionNumber];
    }

    private void updateEmotionsGrid(int emotionNumber) {
        String emotion = getEmotionNameinBaseLanguage(emotionNumber);
        currentEmotionName = emotion;

        GridCheckboxImageBean[] tabPhotos = getEmotionPhotos(emotion);

        final GridView listView = (GridView) findViewById(R.id.grid_photos);
        CheckboxImageAdapter adapter = new CheckboxImageAdapter(this, R.layout.grid_element_checkbox_image, tabPhotos, level, false);
        listView.setAdapter(adapter);
        //createDefaultStepName();
    }

    private void updateSelectedEmotions() {
        createDefaultStepName();
        Toast.makeText(LevelConfigurationActivity.this, R.string.generated_name, Toast.LENGTH_LONG).show();
    }


    private void activeNumberEmotionPlusMinus() {
        final EditText nrEmotions = (EditText) findViewById(R.id.nr_emotions);
        final EditText nrPhotos = (EditText) findViewById(R.id.number_photos);
        final Button minusButton = (Button) findViewById(R.id.button_minus);
        minusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int lastElement = getLevel().getEmotions().size() - 1;
                if (lastElement > 0) {
                    String name = "";
                    for (int emotion : getLevel().getEmotions()) {
                        name += getEmotionNameInLocalLanguage(emotion) + " ";
                    }
                    deleteAllPicturesWithEmotionId(getLevel().getEmotions().get(lastElement));
                    getLevel().deleteEmotion(lastElement);


                    updateEmotionsGrid(getLevel().lastEmotionNumber());
                    if (name.contains(level.getName().substring(0, level.getName().length() - 1))) {
                        updateSelectedEmotions();
                    } else
                        Toast.makeText(LevelConfigurationActivity.this, "Sprawdź czy nazwa poziomu nie powinna zostać zmieniona", Toast.LENGTH_LONG).show();

                    nrEmotions.setText(Integer.toString(lastElement));
                    if (Integer.parseInt(nrPhotos.getText().toString()) > lastElement)
                        nrPhotos.setText(Integer.toString(lastElement));
                }
            }
        });

        final Button plusButton = (Button) findViewById(R.id.button_plus);
        plusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numberOfEmotions = Integer.parseInt(nrEmotions.getText().toString());
                String name = "";
                for (int emotion : getLevel().getEmotions()) {
                    name += getEmotionNameInLocalLanguage(emotion) + " ";
                }
                if (numberOfEmotions < getAllEmotionsQuantity()) {
                    int newUniqueEmotionId = getLevel().newEmotionId();
                    getLevel().addEmotion(newUniqueEmotionId);
                    SelectAllPicturesWithEmotionId(newUniqueEmotionId);
                    nrEmotions.setText(Integer.toString(getLevel().getEmotions().size()));
                    if (name.contains(level.getName().substring(0, level.getName().length() - 1)))
                        updateSelectedEmotions();
                } else
                    Toast.makeText(LevelConfigurationActivity.this, "Sprawdź czy nazwa poziomu nie powinna zostać zmieniona", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createTabs() {
        TabHost tab = (TabHost) findViewById(R.id.tabHost);
        tab.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tab) {
                if ("tab1_material".equals(tab)) {
                    findViewById(R.id.button_prev).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.button_prev).setVisibility(View.VISIBLE);
                }
                findViewById(R.id.button_save).setVisibility(View.VISIBLE);
                ImageButton button = (ImageButton) findViewById(R.id.button_next);
                ImageView savingButton = (ImageView) findViewById(R.id.button_save);
                if ("tab5_save".equals(tab)) {
                    updateInfo();
                    button.setImageResource(R.drawable.icon_save);
                    savingButton.setVisibility(View.INVISIBLE);
                } else {
                    button.setImageResource(R.drawable.icon_next);
                }
            }
        });
        tab.setup();

        final LevelValidator lv = new LevelValidator(level, LevelConfigurationActivity.this);

        final EditText levelName = (EditText) findViewById(R.id.step_name);

        final ImageView savingButton = (ImageView) findViewById(R.id.button_save);
        savingButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                flag[0] = 1;
                if (level.isMaterialForTest()) {
                    save(LevelConfigurationActivity.this);

                } else
                    save(LevelConfigurationActivity.this);
            }
        });
        String[] tabsFiles = {"tab1_material", "tab2_learning_ways", "tab3_consolidation", "tab4_test", "tab5_save"};
        TabHost.TabSpec spec;
        for (String tabFile : tabsFiles) {
            spec = tab.newTabSpec(tabFile);
            spec.setIndicator(getResourceString(tabFile));
            spec.setContent(getResourceId(tabFile));
            tab.addTab(spec);
        }
    }

    public void nextTab(View view) {

        TabHost tabs = (TabHost) findViewById(R.id.tabHost);

        if (tabs.getCurrentTab() == 4) {
            if (level.isMaterialForTest()) {
                save(LevelConfigurationActivity.this);
            } else if (level.getPhotosOrVideosIdListInTest().isEmpty()) {
                Toast.makeText(LevelConfigurationActivity.this, R.string.empty_test_photos, Toast.LENGTH_LONG).show();
            } else {
                save(LevelConfigurationActivity.this);
            }
        } else {
            tabs.setCurrentTab(tabs.getCurrentTab() + 1);
        }
    }

    void save(Object obj) {
        LevelValidator lv = new LevelValidator(getLevel(), obj);
        final RadioButton plciOpcja1 = (RadioButton) findViewById(R.id.plci_opcja1);
        final RadioButton plciOpcja2 = (RadioButton) findViewById(R.id.plci_opcja2);
        EditText levelName = (EditText) findViewById(R.id.step_name);
        CheckBox materialLikeLearn = (CheckBox) findViewById(R.id.theSameMaterial);
        gatherInfoFromGUI(); //save
        if (lv.validateLevel()) {
            if (!lv.numberOfPhotosSelected(level.getPhotosOrVideosShowedForOneQuestion(), plciOpcja2.isChecked())) {
                doclick();
            } else if (does_name_exist((String) levelName.getText().toString())) {
                Toast.makeText(LevelConfigurationActivity.this, R.string.duplicated_name, Toast.LENGTH_LONG).show();
            } else {
                saveLevelToDatabaseAndShowLevelSavedText();
                getLevel().setId(0);
            }
        }
    }

    void saveLevelToDatabaseAndShowLevelSavedText() {

        SqliteManager sqlm = getInstance(this);

        sqlm.saveLevelToDatabase(getLevel(), false);
        showTextInformation(R.string.save_message);
        finish();
    }

    private void showTextInformation(int intMessage) {
        showTextInformation(getResources().getString(intMessage));
    }

    private void showTextInformation(String textMessage) {

        final TextView msg = (TextView) findViewById(R.id.status_info);
        msg.setText(textMessage);
        msg.setVisibility(View.VISIBLE);
        msg.postDelayed(new Runnable() {
            public void run() {
                msg.setVisibility(View.INVISIBLE);
            }
        }, 2000);

    }

    void gatherInfoFromGUI() {

        // 1 panel

        // 2 panel

        TextView pvPerLevel = (TextView) findViewById(R.id.number_photos);
        getLevel().setPhotosOrVideosShowedForOneQuestion(Integer.parseInt(pvPerLevel.getText() + ""));

        TextView sublevels = (TextView) findViewById(R.id.number_try);
        getLevel().setSublevelsPerEachEmotion(Integer.parseInt(sublevels.getText() + ""));

        // question type

        Spinner spinner = (Spinner) findViewById(R.id.spinner_command);
        int spinnerPosition = spinner.getSelectedItemPosition();

        switch (spinnerPosition) {
            case 0:
                getLevel().setQuestionType(Level.Question.EMOTION_NAME);
                break;
            case 1:
                getLevel().setQuestionType(Level.Question.SHOW_EMOTION_NAME);
                break;
            case 2:
                getLevel().setQuestionType(Level.Question.SHOW_WHERE_IS_EMOTION_NAME);
                break;
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox checkbox1 = (CheckBox) findViewById(R.id.show);
                CheckBox checkbox2 = (CheckBox) findViewById(R.id.select);
                CheckBox checkbox3 = (CheckBox) findViewById(R.id.point);
                CheckBox checkbox4 = (CheckBox) findViewById(R.id.touch);
                CheckBox checkbox5 = (CheckBox) findViewById(R.id.find);
                if (i == 0 || i == 1) {
                    checkbox1.setVisibility(View.INVISIBLE);
                    checkbox2.setVisibility(View.INVISIBLE);
                    checkbox3.setVisibility(View.INVISIBLE);
                    checkbox4.setVisibility(View.INVISIBLE);
                    checkbox5.setVisibility(View.INVISIBLE);
                } else {
                    checkbox1.setVisibility(View.VISIBLE);
                    checkbox2.setVisibility(View.VISIBLE);
                    checkbox3.setVisibility(View.VISIBLE);
                    checkbox4.setVisibility(View.VISIBLE);
                    checkbox5.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //should photos below command be of one gender or of both genders

        RadioButton plciOpcja1 = (RadioButton) findViewById(R.id.plci_opcja1);
        if (plciOpcja1.isChecked())
            getLevel().setOptionDifferentSexes(false);
        RadioButton plciOpcja2 = (RadioButton) findViewById(R.id.plci_opcja2);
        if (plciOpcja2.isChecked())
            getLevel().setOptionDifferentSexes(true);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        getLevel().setShouldQuestionBeReadAloud(checkBox.isChecked());

        TextView secondsToHint = (TextView) findViewById(R.id.time);
        if (Integer.parseInt(secondsToHint.getText().toString()) > 1) {
            getLevel().setTimeLimit(Integer.parseInt(secondsToHint.getText() + ""));
        }

        //commandTypes

        getLevel().setCommandTypesAsNumber(0);
        checkBox = (CheckBox) findViewById(R.id.show);
        if (checkBox.isChecked()) {
            getLevel().addCommandTypeAsNumber(1);
        }

        checkBox = (CheckBox) findViewById(R.id.select);
        if (checkBox.isChecked()) {
            getLevel().addCommandTypeAsNumber(2);
        }

        checkBox = (CheckBox) findViewById(R.id.point);
        if (checkBox.isChecked()) {
            getLevel().addCommandTypeAsNumber(4);
        }

        checkBox = (CheckBox) findViewById(R.id.touch);
        if (checkBox.isChecked()) {
            getLevel().addCommandTypeAsNumber(8);
        }

        checkBox = (CheckBox) findViewById(R.id.find);
        if (checkBox.isChecked()) {
            getLevel().addCommandTypeAsNumber(16);
        }

        // hint types

        getLevel().setHintTypesAsNumber(0);
        checkBox = (CheckBox) findViewById(R.id.obramuj);
        if (checkBox.isChecked()) {
            getLevel().addHintTypeAsNumber(1);
        }

        checkBox = (CheckBox) findViewById(R.id.powieksz);
        if (checkBox.isChecked()) {
            getLevel().addHintTypeAsNumber(2);
        }

        checkBox = (CheckBox) findViewById(R.id.porusz);
        if (checkBox.isChecked()) {
            getLevel().addHintTypeAsNumber(4);
        }

        checkBox = (CheckBox) findViewById(R.id.wyszarz);
        if (checkBox.isChecked()) {
            getLevel().addHintTypeAsNumber(8);
        }

        // praises
        int praisePositionCheckbox = 1;
        getLevel().setPraiseBinaryTypesAsNumber();
        for (CheckboxGridBean objectItem : praiseList) {
            if (objectItem.isChecked()) {
                level.addPraiseBinaryTypesAsNumber(praisePositionCheckbox);
            }
            praisePositionCheckbox *= 2;
        }

        // 4 panel
        EditText timeLimit = (EditText) findViewById(R.id.number_time_test);
        getLevel().setTimeLimitInTest(Integer.parseInt(timeLimit.getText() + ""));

        CheckBox materialLikeLearn = (CheckBox) findViewById(R.id.theSameMaterial);
        getLevel().setMaterialForTest(materialLikeLearn.isChecked());

        getLevel().setNumberOfTriesInTest(Integer.parseInt(((TextView) findViewById(R.id.numberOfTriesLabel)).getText().toString()));

        // panel 5

        EditText levelName = (EditText) findViewById(R.id.step_name);

        getLevel().setName(levelName.getText() + "");


    }


    public StringBuilder getHintName() {
        StringBuilder str = new StringBuilder();
        CheckBox[] checkBoxList = {(CheckBox) findViewById(R.id.obramuj),
                (CheckBox) findViewById(R.id.wyszarz),
                (CheckBox) findViewById(R.id.powieksz),
                (CheckBox) findViewById(R.id.porusz)};

        for (CheckBox ch : checkBoxList) {
            if (ch.isChecked()) {
                str.append(ch.getText() + ";");
            }
        }
        if (str.length() == 0) str.append("-");
        else
            str.deleteCharAt(str.length() - 1);
        return str;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MATERIAL_FOR_TEST_DATA && resultCode == Activity.RESULT_OK) {
            //  level = (Level) data.getExtras().getSerializable("level");
            Level level1 = (Level) data.getExtras().getSerializable("level");
            level.setPhotosOrVideosIdListInTest(level1.getPhotosOrVideosIdListInTest());
            findViewById(R.id.button_save).setVisibility(View.VISIBLE);
        }
    }

    public void prevTab(View view) {
        TabHost tabs = (TabHost) findViewById(R.id.tabHost);
        tabs.setCurrentTab(tabs.getCurrentTab() - 1);
    }

    private int getResource(String variableName, String resourceName) {
        return getResources().getIdentifier(variableName, resourceName, getPackageName());
    }

    private int getResourceId(String resourceName) {
        return getResource(resourceName, "id");
    }

    private String getResourceString(String resourceName) {
        return getString(getResource(resourceName, "string"));
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return getLevel().getEmotions().size();
        }

        @Override
        public Object getItem(int n) {
            return getLevel().getEmotions().get(n);
        }

        @Override
        public long getItemId(int n) {
            return getLevel().getEmotions().get(n);
        }

        public void addCommandTypeAsNumber(int newType) {
            setCommandTypesAsNumber(getCommandTypesAsNumber() + newType);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.row_spinner, parent, false);

            final Spinner spinner = (Spinner) convertView.findViewById(R.id.spinner_emotions);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(LevelConfigurationActivity.this,
                    R.array.emotions_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(getLevel().getEmotions().get(position));

            ImageButton button = (ImageButton) convertView.findViewById(R.id.button_edit);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //wyświetlenie zdjęć dla wybranej emocji
                    updateEmotionsGrid(getLevel().getEmotions().get(position));
                }
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int emotionSelectedId, long l) {
                    if (getLevel().isEmotionNew(emotionSelectedId)) {
                        deleteAllPicturesWithEmotionId(getLevel().getEmotions().get(position));
                        String previous_name = "";
                        for (int emotion : getLevel().getEmotions()) {
                            previous_name += getEmotionNameInLocalLanguage(emotion) + " ";
                        }
                        updateEmotionsGrid(position);
                        getLevel().getEmotions().set(position, emotionSelectedId);
                        SelectAllPicturesWithEmotionId(emotionSelectedId);

                        String name = "";
                        for (int emotion : getLevel().getEmotions()) {
                            name += getEmotionNameInLocalLanguage(emotion) + " ";
                        }
                        if (previous_name.contains(level.getName().substring(0, level.getName().length() - 1)))
                            updateSelectedEmotions();
                        else
                            Toast.makeText(LevelConfigurationActivity.this, "Sprawdź czy nazwa poziomu nie powinna zostać zmieniona", Toast.LENGTH_LONG).show();

                        //position - który spiner, i - która emocja (licząc od 0)


                    } else {
                        spinner.setSelection(getLevel().getEmotions().get(position));
                    }

                    updateEmotionsGrid(getLevel().getEmotions().get(position));
                }


                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return convertView;
        }
    }

    public void SelectAllPicturesWithEmotionId(int newEmotionId) {
        SqliteManager sqlm = getInstance(this);
        Cursor cursor = sqlm.givePhotosWithEmotion(getEmotionNameinBaseLanguage(newEmotionId));
        while (cursor.moveToNext()) {
            getLevel().addPhoto(cursor.getInt(cursor.getColumnIndex("id")));

        }

    }

    public void deleteAllPicturesWithEmotionId(int newEmotionId) {
        SqliteManager sqlm = getInstance(this);
        Cursor cursor = sqlm.givePhotosWithEmotion(getEmotionNameinBaseLanguage(newEmotionId));
        while (cursor.moveToNext()) {
            getLevel().removePhoto(cursor.getInt(cursor.getColumnIndex("id")));
            getLevel().removePhotoForTest(cursor.getInt(cursor.getColumnIndex("id")));
        }

    }


    public void doclick() {
        DialogHandler appdialog = new DialogHandler();
        String information = "";
        LevelValidator lv = new LevelValidator(level, LevelConfigurationActivity.this);
        if (lv.femalePhotos(0) < level.getPhotosOrVideosShowedForOneQuestion()) {
            String part1 = getResources().getString(R.string.chosen_amount);
            String part2 = getResources().getString(R.string.less_photos_than_chosen_amount_female_learn);

            information = part1 + level.getPhotosOrVideosShowedForOneQuestion() + ". " + part2;
        } else if (lv.malePhotos(0) < level.getPhotosOrVideosShowedForOneQuestion()) {
            String part1 = getResources().getString(R.string.chosen_amount);
            String part2 = getResources().getString(R.string.less_photos_than_chosen_amount_male_learn);
            information = part1 + level.getPhotosOrVideosShowedForOneQuestion() + ". " + part2;
        }

        appdialog.Confirm(this, getResources().getString(R.string.insufficient_photos), information,
                getResources().getString(R.string.choose_more_photos), getResources().getString(R.string.save), aproc(), bproc());
    }


    public Runnable aproc() {
        return new Runnable() {
            public void run() {
                saveLevelToDatabaseAndShowLevelSavedText();
                getLevel().setId(0);
                finish();
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

}
