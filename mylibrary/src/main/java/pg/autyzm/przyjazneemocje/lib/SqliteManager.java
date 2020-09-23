package pg.autyzm.przyjazneemocje.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import pg.autyzm.przyjazneemocje.lib.entities.Level;

public class SqliteManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "przyjazneemocje";
    private static SqliteManager appContext;
    private static SqliteManager sInstance;
    private SQLiteDatabase db;

    private SqliteManager(final Context context) {
        super(new DatabaseContext(context), DATABASE_NAME, null, 18);
        db = getWritableDatabase();
    }

    public static SqliteManager getAppContext() {
        return appContext;
    }

    public static synchronized SqliteManager getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new SqliteManager(context.getApplicationContext());
        }
        return sInstance;

    }

    public void onCreate(SQLiteDatabase db) {

        this.db = db;


        createTablesInDatabase();
        addDefaultLanguages();
        addEmotionsToDatabase();
//addDefaultLevels();

    }

    private void addDefaultLanguages() {
        addLang(1, "pl", 1);
        addLang(2, "en", 0);
    }

    public void AddDefaultLevelsIfNeeded() {
        if (countAll("levels") > 0)
            return;
        Level level_easy_icons = new Level();
        level_easy_icons.setPhotosOrVideosIdList(new ArrayList<Integer>());
        level_easy_icons.setName("IKONY - 2 emocje::ICONS - 2 emotions");
        level_easy_icons.setLevelActive(true);
        level_easy_icons.setTimeLimit(10);
        level_easy_icons.setAmountOfAllowedTriesForEachEmotion(3);
        level_easy_icons.setSublevelsPerEachEmotion(3);
        level_easy_icons.setPhotosOrVideosShowedForOneQuestion(2);
        level_easy_icons.setForTests(true);
        level_easy_icons.setOptionDifferentSexes(false);
        level_easy_icons.setShouldQuestionBeReadAloud(true);
        //level_easy_icons.setHintTypes();
        level_easy_icons.setQuestionType(Level.Question.EMOTION_NAME);
        level_easy_icons.setIs_default(true);
        level_easy_icons.setPraisesBinary(level_easy_icons.allSelected(5));
        //level_easy_icons.setCommandTypesAsNumber(level_easy_icons.allSelected(5));
        level_easy_icons.setHintTypesAsNumber(11);
        //level_easy_icons.setPraises("dobrze;wspaniale;świetnie;ekstra;super;good;great;excellent;extra;super;");


        level_easy_icons.setEmotions(new ArrayList<Integer>() {
            {
                add(0);
                add(1);
            }
        });
        level_easy_icons.setPhotosOrVideosIdList(new ArrayList<Integer>() {
            {
                add(givePhotoIdFromName("happy_woman_r_13.jpg"));
                add(givePhotoIdFromName("happy_woman_r_11.jpg"));
                add(givePhotoIdFromName("happy_woman_r_12.jpg"));

                add(givePhotoIdFromName("sad_woman_r_10.jpg"));
                add(givePhotoIdFromName("sad_woman_r_11.jpg"));
                add(givePhotoIdFromName("sad_woman_r_12.jpg"));


            }
        });

        level_easy_icons.setLearnMode(true);
        level_easy_icons.setTestMode(false);
        level_easy_icons.setNumberOfTriesInTest(3);
        level_easy_icons.setMaterialForTest(true);
        level_easy_icons.setTimeLimitInTest(10);
        // level_easy_icons.setCommandTypesAsNumber(level_easy_icons.allSelected(5));

        saveLevelToDatabase(level_easy_icons, false);


        Level level_easy_photos = new Level();
        level_easy_photos.setPhotosOrVideosIdList(new ArrayList<Integer>());
        level_easy_photos.setName("ZDJĘCIA - 2 emocje::PHOTOS - 2 emotions");
        level_easy_photos.setLevelActive(false);
        level_easy_photos.setTimeLimit(10);
        //level_easy_photos.setPraises("dobrze;wspaniale;świetnie;ekstra;super;good;great;excellent;extra;super;");
        level_easy_photos.setAmountOfAllowedTriesForEachEmotion(3);
        level_easy_photos.setSublevelsPerEachEmotion(3);
        level_easy_photos.setPhotosOrVideosShowedForOneQuestion(2);
        level_easy_photos.setForTests(true);
        level_easy_photos.setOptionDifferentSexes(false);
        level_easy_photos.setShouldQuestionBeReadAloud(true);
        level_easy_photos.setQuestionType(Level.Question.EMOTION_NAME);    /// PÓXNIEJ jak wszystko będzie działało zmienić na EMOTION_NAME
        level_easy_photos.setPraisesBinary(level_easy_photos.allSelected(5));
        // level_easy_photos.setCommandTypesAsNumber(level_easy_photos.allSelected(5));
        level_easy_photos.setHintTypesAsNumber(11);

        level_easy_photos.setEmotions(new ArrayList<Integer>() {
            {
                add(0);
                add(1);
            }
        });
        level_easy_photos.setPhotosOrVideosIdList(new ArrayList<Integer>() {
            {
                add(givePhotoIdFromName("happy_woman_r_01.jpg"));
                add(givePhotoIdFromName("happy_woman_r_02.jpg"));
                add(givePhotoIdFromName("happy_woman_r_03.jpg"));
                add(givePhotoIdFromName("happy_woman_r_04.jpg"));
                add(givePhotoIdFromName("happy_woman_r_06.jpg"));
                add(givePhotoIdFromName("happy_woman_r_07.jpg"));
                add(givePhotoIdFromName("happy_woman_r_08.jpg"));
                add(givePhotoIdFromName("happy_woman_r_09.jpg"));
                add(givePhotoIdFromName("happy_woman_r_10.jpg"));

                add(givePhotoIdFromName("happy_man_r_01.jpg"));
                add(givePhotoIdFromName("happy_man_r_02.jpg"));

                add(givePhotoIdFromName("sad_woman_r_02.jpg"));
                add(givePhotoIdFromName("sad_woman_r_03.jpg"));
                add(givePhotoIdFromName("sad_woman_r_04.jpg"));
                add(givePhotoIdFromName("sad_man_r_01.jpg"));
                add(givePhotoIdFromName("sad_man_r_02.jpg"));

            }
        });

        level_easy_photos.setLearnMode(false);
        level_easy_photos.setTestMode(false);
        level_easy_photos.setNumberOfTriesInTest(3);
        level_easy_photos.setTimeLimitInTest(10);
        level_easy_photos.setMaterialForTest(true);
        level_easy_photos.setIs_default(true);
        //level_easy_photos.setCommandTypesAsNumber(level_easy_photos.allSelected(5));

        saveLevelToDatabase(level_easy_photos, false);

        Level level_medium = new Level();
        level_medium.setPhotosOrVideosIdList(new ArrayList<Integer>());
        level_medium.setName("ZDJĘCIA - 4 emocje::PHOTOS - 4 emotions");
        level_medium.setLevelActive(false);
        level_medium.setTimeLimit(10);
        //level_medium.setPraises("dobrze;wspaniale;świetnie;ekstra;super;good;great;excellent;extra;super;");

        // level_medium.setPraises(pg.autyzm.przyjazneemocje.lib.R.string.  array.praise_array);
        level_medium.setPraisesBinary(level_medium.allSelected(5));
        level_medium.setAmountOfAllowedTriesForEachEmotion(3);
        level_medium.setPhotosOrVideosShowedForOneQuestion(3);
        level_medium.setSublevelsPerEachEmotion(3);
        level_medium.setForTests(true);
        level_medium.setShouldQuestionBeReadAloud(true);
        level_medium.setOptionDifferentSexes(true);
        level_medium.setQuestionType(Level.Question.SHOW_EMOTION_NAME);
        level_medium.setAmountOfEmotions(Integer.toString(level_medium.getEmotions().size()));
        //level_medium.setCommandTypesAsNumber(level_medium.allSelected(5));
        level_medium.setHintTypesAsNumber(11);
        //level_medium.setPraises("dobrze;wspaniale;świetnie;ekstra;super;good;great;excellent;extra;super;");

        level_medium.setEmotions(new ArrayList<Integer>() {
            {
                add(0);
                add(1);
                add(4);
                add(3);
            }
        });
        level_medium.setPhotosOrVideosIdList(new ArrayList<Integer>() {
            //level_medium.setPhotosOrVideosNameList(new ArrayList<String>() {
            {
                add(givePhotoIdFromName("happy_woman_r_01.jpg"));
                add(givePhotoIdFromName("happy_woman_r_02.jpg"));
                add(givePhotoIdFromName("happy_woman_r_03.jpg"));
                add(givePhotoIdFromName("happy_woman_r_04.jpg"));
                add(givePhotoIdFromName("happy_woman_r_06.jpg"));
                add(givePhotoIdFromName("happy_woman_r_07.jpg"));
                add(givePhotoIdFromName("happy_woman_r_08.jpg"));
                add(givePhotoIdFromName("happy_woman_r_09.jpg"));
                add(givePhotoIdFromName("happy_woman_r_10.jpg"));

                add(givePhotoIdFromName("happy_man_r_01.jpg"));
                add(givePhotoIdFromName("happy_man_r_02.jpg"));

                //add(givePhotoIdFromName("sad_woman_r_01.jpg"));
                add(givePhotoIdFromName("sad_woman_r_02.jpg"));
                add(givePhotoIdFromName("sad_woman_r_03.jpg"));
                add(givePhotoIdFromName("sad_woman_r_04.jpg"));
                add(givePhotoIdFromName("sad_man_r_01.jpg"));
                add(givePhotoIdFromName("sad_man_r_02.jpg"));
                //add(givePhotoIdFromName("sad_woman_r_02.jpg"));

                add(givePhotoIdFromName("angry_man_r_01.jpg"));
                add(givePhotoIdFromName("angry_man_r_02.jpg"));
                add(givePhotoIdFromName("angry_man_r_03.jpg"));
                add(givePhotoIdFromName("angry_man_r_04.jpg"));
                add(givePhotoIdFromName("angry_woman_r_01.jpg"));
                //add(givePhotoIdFromName("angry_woman_r_03.jpg"));

                add(givePhotoIdFromName("scared_man_r_01.jpg"));
                add(givePhotoIdFromName("scared_man_r_02.jpg"));
                //add(givePhotoIdFromName("scared_woman_r_01.jpg"));
                add(givePhotoIdFromName("scared_woman_r_02.jpg"));
                add(givePhotoIdFromName("scared_woman_r_03.jpg"));
            }
        });

        level_medium.setLearnMode(false);
        level_medium.setTestMode(false);
        level_medium.setNumberOfTriesInTest(3);
        level_medium.setTimeLimitInTest(10);
        level_medium.setMaterialForTest(true);
        level_medium.setIs_default(true);
        saveLevelToDatabase(level_medium, false);


        Level level_difficult = new Level();
        level_difficult.setPhotosOrVideosIdList(new ArrayList<Integer>());
        level_difficult.setName("ZDJĘCIA - 6 emocji::PHOTOS - 6 emotions");
        level_difficult.setTimeLimit(10);
        //level_difficult.setPraises("dobrze");//addPraises
        // level_difficult.setPraises("good");
        level_difficult.setAmountOfAllowedTriesForEachEmotion(3);
        level_difficult.setSublevelsPerEachEmotion(3);
        level_difficult.setPhotosOrVideosShowedForOneQuestion(4);
        level_difficult.setForTests(true);
        level_difficult.setOptionDifferentSexes(true);
        level_difficult.setShouldQuestionBeReadAloud(true);
        level_difficult.setQuestionType(Level.Question.SHOW_WHERE_IS_EMOTION_NAME);
        level_difficult.setCommandTypesAsNumber(level_difficult.allSelected(5));
        level_difficult.setPraisesBinary(level_difficult.allSelected(5));
        level_difficult.setHintTypesAsNumber(11);
        level_difficult.setEmotions(new ArrayList<Integer>() {
            {
                add(0);
                add(1);
                add(2);

                add(3);
                add(4);
                add(5);
            }
        });
        level_difficult.setPhotosOrVideosIdList(new ArrayList<Integer>()

        {
            {
                add(givePhotoIdFromName("happy_woman_r_01.jpg"));
                add(givePhotoIdFromName("happy_woman_r_02.jpg"));
                add(givePhotoIdFromName("happy_woman_r_03.jpg"));
                add(givePhotoIdFromName("happy_woman_r_04.jpg"));
                add(givePhotoIdFromName("happy_woman_r_06.jpg"));
                add(givePhotoIdFromName("happy_woman_r_07.jpg"));
                add(givePhotoIdFromName("happy_woman_r_08.jpg"));
                add(givePhotoIdFromName("happy_woman_r_09.jpg"));
                add(givePhotoIdFromName("happy_woman_r_10.jpg"));

                add(givePhotoIdFromName("happy_man_r_01.jpg"));
                add(givePhotoIdFromName("happy_man_r_02.jpg"));

                //add(givePhotoIdFromName("sad_woman_r_01.jpg"));
                add(givePhotoIdFromName("sad_woman_r_02.jpg"));
                add(givePhotoIdFromName("sad_woman_r_03.jpg"));
                add(givePhotoIdFromName("sad_woman_r_04.jpg"));
                add(givePhotoIdFromName("sad_man_r_01.jpg"));
                add(givePhotoIdFromName("sad_man_r_02.jpg"));
                //add(givePhotoIdFromName("sad_woman_r_02.jpg"));

                add(givePhotoIdFromName("angry_man_r_01.jpg"));
                add(givePhotoIdFromName("angry_man_r_02.jpg"));
                add(givePhotoIdFromName("angry_man_r_03.jpg"));
                add(givePhotoIdFromName("angry_man_r_04.jpg"));
                add(givePhotoIdFromName("angry_woman_r_01.jpg"));
                add(givePhotoIdFromName("scared_man_r_01.jpg"));
                add(givePhotoIdFromName("scared_man_r_02.jpg"));
                add(givePhotoIdFromName("scared_woman_r_02.jpg"));
                add(givePhotoIdFromName("scared_woman_r_03.jpg"));

                add(givePhotoIdFromName("surprised_woman_r_03.jpg"));
                add(givePhotoIdFromName("surprised_woman_r_04.jpg"));
                add(givePhotoIdFromName("surprised_man_r_01.jpg"));
                add(givePhotoIdFromName("surprised_man_r_03.jpg"));

                add(givePhotoIdFromName("bored_man_r_02.jpg"));
                add(givePhotoIdFromName("bored_man_r_01.jpg"));
                add(givePhotoIdFromName("bored_woman_r_02.jpg"));
                add(givePhotoIdFromName("bored_woman_r_03.jpg"));
            }
        });

        level_difficult.setLearnMode(false);
        level_difficult.setTestMode(false);
        level_difficult.setNumberOfTriesInTest(3);
        level_difficult.setTimeLimitInTest(10);
        level_difficult.setMaterialForTest(true);
        level_difficult.setIs_default(true);
        saveLevelToDatabase(level_difficult, false);
    }


    public void onOpen(SQLiteDatabase db) {

        this.db = db;

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.db = db;

        deleteTablesFromDatabase();
        createTablesInDatabase();
        addEmotionsToDatabase();
    }

    public void addEmotion(String emotion) {
        ContentValues values = new ContentValues();
        values.put("emotion", emotion);
        db.insertOrThrow("emotions", null, values);
    }

    public void addPhoto(int path, String emotion, String fileName) {
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("emotion", emotion);
        values.put("name", fileName);
        db.insertOrThrow("photos", null, values);
    }

    public void addVideo(int path, String emotion, String fileName) {
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("emotion", emotion);
        values.put("name", fileName);
        db.insertOrThrow("videos", null, values);
    }

    public void addLang(int id, String lang, Integer selected) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("language", lang);
        values.put("selected", selected);
        db.insertOrThrow("language", null, values);
    }


    public void saveLevelToDatabase(Level level, Boolean update_only_data_level) {
        ContentValues values = new ContentValues();
        values.put("photos_or_videos", level.getPhotosOrVideosFlag());
        values.put("name", level.getName());
        values.put("photos_or_videos_per_level", level.getPhotosOrVideosShowedForOneQuestion());
        values.put("time_limit", level.getTimeLimit());
        values.put("correctness", level.getAmountOfAllowedTriesForEachEmotion());
        values.put("sublevels_per_each_emotion", level.getSublevelsPerEachEmotion());
        values.put("question_type", level.getQuestionType().toString());
        values.put("question_type", level.getQuestionType().toString());
        values.put("hint_types_as_number", level.getHintTypesAsNumber());
        values.put("command_types_as_number", level.getCommandTypesAsNumber());
        values.put("optionDifferentSexes", level.isOptionDifferentSexes());
        values.put("praisesBinary", level.getPraisesBinary());
        values.put("shouldQuestionBeReadAloud", level.isShouldQuestionBeReadAloud());
        values.put("is_learn_mode", level.isLearnMode());
        values.put("is_test_mode", level.isTestMode());
        values.put("material_for_test", level.isMaterialForTest());
        values.put("number_of_tries_in_test", level.getNumberOfTriesInTest());
        values.put("time_limit_in_test", level.getTimeLimitInTest());


        if (level.getId() != 0) {
            db.update("levels", values, "id=" + level.getId(), null);
            if (!update_only_data_level) {
                delete("levels_photos", "levelid", new String[]{String.valueOf(level.getId())});
                delete("levels_emotions", "levelid", new String[]{String.valueOf(level.getId())});
            }
        } else {
            long longId = db.insertOrThrow("levels", null, values);
            level.setId((int) longId);
        }
        if (!update_only_data_level) {
            updatePhotosAndEmotions(level.getId(), level.getPhotosOrVideosIdList(), level.getEmotions(), values, false);

            if (level.isMaterialForTest()) {
                level.setPhotosOrVideosIdListInTest(level.getPhotosOrVideosIdList());
                level.setEmotionsInTest(level.getEmotions());
            }
            updatePhotosAndEmotions(level.getId(), level.getPhotosOrVideosIdListInTest(), level.getEmotionsInTest(), values, true);
            deletePhotosFromDatabase(level.getPhotoToBeDeletedFromDatabaseId());
            deletePhotosFromDirectory(level.getPhotoNameToBeDeletedFromDirectory());
        }
    }

    public void deletePhotosFromDirectory(List<String> photoNames) {

        String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

        String newFileName = "";
        for (String filename : photoNames) {
            //filename = root + "/FriendlyEmotions/Photos/" + filename;
            File fileToDelete = new File(root + "/FriendlyEmotions/Photos/", filename);

            try {

                fileToDelete.delete();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void deletePhotosFromDatabase(List<Integer> photoIds) {
        for (int id : photoIds) {
            delete("levels_photos", "photoid", new String[]{String.valueOf(id)});
            delete("photos", "id", new String[]{String.valueOf(id)});
        }


    }

    public void updatePhotosAndEmotions(int levelId, List<Integer> photosOrVideosList, List<Integer> emotions, ContentValues values, boolean forTestMode) {
        for (Integer photoOrVideo : photosOrVideosList) {
            values = new ContentValues();
            values.put("levelid", levelId);
            values.put("material_for_test", forTestMode);
            values.put("photoid", photoOrVideo);
            db.insertOrThrow("levels_photos", null, values);
        }

        for (Integer emotion : emotions) {
            values = new ContentValues();
            values.put("levelid", levelId);
            values.put("material_for_test", forTestMode);
            values.put("emotionid", emotion + 1);
            db.insertOrThrow("levels_emotions", null, values);
        }
    }

    public void delete(String tableName, String columnName, String[] value) {
        db.delete(tableName, columnName + "=?", value);
    }

    public void delete(String tableName, String columnName, String value) {
        String[] args = {"" + value};
        db.delete(tableName, columnName + "=?", args);
    }

    public void cleanTable(String tableName) {
        db.execSQL("delete from " + tableName);
        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + tableName + "'");
    }

    public Cursor givePhotosWithEmotion(String emotion) {
        return givePhotosWithEmotionSource(emotion, Source.BOTH);
    }

    public Cursor givePhotosWithEmotionSource(String emotion, Source source) {

        String[] columns = {"id", "path", "emotion", "name", "sex"};
        String where;
        if (source == Source.BOTH) {
            where = "name like '" + emotion + "%'";
        } else if (source == Source.EXTERNAL) {
            where = "name like '" + emotion + "_e%'";
        } else {
            where = "name like '" + emotion + "_r%'";
        }
        Cursor cursor = db.query("photos", columns, where, null, null, null, "name");
        return cursor;
    }

    public Cursor giveVideosWithEmotion(String emotion) {
        String[] columns = {"id", "path", "emotion", "name"};
        Cursor cursor = db.query("videos", columns, "emotion like " + "'%" + emotion + "%'", null, null, null, null);
        return cursor;
    }

    public int countAll(String tableName) {
        //todo liczymy ilosc rekordow
        String[] columns = {"id"};
        Cursor cursor = db.query(tableName, columns, null, null, null, null, null);
        int maxNumber = cursor.getCount();

        // todo zamknięcie kursora
        return maxNumber;
    }

    public Cursor givePhotoWithPath(String path) {
        String[] columns = {"id", "path", "emotion", "name"};
        Cursor cursor = db.query("photos", columns, "path like " + "'%" + path + "%'", null, null, null, null);
        return cursor;
    }

    public List<Integer> givePhotosId(List<String> photosNames) {
        String[] columns = {"id", "path", "emotion", "name", "sex"};
        List<Integer> idzdjec = new ArrayList<>();
        for (int i = 0; i < photosNames.size(); i++) {
            Cursor cursor = db.query("photos", columns, "name like " + photosNames.get(i) + "%'", null, null, null, null);
            if (cursor != null) idzdjec.add(cursor.getInt(cursor.getColumnIndex("id")));
        }

        return idzdjec;

    }

    public Integer givePhotoIdFromName(String photoName) {
        String[] columns = {"id", "path", "emotion", "name", "sex"};


        Cursor cursor = db.query("photos", columns, "name = '" + photoName + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor != null && cursor.getCount() > 0)
            return (cursor.getInt(cursor.getColumnIndex("id")));
        return -1;
    }

    public Cursor givePhotoWithId(int id) {
        String[] columns = {"id", "path", "emotion", "name"};
        Cursor cursor = db.query("photos", columns, "id = " + id, null, null, null, null);
        // Cursor cursor = db.query("photos", columns,"id like " + "'%" + id + "%'", null, null, null, null);
        return cursor;
    }

    public Cursor givePhotosInLevel(int levelId) {
        String[] columns = {"id", "levelid", "material_for_test", "photoid"};
        Cursor cursor = db.query("levels_photos", columns, "levelid like " + "'%" + levelId + "%'", null, null, null, null);
        return cursor;
    }

    public Cursor giveAllVideos()
    {
        String[] columns = {"id", "emotion", "name"};
        Cursor cursor = db.query("videos", columns, null, null, null, null, null);
        return cursor;
    }


    public Cursor giveEmotionsInLevel(int levelId) {
        String[] columns = {"id", "levelid", "material_for_test", "emotionid"};
        Cursor cursor = db.query("levels_emotions", columns, "levelid like " + "'%" + levelId + "%'", null, null, null, null);
        return cursor;
    }


    public Cursor giveEmotionId(String name) {

        String[] columns = {"id", "emotion"};
        Cursor cursor = db.query("emotions", columns, "emotion like " + "'%" + name + "%'", null, null, null, null);
        return cursor;

    }


    public Cursor giveEmotionName(int id) {

        String[] columns = {"id", "emotion"};
        Cursor cursor = db.query("emotions", columns, "id = " + id, null, null, null, null);
        return cursor;

    }

    public Cursor giveAllEmotions() {
        Cursor cursor = db.rawQuery("select * from emotions", null);
        return cursor;
    }

    public Cursor giveAllLevels() {
        Cursor cursor = db.rawQuery("select * from levels", null);
        return cursor;
    }

    public Cursor giveLevel(int id) {
        Cursor cursor = db.rawQuery("select * from levels where id='" + id + "'", null);
        return cursor;
    }

    public Cursor giveLevel(String name) {
        Cursor cursor = db.rawQuery("select * from levels where name='" + name + "'", null);
        return cursor;
    }

    public String giveNameOfEmotionFromPhoto(String nameOfPhoto) {
        String[] columns = {"id", "path", "emotion", "name"};
        Cursor cursor = db.query("photos", columns, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(3);
            if (name.equals(nameOfPhoto))
                return cursor.getString(2);
        }
        return "Fail";
    }


    private void createTablesInDatabase() {
        db.execSQL("create table photos(" + "id integer primary key autoincrement," + "path int,"
                + "emotion text," + "name text," + "sex text);" + "");
        db.execSQL("create table emotions(" + "id integer primary key autoincrement," + "emotion text);" + "");
        db.execSQL("create table levels(" + "id integer primary key autoincrement, photos_or_videos text, photos_or_videos_per_level int, " +
                "time_limit int, is_learn_mode int, is_test_mode int, number_of_tries_in_test int, time_limit_in_test int, material_for_test int, name text, correctness int, sublevels_per_each_emotion int, question_type text, hint_types_as_number int, command_types_as_number int, praisesBinary int, optionDifferentSexes boolean, shouldQuestionBeReadAloud boolean,is_default boolean);" + "");
        db.execSQL("create table levels_photos(" + "id integer primary key autoincrement," + "levelid integer references levels(id)," + "material_for_test integer," + "photoid integer references photos(id));" + "");
        db.execSQL("create table levels_emotions(" + "id integer primary key autoincrement," + "levelid integer references levels(id)," + "material_for_test integer," + "emotionid integer references emotions(id));" + "");
        db.execSQL("create table videos(" + "id integer primary key autoincrement," + "path int," + "emotion text," + "name text);" + "");
        db.execSQL("create table language(" + "id integer primary key autoincrement," + "language text not null unique," + "selected integer default 0);" + "");
    }

    private void deleteTablesFromDatabase() {

        db.execSQL("drop table levels_emotions");
        db.execSQL("drop table levels_photos");
        db.execSQL("drop table levels");
        db.execSQL("drop table emotions");
        db.execSQL("drop table photos");
        db.execSQL("drop table videos");
        db.execSQL("drop table language");
        db.execSQL("drop table parametrs");
    }

    private void addEmotionsToDatabase() {
        addEmotion("happy_woman");
        addEmotion("sad_woman");
        addEmotion("surprised_woman");
        addEmotion("angry_woman");
        addEmotion("scared_woman");
        addEmotion("bored_woman");
        addEmotion("happy_man");
        addEmotion("sad_man");
        addEmotion("surprised_man");
        addEmotion("angry_man");
        addEmotion("scared_man");
        addEmotion("bored_man");
    }

    public int getPhotoIdByName(String name) {

        String[] columns = {"id", "path", "emotion", "name"};
        Cursor cursor = db.query("photos", columns, "name like " + "'%" + name + "%'", null, null, null, null);
        cursor.moveToNext();


        return cursor.getInt(0);
    }

    public String getCurrentLang() {
        String[] columns = {"id", "language", "selected"};
        String where = "selected == 1";
        Cursor cursor = db.query("language", columns, where, null, null, null, null, null);
        String result = null;
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(1);
        }
        return result;
    }


    public void updateCurrentLang(String lang) {
        ContentValues values = new ContentValues();
        values.put("selected", 1);
        String whereTrue = "language=?";
        db.update("language", values, whereTrue, new String[]{lang});
        values.put("selected", 0);
        String whereFalse = "language!=?";
        db.update("language", values, whereFalse, new String[]{lang});
    }

    public enum Source {
        BOTH,
        EXTERNAL,
        INTERNAL

    }


}
