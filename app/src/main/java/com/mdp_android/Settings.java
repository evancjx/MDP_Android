package com.mdp_android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
    String xCoord, yCoord, direction;
    TextView tvSettingStatus;
    EditText tbXcoord, tbYcoord, tbDirection, tbCtm1, tbCtm2;
    Button btnSaveSettings;
    public GridViewRect arena;
    public SharedPreferences settings;
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("MDP Group 1 Settings");

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        edit = settings.edit();

        tvSettingStatus = findViewById(R.id.tvSettingStatus);
        tbXcoord = findViewById(R.id.tbXcoord);
        tbXcoord.setText(settings.getString("xCoord", "0"));
        tbYcoord = findViewById(R.id.tbYcoord);
        tbYcoord.setText(settings.getString("yCoord", "0"));
        tbDirection = findViewById(R.id.tbDirection);
        tbDirection.setText(settings.getString("direction", "0"));

        tbCtm1 = findViewById(R.id.tbCtm1);
        tbCtm1.setText(settings.getString("Custom1", ""));
        tbCtm2 = findViewById(R.id.tbCtm2);
        tbCtm2.setText(settings.getString("Custom2", ""));

        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnSaveSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                xCoord = tbXcoord.getText().toString();
                yCoord = tbYcoord.getText().toString();
                direction = tbDirection.getText().toString();

                try{
                    if (!(Integer.parseInt(direction) <= 4) || !(Integer.parseInt(direction) >= 1) ||
                        !(Integer.parseInt(xCoord) <= 14) || !(Integer.parseInt(xCoord) >= 1) ||
                        !(Integer.parseInt(yCoord) <= 19) || !(Integer.parseInt(yCoord) >= 1)){
                        error();
                        return;
                    }
                }catch(NumberFormatException e) {
                    error();
                    return;
                }

                edit.putString("xCoord", xCoord);
                edit.putString("yCoord", yCoord);
                edit.putString("direction", direction);
                edit.putString("Custom1", tbCtm1.getText().toString());
                edit.putString("Custom2", tbCtm2.getText().toString());
                edit.apply();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void error(){
        tvSettingStatus.setText("Invalid input!");
        Toast.makeText(getApplicationContext(), "Invalid input!", Toast.LENGTH_SHORT).show();
    }
}
