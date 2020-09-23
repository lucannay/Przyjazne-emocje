package friendlyapps.animationsloader.categorylist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

import friendlyapps.animationsloader.R;
import friendlyapps.animationsloader.api.entities.Picture;
import friendlyapps.animationsloader.api.entities.PicturesContainer;
import friendlyapps.animationsloader.api.managers.StorageAnimationsManager;
import friendlyapps.animationsloader.api.managers.DatabaseHelper;

public class ListAdapterPictures extends ArrayAdapter<Picture> {

    DatabaseHelper databaseHelper;
    List<Picture> items;
    PicturesContainer picturesContainer;
    Context context;

    public ListAdapterPictures(Context context, int resource, List<Picture> items, PicturesContainer picturesContainer) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
        databaseHelper = new DatabaseHelper(context);
        this.picturesContainer = picturesContainer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.picturerow, null);
        }

        final Picture picture = getItem(position);

        if (picture != null) {

            final CheckBox tt2 = v.findViewById(R.id.isEnabled);
            final ImageButton deleteButton = v.findViewById(R.id.delete_btn);
            final ImageView imageView = v.findViewById(R.id.imageView);

            if (tt2 != null) {

                if (picture.getEnabled() == 1) {
                    tt2.setChecked(true);
                } else {
                    tt2.setChecked(false);
                }

            }

            tt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (tt2.isChecked()) {
                        picture.setEnabled(1);
                    } else {
                        picture.setEnabled(0);
                    }

                    try {
                        databaseHelper.getPictureDao().update(picture);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    confirmAndDeletePicture(picture);

                }
            });

            loadPictureToImageView(imageView, picture);


        }

        return v;
    }

    private void loadPictureToImageView(ImageView imageView, Picture picture) {

        File imgFile = new File(picture.getPath());

        if (imgFile.exists()) {

            try {
                FileInputStream fileInputStream = new FileInputStream(imgFile);
                Bitmap myBitmap = BitmapFactory.decodeStream(fileInputStream);
                Bitmap resized = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth() * 2, myBitmap.getHeight() * 2, true);
                imageView.setImageBitmap(resized);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            Log.i("Files", "Picture loaded from path: " + picture.getPath());

        }
    }

    public void confirmAndDeletePicture(final Picture picture) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder
                .setMessage(context.getString(R.string.confirm_question))
                .setPositiveButton(context.getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Yes-code

                        try {
                            StorageAnimationsManager.getInstance().deletePictureFromStorage(picture);
                            databaseHelper.getPictureDao().delete(picture);
                            items.remove(picture);
                            picturesContainer.getPicturesInCategory().remove(picture);
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


}