package com.gkmicro.audiorecorderpro

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_recording_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordingListFragment : Fragment() {

    private val viewModel: RecordingListViewModel by viewModels()
    private lateinit var adapter: RecordingListAdapter
    private val LOG_TAG = "recordinglist"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recording_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecordingListAdapter {
            val action = RecordingListFragmentDirections.actionRecordingListFragmentToPlaybackFragment(it)
            findNavController().navigate(action)
        }
        recordingsRecyclerView.adapter = adapter

        lifecycleScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            viewModel.recordings.collectLatest {
                Log.d(LOG_TAG, "Data changed: ${it}")
                adapter.submitData(it)
            }
        }
    }
}
