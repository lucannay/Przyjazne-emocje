package friendlyapps.animationsloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import friendlyapps.animationsloader.api.entities.Picture;
import friendlyapps.animationsloader.api.entities.PicturesContainer;
import friendlyapps.animationsloader.api.managers.StorageAnimationsManager;
import friendlyapps.animationsloader.categorylist.ListAdapterPicturesContainers;
import friendlyapps.animationsloader.api.managers.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private MyAssetsManager myAssetsManager;
    private PicturesContainer currentPicturesContainer;

    private List<PicturesContainer> storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setCheckboxEventListeners();

        databaseHelper = new DatabaseHelper(this);
        myAssetsManager = new MyAssetsManager(getAssets());

        if (!myAssetsManager.wereAnimationsLoadedToStorageSomewhenInThePast()) {
            myAssetsManager.copyAnimationsFromAssetsToStorage();
        }

        checkExternalStorageAndDatabaseIntegrity();
        storage = getStorageStateFromDatabase(); // getting state from db to acquire ids of resources as well
        loadCategoriesToGUI();

    }

    private void setCheckboxEventListeners() {

        final CheckBox checkBoxLEFT_TO_RIGHT = findViewById(R.id.checkBoxLEFT_TO_RIGHT);
        final CheckBox checkBoxSPIRAL = findViewById(R.id.checkBoxSPIRAL);
        final CheckBox checkBoxUP_DOWN = findViewById(R.id.checkBoxUP_DOWN);

        checkBoxLEFT_TO_RIGHT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAnimationTypes();
            }
        });

        checkBoxSPIRAL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAnimationTypes();
            }
        });

        checkBoxUP_DOWN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAnimationTypes();
            }
        });
    }

    void updateAnimationTypes() {

        String animationTypes = "";

        final CheckBox checkBoxLEFT_TO_RIGHT = findViewById(R.id.checkBoxLEFT_TO_RIGHT);
        final CheckBox checkBoxSPIRAL = findViewById(R.id.checkBoxSPIRAL);
        final CheckBox checkBoxUP_DOWN = findViewById(R.id.checkBoxUP_DOWN);

        if (checkBoxLEFT_TO_RIGHT.isChecked()) {
            animationTypes += "LEFT_TO_RIGHT;";
        }
        if (checkBoxSPIRAL.isChecked()) {
            animationTypes += "SPIRAL;";
        }
        if (checkBoxUP_DOWN.isChecked()) {
            animationTypes += "UP_DOWN";
        }

        currentPicturesContainer.setAnimationTypes(animationTypes);
        try {
            databaseHelper.getPictureContainerDao().update(currentPicturesContainer);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void checkExternalStorageAndDatabaseIntegrity() {

        List<PicturesContainer> externalStorage = StorageAnimationsManager.getInstance().getAllAnimationsFromStorage();
        List<PicturesContainer> databaseStorageState = getStorageStateFromDatabase();

        for (PicturesContainer storagePicturesContainer : externalStorage) {

            PicturesContainer databasePicturesContainer = getDatabasePicturesContainer(databaseStorageState, storagePicturesContainer);

            // create record for container if it does not exist
            if (databasePicturesContainer == null) {
                try {
                    databaseHelper.getPictureContainerDao().create(storagePicturesContainer);
                    Log.i("Animation", storagePicturesContainer.getCategoryName() + " record added to database");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                // pictures which will be added to db below are connected with storagePictureContainer,
                // so it needs id
                storagePicturesContainer.setId(databasePicturesContainer.getId());
            }

            // create records for pictures in the container if they are not exist
            for (Picture picture : storagePicturesContainer.getPicturesInCategory()) {
                if (!isPictureInDatabase(databasePicturesContainer, picture)) {
                    try {
                        databaseHelper.getPictureDao().create(picture);
                        Log.i("Animation", picture.getName() + " record added to database");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    PicturesContainer getDatabasePicturesContainer(List<PicturesContainer> datatbaseStorageState, PicturesContainer picturesContainerFromStorage) {

        for (PicturesContainer picturesContainerFromDatabase : datatbaseStorageState) {
            if (picturesContainerFromDatabase.getCategoryName().equals(picturesContainerFromStorage.getCategoryName())) {
                return picturesContainerFromDatabase;
            }
        }
        return null;
    }

    boolean isPictureInDatabase(PicturesContainer picturesContainer, Picture picture) {

        if (picturesContainer == null)
            return false;

        for (Picture picture1 : picturesContainer.getPicturesInCategory()) {
            if (picture.getPath().equals(picture1.getPath())) {
                return true;
            }
        }
        return false;
    }


    private void loadCategoriesToGUI() {

        ListView yourListView = (ListView) findViewById(R.id.itemListView);

        // get data from the table by the ListAdapter
        ListAdapterPicturesContainers customAdapter = new ListAdapterPicturesContainers(this, R.layout.categoryrow, storage);

        yourListView.setAdapter(customAdapter);

    }

    public List<PicturesContainer> getStorageStateFromDatabase() {

        List<PicturesContainer> storage = new ArrayList<>();

        try {

            storage = databaseHelper.getPictureContainerDao().queryForAll();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return storage;

    }

    public void restoreDefaultStorage(View v) {
        myAssetsManager.copyAnimationsFromAssetsToStorage();
        checkExternalStorageAndDatabaseIntegrity();
        storage = getStorageStateFromDatabase(); // getting state from db to acquire ids of resources as well
        loadCategoriesToGUI();
    }


    public PicturesContainer getCurrentPicturesContainer() {
        return currentPicturesContainer;
    }

    public void setCurrentPicturesContainer(PicturesContainer currentPicturesContainer) {
        this.currentPicturesContainer = currentPicturesContainer;
    }
}
