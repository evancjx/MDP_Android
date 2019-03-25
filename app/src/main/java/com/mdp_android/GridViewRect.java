package com.mdp_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class GridViewRect extends View {
    public short[] obstacleArray, exploredArray;
    private int[] robotCenter = {-1, -1}, robotFront = {-1, -1}, waypoint = {-1, -1};
    private int mDirection = 0;

    public ArrayList<Integer>
            arrowX = new ArrayList<>(5),
            arrowY = new ArrayList<>(5),
            arrowDirection = new ArrayList<>(5);

    private Paint paint = null;

    private static final int NUM_COLUMNS = 15, NUM_ROWS = 20;
    private static Bitmap Up, Left, Right, Down, WapPoint;

    private int cellWidth, cellHeight;

    public GridViewRect (Context context){
        this(context, null);
    }

    public GridViewRect (Context context, AttributeSet attrs){
        super(context,attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    public GridViewRect(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void calculateCellDimesions(){
        cellWidth = getWidth() / NUM_COLUMNS;
        cellHeight = getHeight() / NUM_ROWS;
        if (cellWidth > cellHeight) cellWidth = cellHeight;
        else cellHeight = cellWidth;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        calculateCellDimesions();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        Bitmap tmpRobotUp = BitmapFactory.decodeResource(getResources(), R.drawable.up),
                tmpRobotDown = BitmapFactory.decodeResource(getResources(), R.drawable.down),
                tmpRobotLeft = BitmapFactory.decodeResource(getResources(), R.drawable.left),
                tmpRobotRight = BitmapFactory.decodeResource(getResources(), R.drawable.right),
                tmpWayPoint = BitmapFactory.decodeResource(getResources(), R.drawable.waypoint_icon);
        Up = Bitmap.createScaledBitmap(tmpRobotUp,cellWidth,cellHeight,true);
        Left = Bitmap.createScaledBitmap(tmpRobotLeft,cellWidth,cellHeight,true);
        Right = Bitmap.createScaledBitmap(tmpRobotRight,cellWidth,cellHeight,true);
        Down = Bitmap.createScaledBitmap(tmpRobotDown,cellWidth,cellHeight,true);
        WapPoint = Bitmap.createScaledBitmap(tmpWayPoint, cellWidth, cellHeight, true);

        //Arena with Start and Goal
        for (int x= 0; x<NUM_COLUMNS; x++){
            for (int y=0; y<NUM_ROWS; y++){

                paint.setColor(Color.GRAY);
                canvas.drawRect(x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                        (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight, paint);

                if (x < 3 && y < 3){//Start
                    paint.setColor(Color.GREEN);
                    canvas.drawRect(
                            x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                            (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight,
                            paint);
                }
                else if (x > 11 && y > 16){//Goal
                    paint.setColor(Color.RED);
                    canvas.drawRect(x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                            (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight, paint);
                }
            }
        }

        // Explored
        if (exploredArray != null){
            int exploredX, exploredY;
            for (int arrayPos = 0; arrayPos < exploredArray.length; arrayPos++){
                if (arrayPos > NUM_COLUMNS * NUM_ROWS) break;

                if (exploredArray[arrayPos] == 1){
                    if (arrayPos % NUM_COLUMNS == 0){ // % 15
                        exploredX = NUM_COLUMNS - 15;
                        exploredY = arrayPos / NUM_COLUMNS;
                    } else {
                        exploredX = arrayPos % NUM_COLUMNS;
                        exploredY = arrayPos / NUM_COLUMNS;
                    }
                    paint.setColor(Color.WHITE);
                    canvas.drawRect(exploredX * cellWidth, (exploredY + 1) * cellHeight,
                            (exploredX + 1) * cellWidth, (exploredY) * cellHeight, paint);
//                    if(arrayPos == 129) break;
                }
            }
        }
        // Obstacle
        if (obstacleArray != null){
            int obstacleX, obstacleY;
            for (int arrayPos = 0; arrayPos < obstacleArray.length; arrayPos++){//pointer
                if (arrayPos > NUM_COLUMNS * NUM_ROWS) break;

                if (obstacleArray[arrayPos] == 1){
                    if (arrayPos % NUM_COLUMNS == 0){ // % 15
                        obstacleX = NUM_COLUMNS - 15;
                        obstacleY = arrayPos / NUM_COLUMNS;
                    } else {
                        obstacleX = arrayPos % NUM_COLUMNS;
                        obstacleY = arrayPos / NUM_COLUMNS;
                    }
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(obstacleX * cellWidth, (obstacleY + 1) * cellHeight,
                            (obstacleX + 1) * cellWidth, (obstacleY) * cellHeight, paint);
                }
            }
        }

        for (int x= 0; x<NUM_COLUMNS; x++) {
            for (int y = 0; y < NUM_ROWS; y++) {
                if (x < 3 && y < 3) {//Start
                    paint.setColor(Color.GREEN);
                    canvas.drawRect(
                            x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                            (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight,
                            paint);
                }
                else if (x > 11 && y > 16) {//Goal
                    paint.setColor(Color.RED);
                    canvas.drawRect(x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                            (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight, paint);
                }
            }
        }

        //GRID
        for (int c = 0; c < NUM_COLUMNS + 1; c++){
            paint.setColor(Color.BLACK);
            canvas.drawLine(c * cellWidth, 0, c * cellWidth, NUM_ROWS * cellHeight, paint);
        }
        for (int r = 0; r < NUM_ROWS + 1; r++) {
            paint.setColor(Color.BLACK);
            canvas.drawLine(0, r * cellHeight, NUM_COLUMNS * cellWidth, r * cellHeight, paint);
        }

        //ROBOT
        if (robotCenter[0] >= 0){
            paint.setColor(Color.BLUE);
            canvas.drawCircle((robotCenter[0] -  1) * cellWidth + cellWidth / 2,
                    (NUM_ROWS - robotCenter[1] + 1) * cellHeight - cellHeight / 2, 1.3f * cellWidth, paint);
        }
        if (robotFront[0] >= 0) {
            paint.setColor(Color.MAGENTA);
            canvas.drawCircle((robotFront[0] - 1 ) * cellWidth + cellWidth / 2,
                    (NUM_ROWS - robotFront[1] + 1) * cellHeight - cellHeight / 2, 0.3f * cellWidth, paint);
        }
        //Waypoint
        if (waypoint[0] >= 1 && waypoint[1] >= 1 && waypoint[0] <= NUM_COLUMNS && waypoint[1] <= NUM_ROWS ){
            paint.setColor(Color.YELLOW);
            canvas.drawRect((waypoint[0] - 1) * cellHeight, (NUM_ROWS - waypoint[1] + 1) * cellWidth,
                    waypoint[0] * cellHeight, (NUM_ROWS - waypoint[1]) * cellWidth, paint);
        }

//        if (waypoint[0] >= 1 && waypoint[1] >= 1 && waypoint[0] <= NUM_COLUMNS && waypoint[1] <= NUM_ROWS ){
//            canvas.drawBitmap(WapPoint, (waypoint[0] - 1) * cellWidth, (NUM_ROWS - waypoint[1]) * cellHeight, null);
//        }

        //Arrow
        if(arrowX!=null) {
            Bitmap bmpArrowDirection = null;
            for (int i = 0; i < arrowX.size(); i++) {
                switch (arrowDirection.get(i)) {
                    case 1:
                        bmpArrowDirection = Up;
                        break;
                    case 2:
                        bmpArrowDirection = Down;
                        break;
                    case 3:
                        bmpArrowDirection = Left;
                        break;
                    case 4:
                        bmpArrowDirection = Right;
                        break;
                    default:
                        break;
                }
                canvas.drawBitmap(bmpArrowDirection, (arrowX.get(i) - 1) * cellWidth, (NUM_ROWS - arrowY.get(i)) * cellHeight, null);
            }
        }
    }

    public boolean forward(){
        switch (mDirection){
            case 1: //currently facing up
                if (robotCenter[1] + 1 <= 19)
                    robotCenter[1] += 1;
                break;
            case 2: //currently facing reverse
                if (robotCenter[1] - 1 >= 2) //robot is facing downwards, going to hit the wall
                    robotCenter[1] -= 1;
                break;
            case 3: //currently facing left
                if (robotCenter[0] - 1 >= 2) //Robot is facing left, going to hit the wall
                    robotCenter[0] -= 1;
                break;
            case 4: //currently facing right
                if (robotCenter[0] + 1 <= 14)
                    robotCenter[0] += 1;
                break;
        }
        return updateRobotCoords(robotCenter[0], robotCenter[1], mDirection);
    }

    public boolean reverse(){//reverse
        boolean bUpdate = false;
        switch (mDirection){
            case 1: //currently facing up
                if (robotCenter[1] - 1 >= 2)
                    robotCenter[1] -= 1;
                break;
            case 2: //currently facing reverse
                if (robotCenter[1] + 1 <= 19)
                    robotCenter[1] += 1;
                break;
            case 3: //currently facing left
                if (robotCenter[0] + 1 <= 14)
                    robotCenter[0] += 1;
                break;
            case 4: //currently facing right
                if (robotCenter[0] - 1 >= 2)
                    robotCenter[0] -= 1;
                break;
        }
        return updateRobotCoords(robotCenter[0], robotCenter[1], mDirection);
    }

    public boolean left(){
        int d = 0;
        switch (mDirection){
            case 1: //currently facing up
                d = 3;
                break;
            case 2: //currently facing down
                d = 4;
                break;
            case 3: //currently facing left
                d = 2;
                break;
            case 4: //currently facing right
                d = 1;
                break;
        }
        return updateRobotCoords(robotCenter[0], robotCenter[1], d);
    }

    public boolean right(){
        int d = 0;
        switch (mDirection){
            case 1: //currently facing up
                d = 4;
                break;
            case 2: //currently facing reverse
                d = 3;
                break;
            case 3: //currently facing left
                d = 1;
                break;
            case 4: //currently facing right
                d = 2;
                break;
        }
        return updateRobotCoords(robotCenter[0], robotCenter[1], d);
    }

    public boolean updateRobotCoords(int col, int row, int direction){
        if(col < 1|| row < 1 || direction < 0){
            MainActivity mainAct = (MainActivity) getContext();
            Toast.makeText(mainAct,"Config Robot Start Coords", Toast.LENGTH_LONG).show();
            return false;
        }

        robotCenter[0] = col; //X coord 1
        robotCenter[1] = row; //Y coord 1
        mDirection = direction;

        switch (direction){
            case 1: //face up
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] + 1;
                break;
            case 2: //face reverse
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] - 1;
                break;
            case 3: //face left
                robotFront[0] = robotCenter[0] - 1; //x coord
                robotFront[1] = robotCenter[1];
                break;
            case 4: //face right
                robotFront[0] = robotCenter[0] + 1; //x coord
                robotFront[1] = robotCenter[1];
                break;
        }
        if (((MainActivity) getContext()).bAutoUpdate)
            invalidate();
        return true;
    }

    public void updateWayPoint(int x, int y){
        this.waypoint[0] = x;
        this.waypoint[1] = y;
        invalidate();
    }

    public void updateArrow(int x, int y, int direction){
        arrowX.add(x);
        arrowY.add(y);
        arrowDirection.add(direction);
        if (((MainActivity) getContext()).bAutoUpdate)
            invalidate();
    }

    public void updateExplored(short[] exploredArray){
        this.exploredArray = exploredArray;
        if (((MainActivity) getContext()).bAutoUpdate)
            invalidate();
    }

    public void updateObstacle(short[] obstacleArray){
        this.obstacleArray = obstacleArray;
        if (((MainActivity) getContext()).bAutoUpdate)
            invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() != MotionEvent.ACTION_DOWN)
            return true;
        String message;
        int x = 1 + (int)(event.getX()/cellWidth);
        int y = NUM_ROWS - (int)(event.getY() / cellHeight);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor edit = settings.edit();
        if ((x == waypoint[0] && y == waypoint[1]) || ((x < 4 && y < 4) || (x > 12 && y > 17))){
            waypoint[0] = 0;
            waypoint[1] = 0;
            message = "Waypoint Coords resetted";
        } else {
            waypoint[0] = x;
            waypoint[1] = y;
            message = "Waypoint Coords [x: " + x + " y: " + y + "]";
        }
        ((MainActivity) getContext()).tvStatus.setText(message);
        arr = new JSONArray();
        arr.put(waypoint[0]);
        arr.put(waypoint[1]);
        jObj.put("waypoint", arr);
        ((MainActivity) getContext()).sendMessage("p"+jObj.toString());
        edit.putString("WPxCoord", Integer.toString(waypoint[0]));
        edit.putString("WPyCoord", Integer.toString(waypoint[1]));
        edit.apply();
        invalidate();
        return true;
    }
}