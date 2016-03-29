package rc_car.com.carcontroller;

/**
 * RemoteSettings is responsible for downloading preferences from the web
 * application and, on rare occasions, sending preferences to it.
 */
public class RemoteSettings {
    private boolean isRecording;
    private Config config = Config.getSingleton();
    private int updateInterval = 2000;
    private VideoRecordingListener listener;

    public RemoteSettings(VideoRecordingListener listener) {
        if (listener == null)
            throw new NullPointerException("Listener must not be null");

        this.listener = listener;
    }

    private void downloadSettings() {
        // send HTTP request to server to get settings.

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
            // FIXME: if config.isServerRunning(), make api call to 'api/startRecording'.

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
            // FIXME: if config.isServerRunning(), make api call to 'api/stopRecording'.

        }
    }
}
