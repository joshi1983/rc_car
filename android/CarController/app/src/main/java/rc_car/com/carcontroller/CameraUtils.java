package rc_car.com.carcontroller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.IOException;

/**
 * CameraUtils wraps up functionality for controling and getting images from the Android camera device.
 */
public class CameraUtils {
    private Camera camera;
    private boolean cameraFront;
    private Camera.PreviewCallback previewCallback;

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
        if (camera != null && previewCallback != null)
            camera.setPreviewCallback(previewCallback);
    }

    public void startCamera(Context context, SurfaceHolder previewer) throws IOException {
        if (camera != null)
            stopCamera();
        if (hasCamera(context)) {
            camera = Camera.open();
            camera.setPreviewDisplay(previewer);
            if (this.previewCallback != null)
                camera.setPreviewCallback(this.previewCallback);
            camera.startPreview();
        }
        else {
            Log.d("CameraDemo", "Device does not have a camera");
        }
    }

    public void stopCamera() {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public boolean isRecording() {
        return camera != null;
    }

    public boolean hasCamera(Context context) {
        //check if the device has camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void releaseCamera() {
        // stop and release camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

}
