package rc_car.com.carcontroller;

public interface RemoteSettingsListener {
    void recordingStopped();
    void recordingStarted();
    void setSteeringValue(double newValue);
    void setSpeedValue(double newValue);
}
