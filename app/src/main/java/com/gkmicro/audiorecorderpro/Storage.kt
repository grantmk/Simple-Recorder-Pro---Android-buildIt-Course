package com.gkmicro.audiorecorderpro

import android.content.Context
import android.content.SharedPreferences

class Storage (private val context : Context) {

    fun getIntPref (key: String): Int {
        return getSharedPrefs().getInt(key, 0)
    }

    fun getBoolPref (key: String): Boolean {
        return getSharedPrefs().getBoolean(key, false)
    }

    fun writePref (key: String, value: Int) {
        with (getSharedPrefs().edit()) {
            putInt(key, value)
            commit()
        }
    }

    fun writePref (key: String, value: Boolean) {
        with (getSharedPrefs().edit()) {
            putBoolean(key, value)
            commit()
        }
    }

    private fun getSharedPrefs (): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
}