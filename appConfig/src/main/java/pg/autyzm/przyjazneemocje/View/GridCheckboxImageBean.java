package pg.autyzm.przyjazneemocje.View;

import android.content.ContentResolver;

public class GridCheckboxImageBean {


    String photoName;
    ContentResolver cr;
    int id;

    public int getId() {
        return id;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }


    public GridCheckboxImageBean(String photoName, int icon, ContentResolver cr, int id) {
        this.photoName = photoName;
        this.cr = cr;
        this.id = id;
    }
}