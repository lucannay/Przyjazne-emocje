package pg.autyzm.graprzyjazneemocje;

import android.app.Activity;

//not used - for future versions
public class VideoWelcomeActivity extends Activity {

   /* ]@Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_welcome);

        TextView txt = (TextView) findViewById(R.id.question);
        final String commandText = getResources().getString(R.string.label_video_question);
        txt.setText(commandText);
        final Speaker speaker = Speaker.getInstance(VideoWelcomeActivity.this);

        ImageButton speakerButton = (ImageButton) findViewById(R.id.speakerButton);
        speakerButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                speaker.speak(commandText);
            }
        });
    }

    public void goToGame(View view){
        finish();
    }*/
}
