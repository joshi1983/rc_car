package rc_car.com.carcontroller;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * RemoteSettings is responsible for downloading preferences from the web
 * application and, on rare occasions, sending preferences to it.
 */
public class RemoteSettings {
    private boolean isRecording;
    private Config config = Config.getSingleton();
    private int updateInterval = 2000;
    private RemoteSettingsListener listener;

    public RemoteSettings(RemoteSettingsListener listener) {
        if (listener == null)
            throw new NullPointerException("Listener must not be null");

        this.listener = listener;
        new SettingsDownloadThread().start();
    }

    /**
     * For continuously retrieving settings from the web application
     * */
    private class SettingsDownloadThread extends Thread {
        public void run() {
            try {
                while (true) {
                    downloadSettings();
                    Thread.sleep(updateInterval);
                }
            }
            catch (InterruptedException interuption) {
                Log.d("RemoteSettings", "SettingsDownloadThread was interrupted: " + interuption);
            }
        }
    }

    /**
     *
     * Copied from: http://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
     * */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     *
     * Copied from: http://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
     * */
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }

    private void downloadSettings() {
        // send HTTP request to server to get settings.
        try {
            JSONObject json = readJsonFromUrl(config.getPreferencesURL());
            boolean newIsRecordingValue = json.getBoolean("is_recording");
            if (isRecording != newIsRecordingValue) {
                isRecording = newIsRecordingValue;
                if (isRecording)
                    listener.recordingStarted();
                else
                    listener.recordingStopped();
            }
            json = readJsonFromUrl(config.getCarStatesURL());
            JSONObject desired = json.getJSONObject("desired");
            listener.setSpeedValue(desired.getDouble("speed_value"));
            listener.setSteeringValue(desired.getDouble("steering_value"));
        }
        catch (MalformedURLException e) {
            Log.d("RemoteSettings", "Unable to download preferences due to bad URL: " + e.getMessage());
        }
        catch (JSONException jsonE) {
            Log.d("RemoteSettings", "JSON parsing error: " + jsonE.getMessage());
        }
        catch (IOException ioe) {
            Log.d("RemoteSettings", "Unable to download preferences: " + ioe.getMessage());
        }
    }

    private static void sendPostRequestToURL(String url, String bodyParameters) {
        try {
            HttpURLConnection http = (HttpURLConnection)(new URL(url).openConnection());
            http.setRequestMethod("POST");
            http.setUseCaches(false);
            http.setDoInput(true);
            http.setDoOutput(true);
            http.connect();
            if (bodyParameters != null) {
                DataOutputStream wr = new DataOutputStream (
                        http.getOutputStream ());
                wr.writeBytes (bodyParameters);
                wr.flush ();
                wr.close();
            }
            InputStream in = new BufferedInputStream(http.getInputStream());
            in.read();
            http.disconnect();
        }
        catch (Exception e) {
            Log.d("RemoteSettings", "Problem: " + e.getMessage() + ", description: " + e.toString());
        }
    }

    /**
     * A task for sending HTTP POST requests to a specific URL
     *
     * Used to keep the HTTP post sending off the main UI thread
     * */
    private class HttpPostTask extends AsyncTask<String, Void, Void> {
        private String bodyParameters;

        public HttpPostTask(String bodyParameters) {
            this.bodyParameters = bodyParameters;
        }

        public HttpPostTask() {
            this(null);
        }

        @Override
        protected Void doInBackground(String... urls) {
            Log.d("RemoteSettings", "HttpPostTask, doInBackground called.  publish URL: " + urls[0]);
            sendPostRequestToURL(urls[0], bodyParameters);
            return null;
        }
    }


    /**
     * Called when an end user of this native application wants to start recording.
     */
    public void startRecording() {
        if (isRecording)
            return;

        isRecording = true;
        listener.recordingStarted();
        if (config.isServerRunning()) {
            new HttpPostTask().execute(config.getStartRecordingURL());
        }
    }

    /**
     * Called when an end user of this native application wants to stop recording.
     */
    public void stopRecording() {
        if (!isRecording)
            return;

        isRecording = false;
        listener.recordingStopped();
        if (config.isServerRunning()) {
            new HttpPostTask().execute(config.getStopRecordingURL());
        }
    }
}
