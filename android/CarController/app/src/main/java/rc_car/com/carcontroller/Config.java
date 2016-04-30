package rc_car.com.carcontroller;

import java.util.LinkedList;
import java.util.List;

public class Config {
    private String hostName = "192.168.1.54:8000";
    private String protocol = "http";
    private List<PublishURLChangeListener> publishURLChangeListeners = new LinkedList<PublishURLChangeListener>();
    private static Config singleton = new Config();

    /**
     * Config constructor is made private to enforce singleton pattern.
     * We want the static getSingleton() method used instead.
     * */
    private Config() {

    }

    public static Config getSingleton() {
        return singleton;
    }

    public boolean isServerRunning() {
        return true;
    }

    private void dispatchPublishURLChanged() {
        for (PublishURLChangeListener urlChangeListener: publishURLChangeListeners) {
            urlChangeListener.publishURLChanged(getPicturePublishURL());
        }
    }

    public void addPublishURLChangeListener(PublishURLChangeListener urlChangeListener) {
        publishURLChangeListeners.add(urlChangeListener);
    }

    public void setHostName(String hostName) {
        if (hostName == null)
            throw new IllegalArgumentException("Host name can not be null");

        hostName = hostName.replaceAll("\\s+","");
        if (hostName.length() < 2)
            throw new IllegalArgumentException("Host name too short");

        this.hostName = hostName;
        dispatchPublishURLChanged();
    }

    public String getServerHost() {
        return hostName;
    }

    /**
     * For use in RemoteSettings class
     * */
    public String getCarStatesURL() {
        return protocol + "://" + getServerHost() + "/rc_car/api/carStates";
    }

    /**
     * For use in RemoteSettings class
     * */
    public String getStartRecordingURL() {
        return protocol + "://" + getServerHost() + "/rc_car/api/startRecording";
    }

    /**
     * For use in RemoteSettings class
     * */
    public String getStopRecordingURL() {
        return protocol + "://" + getServerHost() + "/rc_car/api/stopRecording";
    }

    public String getPreferencesURL() {
        return protocol + "://" + getServerHost() + "/rc_car/api/preferences";
    }

    public String getPicturePublishURL() {
        return protocol + "://" + getServerHost() + "/rc_car/api/saveCameraFrame";
    }

    public String getSaveLatestControlStateURL() {
        return protocol + "://" + getServerHost() + "/rc_car/api/saveLatestControlState";
    }

    public void save() {
        // FIXME: save to preferences.
    }
}
