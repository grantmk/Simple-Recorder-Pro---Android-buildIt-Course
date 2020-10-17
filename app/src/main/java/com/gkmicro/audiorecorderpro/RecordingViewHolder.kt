package com.gkmicro.audiorecorderpro

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordingViewHolder (parent : ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.recording_item, parent, false)) {

    private val nameView = itemView.findViewById<TextView>(R.id.name)
    private val dateView = itemView.findViewById<TextView>(R.id.dateTextView)
    private val lengthView = itemView.findViewById<TextView>(R.id.lengthTextView)
    var rec: Recording? = null

    fun bindTo(rec: Recording?) {
        this.rec = rec
        nameView.text = rec?.name
        rec?.unixDateMillis?.let {
            dateView.text = Utils().convertMillisToDateTime(rec.unixDateMillis)
        }
        rec?.length_secs?.let {
            lengthView.text = Utils().convertSecsToMinsSecsReadable(it)
        }
    }
}