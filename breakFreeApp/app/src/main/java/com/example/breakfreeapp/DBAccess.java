package com.example.breakfreeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.StringTokenizer;

/** Creates database for the highscore-list
 * @author Annika Stadelmann
 * @version 1.0
 * @since 1.0
 */
public class DBAccess extends SQLiteOpenHelper {
    /** Database
     */
    private SQLiteDatabase db;
    /** SQL-instruction
     */
    private final String tableSQL;
    /** Table for highscore-list
     */
    private String table;

    /** Construcs a DBAccess instance
     * @param activity Activity where the database should be
     * @param dbName Name of the table
     * @param tableSQL SQL-instruction for creating the table
     */
    //create DBAccess-object and create table
    public DBAccess(Context activity, String dbName, String tableSQL){
        super(activity, dbName, null, 1);
        this.tableSQL = tableSQL;
        identifyTable();
        db = this.getWritableDatabase();
        onCreate(db);
        this.table = dbName.replace(".dat", "");
    }

    /** Identifies the table
     */
    //identify table
    private void identifyTable(){
        String sql = tableSQL.toUpperCase();
        StringTokenizer tokenizer = new StringTokenizer(sql);

        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();

            if(token.equals("EXISTS")){
                table = tokenizer.nextToken();
                break;
            }
        }
    }

    /** Creates the database
     * @param db Database that should be created
     */
    //create db
    @Override
    public void onCreate(SQLiteDatabase db){
        try{
            db.execSQL(tableSQL);
            Log.d("TAG", "Create database");
        } catch(Exception ex) {
            Log.e("TAG", ex.getMessage());
        }
    }

    /** Gets database
     * @return database to use it in another activity
     */
    public SQLiteDatabase getDB(){
        return db;
    }

    /** Recreates table
     * @param db Database that should be upgraded
     * @param oldVersion Number of the old Version
     * @param newVersion Number of the new Version
     */
    //delete table if it exists and create new one
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + table);
        onCreate(db);
    }

    /** Closes database
     */
    //close db
    @Override
    public synchronized void close(){
        if(db != null){
            db.close();
            db = null;
        }
        super.close();
    }

    /** Inserts new dataset in table
     * @param dataset Dataset that should be inserted
     * @return ID of dataset if successful
     */
    //insert the data into the table
    public long insertDataSet(DataSet dataset){
        try{
            ContentValues data = createDataObject(dataset);
            return db.insert(table, null, data);
        } catch(Exception ex){
            Log.e("TAG", ex.getMessage());
            return -1;
        }
    }

    /** Creates listview-cursor
     * @return the table ordered by time it took the player to finish the maze
     */
    //create a cursor
    public Cursor createListViewCursor(){
        String[] columns = new String[]{"_id", "name", "time", "maze"};
        return db.query(table, columns, null, null, null, null, "time");
    }

    /** Creates a new data-object
     * @param dataset Dataset that should become a data-object
     * @return data
     */
    //create a new data-object
    private ContentValues createDataObject(DataSet dataset){
        ContentValues data = new ContentValues();
        data.put("name", dataset.name);
        data.put("time", dataset.time);
        data.put("maze", dataset.maze);
        return data;
    }
}