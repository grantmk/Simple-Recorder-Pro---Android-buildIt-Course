package com.gkmicro.audiorecorderpro

import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.gkmicro.audiorecorderpro.databinding.PlaybackFragmentBinding
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.playback_fragment.*
import kotlinx.android.synthetic.main.playback_fragment.adView
import kotlinx.android.synthetic.main.playback_fragment.filenameEditText
import kotlinx.android.synthetic.main.playback_fragment.saveButton
import kotlinx.android.synthetic.main.record_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PlaybackFragment : Fragment() {

    private lateinit var binding: PlaybackFragmentBinding
    val args: PlaybackFragmentArgs by navArgs()

    companion object {
        fun newInstance() = PlaybackFragment()
    }

    private lateinit var viewModel: PlaybackViewModel

    private lateinit var sbl: SeekBarListener

    private class SeekBarListener(private val viewModel: PlaybackViewModel): SeekBar.OnSeekBarChangeListener
    {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            //Log.d("playback", "On prog changed")
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            Log.d("playback", "On start tracking")
            viewModel.mediaPlayer?.pause()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            Log.d("playback", "On stop tracking")
            seekBar?.progress?.let {
                viewModel.mediaPlayer?.seekTo(it)
                viewModel.mediaPlayer?.start()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playImageButton.setOnClickListener {
            if(viewModel.recordingSetupFinished) viewModel.togglePlayback()
            else viewModel.setupAndStartPlayback(true)
        }
        ffImageButton.setOnClickListener {
            seekMediaPlayer(10000)
        }
        rwImageButton.setOnClickListener {
            seekMediaPlayer(-10000)
        }
        saveButton.setOnClickListener {
            viewModel.changeStoredFilename(filenameEditText.text.toString())
            context?.let {
                Utils().showToast("Saved", it)
                activity?.onBackPressed()
            }
        }
        shareButton.setOnClickListener {
            val FILE_PROVIDER= "com.gkmicro.fileprovider"
            val content = getUriForFile(requireActivity().applicationContext, FILE_PROVIDER, viewModel.getMediaFile())
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, content)
                type = "audio/mp4"
            }
            //Log.d("playback", "$content")
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share recording file"))
        }
        deleteButton.setOnClickListener {
            this.context?.let { con ->
                AlertDialog.Builder(con)
                    .setTitle("Please Confirm")
                    .setMessage("Do you really want to delete?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes,
                        DialogInterface.OnClickListener { dialog, whichButton ->
                            GlobalScope.launch {
                                viewModel.deleteRecording()
                                activity?.onBackPressed()
                            }
                        })
                    .setNegativeButton(android.R.string.no, null).show()
            }
        }
        playbackSeekBar.setOnSeekBarChangeListener(sbl) //todo check or memory leaks
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun seekMediaPlayer (offset: Long) {
        viewModel.mediaPlayer?.let {
            val pos = it.currentPosition
            val newPos = pos + offset
            it.seekTo(newPos, MediaPlayer.SEEK_PREVIOUS_SYNC)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.playback_fragment,
            container,
            false
        )
        viewModel = ViewModelProviders.of(this).get(PlaybackViewModel::class.java)
        viewModel.initViewmodel(args.recording)
        sbl = SeekBarListener(viewModel)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.mediaPlayer?.stop()
    }

}
