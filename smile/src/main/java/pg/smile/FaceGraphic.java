/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pg.smile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;

import pg.smile.Camera.CameraSourcePreview;
import pg.smile.Camera.GraphicOverlay;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.GRAY,
            Color.LTGRAY,
            Color.DKGRAY,
    };
    private static int mCurrentColorIndex = 0;
    Context context;
    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;
    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;
    private CameraSourcePreview mPreview;
    private Animation shakeAniamtion = null;
    private ImageView crownImage;
    private int happinessNum;
    private long crownSec = Long.MAX_VALUE;
    private boolean displayHappiness;
    private boolean displayFrame;

    FaceGraphic(GraphicOverlay overlay, Context context, CameraSourcePreview preview, int happiness, boolean displayHappiness, boolean displayFrame) {
        super(overlay);
        this.context = context;
        this.mPreview = preview;
        this.happinessNum = happiness;
        this.displayHappiness = displayHappiness;
        this.displayFrame = displayFrame;
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        //canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);

        /*for (Landmark landmark : face.getLandmarks()) {
            int cx = (int) translateX(landmark.getPosition().x);
            int cy = (int) translateY(landmark.getPosition().y);
            canvas.drawCircle(cx, cy, 10, mFacePositionPaint);
        }*/

        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);

        float smileProb = face.getIsSmilingProbability();
        if (smileProb > 0)
            smileProb = smileProb * 100;

        if (displayHappiness)
            canvas.drawText(String.format("%.2f", smileProb), x + xOffset, y + yOffset, mIdPaint);

        if (crownSec <= ((System.currentTimeMillis() / 1000) - 2)) { //shakeAniamtion != null && shakeAniamtion.hasEnded()
            crownImage.setVisibility(View.INVISIBLE);
            //shakeAniamtion = null;
            crownSec = Long.MAX_VALUE;
        }

        if (crownSec == Long.MAX_VALUE && smileProb >= happinessNum) //shakeAniamtion == null && smileProb >= happinessNum
        {
            mPreview.setLevel(smileProb);
            mPreview.takePic();

            View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
            crownImage = rootView.findViewById(R.id.headImage);
            crownImage.requestLayout();
            crownImage.getLayoutParams().width = (int) (face.getWidth() * 1.3);

            crownImage.setX((x - xOffset + face.getWidth() / 6)); //x - face.getWidth() / 2)
            crownImage.setY(y - yOffset + face.getHeight() / 8); //y - face.getHeight()


            //mPreview.stop();
            crownImage.setVisibility(View.VISIBLE);
            crownSec = System.currentTimeMillis() / 1000;
            //shakeAniamtion = AnimationUtils.loadAnimation(context, R.anim.shake);
            //crownImage.startAnimation(shakeAniamtion);
        }


        //canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        //canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        if (displayFrame) {
            float left = x - xOffset;
            float top = y - yOffset;
            float right = x + xOffset;
            float bottom = y + yOffset;
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
        }
    }

}
