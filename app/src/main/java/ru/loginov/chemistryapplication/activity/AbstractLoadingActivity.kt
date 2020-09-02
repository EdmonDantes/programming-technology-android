package ru.loginov.chemistryapplication.activity

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.ChemistryApplication.TAG
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractLoadingActivity : AppCompatActivity() {

    private var _isUpdating = AtomicBoolean(false)
    private var future: Future<*>? = null

    protected val isUpdating
        get() = _isUpdating.get()

    /**
     * Run in UI thread
     */
    abstract fun onUpdate()

    /**
     * Run in UI thread
     */
    abstract fun onPreUpdate()

    abstract fun onPostUpdate()

    /**
     * Boolean param in unit is true, if update is success, else update is failed
     */
    fun update(unit: () -> Unit = {}) {
        _isUpdating.set(true)

        runOnUiThread {
            try {
                Log.i(TAG, "update: Start pre update function")
                onPreUpdate()
            } catch (e: Exception) {
                Log.w(TAG, "update: Can not invoke onPreUpdate()", e)
            }
        }

        future = ChemistryApplication.EXECUTOR.submit {

            try {
                Log.i(TAG, "update: Start update")

                onUpdate()
            } catch (e: Exception) {
                Log.w(TAG, "update: Can not invoke onUpdate()", e)
            }

            runOnUiThread {
                try {
                    Log.i(TAG, "update: Start post update function")

                    onPostUpdate()
                } catch (e: Exception) {
                    Log.w(TAG, "update: Can not invoke onPostUpdate()", e)
                }
            }

            try {
                Log.i(TAG, "update: Start callback function")

                unit.invoke()
            } catch (e: Exception) {
                Log.w(TAG, "update: Can not invoke final function unit()", e)
            }

            _isUpdating.set(false)
        }
    }

    protected fun stopUpdate() {

        if (future != null && isUpdating) {
            Log.i(TAG, "stopUpdate: Stop update")

            future!!.cancel(true)
        }
        _isUpdating.set(false)
    }

    override fun onStop() {
        stopUpdate()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        update()
    }
}