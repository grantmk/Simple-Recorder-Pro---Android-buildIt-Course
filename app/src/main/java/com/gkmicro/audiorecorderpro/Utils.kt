package com.gkmicro.audiorecorderpro

import android.content.Context
import android.os.StatFs
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class Utils {
    val LOGTAG = "Utils"
    fun getDateUnixMillis(): Long {
        return System.currentTimeMillis()
    }

    fun getFilenameAsDateTime(): String {
        val df = Date(getDateUnixMillis())
        return SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault()).format(df)
    }

    fun convertMillisToDateTime(unixTimeMillis: Long): String {
        val df = Date(unixTimeMillis)
        return SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault()).format(df)
    }

    fun convertSecsToMinsSecsReadable (secs: Long): String {
        val mins: Int = (secs / 60).toInt()
        var s = secs.rem(60).toString()
        if(s.count() == 1) {
            s = "0$s"
        }
        return "$mins:$s"
    }

    fun convertMillisToMinsSecsReadable(millis: Long): String {
        return convertSecsToMinsSecsReadable(millis / 1000)
    }

    fun getDb (context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-name"
        ).build()
    }

    fun getExternalStorageDir (context: Context): String {
        val externalStorageVolumes: Array<out File> =
            ContextCompat.getExternalFilesDirs(context, null)
        return externalStorageVolumes[0].absolutePath + "/"
    }

    fun getFreeSpaceMb(context: Context): Long {
        return StatFs(getExternalStorageDir(context)).availableBytes / 1000000
    }

    fun getRecordingPath (storageDir: String, folder: String, fileName: String, fileExtension: String): File {
        return File(storageDir, "$folder/$fileName$fileExtension")
    }

    fun getRecordingDirectory (storageDir: String, folder: String): File {
        return File(storageDir, "$folder/")
    }

    fun changeFileNameOnDisk (from: File, to: File) {
        from.renameTo(to)
    }

    fun changeRecordingName (rec: Recording?, newName: String, appDb: AppDatabase?): Recording? {
        rec?.apply {
            name = newName
        }
        updateRecordingToDb(rec, appDb)
        return rec
    }

    fun showToast(msg: String, con: Context) {
        val toast = Toast.makeText(con, msg, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun updateRecordingToDb (rec: Recording?, appDb: AppDatabase?) {
        rec?.let {
            GlobalScope.launch {
                val result = appDb?.recordingDao()?.update(it)
            }
        }
    }

    suspend fun deleteRecording (rec: Recording?, appDb: AppDatabase, extStorage: String, storageDir: String) {
        //MAY have some weird thread clash issues here - investigate if crashes
        rec?.let {
            val job = GlobalScope.launch {
                appDb.recordingDao().delete(it)
            }
            getRecordingDirectory(extStorage, storageDir).delete()
            job.join()
        }
    }

    fun writeNewRecordingToDb (rec: Recording?, appDb: AppDatabase?) {
        Log.d(LOGTAG, "writing new")
        rec?.let {
            Log.d(LOGTAG, "Recording exists")
            GlobalScope.launch {
                val result = appDb?.recordingDao()?.insert(it)
                Log.d(LOGTAG, "Result: $result")
            }
        }
    }

}