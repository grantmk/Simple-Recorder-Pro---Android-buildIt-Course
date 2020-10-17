package com.gkmicro.audiorecorderpro

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.lang.Exception

class PlaybackViewModel(private val app: Application) : AndroidViewModel(app) {

    var mediaPlayer: MediaPlayer? = null
    private var recording: Recording? = null

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    private val _mediaDurationMilliSecs = MutableLiveData<Int>()
    val mediaDurationMilliSecs: LiveData<Int>
    get() = _mediaDurationMilliSecs

    private val _mediaDurationReadable = MutableLiveData<String>()
    val mediaDurationReadable: LiveData<String>
    get() = _mediaDurationReadable

    private val _currPosReadable = MutableLiveData<String>()
    val currPosReadable: LiveData<String>
    get() = _currPosReadable

    private val _currPosMilliSecs = MutableLiveData<Int>()
    val currPosMilliSecs: LiveData<Int>
    get() = _currPosMilliSecs

    private val _filenameText = MutableLiveData<String>()
    val filenameText: LiveData<String>
        get() = _filenameText

    private val statusInterval = 100L
    private val handler = Handler()
    private var statusChecker: Runnable? = null

    private var extStorage: String = ""
    private var fileName: String = ""
    private var storageDir: String = ""
    private var fileExtension = ".mp4"

    var recordingSetupFinished = false

    init {
        setMpStatusWatcher()
    }

    fun initViewmodel (rec: Recording) {
        recording = rec
        _filenameText.postValue(recording?.name)
        setMediaVars()
    }

    suspend fun deleteRecording() {
        Utils().deleteRecording(recording, Utils().getDb(app), extStorage, storageDir)
    }

    private fun setMediaVars () {
        recording?.let {
            extStorage = Utils().getExternalStorageDir(app)
            storageDir = it.unixDateMillis.toString()
            fileName = it.name
            fileExtension = it.extension
        }
    }

    private fun setMpStatusWatcher () {
        statusChecker = object : Runnable {
            override fun run() {
                try {
                    updateStatus()
                } finally {
                    handler.postDelayed(this, statusInterval)
                }
            }
        }
        statusChecker?.run()
    }

    private fun updateStatus() {
        mediaPlayer?.let {
            if(it.isPlaying) {
                _isPlaying.postValue(true)
                _currPosMilliSecs.postValue(it.currentPosition)
                currPosMilliSecs.value?.let { currP ->
                    _currPosReadable.postValue(Utils().convertMillisToMinsSecsReadable(currP.toLong()))
                }
                _mediaDurationMilliSecs.postValue(it.duration)
                mediaDurationMilliSecs.value?.let {md ->
                    _mediaDurationReadable.postValue(Utils().convertMillisToMinsSecsReadable(md.toLong()))
                }
            } else {
                _isPlaying.postValue(false)
            }
            return
        }
        recording?.length_secs?.let {
            _mediaDurationReadable.postValue(Utils().convertSecsToMinsSecsReadable(it))
        }
        _currPosReadable.postValue("0:00")
    }

    fun getMediaUri (): Uri {
        return Uri.parse(Utils().getRecordingPath(extStorage, storageDir, fileName, fileExtension).toString())
    }

    fun getMediaFile (): File {
        return File(Utils().getRecordingPath(extStorage, storageDir, fileName, fileExtension).toString())
    }

    fun setupAndStartPlayback (startNow: Boolean) {
        recording?.let {

            val myUri = getMediaUri()
            Log.d("playback", "Uri = ${myUri.toString()}")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setWakeMode(app, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(app, myUri)
            }
            mediaPlayer?.setOnPreparedListener {
                recordingSetupFinished = true
                if(startNow)
                    mediaPlayer?.start()
            }
            mediaPlayer?.prepareAsync()
        }
    }

    private fun setFilename (fn: String) {
        recording?.name = fn
        fileName = fn
        _filenameText.postValue(fn)
    }

    fun changeStoredFilename (newFilename: String) {
        val from = Utils().getRecordingPath(extStorage, storageDir, fileName, fileExtension)
        //Log.d("playback", "From: $from")
        setFilename(newFilename)
        val to = Utils().getRecordingPath(extStorage, storageDir, newFilename, fileExtension)
        //Log.d("playback", "To: $to")
        try {
            Utils().changeFileNameOnDisk(from, to)
            Utils().changeRecordingName(recording, newFilename, Utils().getDb(app))
        } catch(ex: Exception) {
            //revert to ld filename and old DB rec name
        }
    }

    fun togglePlayback () {
        isPlaying.value?.let {
            if(it) pausePlayback()
            else resumePlayback()
        }
    }

    private fun pausePlayback () {
        mediaPlayer?.pause()
    }

    private fun resumePlayback () {
        mediaPlayer?.start()
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(statusChecker)
    }
}
