package rc_car.com.carcontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {
    private EditText serverNameInput;
    private EditText usbDevicesList;
    private Config config = Config.getSingleton();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        serverNameInput = (EditText)findViewById(R.id.server_host);
        serverNameInput.setText(config.getServerHost());
        usbDevicesList = (EditText)findViewById(R.id.usb_devices);
    }

    public void backClicked(View view) {
        finish();
    }

    private String testArduinoConnection() {
        return ArduinoIO.getUSBDevicesDescription(this);
    }

    private String testServerConnection() {
        String result = "";

        return result;
    }

    public void testConnections(View v) {
        usbDevicesList.setText(testArduinoConnection());
    }

}
