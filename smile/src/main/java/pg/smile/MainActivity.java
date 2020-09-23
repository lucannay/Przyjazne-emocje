package pg.smile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.NumberPicker;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main_smile);

        final NumberPicker np = findViewById(R.id.happinessNumber);
        np.setMinValue(25);
        np.setMaxValue(95);
        np.setValue(85);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                picker.setValue((newVal < oldVal) ? oldVal - 10 : oldVal + 10);
            }

        });

        ImageButton startButton = findViewById(R.id.startButton);

        final CheckBox levelCheckbox = findViewById(R.id.levelCheckbox);
        final CheckBox frameCheckbox = findViewById(R.id.frameCheckbox);

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("happinessNum", np.getValue());
                intent.putExtra("displayHappiness", levelCheckbox.isChecked());
                intent.putExtra("displayFrame", frameCheckbox.isChecked());

                startActivity(intent);
            }
        });


    }
}

