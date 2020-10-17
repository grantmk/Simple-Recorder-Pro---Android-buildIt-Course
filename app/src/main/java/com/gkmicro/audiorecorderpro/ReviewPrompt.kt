package com.gkmicro.audiorecorderpro

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReviewPrompt (private val context: Context) {

    private val review_prompted_key = "prompted"
    private val num_app_opened = "numopened"

    fun promptForReview () {
        var numTimesOpened = numTimesAppOpened()
        var reviewAlreadyPrompted = reviewAlreadyPrompted()
        Log.d("ReviewPrompt", "$numTimesOpened $reviewAlreadyPrompted")
        if (!reviewAlreadyPrompted && numTimesOpened > 2) {
            showReviewPrompt()
        } else {
            addOneToOpens(numTimesOpened)
        }
    }

    private fun addOneToOpens (numTimes: Int) {
        Storage(context).writePref(num_app_opened, numTimes + 1)
    }

    private fun setReviewPrompted () {
        Storage(context).writePref(review_prompted_key, true)
    }

    private fun showReviewPrompt () {

        //show "do like this?"
        AlertDialog.Builder(context)
            .setTitle("Message")
            .setMessage("Are You Enjoying this App?")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    showReviewDialog()
                    setReviewPrompted()
                })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    setReviewPrompted()
                }).show()
    }

    private fun showReviewDialog () {
        Log.d("ReviewPrompt", "showing review dialog")
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
            } else {
                // There was some problem, continue regardless of the result.
            }
        }
    }

    private fun reviewAlreadyPrompted (): Boolean {
        return Storage(context).getBoolPref(review_prompted_key)
    }

    private fun numTimesAppOpened (): Int {
        return Storage(context).getIntPref(num_app_opened)
    }
}