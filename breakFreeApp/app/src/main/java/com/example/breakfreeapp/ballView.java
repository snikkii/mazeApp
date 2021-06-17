package com.example.breakfreeapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.concurrent.TimeUnit;

/** Draws the ball that rolls through the maze
 * @author Annika Stadelmann
 * @version 1.0
 * @since 1.0
 */
public class ballView extends AppCompatImageView {
    /** Current position of ball in matrix
     */
    private int xCurr=0, yCurr=0;
    /** Directions from sensorlistener and position of ball in the drawing
     */
    private float xDir=0, yDir=0, xPos=0, yPos=0;
    /** Size of the maze
     */
    private static final int COL = 21, ROW = 21;
    /** Color of the ball
     */
    private final Paint ballPaint = new Paint();
    /** Matrix where the maze is stored in
     */
    private char[][] maze = new char[COL][ROW];
    /** Flags that are used to check if game is started or over and if it is finished
     */
    public boolean gameOver = false, isGameStarted, isWinner = false;

    /** Construcs a ballView instance
     * @param context Context of the instance
     * @param attrs Attributes that need to be specified in super() call
     */
    public ballView(@NonNull Context context, AttributeSet attrs){
        super(context, attrs);
    }

    /** Draws the ball everytime it changes it direction
     * @param canvas Canvas in which ball is drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cellSize, hMargin, vMargin, radius;

        ballPaint.setColor(getResources().getColor(R.color.midRed));
        ballPaint.setStrokeWidth(0);
        ballPaint.setStyle(Paint.Style.FILL);

        //get values for size of the maze the ball will be in
        int height = getHeight();
        int width = getWidth();
        //calculate size for path-, start-, end- and wall-pieces
        if(width/height < COL/ROW){
            cellSize = width/(COL+1);
        }
        else{
            cellSize = height/(ROW+1);
        }
        //calculate size of maze
        hMargin = (width-COL*cellSize)/2;
        vMargin = (height-ROW*cellSize)/2;
        canvas.translate(hMargin, vMargin);
        radius = cellSize/2;

        //if start button has been clicked get positions of the ball
        //in the maze-array (maze[yCurr][xCurr]) and in the drawing (xPos, yPos)
        if(isGameStarted){
            if(xDir < -3 && xCurr+1 != 21 && maze[yCurr][xCurr+1] != 'w'){
                xCurr += 1;
                xPos += cellSize;
                try {
                    //give ball a delay to make sure it doesn't move too fast
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(xDir > 3 && xCurr != 0 && maze[yCurr][xCurr-1] != 'w'){
                xCurr -= 1;
                xPos -= cellSize;
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(yDir < -1 && yCurr != 0 && maze[yCurr-1][xCurr] != 'w'){
                yCurr -= 1;
                yPos -= cellSize;
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(yDir > 5 && yCurr+1 != 21 && maze[yCurr+1][xCurr] != 'w'){
                yCurr += 1;
                yPos += cellSize;
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            canvas.drawCircle(xPos+radius, yPos+radius, radius, ballPaint);
            //when ball lands on field named "e" for end, the game is over
            // and the ball will be placed on a start-field
            if(maze[yCurr][xCurr] == 'e'){
                gameOver = true;
                isWinner = true;
                xCurr = 0;
                yCurr = 0;
                xPos = 0;
                yPos = 0;
            }
        }
    }

    /** Updates the values of x- and y-tilt
     * @param xDir The x value of the accelerometer
     * @param yDir The y value of the accelerometer
     */
    public void getDirection (float xDir, float yDir){
        this.xDir = xDir;
        this.yDir = yDir;
    }

    /** Stores maze from main-activity in the matrix "maze"
            * @param mazeCharArr Maze from main-activity
     */
    public void getMaze(char[][] mazeCharArr){
        maze = new char[COL][ROW];
        for(int row = 0; row < mazeCharArr.length; row++){
            for(int col = 0; col < mazeCharArr.length; col++){
                maze[row][col] = mazeCharArr[row][col];
            }
        }
    }

    /** Checks if game is started or not
     * @param isGameStarted True is game has started
     */
    public void getStart(boolean isGameStarted){
        this.isGameStarted = isGameStarted;
    }
}