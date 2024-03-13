package com.example.mobile_demo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mobile_demo.FeedReaderContract.FeedEntry

class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "FeedReader.db"
        private const val DATABASE_VERSION = 1

        private val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${FeedEntry.TABLE_NAME} (" +
                    "${FeedEntry.ID} INTEGER PRIMARY KEY," +
                    "${FeedEntry.COLUMN_NAME_TITLE} TEXT," +
                    "${FeedEntry.COLUMN_NAME_SUBTITLE} TEXT)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS ${FeedEntry.TABLE_NAME}")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    // Query for rows where the title is "My Title"
    fun queryMyTitle(): Cursor {
        val db = this.readableDatabase

        val projection = arrayOf(
            FeedEntry.ID,
            FeedEntry.COLUMN_NAME_TITLE,
            FeedEntry.COLUMN_NAME_SUBTITLE
        )

        val selection = "${FeedEntry.COLUMN_NAME_TITLE} = ?"
        val selectionArgs = arrayOf("My Title")

        val sortOrder = "${FeedEntry.COLUMN_NAME_SUBTITLE} DESC"

        return db.query(
            FeedEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
    }

//    It gets a readable instance of the SQLite database.
//    It defines a projection, which specifies the columns that should be included for each row in the result set.
//    It defines a selection criteria, which is a SQL WHERE clause that determines which rows to include in the result set.
//    In this case, it's looking for rows where the title is "My Title".
//    It defines the arguments for the selection criteria. In this case, it's the string "My Title".
//    It defines a sort order, which is a SQL ORDER BY clause that determines the order of the rows in the result set.
//    In this case, it's sorting the rows by the subtitle in descending order.
//    It calls the query method on the database instance, passing in the table name,
//    the projection, the selection criteria, the selection arguments, and the sort order.
//    This method queries the database and returns a Cursor object that can be used to navigate
//    through the rows in the result set.

    // Delete rows where the title is "My Title"
    fun deleteMyTitle(): Int {
        val db = this.writableDatabase

        val selection = "${FeedEntry.COLUMN_NAME_TITLE} LIKE ?"
        val selectionArgs = arrayOf("My Title")

        return db.delete(FeedEntry.TABLE_NAME, selection, selectionArgs)
    }

    // Insert a new row
    fun insertData(title: String, subtitle: String): Long {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(FeedEntry.COLUMN_NAME_TITLE, title)
            put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle)
        }

        return db.insert(FeedEntry.TABLE_NAME, null, values)
    }
}


