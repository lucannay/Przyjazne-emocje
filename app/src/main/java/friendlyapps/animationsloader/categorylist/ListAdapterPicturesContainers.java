package friendlyapps.animationsloader.categorylist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import friendlyapps.animationsloader.MainActivity;
import friendlyapps.animationsloader.R;
import friendlyapps.animationsloader.api.entities.Picture;
import friendlyapps.animationsloader.api.entities.PicturesContainer;
import friendlyapps.animationsloader.api.managers.StorageAnimationsManager;
import friendlyapps.animationsloader.api.managers.DatabaseHelper;

public class ListAdapterPicturesContainers extends ArrayAdapter<PicturesContainer> {

    DatabaseHelper databaseHelper;
    List<PicturesContainer> items;
    Context context;

    public ListAdapterPicturesContainers(Context context, int resource, List<PicturesContainer> items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
        databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.categoryrow, null);
        }

        final PicturesContainer currentPicturesContainer = getItem(position);

        if (currentPicturesContainer != null) {
            TextView tt1 = v.findViewById(R.id.categoryName);
            final CheckBox tt2 = v.findViewById(R.id.isEnabled);
            final ImageButton deleteButton = v.findViewById(R.id.delete_btn);
            final ImageButton editButton = v.findViewById(R.id.edit_btn);

            if (tt1 != null) {
                tt1.setText(getCategoryName(currentPicturesContainer.getCategoryName()));
            }

            if (tt2 != null) {

                if (currentPicturesContainer.getEnabled() == 1) {
                    tt2.setChecked(true);
                } else {
                    tt2.setChecked(false);
                }

            }

            tt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (tt2.isChecked()) {
                        currentPicturesContainer.setEnabled(1);
                    } else {
                        currentPicturesContainer.setEnabled(0);
                    }

                    try {
                        databaseHelper.getPictureContainerDao().update(currentPicturesContainer);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    confirmAndDeleteCategory(currentPicturesContainer);

                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    loadPicturesToRightPanel(currentPicturesContainer);

                }
            });
        }

        return v;
    }

    private void hideRightPanel() {
        MainActivity mainActivity = (MainActivity) getContext();

        mainActivity.findViewById(R.id.newList).setVisibility(View.INVISIBLE);

        mainActivity.findViewById(R.id.checkBoxLEFT_TO_RIGHT).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.checkBoxSPIRAL).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.checkBoxUP_DOWN).setVisibility(View.INVISIBLE);

    }

    private void loadPicturesToRightPanel(PicturesContainer picturesContainer) {

        MainActivity mainActivity = (MainActivity) getContext();

        ListView yourListView = mainActivity.findViewById(R.id.newList);
        yourListView.setVisibility(View.VISIBLE);

        // get data from the table by the ListAdapter
        ListAdapterPictures customAdapter =
                new ListAdapterPictures(getContext(), R.layout.picturerow, new ArrayList<>(picturesContainer.getPicturesInCategory()), picturesContainer);

        yourListView.setAdapter(customAdapter);
        mainActivity.setCurrentPicturesContainer(picturesContainer);
        prepareAnimationTypesCheckBoxes(mainActivity);

    }

    void prepareAnimationTypesCheckBoxes(MainActivity mainActivity) {

        final CheckBox checkBoxLEFT_TO_RIGHT = mainActivity.findViewById(R.id.checkBoxLEFT_TO_RIGHT);
        final CheckBox checkBoxSPIRAL = mainActivity.findViewById(R.id.checkBoxSPIRAL);
        final CheckBox checkBoxUP_DOWN = mainActivity.findViewById(R.id.checkBoxUP_DOWN);

        checkBoxLEFT_TO_RIGHT.setVisibility(View.VISIBLE);
        checkBoxSPIRAL.setVisibility(View.VISIBLE);
        checkBoxUP_DOWN.setVisibility(View.VISIBLE);

        checkBoxLEFT_TO_RIGHT.setChecked(mainActivity.getCurrentPicturesContainer().getAnimationTypes().contains("LEFT_TO_RIGHT"));
        checkBoxSPIRAL.setChecked(mainActivity.getCurrentPicturesContainer().getAnimationTypes().contains("SPIRAL"));
        checkBoxUP_DOWN.setChecked(mainActivity.getCurrentPicturesContainer().getAnimationTypes().contains("UP_DOWN"));

    }

    public void confirmAndDeleteCategory(final PicturesContainer currentPicturesContainer) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder
                .setMessage(context.getString(R.string.confirm_question))
                .setPositiveButton(context.getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Yes-code

                        try {
                            StorageAnimationsManager.getInstance().deletePicturesContainerFromStorage(currentPicturesContainer);

                            for (Picture picture : currentPicturesContainer.getPicturesInCategory()) {
                                databaseHelper.getPictureDao().delete(picture);
                            }

                            databaseHelper.getPictureContainerDao().delete(currentPicturesContainer);
                            items.remove(currentPicturesContainer);
                            hideRightPanel();
                            notifyDataSetChanged();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }


                    }
                })
                .setNegativeButton(context.getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    String getCategoryName(String category) {

        String categoryNameInCorrectLanguage = "";

        switch (category) {
            case "Butterflies":
                categoryNameInCorrectLanguage = context.getString(R.string.category_name_butterflies);
                break;
            case "Balls":
                categoryNameInCorrectLanguage = context.getString(R.string.category_name_balls);
                break;
            case "Trains":
                categoryNameInCorrectLanguage = context.getString(R.string.category_name_trains);
                break;
            case "Cars":
                categoryNameInCorrectLanguage = context.getString(R.string.category_name_cars);
                break;
            case "Planes":
                categoryNameInCorrectLanguage = context.getString(R.string.category_name_planes);
                break;
            case "Ships":
                categoryNameInCorrectLanguage = context.getString(R.string.category_name_ships);
                break;
            case "Suns":
                categoryNameInCorrectLanguage = context.getString(R.string.category_name_suns);
                break;
            case "Custom":
                categoryNameInCorrectLanguage = context.getString(R.string.category_name_custom);
                break;
            default:
                categoryNameInCorrectLanguage = category;
        }

        return categoryNameInCorrectLanguage;

    }

}