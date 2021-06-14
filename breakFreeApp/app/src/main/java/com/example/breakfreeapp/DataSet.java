package com.example.breakfreeapp;

/** Creates a new dataset
 * @author Annika Stadelmann
 * @version 1.0
 * @since 1.0
 */
public class DataSet {
    /** ID of the row
     */
    public long id;
    /** Columns of the table
     */
    public String name, time, maze;

    /** Creates a row that could be inserted in the table
     * @param name Name of the player
     * @param time Time it took the player to finish the maze
     * @param maze Maze the player played
     */
    //set dataset which has name of the user, time it took the user to play
    // and maze the user played in it
    public DataSet(String name, String time, String maze) {
        this.name = name;
        this.time = time;
        this.maze = maze;

        id = -1; //id will be created when inserting in db
    }
}