package rc_car.com.carcontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {
    private EditText serverNameInput;
    private Config config = Config.getSingleton();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        serverNameInput = (EditText)findViewById(R.id.server_host);
        serverNameInput.setText(config.getServerHost());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // close activity.

            }
        });
    }

}
