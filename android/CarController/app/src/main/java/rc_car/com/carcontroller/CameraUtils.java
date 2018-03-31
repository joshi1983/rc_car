package rc_car.com.carcontroller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.IOException;

/**
 * CameraUtils wraps up functionality for controling and getting images from
 * the Android camera device.
 */
public class CameraUtils implements SurfaceHolder.Callback {
    private Camera camera;
    private boolean cameraFront;
    private Camera.PreviewCallback previewCallback;
    private Context context;

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // may want to restart camera
        stopCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // may want to restart camera
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
        if (camera != null && previewCallback != null)
            camera.setPreviewCallback(previewCallback);
    }

    public synchronized void startCamera(Context context, SurfaceHolder previewer) throws IOException {
        if (context == null)
            throw new NullPointerException("startCamera requires context to not be null");

        this.context = context;
        if (camera != null)
            stopCamera();
        if (hasCamera(context)) {
            camera = Camera.open();
            camera.setPreviewDisplay(previewer);
            if (this.previewCallback != null)
                camera.setPreviewCallback(this.previewCallback);

            camera.startPreview();
            previewer.addCallback(this);
        }
        else {
            Log.d("CameraDemo", "Device does not have a camera");
        }
    }

    public synchronized void stopCamera() {
        camera.stopPreview();
        camera.setPreviewCallback(null);
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
