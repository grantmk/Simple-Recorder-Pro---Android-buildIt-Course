package com.gkmicro.audiorecorderpro

import android.app.Application
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.io.File
import java.io.IOException


class RecordViewModel(private val app: Application) : AndroidViewModel(app) {

    private val LOG_TAG = "RecordViewModel"
    private val _recordButtonText = MutableLiveData<String>()
    val recordButtontext: LiveData<String>
        get() = _recordButtonText

    private val _showStopBtn = MutableLiveData<Boolean>()
    val showStopBtn: LiveData<Boolean>
        get() = _showStopBtn

    private val _showFilename = MutableLiveData<Boolean>()
    val showFilename: LiveData<Boolean>
        get() = _showFilename

    private val _showNewBtn = MutableLiveData<Boolean>()
    val showNewBtn: LiveData<Boolean>
        get() = _showNewBtn

    private val _recorderState = MutableLiveData<RecorderState>()
    val recorderState: LiveData<RecorderState>
        get() = _recorderState

    private val _filenameText = MutableLiveData<String>()
    val filenameText: LiveData<String>
        get() = _filenameText

    private val _freeSpaceText = MutableLiveData<String>()
    val freeSpaceText: LiveData<String>
        get() = _freeSpaceText


    private var recorder: MediaRecorder? = null

    private var startTimeNano: Long = 0
    private var elapsedTimeNano: Long = 0
    private var isTiming = false

    private var folder: String = ""
    private var fileName: String = ""
    private var storageDir: String = ""
    private val fileExtension = ".mp4"

    var appDb: AppDatabase? = null
    var recording: Recording? = null

    val recorderStateObserver = Observer<RecorderState> {
        when (it) {
            RecorderState.NOTSTARTED -> {
                startTimeNano = 0
                elapsedTimeNano = 0
                isTiming = false
                _recordButtonText.postValue("Record")
                showStopBtn(false)
                showFilenameEdittext(false)
                showNewButtonDis(false)
                calcFreeSpace()
            }
            RecorderState.RECORDING -> {
                startTimeNano = System.nanoTime()
                isTiming = true
                _recordButtonText.postValue("Pause")
                showStopBtn(true)
            }
            RecorderState.PAUSED -> {
                elapsedTimeNano += System.nanoTime() - startTimeNano
                isTiming = false
                _recordButtonText.postValue("Resume")
                showStopBtn(true)
            }
            RecorderState.ENDED -> {
                if(isTiming) {
                    elapsedTimeNano += System.nanoTime() - startTimeNano
                }
                recording?.length_secs = elapsedTimeNano / 1000000000
                _recordButtonText.postValue("Play")
                showStopBtn(false)
                showFilenameEdittext(true)
                showNewButtonDis(true)
                calcFreeSpace()
            }
        }
    }

    fun initVars () {
        calcFreeSpace()
    }

    private fun calcFreeSpace() {
        val space = Utils().getFreeSpaceMb(app)
        _freeSpaceText.postValue("$space Mb Space")
    }

    private fun showStopBtn (show: Boolean) {
        _showStopBtn.postValue(show)
    }

    private fun showFilenameEdittext (show: Boolean) {
        _showFilename.postValue(show)
    }

    private fun showNewButtonDis (show: Boolean) {
        _showNewBtn.postValue(show)
    }

    init {
        recorderState.observeForever(recorderStateObserver)
        _recorderState.postValue(RecorderState.NOTSTARTED)
    }

    fun setStorageDir(sd: String) {
        storageDir = sd
    }

    private fun ensureStorageDirExists () {
        File(storageDir, folder).mkdir()
    }

    private fun setFilename (fn: String) {
        fileName = fn
        _filenameText.postValue(fn)
    }

    fun changeStoredFilename (fn: String) {
        val from = Utils().getRecordingPath(storageDir, folder, fileName, fileExtension)
        setFilename(fn)
        val to = Utils().getRecordingPath(storageDir, folder, fileName, fileExtension)
        Utils().changeFileNameOnDisk(from, to)
        Utils().changeRecordingName(recording, fileName, appDb)
    }

    private fun getAbsoluteFilePath (): String {
        return "$storageDir$folder/$fileName$fileExtension"
    }

    fun onRecord() {
        when (recorderState.value) {
            RecorderState.NOTSTARTED -> {
                startRecording()
            }
            RecorderState.RECORDING -> {
                pauseRecording()
            }
            RecorderState.PAUSED -> {
                resumeRecording()
            }
            RecorderState.ENDED -> {

            }
        }
    }

    private fun startRecording() {
        _recorderState.postValue(RecorderState.RECORDING)
        val unixMillis = Utils().getDateUnixMillis()
        folder = unixMillis.toString()
        ensureStorageDirExists()
        val filename = Utils().getFilenameAsDateTime()
        setFilename(filename)
        recording = Recording(unixMillis, filename, null, ".mp4")

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(getAbsoluteFilePath())
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
                Log.e(LOG_TAG, e.message)
            }
        }
    }

    fun resumeRecording () {
        _recorderState.postValue(RecorderState.RECORDING)
        recorder?.apply { resume() }
    }

    fun pauseRecording() {
        _recorderState.postValue(RecorderState.PAUSED)
        recorder?.apply {
            pause()
        }
    }

    fun stopRecording() {
        Log.d(LOG_TAG, "stopping recording")
        _recorderState.postValue(RecorderState.ENDED)
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        Utils().writeNewRecordingToDb(recording, appDb)
    }

    fun setupNewRecording() {
        _recorderState.postValue(RecorderState.NOTSTARTED)
    }

    override fun onCleared() {
        super.onCleared()
        recorder?.release()
        recorder = null
        recorderState.removeObserver(recorderStateObserver)
    }

}