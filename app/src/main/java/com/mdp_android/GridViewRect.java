package com.mdp_android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class GridViewRect extends View {
    public int[] gridArray, exploredArray;
    private int[] robotCenter = {-1, -1}, robotFront = {-1, -1}, waypoint = {-1, -1};
    private int mDirection = 0;

    public ArrayList<Integer>
            arrowX = new ArrayList<>(5),
            arrowY = new ArrayList<>(5),
            arrowDirection = new ArrayList<>(5);

    private int x, x2, y, y2;
    private Paint paint = null;

    private static final int NUM_COLUMNS = 15, NUM_ROWS = 20;
    private static Bitmap Up, Left, Right, Down;

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

        canvas.drawColor(Color.WHITE);

        //GRID
        for (int c = 0; c < NUM_COLUMNS + 1; c++){
            paint.setColor(Color.BLACK);
            canvas.drawLine(x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                    (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight, paint);
        }
        for (int r = 0; r < NUM_ROWS + 1; r++) {
            canvas.drawLine(0, r * cellHeight, NUM_COLUMNS * cellWidth, r * cellHeight, paint);
        }

        int[] gArray = gridArray, eArray = exploredArray;

        Bitmap tmpRobotUp = BitmapFactory.decodeResource(getResources(), R.drawable.up),
                tmpRobotDown = BitmapFactory.decodeResource(getResources(), R.drawable.down),
                tmpRobotLeft = BitmapFactory.decodeResource(getResources(), R.drawable.left),
                tmpRobotRight = BitmapFactory.decodeResource(getResources(), R.drawable.right);
        Up = Bitmap.createScaledBitmap(tmpRobotUp,cellWidth,cellHeight,true);
        Left = Bitmap.createScaledBitmap(tmpRobotLeft,cellWidth,cellHeight,true);
        Right = Bitmap.createScaledBitmap(tmpRobotRight,cellWidth,cellHeight,true);
        Down = Bitmap.createScaledBitmap(tmpRobotDown,cellWidth,cellHeight,true);

        //Arena with Start and Goal
        for (int i= 0; i<NUM_COLUMNS; i++){
            for (int j=0; j<NUM_ROWS; j++){
                x = i; y = j;

                paint.setColor(Color.GRAY);
                canvas.drawRect(x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                        (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight, paint);

                if (i < 3 && j < 3){//Start
                    paint.setColor(Color.GREEN);
                    canvas.drawRect(x * cellWidth, (NUM_ROWS - 1 - y) * cellHeight,
                            (x + 1) * cellWidth, (NUM_ROWS - y) * cellHeight, paint);
                } else if (i > 11 && j> 16){//Goal
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
        if (eArray != null){
            int exploredX, exploredY;
            for (int arrayPos = 0; arrayPos < eArray.length; arrayPos++){
                if (arrayPos > NUM_COLUMNS * NUM_ROWS) break;

                if (eArray[arrayPos] == 1){
                    if (arrayPos % NUM_COLUMNS == 0){ // % 15
                        exploredX = NUM_COLUMNS - 15;
                        exploredY = arrayPos / NUM_COLUMNS;
                    } else {
                        exploredX = arrayPos % NUM_COLUMNS;
                        exploredY = arrayPos / NUM_COLUMNS;
                    }
                    paint.setColor(Color.YELLOW);
                    x2 = exploredX;
                    y2 = exploredY;
                    canvas.drawRect(x2 * cellWidth, (y2 + 1) * cellHeight,
                            (x2 + 1) * cellWidth, (y2) * cellHeight, paint);
                }
            }
        }
        if (gArray != null){//TEST THIS WITH TABLET!!!
            int obstacleX, obstacleY;
            for (int arrayPos = 0; arrayPos < gArray.length; arrayPos++){//pointer
                if (arrayPos > NUM_COLUMNS * NUM_ROWS) break;

                if (gArray[arrayPos] == 1){
                    if (arrayPos % NUM_COLUMNS == 0){ // % 15
                        obstacleX = NUM_COLUMNS - 15;
                        obstacleY = arrayPos / NUM_COLUMNS;
                    } else {
                        obstacleX = arrayPos % NUM_COLUMNS;
                        obstacleY = arrayPos / NUM_COLUMNS;
                    }

                    paint.setColor(Color.BLACK);
                    x2 = obstacleX;
                    y2 = obstacleY;
                    canvas.drawRect(x2 * cellWidth, (y2 + 1) * cellHeight,
                            (x2 + 1) * cellWidth, (y2) * cellHeight, paint);
                }
            }
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

    public void forward(){
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
        if (updateRobotCoords(robotCenter[0], robotCenter[1], mDirection)){
            sendMessage("aForward");
            MainActivity mainAct = (MainActivity) getContext();
            mainAct.tvStatus.setText("Moving robot. Messsage: aForward");
        }
    }

    public void reverse(){//reverse
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
        if (updateRobotCoords(robotCenter[0], robotCenter[1], mDirection)){
            sendMessage("aReverse");
            MainActivity mainAct = (MainActivity) getContext();
            mainAct.tvStatus.setText("Moving robot. Message: aReverse");
        }
    }

    public void left(){
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
        if (updateRobotCoords(robotCenter[0], robotCenter[1], d)){
            sendMessage("arLeft");
            MainActivity mainAct = (MainActivity) getContext();
            mainAct.tvStatus.setText("Moving robot. Message: arLeft");
        }
    }

    public void right(){
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
        if (updateRobotCoords(robotCenter[0], robotCenter[1], d)){
            sendMessage("arRight");
            MainActivity mainAct = (MainActivity) getContext();
            mainAct.tvStatus.setText("Moving robot. Message: arRight");
        }
    }

    private void sendMessage(String message) {
        MainActivity mainAct = (MainActivity) getContext();
        mainAct.sendMessage(message);
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

    public void updateArena(int[] gridArray){
        this.gridArray = gridArray;
        if (((MainActivity) getContext()).bAutoUpdate)
            invalidate();
    }

    public void updateWaypoint(int x, int y){
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

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() != MotionEvent.ACTION_DOWN)
            return true;
        String message;
        int x = 1 + (int) (event.getX()/cellWidth);
        int y = NUM_ROWS - (int)(event.getY() / cellHeight);
        if ((x == waypoint[0] && y == waypoint[1]) || ((x < 4 && y < 4) || (x > 12 && y > 17))){
            waypoint[0] = -1;
            waypoint[1] = -1;
            message = "Waypoint Coords resetted";
            ((MainActivity) getContext()).tvStatus.setText(message);
            ((MainActivity) getContext()).sendMessage(message);
        } else {
            waypoint[0] = x;
            waypoint[1] = y;
            message = "Waypoint Coords [x: " + x + " y: " + y + "]";
            ((MainActivity) getContext()).tvStatus.setText(message);
            ((MainActivity) getContext()).sendMessage(message);
        }
        invalidate();
        return true;
    }
}