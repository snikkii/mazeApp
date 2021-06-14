package com.example.breakfreeapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

/** Creates the maze
 * @author Annika Stadelmann
 * @version 1.0
 * @since 1.0
 */
public class mazeView extends AppCompatImageView {
    /** Size of the maze
     */
    private static final int COL = 21, ROW = 21;
    /** Colors of path, wall, start and end of the maze
     */
    private final Paint wallPaint = new Paint(), pathPaint = new Paint(),
            startPaint = new Paint(), endPaint = new Paint();
    /** Matrix the maze will be stored in
     */
    private char[][] maze = new char[COL][ROW];

    /** Construcs a mazeView instance
     * @param context Context of the instance
     * @param attrs Attributes that need to be specified in super() call
     */
    public mazeView(@NonNull Context context, AttributeSet attrs){
        super(context, attrs);
    }

    /** Draws the maze
     * @param canvas Canvas in which maze is drawn
     */
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float cellSize, vMargin, hMargin;

        wallPaint.setColor(getResources().getColor(R.color.darkGrey));
        wallPaint.setStrokeWidth(0);
        wallPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(getResources().getColor(R.color.dirtyWhite));
        pathPaint.setStrokeWidth(0);
        pathPaint.setStyle(Paint.Style.FILL);
        startPaint.setColor(getResources().getColor(R.color.darkBlue));
        startPaint.setStrokeWidth(0);
        startPaint.setStyle(Paint.Style.FILL);
        endPaint.setColor(getResources().getColor(R.color.midBlue));
        endPaint.setStrokeWidth(0);
        endPaint.setStyle(Paint.Style.FILL);

        //calculate size for path-, start-, end- and wall-pieces
        int height = getHeight();
        int width = getWidth();

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

        for(int row = 0; row < maze.length; row++){
            for(int col = 0; col < maze[0].length; col++){
                if(maze[row][col] == 's'){ //if field is "start" draw a dark blue rectangle
                    canvas.drawRect(col*cellSize,row*cellSize,(col+1)*cellSize,(row+1)*cellSize, startPaint);
                }
                else if(maze[row][col] == 'e') { //if field is "end" draw a light blue rectangle
                    canvas.drawRect(col*cellSize,row*cellSize,(col+1)*cellSize,(row+1)*cellSize, endPaint);
                }
                else if(maze[row][col] == 'w') { //if field is "wall" draw a dark grey rectangle
                    canvas.drawRect(col*cellSize,row*cellSize,(col+1)*cellSize,(row+1)*cellSize, wallPaint);
                }
                else if(maze[row][col] == 'p') { //if field is "path" draw a light grey rectangle
                    canvas.drawRect(col*cellSize,row*cellSize,(col+1)*cellSize,(row+1)*cellSize, pathPaint);
                }
            }
        }
    }

    /** Stores maze from main-activity in the matrix "maze"
     * @param mazeCharArr Maze from main-activity
     */
    //get maze from main-activity and store it in "maze"
    public void getMaze(char[][] mazeCharArr){
        maze = new char[COL][ROW];
        for(int row = 0; row < mazeCharArr.length; row++){
            for(int col = 0; col < mazeCharArr.length; col++){
                maze[row][col] = mazeCharArr[row][col];
            }
        }
    }
}