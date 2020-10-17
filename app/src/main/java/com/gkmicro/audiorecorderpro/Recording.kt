package com.gkmicro.audiorecorderpro
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Recording (
    @PrimaryKey val unixDateMillis: Long,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "length_secs") var length_secs: Long?,
    @ColumnInfo(name = "extension") val extension: String
) : Parcelable