package rc_car.com.carcontroller;


public class Config {
    private String hostName = "";
    private String protocol = "http";

    public void setHostName(String hostName) {
        if (hostName == null)
            throw new IllegalArgumentException("Host name can not be null");

        hostName = hostName.replaceAll("\\s+","");
        if (hostName.length() < 2)
            throw new IllegalArgumentException("Host name too short");

        this.hostName = hostName;
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
