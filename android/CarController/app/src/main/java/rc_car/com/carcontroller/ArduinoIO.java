package rc_car.com.carcontroller;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Responsible for communicating with Android application through USB cables.
 */
public class ArduinoIO {
    private UsbManager usbManager;
    private UsbDevice arduinoDevice;
    private int boadRate = 9600; // must match with Arduino code
    private CarState desired = new CarState(0, 0);
    private LatestCarStateListener latestCarStateListener;

    /**
     * Used in SettingsActivity
     */
    public static String getUSBDevicesDescription(Context context) {
        String result = "";
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessories = usbManager.getAccessoryList ();
        HashMap<String , UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (accessories != null)
            Log.d("ArduinoIO", "accessories.length = " + accessories.length + ".");

        result += "about to list USB devices. size = " + deviceList.size() + ":";
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            result += "usb device = " + device;
        }
        return result;
    }

    public ArduinoIO(Context context, LatestCarStateListener latestCarStateListener) {
        if (latestCarStateListener == null)
            throw new NullPointerException("LatestCarStateListener must not be null");

        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.latestCarStateListener = latestCarStateListener;
    }

    /**
     *
     * setSteeringValue commands tend to originate from the web application
     * */
    public void setSteeringValue(double value) {
        if (arduinoDevice != null) {
            // FIXME: pass value through USB interface to the Arduino device.
        }
    }

    /**
     *
     * setSpeedValue commands tend to originate from the web application
     * */
    public void setSpeedValue(double value) {
        if (arduinoDevice != null) {
            // FIXME: pass value through USB interface to the Arduino device.

        }
    }

    public void setDesiredCarState(CarState newState) {
        desired.copyFrom(newState);

        // FIXME: remove this after testing.
        latestCarStateListener.setLatestCarState(desired);
    }
}
