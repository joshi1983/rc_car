package rc_car.com.carcontroller;

public interface RemoteSettingsListener {
    void recordingStopped();
    void recordingStarted();
    void setDesiredState(CarState newState);
}
