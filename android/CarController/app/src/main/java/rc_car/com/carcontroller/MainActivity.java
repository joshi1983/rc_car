package rc_car.com.carcontroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int TAKE_PHOTO_CODE = 2;
    private EditText serverNameInput;
    private ImageView imageView;
    private SurfaceView surfaceView;
    private CameraUtils cameraUtils = new CameraUtils();
    private PicturePublisher picturePublisher;
    private Config config = Config.getSingleton();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView)findViewById(R.id.surface_view);
        serverNameInput = (EditText)findViewById(R.id.server_host);
        String host = config.getServerHost();
        serverNameInput.setText(host);
        picturePublisher = new PicturePublisher(config.getPicturePublishURL());
        config.addPublishURLChangeListener(picturePublisher);
        cameraUtils.setPreviewCallback(picturePublisher);
        Log.d("CameraDemo", "Set picturePublisher for preview callbacks");
    }

    @Override
    public void onResume() {
        super.onResume();
        surfaceView.setVisibility(View.INVISIBLE);
        surfaceView.setVisibility(View.VISIBLE);
    }

    public void toggleRecordingClicked(View view) {
        Log.d("CameraDemo", "Device has camera: " + cameraUtils.hasCamera(this));
        if (cameraUtils.isRecording()) {
            cameraUtils.stopCamera();
        }
        else {
            try {
                cameraUtils.startCamera(this, surfaceView.getHolder());
            } catch (IOException ioE) {
                Log.d("CameraDemo", "problem in startCamera.  message: "
                        + ioE.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
