package pg.autyzm.przyjazneemocje.View;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.io.File;
import java.util.List;


import pg.autyzm.przyjazneemocje.R;
import pg.autyzm.przyjazneemocje.lib.entities.Level;


public class CheckboxImageAdapter extends ArrayAdapter<GridCheckboxImageBean> {


    int layoutResourceId;
    GridCheckboxImageBean data[] = null;

    private List<GridCheckboxImageBean> rowBeanList;
    private Context context;
    private Level level;
    private boolean isForTest;

    public CheckboxImageAdapter(Context context, int layoutResourceId, GridCheckboxImageBean[] data, Level level, boolean isForTest) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.level = level;
        this.isForTest = isForTest;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;
        final RowBeanHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RowBeanHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.checkBox = (CheckBox) row.findViewById(R.id.checkBoxImagesToChoose);
            holder.delete_photo_button = (Button) row.findViewById(R.id.delete_photo);

            row.setTag(holder);
        } else {
            holder = (RowBeanHolder) row.getTag();
        }

        final GridCheckboxImageBean photoWithCheckBox = data[position];

        try {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

            File fileOut;
            Bitmap captureBmp;
            if (photoWithCheckBox.photoName.contains(".mp4")) {
                fileOut = new File(root + "FriendlyEmotions/Videos" + File.separator + photoWithCheckBox.photoName);
                captureBmp = ThumbnailUtils.createVideoThumbnail(fileOut.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
            } else {
                fileOut = new File(root + "FriendlyEmotions/Photos" + File.separator + photoWithCheckBox.photoName);
                captureBmp = MediaStore.Images.Media.getBitmap(photoWithCheckBox.cr, Uri.fromFile(fileOut));
            }

            holder.imgIcon.setImageBitmap(captureBmp);
        } catch (Exception e) {
            System.out.println(e);
        }

        final CheckBox checkBox = holder.checkBox;
        if (isForTest) {
            checkBox.setChecked(level.getPhotosOrVideosIdListInTest().contains(photoWithCheckBox.getId()));
        } else {
            checkBox.setChecked(level.getPhotosOrVideosIdList().contains(photoWithCheckBox.getId()));
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            Integer photoId = photoWithCheckBox.getId();

            @Override
            public void onClick(View arg0) {
                if (checkBox.isChecked()) {
                    if (photoWithCheckBox.photoName.contains(".mp4")) {
                        level.setPhotosOrVideosFlag("videos");
                    } else {
                        if (isForTest) {
                            level.addPhotoForTest(photoId);
                        } else {
                            level.addPhoto(photoId);
                        }
                    }
                } else {
                    if (isForTest) {
                        level.removePhotoForTest(photoId);
                    } else {
                        level.removePhoto(photoId);
                    }
                }
            }
        });

        final Button delete_photo_button = holder.delete_photo_button;

        if (photoWithCheckBox.photoName.contains("_r_")) {
            holder.delete_photo_button.setVisibility(View.INVISIBLE);
        } else {
            holder.delete_photo_button.setVisibility(View.VISIBLE);
        }

        delete_photo_button.setOnClickListener(new View.OnClickListener() {
            Integer photoId = photoWithCheckBox.getId();

            @Override
            public void onClick(View arg0) {

                holder.imgIcon.setVisibility(View.INVISIBLE);
                holder.checkBox.setVisibility(View.INVISIBLE);
                holder.delete_photo_button.setVisibility(View.INVISIBLE);

                level.removePhoto(photoId);
                level.addPhotoToBePermanentlyDeleted(photoId, photoWithCheckBox.photoName);
            }
        });


        return row;
    }

    static class RowBeanHolder {
        public ImageView imgIcon;
        public CheckBox checkBox;
        public Button delete_photo_button;
    }

}