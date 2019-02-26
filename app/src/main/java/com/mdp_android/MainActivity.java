package com.mdp_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Debugging
    private static final boolean debug = false;
    private static final String dTag = "DEBUG";

    int rX = - 1, rY = -1 , rD = 1; //Start coordinates placeholder
    int wpX = -1, wpY = -1;

    Button btnExplore, btnFastestP, btnRobotReset, btnResetAll, btnCtm1, btnCtm2, btnUpdateGrid;
    ImageButton imgBtnUp, imgBtnDown, imgBtnLeft, imgBtnRight;
    TextView tvStatus;
    Switch swtTilt, swAutoUpdate;
    public GridViewRect arena;
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBTAdapter = null;
    private BluetoothCommunicate mBTCom = null;
    private SharedPreferences settings = null;
    private SensorManager sensorManager = null;

    private short[] obstacle, explored;
    private String obstacleHex, exploredHex;
    public boolean bAutoUpdate = false;

    // Message types sent from the BluetoothCommunicate Handler
    public static final int
            MESSAGE_STATE_CHANGE = 1,
            MESSAGE_READ = 2,
            MESSAGE_WRITE = 3,
            MESSAGE_DEVICE_NAME = 4,
            MESSAGE_TOAST = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("MDP Group 1");
        setStatus(R.string.not_connected);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        arena = findViewById(R.id.arena);
        arena.setClickable(true);
        tvStatus = findViewById(R.id.tvStatus);

        String xCoord = settings.getString("xCoord", "-1");
        String yCoord = settings.getString("yCoord", "-1");
        String direction = settings.getString("direction", "1");
        rX = Integer.parseInt(xCoord);
        rY = Integer.parseInt(yCoord);
        rD = Integer.parseInt(direction);
        arena.updateRobotCoords(rX, rY, rD);

        String wpXcoord = settings.getString("WPxCoord", "0");
        String wpYcoord = settings.getString("WPyCoord", "0");
        wpX = Integer.parseInt(wpXcoord);
        wpY = Integer.parseInt(wpYcoord);
        arena.updateWaypoint(wpX, wpY);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null)
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

        btnExplore = findViewById(R.id.btnExplore);
        btnExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                String message = "beginExplore";
                sendMessage(message);
                tvStatus.setText("message: " + message);
            }
        });
        btnFastestP = findViewById(R.id.btnFastest);
        btnFastestP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                String message = "beginFastest";
                sendMessage(message);
                tvStatus.setText("message: " + message);
            }
        });
        btnRobotReset = findViewById(R.id.btnRobotReset);
        btnRobotReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                arena.updateRobotCoords(rX, rY, rD);
                String message = "Reset Robot";
                sendMessage(message);
                tvStatus.setText("message: " + message);
            }
        });
        btnResetAll = findViewById(R.id.btnResetAll);
        btnResetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                arena.updateRobotCoords(rX, rY, rD);
                arena.updateArena(stringHexToIntArray(
                        "000000000000000000000000000000000000000000000000000000000000000000000000000"));
                arena.updateWaypoint(-1,-1);
                String message = "Reset All";
                sendMessage(message);
                tvStatus.setText("message: " + message);
            }
        });
        btnCtm1 = findViewById(R.id.btnCtm1);
        btnCtm1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                String message = settings.getString("Custom1", "");
                sendMessage(message);
                tvStatus.setText("Custom1: " + message);
            }
        });
        btnCtm2 = findViewById(R.id.btnCtm2);
        btnCtm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                String message = settings.getString("Custom2", "");
                sendMessage(message);
                tvStatus.setText("Custom2: " + message);
            }
        });

        imgBtnUp = findViewById(R.id.imgBtnUp);
        imgBtnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                if(arena.forward()){
                    sendMessage("aForward");
                    tvStatus.setText("Moving robot. Messsage: aForward");
                }
            }
        });

        imgBtnDown = findViewById(R.id.imgBtnDown);
        imgBtnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                if(arena.reverse()){
                    sendMessage("aReverse");
                    tvStatus.setText("Moving robot. Message: aReverse");
                }
            }
        });

        imgBtnLeft = findViewById(R.id.imgBtnLeft);
        imgBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                if(arena.left()){
                    sendMessage("arLeft");
                    tvStatus.setText("Moving robot. Message: arLeft");
                }
            }
        });

        imgBtnRight = findViewById(R.id.imgBtnRight);
        imgBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                if(arena.right()){
                    sendMessage("arRight");
                    tvStatus.setText("Moving robot. Message: arRight");
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        swtTilt = findViewById(R.id.swtTilt);
        swtTilt.setChecked(false);
        swtTilt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(sensorManager == null) return;
                if (isChecked) sensorManager.registerListener(MainActivity.this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_NORMAL);
                else sensorManager.unregisterListener(MainActivity.this);
            }
        });

        swAutoUpdate = findViewById(R.id.swtAutoUpdate);
        swAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    bAutoUpdate = true;
                    arena.updateArena(obstacle);
                }
                else bAutoUpdate = false;
            }
        });

        btnUpdateGrid = findViewById(R.id.btnUpdateGrid);
        btnUpdateGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                arena.updateArena(obstacle);
                arena.invalidate();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent settingIntent;
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            settingIntent = new Intent(this, Config.class);
            //startActivity(settingIntent);
            settingIntent.putExtra("GridHexDec", exploredHex);
            startActivityForResult(settingIntent, 4);
        } else if (id == R.id.action_bluetooth) openBluetoothManager();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBTAdapter != null && !mBTAdapter.isEnabled()){
            Intent enableIT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIT, 3);
        } else if (mBTCom == null)
            setupConnection();
    }

    @Override
    public void onDestroy(){ super.onDestroy();}

    public void openBluetoothManager() {
        if (mBTAdapter==null){
            mBTAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBTAdapter==null){
                Toast.makeText(this,"Bluetooth is not available", Toast.LENGTH_LONG).show();
                return;
            }
        }
        try{
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBTAdapter, null);
            if(uuids != null){
                for (ParcelUuid uuid: uuids){
                    Log.d("UUID: ", uuid.getUuid().toString());
                    //0000110a-0000-1000-8000-00805f9b34fb
                }
            }
        }catch (NoSuchMethodException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }catch (InvocationTargetException e){
            e.printStackTrace();
        }
        tvStatus.setText("Initializing Bluetooth Connection");

        Intent serverIntent = new Intent(this, BluetoothManager.class);
        //startActivity(serverIntent);
        startActivityForResult(serverIntent, 1);
    }

    private void setupConnection(){ //setupchat
        if(debug) Log.d(dTag, "Setting up connection");
        mBTCom = new BluetoothCommunicate(this, mHandler);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(dTag, "onActivityResult" + resultCode);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case 1:
                    connectDevice(data, true);
                    break;
                case 2:
                    connectDevice(data, false);
                    break;
                case 3:
                    setupConnection();
                    break;
                case 4:
                    String xCoord = settings.getString("xCoord", "0");
                    String yCoord = settings.getString("yCoord", "0");
                    String direction = settings.getString("direction", "1");
                    rX = Integer.parseInt(xCoord);
                    rY = Integer.parseInt(yCoord);
                    rD = Integer.parseInt(direction);
                    arena.updateRobotCoords(rX, rY, rD);
                    String wpXcoord = settings.getString("WPxCoord", "0");
                    String wpYcoord = settings.getString("WPyCoord", "0");
                    wpX = Integer.parseInt(wpXcoord);
                    wpY = Integer.parseInt(wpYcoord);
                    Log.d("Waypoint x: ",wpXcoord);
                    Log.d("Waypoint y: ",wpYcoord);
                    arena.updateWaypoint(wpX, wpY);
                    break;
                default:
                    if (debug) Log.d(dTag, "Bluetooth not enabled");
                    Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(BluetoothManager.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
        mConnectedDeviceName = device.getName();
        mBTCom.connect(device, secure);
    }

    private void setStatus(int resId) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private short[] stringHexToIntArray(String inputHex){ // ONLY FOR AMD TOOL
        short pointer = 0;
        short[] shortArray;
        String binary = "", partial, tempBin;
        while (inputHex.length() - pointer > 0){//pointer != inputHex.length()
            partial = inputHex.substring(pointer, pointer + 1); //every character in the input
            tempBin = Integer.toBinaryString(Integer.parseInt(partial,16));
            for (int i = 0; i < 4 - tempBin.length(); i++) binary = binary.concat("0");
            binary = binary.concat(tempBin);
            pointer++;
        }
        String[] stringArray = binary.split("");
        shortArray = new short[stringArray.length-1];//string start with a blank space \0
        for (int i = 0; i < stringArray.length-1; i++)
            shortArray[i] = Short.parseShort(stringArray[i+1]);
        return shortArray;
    }

    private short[] toIntArrayReversed(String exploredHex){ // FROM ALGO
        int pointer = 0;
        short[] shortExploredArray;
        String partial, tempBin;
        StringBuilder exploredBinary = new StringBuilder();

        while (exploredHex.length() - pointer > 0) {
            partial = exploredHex.substring(pointer, pointer + 1); //every character in the input
            tempBin = Integer.toBinaryString(Integer.parseInt(partial,16));
            for (int i = 0; i < 4 - tempBin.length(); i++) exploredBinary = exploredBinary.append("0");
            exploredBinary = exploredBinary.append(tempBin);
            pointer++;
        }
        String exploredBin = exploredBinary.toString();
        exploredBin = exploredBin.substring(2, exploredBin.length()-2);
//        Log.d("exploredBinary", exploredBin);

        pointer = 0;
        StringBuilder newExploredBin = new StringBuilder();
        while (exploredBin.length() - pointer > 0) {
            partial = exploredBin.substring(pointer, pointer + 15);
            newExploredBin.insert(0, partial);
            pointer += 15;
        }
//        Log.d("newExploredBin", newExploredBin.toString());

        String[] stringArray = newExploredBin.toString().split("");
        shortExploredArray = new short[stringArray.length-1];//string start with a blank space \0
        for (int i = 0; i < stringArray.length-1; i++)
            shortExploredArray[i] = Short.parseShort(stringArray[i+1]);
        return shortExploredArray;
    }

    private short[] toIntArrayReversed_Obstacle_FromExplored(String obstacleHex, String exploredHex){
        int pointer = 0;
        String partial, tempBin;
        StringBuilder exploredBinary = new StringBuilder();

        while (exploredHex.length() - pointer > 0) {
            partial = exploredHex.substring(pointer, pointer + 1); //every character in the input
            tempBin = Integer.toBinaryString(Integer.parseInt(partial,16));
            for (int i = 0; i < 4 - tempBin.length(); i++) exploredBinary = exploredBinary.append("0");
            exploredBinary = exploredBinary.append(tempBin);
            pointer++;
        }
        String exploredBin = exploredBinary.toString();
        exploredBin = exploredBin.substring(2, exploredBin.length()-2);
//        Log.d("exploredBinary", exploredBin);

        //Obstacle
        pointer = 0;
        short[] shortObstacleArray;
        StringBuilder obstacleBinary = new StringBuilder();

        while (obstacleHex.length() - pointer > 0) {
            partial = obstacleHex.substring(pointer, pointer + 1); //every character in the input
            tempBin = Integer.toBinaryString(Integer.parseInt(partial,16));
            for (int i = 0; i < 4 - tempBin.length(); i++) obstacleBinary = obstacleBinary.append("0");
            obstacleBinary = obstacleBinary.append(tempBin);
            pointer++;
        }
        String[] exploredBinArray = exploredBin.split("");

        int countZeros = 0;
        for(int i = 1; i < exploredBinArray.length; i++){
            if(exploredBinArray[i].equals("0")){
                countZeros++;
                obstacleBinary.insert(i-1, "0");
            }
        }
        String obstacleBin = obstacleBinary.toString().substring(0, (obstacleBinary.length()-(countZeros%4)));
//        Log.d("obstacleBinary", obstacleBin);

        pointer = 0;
        StringBuilder newObstacleBin = new StringBuilder();
        while (obstacleBin.length() - pointer > 0) {
            partial = obstacleBin.substring(pointer, pointer + 15);
            newObstacleBin.insert(0, partial);
            pointer += 15;
        }
//        Log.d("newExploredBin", newObstacleBin.toString());

        String[] stringArray = newObstacleBin.toString().split("");
        shortObstacleArray = new short[stringArray.length-1];//string start with a blank space \0
        for (int i = 0; i < stringArray.length-1; i++)
            shortObstacleArray[i] = Short.parseShort(stringArray[i+1]);

        return shortObstacleArray;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];

            if(y < -3) arena.forward();
            else if(y > 3) arena.reverse();
            if(x > 3) arena.left();
            else if(x < -3) arena.right();
        }
    }

    public void sendMessage(String message) {
        if (mBTCom.getState() != BluetoothCommunicate.STATE_CONNECTED) {
            if(debug) Log.d(dTag, "Bluetooth not connected");
            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            try{
                mBTCom.write(send);
                if(debug) Log.d(dTag, "sent: " + new String(send));
            } catch (Exception e){
                Log.e("Error", "error msg: " + e.getMessage());
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private final android.os.Handler mHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_STATE_CHANGE:
                    changeState(msg.arg1);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    readMessage(readBuf, msg.arg1);
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_DEVICE_NAME:
                    changeState(msg.arg1);
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void changeState(int state){
        tvStatus.setText("Initializing Bluetooth Connection");
        switch (state) {
            case BluetoothCommunicate.STATE_CONNECTED:
                setStatus(R.string.connected);
                Toast.makeText(getApplicationContext(), "Connected to: " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                tvStatus.setText("Connected to: " + mConnectedDeviceName);
                break;
            case BluetoothCommunicate.STATE_CONNECTING:
                setStatus(R.string.connecting);
                break;
            case BluetoothCommunicate.STATE_LISTEN:
                setStatus(R.string.listening);
                break;
            case BluetoothCommunicate.STATE_NONE:
            default:
                setStatus(R.string.not_connected);
                break;
        }
    }

    private void readMessage(byte[] message, int state){
        String readMessage = new String(message, 0, state);
        Log.d("readMessage: ", readMessage);
        try{
            JSONObject jObj = new JSONObject(readMessage);
            for (int i = 0; i < jObj.names().length(); i++) {
                String objName = jObj.names().get(i).toString();
                switch (objName){
                    case "move":
                        String move = jObj.getString("move");
                        if (move.contains("forward")){
                            arena.forward();
                            tvStatus.setText("Moving forward");
                        } else if (move.contains("right")){
                            arena.right();
                            tvStatus.setText("Rotate right");
                        } else if (move.contains("left")){
                            arena.left();
                            tvStatus.setText("Rotate left");
                        } else if (move.contains("reverse")){
                            arena.reverse();
                            tvStatus.setText("Reversing");
                        } else if (move.contains("explore")){
                            sendMessage("beginExplore");
                            tvStatus.setText("Start exploring");
                        } else if (move.contains("fastest")){
                            sendMessage("beginFastest");
                            tvStatus.setText("Starting fastest path");
                        }
                        break;
                    case "status":
                        String status = jObj.getString("status");
                        tvStatus.setText(status);
                        break;
                    case "grid":
                        String gridHex = jObj.optString("grid");
                        short[] gridArray = stringHexToIntArray(gridHex);
                        arena.updateExplored(gridArray);
                        break;
                    case "explored":
                        exploredHex = jObj.getString("explored");
                        explored = toIntArrayReversed(exploredHex);
                        arena.updateExplored(explored);
                        break;
                    case "obstacle":
                        obstacleHex = jObj.getString("obstacle");
                        obstacle = toIntArrayReversed_Obstacle_FromExplored(obstacleHex, exploredHex);
                        arena.updateArena(obstacle);
                        break;
                    case "robotPosition":
                        Log.d("robotPosition", jObj.getString("robotPosition"));
                        String[] rbtPos = jObj.getString("robotPosition").substring(1,jObj.getString("robotPosition").length()-1).replaceAll("\\s","").split(",");
                        if(Short.parseShort(rbtPos[0]) < 2 || Short.parseShort(rbtPos[0]) > 14 ||
                            Short.parseShort(rbtPos[1]) < 2 || Short.parseShort(rbtPos[1]) > 19 ||
                            Short.parseShort(rbtPos[2]) < 0 || Short.parseShort(rbtPos[2]) > 4){
                            tvStatus.setText("Error in RobotPosition");
                        }else
                            arena.updateRobotCoords(Integer.parseInt(rbtPos[0]), Integer.parseInt(rbtPos[1]), Integer.parseInt(rbtPos[2]));
                        break;
                    case "arrowPosition":
                        String[] arrowPos = jObj.getString("arrowPosition").substring(1,jObj.getString("arrowPosition").length()-1).replaceAll("\\s","").split(",");
                        if(Short.parseShort(arrowPos[0]) < 1 || Short.parseShort(arrowPos[0]) > 15 ||
                                Short.parseShort(arrowPos[1]) < 1 || Short.parseShort(arrowPos[1]) > 20 ||
                                Short.parseShort(arrowPos[2]) < 0 || Short.parseShort(arrowPos[2]) > 4){
                            tvStatus.setText("Error in RobotPosition");
                        }else
                            arena.updateArrow(Integer.parseInt(arrowPos[0]), Integer.parseInt(arrowPos[1]), Integer.parseInt(arrowPos[2]));
                        break;
                    default:
                        Log.d("Task not done: ", objName);
                }
            }
        } catch(Exception e){
            Log.e("ERROR", "Message: " + e.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
