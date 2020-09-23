package friendlyapps.animationsloader;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyAssetsManager {

    private final String storageAppMainDirectoryName = "happyApplicationsAnimations";
    private final String picturesDirectoryName = "pictures";
    private final String backgroundDirectoryName = "background";
    private final String animationMovementsDirectoryName = "animationMovements";

    private AssetManager assetManager;

    private File storageMainDirectory;


    public MyAssetsManager(AssetManager assetManager) {

        this.assetManager = assetManager;

    }

    public void copyAnimationsFromAssetsToStorage() {

        prepareAppDirectoryInExternalStorage();

        try {
            copyDirectoryFromAssetsToExternalStorage(picturesDirectoryName);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void copyDirectoryFromAssetsToExternalStorage(String directoryName) throws IOException {

        createDirectoryInExternalStorageIfNecessary(directoryName);

        String[] assetsIWant;
        assetsIWant = assetManager.list(directoryName);

        for (String fileName : assetsIWant) {

            //if fileName cointains a dot it means it is a file, not directory

            if (fileName.contains(".")) {
                copyFile(directoryName + java.io.File.separator + fileName);
            } else {
                copyDirectoryFromAssetsToExternalStorage(directoryName +
                        java.io.File.separator + fileName);
            }
        }

    }

    private void createDirectoryInExternalStorageIfNecessary(String directoryName) {

        File newDirectory = new File(storageMainDirectory, directoryName);

        if (!newDirectory.exists()) {
            newDirectory.mkdir();
            Log.i("Files", directoryName + " directory was created");
        }
    }


    private void prepareAppDirectoryInExternalStorage() {

        storageMainDirectory = new File(Environment.getExternalStorageDirectory(), storageAppMainDirectoryName);

        if (!storageMainDirectory.exists()) {
            storageMainDirectory.mkdir();
            Log.i("Files", storageAppMainDirectoryName + " directory was created");
        }
    }

    boolean wereAnimationsLoadedToStorageSomewhenInThePast() {

        storageMainDirectory = new File(Environment.getExternalStorageDirectory(), storageAppMainDirectoryName);

        if (storageMainDirectory.exists()) {
            return true;
        } else {
            return false;
        }

    }

    private void copyFile(String destinationPath) throws IOException {

        AssetFileDescriptor assetFileDescriptor;
        try {
            assetFileDescriptor = assetManager.openFd(destinationPath);

            InputStream in = assetFileDescriptor.createInputStream();
            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() +
                    java.io.File.separator + storageAppMainDirectoryName + File.separator + destinationPath);
            byte[] buff = new byte[1024];
            int read = 0;

            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }

        } catch (IOException e) {
            Log.e("Files", destinationPath + " failed " + e.getMessage());
        }
    }


}
