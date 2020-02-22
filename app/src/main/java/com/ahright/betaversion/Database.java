package com.ahright.betaversion;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class Database {
    public static final String TAG = "Database";

    // Singleton instance
    private static Database database;

    // database name
    public static String DATABASE_NAME = "AreaTodo.db";
    //table name for AREA_INFO
    public static String TABLE_AREA_INFO = "AREA_INFO";
    //version
    public static int DATABASE_VERSION = 1;


    //Helper class defined
    private DatabaseHelper dbHelper;
    //Database object
    SQLiteDatabase db;

    private Context context;


    // Constructor
    private Database(Context context) {
        this.context = context;
    }


    public static Database getInstance(Context context) {
        if (database == null) {
            database = new Database(context);
        }

        return database;
    }

    //open database
    public boolean open() {
        println("opening database [" + DATABASE_NAME + "].");

        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        return true;
    }

    //close database
    public void close() {
        println("closing database [" + DATABASE_NAME + "].");
        db.close();
        database = null;
    }


    public boolean execSQL(String SQL) {
        println("\nexecute called.\n");

        try {
            Log.d(TAG, "SQL : " + SQL);
            db.execSQL(SQL);
        } catch (Exception ex) {
            Log.e(TAG, "Exception in executeQuery", ex);
            return false;
        }

        return true;
    }



    //AREA_DB 생성& AREA_TABLE 생성
    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        //새로운 사용자일 경우
        public void onCreate(SQLiteDatabase _db) {
            // TABLE_AREA_INFO
            println("creating table [" + TABLE_AREA_INFO + "].");

            // drop existing table
            String DROP_SQL = "drop table if exists " + TABLE_AREA_INFO;
            try {
                _db.execSQL(DROP_SQL);
            } catch(Exception ex) {
                Log.e(TAG, "Exception in DROP_SQL", ex);
            }

            // create table
            String CREATE_SQL = "create table " + TABLE_AREA_INFO + "("
                    + "  _id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + "  LATITUDE DOUBLE, "
                    + "  LONGITUDE DOUBLE, "
                    + "  ADDRESS TEXT, "
                    + "  NAME TEXT, "
                    + "  STARTDATE TEXT, "
                    + "  ENDDATE TEXT, "
                    + "  ICON INTEGER "
                    + ")";
            try {
                _db.execSQL(CREATE_SQL);
                Log.d(TAG, "create  TABLE_AREA_INFO");

                //기본 장소 집 추가
                insertAreaRecord(_db,37,126,"서울","집","a","z",1);
                //집에 대한 할일 테이블 생성
                _db.execSQL("create table " + "집" + "(" + " _id integer PRIMARY KEY autoincrement, " + " todo text,  checked integer );" );

            } catch(Exception ex) {
                Log.e(TAG, "Exception in CREATE_SQL", ex);
            }


        }

        public void onOpen(SQLiteDatabase db) {
            println("opened database [" + DATABASE_NAME + "].");

        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            println("Upgrading database from version " + oldVersion + " to " + newVersion + ".");

            if (oldVersion < 2) {   // version 1

            }

        }

        private void insertAreaRecord(SQLiteDatabase _db, double latitude, double longitude, String address, String name, String startDate, String endDate, Integer icon) {
            try {
                _db.execSQL( "insert into " + TABLE_AREA_INFO + "(LATITUDE, LONGITUDE, ADDRESS, NAME, STARTDATE, ENDDATE, ICON) values ('" + latitude + "', '" + longitude + "', '" + address + "', '" +name + "', '" + startDate + "', '" + endDate + "', '" + icon + "');" );
            } catch(Exception ex) {
                Log.e(TAG, "Exception in executing insertAreaRecord SQL.", ex);
            }
        }

    }


    /***************About TABLE_AREA_INFO ****************/
    //AREA TABLE에 레코드 추가
    public void insertAreaRecord(double latitude, double longitude, String address, String name, String startDate, String endDate, Integer icon) {
        try {
            db.execSQL( "insert into " + TABLE_AREA_INFO + "(LATITUDE, LONGITUDE, ADDRESS, NAME, STARTDATE, ENDDATE, ICON) values ('" + latitude + "', '" + longitude + "', '" + address + "', '" +name + "', '" + startDate + "', '" + endDate + "', '" + icon + "');" );
        } catch(Exception ex) {
            Log.e(TAG, "Exception in executing insertAreaRecord SQL.", ex);
        }
    }

    //특정 레코드 삭제
    public void deleteAreaRecord(String name){
        println("delete Area record name [" + name + "].");
        db.delete(TABLE_AREA_INFO, "NAME" + " = ? ", new String[] {name});
        try {
            db.execSQL("drop table if exists " + name);
            println("delete Todo table [" + name + "]");
        } catch(Exception ex) {
            Log.e(TAG, "Exception in DROP_SQL", ex);
        }

    }

    //전체 레코드 조회
    public ArrayList<AreaInfo> selectAllArea() {
        ArrayList<AreaInfo> result = new ArrayList<AreaInfo>();

        try {
            Cursor cursor = db.rawQuery("select LATITUDE, LONGITUDE, ADDRESS, NAME, STARTDATE, ENDDATE, ICON from " + TABLE_AREA_INFO, null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                double latitude = cursor.getDouble(0);
                double longitude = cursor.getDouble(1);
                String address = cursor.getString (2);
                String name = cursor.getString(3);
                String startDate = cursor.getString(4);
                String endDate = cursor.getString(5);
                Integer icon = cursor.getInt(6);


                Log.d(TAG+" ," +name,latitude+ ", " + longitude + ", " + address + ", " + name + ", " + startDate + ", "+ endDate + ", " + icon );

                AreaInfo area = new AreaInfo(latitude,longitude, address, name,startDate, endDate, icon);
                result.add(area);
            }

        } catch(Exception ex) {
            Log.e(TAG, "Exception in executing selectAllArea SQL...", ex);
        }

        return result;
    }

    //특정 레코드 조회
    public AreaInfo selectSpecificArea(String tableName) {
        AreaInfo area = null;

        try {
            Cursor cursor = db.rawQuery("select LATITUDE, LONGITUDE, ADDRESS, NAME, STARTDATE, ENDDATE, ICON from " + TABLE_AREA_INFO +" WHERE NAME = '"+ tableName +"'", null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                double latitude = cursor.getDouble(0);
                double longitude = cursor.getDouble(1);
                String address = cursor.getString (2);
                String name = cursor.getString(3);
                String startDate = cursor.getString(4);
                String endDate = cursor.getString(5);
                Integer icon = cursor.getInt(6);

                Log.d(TAG+" ," +name,latitude+ ", " + longitude + ", " + address + ", " + name + ", " + startDate + ", "+ endDate + ", " + icon );
                area = new AreaInfo(latitude,longitude, address, name,startDate, endDate, icon);
                return area;
            }

        } catch(Exception ex) {
            Log.e(TAG, "Exception in executing selectSpecificArea SQL...", ex);
        }

        return area;

    }


    /******************** About TABLE_TODO_INFO *********************/
    //새로운 TODO_TABLE 생성
    public void createTodoTable(String tableName){
        println("createTodoTable() 호출됨.");

        if(database !=null){
            database.execSQL("create table " + tableName + "(" + " _id integer PRIMARY KEY autoincrement, " + " todo text,  checked integer );" );
            println("Todo 테이블 생성됨.");
        }
        else{
            println("먼저 데이터베이스를 오픈하세요.");
        }
    }

    // 특정 TODO_TABLE에 레코드 추가
    public void insertTodoRecord(String tableName, String todo) {
        try {
            db.execSQL( "insert into " + tableName + "(TODO, CHECKED) values ('" + todo + "', '" + "0" + "');" );
        } catch(Exception ex) {
            Log.e(TAG, "Exception in executing insertTodoRecord SQL.", ex);
        }

    }

    //특정 레코드 삭제
    public void deleteTodoRecord(String todo){
        println("delete Todo record[" + todo + "].");
        try{
            db.delete("집", "TODO" + " = ? ", new String[] {todo});
        }
        catch(Exception ex) {
            Log.e(TAG, "Exception in deleteTodoRecord_SQL", ex);
        }

    }

    //특정 TODO_TABLE 조회
    public ArrayList<String> selectAllTodo(String tableName) {
        ArrayList<String> result = new ArrayList<String>();

        try {
            Cursor cursor = db.rawQuery("select DISTINCT TODO from " + tableName, null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                String haveToDo = cursor.getString(0);

                Log.d(TAG, i+ ":"+ haveToDo );

                result.add(haveToDo);
            }

        } catch(Exception ex) {
            Log.e(TAG, "Exception in executing selectAllTodo SQL...", ex);
        }

        return result;
    }

    //특정 TODO_TABLE 조회
    public ArrayList<Integer> selectAllChecked(String tableName){
        ArrayList<Integer> result = new ArrayList<Integer>();

        try {
            Cursor cursor = db.rawQuery("select TODO, CHECKED from " + tableName, null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                String haveToDo = cursor.getString(0);
                int checked = cursor.getInt(1);

                Log.d(TAG, i+ ":"+ haveToDo + ", checked:" + checked);

                result.add(checked);
            }

        } catch(Exception ex) {
            Log.e(TAG, "Exception in executing selectAllChecked SQL...", ex);
        }

        return result;
    }

    //특정 레코드의 칼럼(TODO) 수정
    public void modifyTodo(String tableName, String oldTodo, String NewTodo){
        println("before: modify todo [" + oldTodo + "].");
        try {
            db.execSQL("UPDATE "+ tableName +" SET TODO = '"+ NewTodo +"' WHERE TODO = '"+ oldTodo+"'");
        } catch(Exception ex) {
            Log.e(TAG, "Exception in ModifyTodo_SQL", ex);
        }
        println("after: modify todo [" + NewTodo + "].");

    }

    //특정 레코드의 칼럼(Checked) 수정 , 0->1 , 1->0
    public void modifyChecked(String tableName, String todo, int checked){
        println("before: modify checked [" + todo +","+ checked + "].");

        if(checked==1){
            checked=0;
        }
        else if(checked==0){
            checked=1;
        }

        try {
            db.execSQL("UPDATE "+ tableName +" SET CHECKED = '"+ checked +"' WHERE TODO = '"+ todo+"'");
        } catch(Exception ex) {
            Log.e(TAG, "Exception in ModifyChecked_SQL", ex);
        }
        println("after: modify checked [" + todo +","+ checked + "].");

    }


    private void println(String msg) {
        Log.d(TAG, msg);
    }
}