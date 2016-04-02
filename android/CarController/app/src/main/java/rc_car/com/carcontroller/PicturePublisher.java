package rc_car.com.carcontroller;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Publishes images to a specific server
 */
public class PicturePublisher implements Camera.PreviewCallback, PublishURLChangeListener {
    private String publishURL;
    private URL url;
    private PictureUploaderTask uploaderTask = new PictureUploaderTask();
    private int publishBacklogCount = 0;

    public PicturePublisher(String publishURL) {
        this.setPublishURL(publishURL);
    }

    public void publishURLChanged(String newPublishURL) {
        setPublishURL(newPublishURL);
    }

    public void setPublishURL(String publishURL) {
        this.publishURL = publishURL;
        try {
            this.url = new URL(publishURL);
        }
        catch (Exception e) {
            Log.d("CameraPublisher", "Problem setting publish URL: " + e.getMessage());
            this.url = null;
        }
    }

    /**
     * Checks if the URL may be working.  If the URL uses an empty host name, obviously it won't work.
     * */
    private boolean isPublishURLWorking() {
        if (publishURL == null || url == null)
            return false;

        int index = publishURL.indexOf("://");
        if (index < 0)
            return false;

        String hostName = publishURL.substring(index + 3);
        index = hostName.indexOf('/');
        if (index > 0)
            hostName = hostName.substring(0, index);

        // too short to be valid
        if (hostName.length() < 2)
            return false;

        return true;
    }

    /**
     * Sends jpeg image data to server using HTTP.
     *
     * @param imageData should be in jpeg format
     */
    private void sendImageDataToServer(byte[] imageData) throws java.io.IOException {
        // send the data as multipart form data.
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "-----WebKitFormBoundaryuxA2kznkujLtKT4i";

        // Open a HTTP connection to the URL
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true); // Allow Inputs
        conn.setDoOutput(true); // Allow Outputs
        conn.setUseCaches(false); // Don't use a Cached Copy
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Cache-Control", "max-age=0");
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        conn.setRequestProperty("frame", "temp.jpg");

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"frame\"; filename=\""
                + "temp.jpg\"" + lineEnd);
        dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
        dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
        dos.writeBytes(lineEnd);

        dos.write(imageData);

        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        dos.flush();
        dos.close();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            Log.d("CameraDemo", "picture upload got HTTP Response code: " + responseCode);
            InputStream is = conn.getInputStream();

            int bytesRead = -1;
            byte[] buffer = new byte[1024];
            String responseText = "";
            Log.d("CameraDemo", "about to read from input stream of HTTP response");
            while ((bytesRead = is.read(buffer)) >= 0) {
                // process the buffer, "bytesRead" have been read, no more, no less
                responseText += new String(buffer, 0, bytesRead);
                Log.d("CameraDemo", "read " + bytesRead + " bytes.");
            }
            Log.d("CameraDemo", "picture upload response: " + responseText);
            is.close();
        }
        dos.close();
    }

    private class PictureUploaderTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... jpegData) {
            try {
                if (publishBacklogCount < 2) {

                    Log.d("CameraPublisher", "PictureUploaderTask, doInBackground called.  publish URL: " + publishURL);
                    try {
                        sendImageDataToServer(jpegData[0]);
                    } catch (IOException e) {
                        Log.d("CameraPublisher", "Problem sending jpeg image: " + e.getMessage());
                    }
                }
            }
            finally {
                publishBacklogCount--;
            }
            return null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null) {
            Log.d("CameraPublisher", "data is null.");
            return;
        }

        if (isPublishURLWorking()) {
            //Log.d("CameraPublisher", "Publishing a picture taken. data.length = " + data.length);
            // send image to server.
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                    size.width, size.height, null);

            Rect rect = new Rect(0, 0, image.getWidth(), image.getHeight());
            int quality = 95;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compressToJpeg(rect, quality, out);
            byte[] jpegData = out.toByteArray();
            if (jpegData == null) {
                Log.d("CameraPublisher", "jpegData is null.");
                return;
            }
            publishBacklogCount++;
            new PictureUploaderTask().execute(jpegData);
            //Log.d("CameraPublisher", "sent image data to server. jpeg data size = " + jpegData.length);
        }
    }
}
