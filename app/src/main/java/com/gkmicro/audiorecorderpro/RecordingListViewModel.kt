package com.gkmicro.audiorecorderpro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.toLiveData

class RecordingListViewModel(app: Application): AndroidViewModel(app) {

    val dao = Utils().getDb(app).recordingDao()

    val recordings = Pager(
        PagingConfig(
            pageSize = 60,
            enablePlaceholders = true,
            maxSize = 200
        )
    ) {
        dao.recordingsByDate()
    }.flow
}