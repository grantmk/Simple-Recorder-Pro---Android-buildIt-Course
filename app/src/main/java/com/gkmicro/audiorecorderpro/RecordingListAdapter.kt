package com.gkmicro.audiorecorderpro
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil

class RecordingListAdapter(private val clickListener: (Recording) -> Unit): PagingDataAdapter<Recording, RecordingViewHolder>(DIFF_CALLBACK) {

    private val LOG_TAG = "recordinglistadapter"

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Recording>() {

            init {
                Log.d("recording", "DIFF UTIL created")
            }

            override fun areItemsTheSame(old: Recording,
                                         new: Recording) = old.unixDateMillis == new.unixDateMillis

            override fun areContentsTheSame(old: Recording,
                                            new: Recording) = old == new
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        //Log.d(LOG_TAG, "CREATE viewholder ")
        return RecordingViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val recording: Recording? = getItem(position)
        //Log.d(LOG_TAG, "BIND item at position: $position")
        recording?.let {
            holder.bindTo(recording)
            holder.itemView.setOnClickListener { clickListener(recording) }
        }
    }
}