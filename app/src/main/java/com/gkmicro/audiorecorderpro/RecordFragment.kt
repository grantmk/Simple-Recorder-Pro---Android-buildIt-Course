package com.gkmicro.audiorecorderpro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.gkmicro.audiorecorderpro.databinding.RecordFragmentBinding
import androidx.databinding.DataBindingUtil
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.record_fragment.*
import java.lang.Exception

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RecordFragment : Fragment() {

    private lateinit var binding: RecordFragmentBinding
    private lateinit var viewModel: RecordViewModel

    private val LOG_TAG = "RecordFragment"

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    var btnAnim: Animation? = null

    val recorderStateObserver = Observer<RecorderState> {
        when (it) {
            RecorderState.NOTSTARTED -> {
                clearRecAnimation()
            }
            RecorderState.RECORDING -> {
                recordButton.startAnimation(btnAnim)
            }
            RecorderState.PAUSED -> {
                clearRecAnimation()
            }
            RecorderState.ENDED -> {
                clearRecAnimation()
            }
        }
    }

    private fun clearRecAnimation () {
        try {
            recordButton.clearAnimation()
        } catch(e: Exception) {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) informUserPermissionsDenied()
    }

    private fun informUserPermissionsDenied () {
        // TODO
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.record_fragment,
            container,
            false
        )
        viewModel = ViewModelProviders.of(this).get(RecordViewModel::class.java)
        context?.let {
            viewModel.appDb = Utils().getDb(it)
        }
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAnim = AnimationUtils.loadAnimation(context, R.anim.button_anim_active)
        context?.let {
            viewModel.setStorageDir(Utils().getExternalStorageDir(it))
        }

        activity?.let {
            ActivityCompat.requestPermissions(it, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }

        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_recordFragment_to_settingsFragment)
        }
        showAllButton.setOnClickListener {
            findNavController().navigate(R.id.action_recordFragment_to_recordingListFragment)
            viewModel.setupNewRecording()
        }
        recordButton.setOnClickListener {
            if(viewModel.recorderState.value == RecorderState.ENDED) {
                viewModel.recording?.let { recording ->
                    val action = RecordFragmentDirections.actionRecordFragmentToPlaybackFragment(recording)
                    findNavController().navigate(action)
                    viewModel.setupNewRecording()
                }
            } else viewModel.onRecord()
        }
        recordFragmentStopImageButton.setOnClickListener {
            viewModel.stopRecording()
        }
        saveButton.setOnClickListener {
            viewModel.changeStoredFilename(filenameEditText.text.toString())
            context?.let {
                Utils().showToast("Saved", it)
            }
        }
        newButton.setOnClickListener {
            viewModel.setupNewRecording()
        }
        viewModel.recorderState.observeForever(recorderStateObserver)
        MobileAds.initialize(context) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
        context?.let {
            ReviewPrompt(it).promptForReview()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.recorderState.removeObserver(recorderStateObserver)
    }

    override fun onResume() {
        super.onResume()
        viewModel.initVars()
    }

    /*
    //todo remove to playfragment
    private var player: MediaPlayer? = null
    var mStartPlaying = true

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }
    private fun stopPlaying() {
        player?.release()
        player = null
    }
    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
     */


}
