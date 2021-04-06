package com.example.collector

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

class AcronymProvider : ContentProvider() {

    companion object{
        val PROVIDER_NAME = "com.example.collector/AcronymProvider"
        val URL = "content://$PROVIDER_NAME/Inbox"
        val CONTENT_URI = Uri.parse(URL)

        val TABLEIN_NAME="Inbox"
        val COL_NAME = "name"
        val COL_AGE = "age"
        val COL_ID = "id"
        val COL_URL = "imageurl"
        val COL_PROFILE = "profileimg"
        val COL_STAGE = "current_stage"
        val COL_IMAGE_BIT = "picturetaken"
        val COL_MODEL = "inf_model"
        val COL_ITEM = "item_of_interest"
        val COL_COUNT = "item_count"
    }
    lateinit var db : SQLiteDatabase

    override fun onCreate(): Boolean {
        var helper = getContext()?.let { DataBaseHandler(it) }
        if (helper != null) {
            db = helper.writableDatabase
        }
        return if(db==null) false else true
    }


    override fun insert(uri: Uri, cv: ContentValues?): Uri? {
        db.insert(TABLEIN_NAME, null, cv)
        context?.contentResolver?.notifyChange(uri, null)
        return uri
    }


    override fun update(uri: Uri, cv: ContentValues?, condition: String?, condition_val: Array<out String>?): Int {
        var count = db.update(TABLEIN_NAME, cv, condition, condition_val)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }


    override fun delete(uri: Uri, condition: String?, condition_val: Array<out String>?): Int {
        var count = db.delete(TABLEIN_NAME, condition, condition_val)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }


    override fun query(uri: Uri, cols: Array<out String>?, condition: String?, condition_val: Array<out String>?, order: String?): Cursor? {
        return db.query(TABLEIN_NAME, cols, condition, condition_val, null, null, order)
    }


    override fun getType(uri: Uri): String? {
        return "vnd.android.cursor.dir/vnd.example.inbox"
    }

}
