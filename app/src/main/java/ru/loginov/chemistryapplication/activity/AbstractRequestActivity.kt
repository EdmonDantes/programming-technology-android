package ru.loginov.chemistryapplication.activity

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.ChemistryApplication.TAG
import java.util.concurrent.Future

abstract class AbstractRequestActivity : AppCompatActivity() {

    private var futures: MutableList<Future<*>?> = ArrayList()

    abstract fun onRequest(index: Int) : Boolean

    fun createRequest(index: Int, callback: (Boolean) -> Unit = {}) : Boolean {

        while (index >= futures.size) {
            futures.add(null)
        }


        synchronized(futures) {
            val future = futures[index]
            if (future == null) {
                futures[index] = ChemistryApplication.EXECUTOR.submit {
                    var requestResult: Boolean

                    try {
                        requestResult = onRequest(index)
                    } catch (e: Exception) {
                        Log.w(TAG, "createRequest: Can not invoke onRequest()", e)
                        requestResult = false
                    }

                    try {
                        callback.invoke(requestResult)
                    } catch (e: Exception) {
                        Log.w(TAG, "createRequest: Can not invoke callback", e)
                    }

                    synchronized(futures) {
                        futures[index] = null
                    }
                }
            }
        }

        return false;
    }

    fun stopRequest() {
        Log.i(TAG, "stopRequest: Stop async request")

        val exception = Exception("Can not cancel all futures")

        futures.forEach { try { it?.cancel(true) } catch (e: Exception) {exception.addSuppressed(e)} }

        if (exception.suppressed.isNotEmpty()) {
            Log.e(TAG, "stopRequest: Can not cancel all futures", exception)
        }
    }

    override fun onStop() {
        stopRequest()
        super.onStop()
    }
}