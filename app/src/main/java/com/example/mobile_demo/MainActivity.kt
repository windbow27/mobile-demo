package com.example.mobile_demo

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.TextView
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load data from SharedPreferences
        loadData()

        // Save data to SharedPreferences when button is clicked
        val sharedPrefButton = findViewById<Button>(R.id.sharedPrefButton)
        sharedPrefButton.setOnClickListener {
            saveData()
        }

        // Load data from Internal Storage
        val internalStorageTextView = findViewById<TextView>(R.id.internalStorageTextView)
        internalStorageTextView.text = readFile("internalStorage.txt")

        // Save data to Internal Storage when button is clicked
        val internalStorageButton = findViewById<Button>(R.id.internalStorageButton)
        internalStorageButton.setOnClickListener {
            writeFile("internalStorage.txt", "Hello, Internal Storage!")
        }

        // Checks if external storage is available for read and write
        fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

        // Checks if external storage is available to at least read
        fun isExternalStorageReadable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

        var albumDir: File? = null

        if (isExternalStorageWritable() && isExternalStorageReadable()) {
            albumDir = getAlbumStorageDir("MyAlbum")
        } else {
            Log.e("MainActivity", "External storage not available")
        }

        // Save data to External Storage when button is clicked
        val externalStorageButton = findViewById<Button>(R.id.externalStorageButton)
        externalStorageButton.setOnClickListener {
            val file = File(albumDir, "externalStorage.txt")
            file.writeText("Heyo, External Storage!")
            Log.i("MainActivity", "File path: ${file.absolutePath}")
        }

        // Load data from External Storage
        val externalStorageTextView = findViewById<TextView>(R.id.externalStorageTextView)
        val file = File(albumDir, "externalStorage.txt")
        externalStorageTextView.text = file.readText()

        val dbHelper = FeedReaderDbHelper(this)

        val insertButton = findViewById<Button>(R.id.insertButton)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val queryTextView = findViewById<TextView>(R.id.queryTextView)

        insertButton.setOnClickListener {
            // Insert a new row
            val newRowId = dbHelper.insertData("My Title", "My Subtitle")
            Log.d("MainActivity", "Inserted new row with ID: $newRowId")

            // Query for rows where the title is "My Title"
            query(dbHelper, queryTextView)
        }

        deleteButton.setOnClickListener {
            // Delete rows where the title is "My Title"
            val deletedRows = dbHelper.deleteMyTitle()
            Log.d("MainActivity", "Deleted $deletedRows rows")

            // Query for rows where the title is "My Title"
            query(dbHelper, queryTextView)
        }

    }

    fun query(dbHelper: FeedReaderDbHelper, queryTextView: TextView) {
        // Query for rows where the title is "My Title"
        val cursor = dbHelper.queryMyTitle()
        val stringBuilder = StringBuilder()
        while (cursor.moveToNext()) {
            val itemId = cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE))
            val subtitle = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE))
            stringBuilder.append("Row ID: $itemId, Title: $title, Subtitle: $subtitle\n")
        }
        cursor.close()
        // Update the TextView with the queried data
        queryTextView.text = stringBuilder.toString()
    }


    // 4 modes: MODE_PRIVATE, MODE_WORLD_READABLE, MODE_WORLD_WRITEABLE, MODE_MULTI_PROCESS
    // Save data to SharedPreferences
    private fun saveData() {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString("myKey", "Hello, SharedPreferences!")
            putString("myKey1", "Heyo, SharedPreferences 1!")
            putString("myKey2", "Howdy, SharedPreferences 2!")
            apply()
        }
    }

    // Load data from SharedPreferences
    private fun loadData() {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val defaultValue = "DefaultString"
        // random a integer 0 1 or 2
        val randomInt = (0..2).random()
        val myString = when (randomInt) {
            0 -> sharedPref.getString("myKey", defaultValue)
            1 -> sharedPref.getString("myKey1", defaultValue)
            2 -> sharedPref.getString("myKey2", defaultValue)
            else -> defaultValue
        }

        // display myString in a TextView
        val sharedPrefsTextView = findViewById<TextView>(R.id.sharedPrefsTextView)
        sharedPrefsTextView.text = myString
    }

    // Save data to Internal Storage
    fun writeFile(filename: String, data: String) {
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load data from Internal Storage
    fun readFile(filename: String): String {
        val file = File(filesDir, filename)
        if (!file.exists()) {
            return "File not found"
        }

        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = openFileInput(filename)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder: StringBuilder = StringBuilder()
        var text: String? = null
        while (run {
                text = bufferedReader.readLine() // readLine() returns null if no more
                text
            } != null) {
            stringBuilder.append(text) // if text is not null, append it to stringBuilder
        }
        fileInputStream?.close()
        return stringBuilder.toString()
    }

    fun getAlbumStorageDir(albumName: String): File {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs()) {
            Log.e("MainActivity", "Directory not created")
        }
        return file
    }

//    fun deleteExternalStoragePrivateFile() {
//        // Get path for the file on external storage. If external
//        // storage is not currently mounted this will fail.
//        val file = File(getExternalFilesDir(null), "externalStorage.txt")
//        file.delete()
//    }
}