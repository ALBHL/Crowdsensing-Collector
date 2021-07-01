package com.example.collector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast


val DATABASE_NAME ="MyDB"
val TABLEIN_NAME="Inbox"
val COL_NAME = "name"
val COL_AGE = "age"
val COL_ID = "id"
val COL_URL = "imageurl"
val COL_PROFILE = "profileimg"
val COL_STAGE = "current_stage"  // true means can be sent to be collected
val COL_IMAGE_BIT = "picturetaken"
val COL_MODEL = "inf_model"
val COL_ITEM = "item_of_interest"
val COL_COUNT = "item_count"

val createTableIn = "CREATE TABLE " + TABLEIN_NAME + " (" +
        COL_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
        COL_NAME + " VARCHAR(256)," +
        COL_STAGE + " VARCHAR(256)," +
        COL_AGE + " INTEGER," +
        COL_PROFILE + " VARCHAR(256)," +
        COL_URL + " VARCHAR(256)," +
        COL_MODEL + " VARCHAR(256)," +
        COL_ITEM + " VARCHAR(256)," +
        COL_COUNT + " INTEGER," +
        COL_IMAGE_BIT + " BLOB)"

val dropTableIn = "DROP TABLE IF EXISTS " + TABLEIN_NAME

class DataBaseHandler(var context: Context) : SQLiteOpenHelper(context,DATABASE_NAME,null,12) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createTableIn)
    }


    override fun onUpgrade(db: SQLiteDatabase?,oldVersion: Int,newVersion: Int) {
        db?.execSQL(dropTableIn)
        onCreate(db)
    }


    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }


    fun insertData(user : User){  // add row into the inbox rows
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_NAME, user.task_name)
        cv.put(COL_AGE, user.age)
        cv.put(COL_URL, user.imageurl)
        cv.put(COL_PROFILE, user.profileurl)
        cv.put(COL_STAGE,  user.cur_stage)
        cv.put(COL_MODEL, user.model)
        cv.put(COL_ITEM, user.item)
        val result = db.insert(TABLEIN_NAME,null,cv)
        if(result == -1.toLong())
            Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show()
    }

    fun insertDataImg(user: User, img: ByteArray){  // add row into the inbox rows
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_NAME, user.task_name)
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
                user.task_name = result.getString(result.getColumnIndex(COL_NAME))
                user.age = result.getString(result.getColumnIndex(COL_AGE)).toInt()
                user.imageurl = result.getString(result.getColumnIndex(COL_URL))
                user.cur_stage = result.getString(result.getColumnIndex(COL_STAGE))
                user.profileurl = result.getString(result.getColumnIndex(COL_PROFILE))
//                user.model = result.getString(result.getColumnIndex(COL_MODEL))
//                user.item = result.getString(result.getColumnIndex(COL_ITEM))
                list.add(user)
            }while (result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }

    fun readDataName(cur_id: String): String {
        val db = this.readableDatabase
        val query = "Select * from " + TABLEIN_NAME + " where " + COL_ID + " = " + cur_id
        val result = db.rawQuery(query,null)
        var name: String = ""
        if(result.moveToFirst()){
            name = result.getString(result.getColumnIndex(COL_NAME))
        }
        result.close()
        db.close()
        return name
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

    fun readDataItem(cur_id: String): String {
        val db = this.readableDatabase
        val query = "Select * from " + TABLEIN_NAME + " where " + COL_ID + " = " + cur_id
        val result = db.rawQuery(query,null)
        var item: String = ""
        if(result.moveToFirst()){
            item = result.getString(result.getColumnIndex(COL_ITEM))
        }
        result.close()
        db.close()
        return item
    }

    fun updateData() {
        val db = this.writableDatabase
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

    fun updateRowModel(id: String, inf_model: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_MODEL, inf_model)
        db.update(TABLEIN_NAME, cv,"$COL_ID=?", arrayOf(id))
    }

    fun updateRowCount(id: String, item_count: Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_COUNT, item_count)
        db.update(TABLEIN_NAME, cv,"$COL_ID=?", arrayOf(id))
    }

    fun updateRowItem(id: String, item: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_ITEM, item)
        db.update(TABLEIN_NAME, cv,"$COL_ID=?", arrayOf(id))
    }
}
