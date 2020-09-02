package ru.loginov.chemistryapplication.api

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.R

data class Response<T>(val status: Boolean, val result: T?, val errorMessageId: Int? = null, val serverErrorMessage: String? = null, val exception: Exception? = null, val needForce: Boolean = false) {

    fun canGetResult() = status && result != null

    fun createErrorDialog(context: Context, buttonCallback: () -> Unit = {}) {
        if (!status) {
            val builder = MaterialAlertDialogBuilder(context).apply {
                setCancelable(false)
                setTitle(R.string.api_error_dialog_title)
                setPositiveButton(R.string.api_error_dialog_ok_button) { dialog, _ ->
                    dialog.dismiss()
                    buttonCallback.invoke()
                }
            }
            if (errorMessageId != null) {
                builder.setMessage(errorMessageId)
            } else {
                builder.setMessage(R.string.api_error_dialog_message)
            }

            Handler(context.mainLooper).post {
                builder.show()
            }
        }
    }

    fun logErrorMessage(resources: Resources) {
        if (!status) {
            (serverErrorMessage ?: errorMessageId?.let { resources.getString(it) })?.also { message ->
                exception?.also {
                    Log.e(ChemistryApplication.TAG, message, it)
                } ?: Log.e(ChemistryApplication.TAG, message)
            }
        }
    }

    fun canGetResult(context: Context, buttonCallback: () -> Unit = {}) : Boolean {

        return if (status) {
            if (result != null) {
                true
            } else {
                createErrorDialog(context, buttonCallback)
                Log.w(ChemistryApplication.TAG, "Can not get result")
                false
            }
        } else {
            logErrorMessage(context.resources)
            createErrorDialog(context)
            false
        }
    }

}