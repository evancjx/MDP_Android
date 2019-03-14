package com.mdp_android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class Config extends AppCompatActivity {
    String xCoord, yCoord, wpXcoord, wpYCoord, direction;
    TextView tvSettingStatus, tvExplored, tvObstacle, tvArrowPositions;
    EditText tbXcoord, tbYcoord, tbDirection, tbCtm1, tbCtm2, tbWPxCoord, tbWPyCoord;
    Button btnSaveSettings, btnShowGridHexDec;
    public SharedPreferences settings;
    private SharedPreferences.Editor edit;
    private String exploredStringP1, obstacleStringP2;
    public ArrayList<Integer>
            arrowX = new ArrayList<>(5),
            arrowY = new ArrayList<>(5),
            arrowDirection = new ArrayList<>(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("MDP Group 1 Config");

        Bundle bundle = getIntent().getExtras();
        exploredStringP1 = bundle.getString("exploredHex");
        obstacleStringP2 = bundle.getString("obstacleHex");
        arrowX = bundle.getIntegerArrayList("arrowX");
        arrowY = bundle.getIntegerArrayList("arrowY");
        arrowDirection = bundle.getIntegerArrayList("arrowDirection");

        Log.d("arrowX", arrowX.toString());
        Log.d("arrowY", arrowY.toString());
        Log.d("arrowDirection", arrowDirection.toString());

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        edit = settings.edit();

        tvSettingStatus = findViewById(R.id.tvSettingStatus);
        tvExplored = findViewById(R.id.tvExplored);
        tvObstacle = findViewById(R.id.tvObstacle);
        tvArrowPositions = findViewById(R.id.tvArrowPositions);

        tbXcoord = findViewById(R.id.tbXcoord);
        tbXcoord.setText(settings.getString("xCoord", "0"));
        tbYcoord = findViewById(R.id.tbYcoord);
        tbYcoord.setText(settings.getString("yCoord", "0"));
        tbDirection = findViewById(R.id.tbDirection);
        tbDirection.setText(settings.getString("direction", "0"));

        tbWPxCoord = findViewById(R.id.tbWPxCoord);
        tbWPxCoord.setText(settings.getString("WPxCoord", "0"));
        tbWPyCoord = findViewById(R.id.tbWPyCoord);
        tbWPyCoord.setText(settings.getString("WPyCoord", "0"));

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
                wpXcoord = tbWPxCoord.getText().toString();
                wpYCoord = tbWPyCoord.getText().toString();
                direction = tbDirection.getText().toString();

                try{
                    if (!(Integer.parseInt(direction) <= 4) || !(Integer.parseInt(direction) >= 1) ||
                        !(Integer.parseInt(xCoord) <= 14) || !(Integer.parseInt(xCoord) >= 1) ||
                        !(Integer.parseInt(yCoord) <= 19) || !(Integer.parseInt(yCoord) >= 1) ||
                        !(Integer.parseInt(wpXcoord) <= 14) || !(Integer.parseInt(wpXcoord) >= 0) ||
                        !(Integer.parseInt(wpYCoord) <= 19) || !(Integer.parseInt(wpYCoord) >= 0)){
                        error();
                        return;
                    }
                }catch(NumberFormatException e) {
                    error();
                    return;
                }

                edit.putString("xCoord", xCoord);
                edit.putString("yCoord", yCoord);
                edit.putString("WPxCoord", wpXcoord);
                edit.putString("WPyCoord", wpYCoord);
                edit.putString("direction", direction);
                edit.putString("Custom1", tbCtm1.getText().toString());
                edit.putString("Custom2", tbCtm2.getText().toString());
                edit.apply();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        btnShowGridHexDec = findViewById(R.id.btnShowGridHexDec);
        btnShowGridHexDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvExplored.setText(exploredStringP1);
                tvObstacle.setText(obstacleStringP2);
                String arrowPositionsString = "";
                int arrowNum;
                for(int i = 0; i < arrowX.size(); i++){
                    arrowNum = 1 + i;
                    arrowPositionsString += "Arrow " + arrowNum + ": [" +
                        arrowX.get(i) + ", " +  arrowY.get(i) + ", " +
                        getArrowDirection(arrowDirection.get(i)) + "] ";
                }
                Log.d("arrowPositionsString", arrowPositionsString);
                tvArrowPositions.setText(arrowPositionsString);
            }
        });
    }

    private void error(){
        tvSettingStatus.setText("Invalid input!");
        Toast.makeText(getApplicationContext(), "Invalid input!", Toast.LENGTH_SHORT).show();
    }

    private String getArrowDirection(int arrowDirection){
        switch (arrowDirection){
            case 1:
                return "U";
            case 2:
                return "D";
            case 3:
                return "L";
            case 4:
                return "R";
            default:
                return "Error";
        }
    }
}
