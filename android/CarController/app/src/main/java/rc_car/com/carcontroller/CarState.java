package rc_car.com.carcontroller;

/**
 * CarState is basically a data transfer object or a Java bean for car state.
 *
 * It may represent either desired state or the latest actual state echoed back from the Arduino device
 */
public class CarState {
    private double speedValue;
    private double steeringValue;

    public CarState(double steeringValue, double speedValue) {
        this.steeringValue = steeringValue;
        this.speedValue = speedValue;
    }

    public void copyFrom(CarState other) {
        this.speedValue = other.speedValue;
        this.steeringValue = other.steeringValue;
    }

    public double getSteeringValue() {
        return steeringValue;
    }

    public double getSpeedValue() {
        return speedValue;
    }

}
