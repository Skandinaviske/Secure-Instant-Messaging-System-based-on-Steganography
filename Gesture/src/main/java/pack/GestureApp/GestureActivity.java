package pack.GestureApp;

import android.app.Activity;
import android.gesture.*;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class GestureActivity extends Activity {
    private GestureLibrary gLib;
    private static final String TAG = "GestureActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        openOptionsMenu();
        gLib = GestureLibraries.fromFile(getExternalFilesDir(null) + "/" + "gesture.txt");
        gLib.load();

        GestureOverlayView gestures = findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(handleGestureListener);
        gestures.setGestureStrokeAngleThreshold(90.0f);
    }

    /**
     * gesture listener
     */
    private OnGesturePerformedListener handleGestureListener = new OnGesturePerformedListener() {
        @Override
        public void onGesturePerformed(GestureOverlayView gestureView,
                                       Gesture gesture) {

            ArrayList<Prediction> predictions = gLib.recognize(gesture);
            Log.d(TAG, "recog thayu");

            // one prediction needed
            if (predictions.size() > 0) {
                Prediction prediction = predictions.get(0);
                // checking prediction
                // Bugfix: https://stackoverflow.com/questions/6380908/android-gestures-single-stroke-multi-stroke-produce-inconsistent-results
                if (prediction.score > 1.0 && gLib.getGestures(prediction.name).get(0).getStrokesCount() == gesture.getStrokesCount()) {
                    // and action
                    Toast.makeText(GestureActivity.this, prediction.name + ", S:" + prediction.score,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(GestureActivity.this, "找不到匹配项！",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(GestureActivity.this, "无可用匹配项！",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
}