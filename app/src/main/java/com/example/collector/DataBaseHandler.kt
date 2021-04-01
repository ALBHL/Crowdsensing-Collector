package com.example.collector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.number.IntegerWidth
import android.util.Log
import android.util.Log.d
import android.widget.Toast
import com.google.mlkit.vision.demo.kotlin.textdetector.TextGraphic


val DATABASE_NAME ="MyDB"
val TABLE_NAME="Users"
val TABLEIN_NAME="Inbox"
val COL_NAME = "name"
val COL_AGE = "age"
val COL_ID = "id"
val COL_URL = "imageurl"
val COL_PROFILE = "profileimg"
val COL_STAGE = "current_stage"  // true means can be sent to be collected
val COL_IMAGE_BIT = "picturetaken"

val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
        COL_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
        COL_NAME + " VARCHAR(256)," +
        COL_STAGE + " VARCHAR(256)," +
        COL_AGE + " INTEGER," +
        COL_PROFILE + " VARCHAR(256)," +
        COL_URL + " VARCHAR(256))"

val dropTable = "DROP TABLE IF EXISTS " + TABLE_NAME

val createTableIn = "CREATE TABLE " + TABLEIN_NAME + " (" +
        COL_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
        COL_NAME + " VARCHAR(256)," +
        COL_STAGE + " VARCHAR(256)," +
        COL_AGE + " INTEGER," +
        COL_PROFILE + " VARCHAR(256)," +
        COL_URL + " VARCHAR(256)," +
        COL_IMAGE_BIT + " BLOB)"

val dropTableIn = "DROP TABLE IF EXISTS " + TABLEIN_NAME

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context,DATABASE_NAME,null,10) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createTableIn)
        db?.execSQL(createTable)
    }


    override fun onUpgrade(db: SQLiteDatabase?,oldVersion: Int,newVersion: Int) {
        db?.execSQL(dropTable)
        db?.execSQL(dropTableIn)
        onCreate(db)
    }


    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }


    fun insertData(user : User){  // add row into the inbox rows
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_NAME, user.name)
        cv.put(COL_AGE, user.age)
        cv.put(COL_URL, user.imageurl)
        cv.put(COL_PROFILE, user.profileurl)
        cv.put(COL_STAGE,  user.cur_stage)
        val result = db.insert(TABLEIN_NAME,null,cv)
        if(result == -1.toLong())
            Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show()
    }

    fun insertDataImg(user: User, img: ByteArray){  // add row into the inbox rows
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_NAME, user.name)
        cv.put(COL_AGE, user.age)
        cv.put(COL_URL, user.imageurl)
        cv.put(COL_PROFILE, user.profileurl)
        cv.put(COL_STAGE,  user.cur_stage)
        cv.put(COL_IMAGE_BIT, img)
        val result = db.insert(TABLEIN_NAME,null,cv)
        if(result == -1.toLong())
            Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show()
    }


    fun readData() : MutableList<User>{
        val list : MutableList<User> = ArrayList()

        val db = this.readableDatabase
        val query = "Select * from " + TABLEIN_NAME
        val result = db.rawQuery(query,null)
        if(result.moveToFirst()){
            do {
                val user = User()
                user.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                user.name = result.getString(result.getColumnIndex(COL_NAME))
                user.age = result.getString(result.getColumnIndex(COL_AGE)).toInt()
                user.imageurl = result.getString(result.getColumnIndex(COL_URL))
                user.cur_stage = result.getString(result.getColumnIndex(COL_STAGE))
                user.profileurl = result.getString(result.getColumnIndex(COL_PROFILE))
                list.add(user)
            }while (result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }

    fun readDataImg(cur_id: String): Bitmap? {
        val db = this.readableDatabase
        val query = "Select * from " + TABLEIN_NAME + " where " + COL_ID + " = " + cur_id
        Log.d("datebasexixi", query)
        val result = db.rawQuery(query,null)
        var retblob: ByteArray? = null
        if(result.moveToFirst()){
            retblob = result.getBlob(result.getColumnIndex(COL_IMAGE_BIT))
        }
        val bmp = retblob?.size?.let { BitmapFactory.decodeByteArray(retblob, 0, it) }
        result.close()
        db.close()
        return bmp
    }

    fun updateData() {
        val db = this.writableDatabase
        db?.execSQL(dropTable)
        db?.execSQL(dropTableIn)
        onCreate(db)
    }

    fun deleteInRow(id: String) {
        val db = this.writableDatabase
        db.delete(TABLEIN_NAME, "$COL_ID=?", arrayOf(id))
    }


    fun updateRow(id: String, stage: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_STAGE, stage)/// TODO: something wrong here
        db.update(TABLEIN_NAME, cv,"$COL_ID=?", arrayOf(id))
    }

    fun updateRowImg(id: String, img: ByteArray) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_IMAGE_BIT, img)
        db.update(TABLEIN_NAME, cv,"$COL_ID=?", arrayOf(id))
    }

}
