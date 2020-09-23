package pg.autyzm.graprzyjazneemocje.api.managers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import pg.autyzm.graprzyjazneemocje.R;
import pg.autyzm.graprzyjazneemocje.api.entities.Picture;
import pg.autyzm.graprzyjazneemocje.api.entities.PicturesContainer;

public class AnimationBuilder {

    private Animation animation;
    private Activity activity;
    private PicturesContainer picturesContainer;


    public AnimationBuilder(Activity activity){
        this.activity = activity;
    }

    private void createAnimation(List<Picture> pictureList){

        AnimationType[] animationTypes = getAllowedAnimationTypes();

        Random rand = new Random();
        AnimationType animationType = animationTypes[rand.nextInt(animationTypes.length)];

        Log.i("Animations", animationType.name());

        switch (animationType) {
            case STRAIGHT_FLY_UP_DOWN:
                createStraightFlyUpDownAnimation(pictureList);
                break;
            case GO_LEFT_TO_RIGHT:
                createGoLeftToRightAnimation(pictureList);
                break;
            case SPIRAL:
                createSpiralAnimation(pictureList);
                break;
        }
    }


    private AnimationType[] getAllowedAnimationTypes() {

        List<AnimationType> animationTypesList = new ArrayList<>();

        if(picturesContainer.getAnimationTypes().contains("LEFT_TO_RIGHT")){
            animationTypesList.add(AnimationType.GO_LEFT_TO_RIGHT);
        }
        if(picturesContainer.getAnimationTypes().contains("SPIRAL")){
            animationTypesList.add(AnimationType.SPIRAL);
        }
        if(picturesContainer.getAnimationTypes().contains("UP_DOWN")){
            animationTypesList.add(AnimationType.STRAIGHT_FLY_UP_DOWN);
        }

        AnimationType[] allowedAnimationTypes;

        if(animationTypesList.size() == 0){
            allowedAnimationTypes = AnimationType.values();
        }
        else{
            allowedAnimationTypes = new AnimationType[animationTypesList.size()];
            allowedAnimationTypes = animationTypesList.toArray(allowedAnimationTypes);
        }

        return allowedAnimationTypes;
    }

    private void loadPictureToAnimation(ImageView imageView, Picture picture){

        File imgFile = new  File(picture.getPath());

        if(imgFile.exists()){

            try {
                FileInputStream fileInputStream = new FileInputStream(imgFile);
                Bitmap myBitmap = BitmapFactory.decodeStream(fileInputStream);
                Bitmap resized = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth()*2, myBitmap.getHeight()*2, true);
                imageView.setImageBitmap(resized);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            Log.i("Files", "Picture loaded from path: " + picture.getPath());

        }
    }

    private void createGoLeftToRightAnimation(List<Picture> images) {

        ImageView animImage;

        activity.setContentView(R.layout.activity_anim_right_left);

        int imagesNr[] = {R.id.image1, R.id.image2, R.id.image3};
        for (int image : imagesNr) {

            animImage = (ImageView) activity.findViewById(image);

            if(! images.isEmpty()) {
                Picture drawnPicture = images.get(new Random().nextInt(images.size()));
                loadPictureToAnimation(animImage, drawnPicture);
            }

            animation = AnimationUtils.loadAnimation(activity, R.anim.right);
            animation.setStartOffset(new Random().nextInt(1500));
            animation.setDuration(new Random().nextInt(1000) + 1500);
            animImage.startAnimation(animation);
        }
    }

    private void createStraightFlyUpDownAnimation(List<Picture> images) {
        ImageView animImage;

        activity.setContentView(R.layout.activity_anim_straight);

        int imagesNr[] = {R.id.image1, R.id.image2, R.id.image3, R.id.image4};
        int n = 0;
        for (int image : imagesNr) {

            animImage = (ImageView) activity.findViewById(image);

            if(! images.isEmpty()) {
                Picture drawnPicture = images.get(new Random().nextInt(images.size()));
                loadPictureToAnimation(animImage, drawnPicture);
            }

            if((n++)%2==0) {
                animImage.setRotation(270);
                animation = AnimationUtils.loadAnimation(activity, R.anim.up);
            } else {
                animImage.setRotation(90);
                animation = AnimationUtils.loadAnimation(activity, R.anim.down);
            }
            animImage.startAnimation(animation);
        }
    }

    private void createSpiralAnimation(List<Picture> images) {
        ImageView imageView;

        activity.setContentView(R.layout.activity_anim_spiral);
        imageView = (ImageView) activity.findViewById(R.id.image1);

        if(! images.isEmpty()) {
            Picture drawnPicture = images.get(new Random().nextInt(images.size()));
            loadPictureToAnimation(imageView, drawnPicture);
        }

        animation = AnimationUtils.loadAnimation(activity, R.anim.spiral);
        imageView.startAnimation(animation);

        imageView = (ImageView) activity.findViewById(R.id.image2);

        if(! images.isEmpty()) {
            Picture drawnPicture = images.get(new Random().nextInt(images.size()));
            loadPictureToAnimation(imageView, drawnPicture);
        }

        animation = AnimationUtils.loadAnimation(activity, R.anim.spirall);
        imageView.startAnimation(animation);

    }

    private enum AnimationType {

        STRAIGHT_FLY_UP_DOWN, GO_LEFT_TO_RIGHT, SPIRAL

    }

    public Animation prepareAndReturnRandomAward() {


        List<PicturesContainer> picturesContainers = StorageAnimationsManager.getInstance().getAllAnimationsFromStorage();

        if(picturesContainers.isEmpty()){
            // use internal storage, if there's nothing in the external one
            picturesContainer = new PicturesContainer("internal_pictures");
            createAnimation(new ArrayList<Picture>());

        }
        else {

            // get random picturescontainer from enabled picturescontainers

            List<PicturesContainer> storageStateFromDatabase = new ArrayList<>();

            try {
                storageStateFromDatabase = new DatabaseHelper(activity).getPictureContainerDao().queryForAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Iterator<PicturesContainer> picturesContainerIterator = storageStateFromDatabase.iterator();
            while (picturesContainerIterator.hasNext()) {
                PicturesContainer picturesContainer = picturesContainerIterator.next();
                if(picturesContainer.getEnabled() == 0){
                    picturesContainerIterator.remove();
                }
            }

            List<Picture> picturesToBeUsed = new ArrayList<>();
            int pictureCategoriesAmount = storageStateFromDatabase.size();

            if(pictureCategoriesAmount > 0) {
                int pictureCategoriesIndexDrawn = new Random().nextInt(pictureCategoriesAmount);
                picturesContainer = storageStateFromDatabase.get(pictureCategoriesIndexDrawn);


                // select enabled pictures

                for (Picture picture : picturesContainer.getPicturesInCategory()) {
                    if (picture.getEnabled() == 1) {
                        picturesToBeUsed.add(picture);
                    }
                }
            }
            else{
                picturesContainer = new PicturesContainer();
            }

            createAnimation(picturesToBeUsed);
        }

        return animation;

    }

}
