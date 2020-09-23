package pg.autyzm.przyjazneemocje;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import pg.autyzm.przyjazneemocje.camera.MainCameraActivity;

public class AddMaterial extends AppCompatActivity {
    private final int REQ_CODE_CAMERA = 1000;
    public String emocja;
    private int sumOfAnother = 0;
    private TextView takePhoto;


    AdapterView.OnItemSelectedListener emotionSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            // TODO Auto-generated method stub

            Spinner spinner_plec = (Spinner) findViewById(R.id.spinner_sex);
            Spinner spinner_emocje = (Spinner) findViewById(R.id.spinner_emotions);


            String plec = String.valueOf(spinner_plec.getSelectedItem());


            if (plec.equals("kobiety") || plec.equals("a woman") || plec.equals("ikony") || plec.equals("an icon")) {


                ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(AddMaterial.this, R.array.emotions_array_woman,
                        R.layout.spinner_camera_item);
                dataAdapter.setDropDownViewResource(R.layout.spinner_camera_item);
                dataAdapter.notifyDataSetChanged();
                spinner_emocje.setAdapter(dataAdapter);
            }
            if (plec.equals("mężczyzny") || plec.equals("a man")) {
                ArrayAdapter<CharSequence> dataAdapter2 = ArrayAdapter.createFromResource(AddMaterial.this, R.array.emotions_array_man,
                        R.layout.spinner_camera_item);
                dataAdapter2.setDropDownViewResource(R.layout.spinner_camera_item);
                dataAdapter2.notifyDataSetChanged();

                spinner_emocje.setAdapter(dataAdapter2);

            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_material);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        Spinner spinner_plec = (Spinner) findViewById(R.id.spinner_sex);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(AddMaterial.this, R.array.sex,
                R.layout.spinner_camera_item);
        genderAdapter.setDropDownViewResource(R.layout.spinner_camera_item);
        genderAdapter.notifyDataSetChanged();
        spinner_plec.setAdapter(genderAdapter);

        spinner_plec.setOnItemSelectedListener(emotionSelectedListener);

        takePhoto = findViewById(R.id.take_photo);

        ImageButton buttonCamera = findViewById(R.id.button_take_photo);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });


    }

    private void takePhoto() {
        Spinner spinner_emocje = findViewById(R.id.spinner_emotions);
        Spinner spinner_sex = findViewById(R.id.spinner_sex);
        Bundle bundle2 = new Bundle();
        bundle2.putString("SpinnerValue_Emotion", spinner_emocje.getSelectedItem().toString());
        bundle2.putString("SpinnerValue_Sex", spinner_sex.getSelectedItem().toString());
        bundle2.putInt("sumOfAnother", sumOfAnother);
        Intent in = new Intent(AddMaterial.this, MainCameraActivity.class);
        in.putExtras(bundle2);

        emocja = spinner_emocje.getSelectedItem().toString();
        startActivity(in);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent backToMenu = new Intent(AddMaterial.this, ActivityChoice.class);
        startActivity(backToMenu);

    }
}
