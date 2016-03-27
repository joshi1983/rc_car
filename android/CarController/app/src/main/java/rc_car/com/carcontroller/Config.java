package rc_car.com.carcontroller;

import java.util.LinkedList;
import java.util.List;

public class Config {
    private String hostName = "";
    private String protocol = "http";
    private List<PublishURLChangeListener> publishURLChangeListeners = new LinkedList<PublishURLChangeListener>();

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

    public String getPicturePublishURL() {
        return protocol + "://" + getServerHost() + "/api/saveCameraFrame";
    }

    public void save() {
        // FIXME: save to preferences.
    }
}
