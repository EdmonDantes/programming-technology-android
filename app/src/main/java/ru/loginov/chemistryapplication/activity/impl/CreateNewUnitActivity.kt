package ru.loginov.chemistryapplication.activity.impl

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_create_new_unit_activity.*
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.activity.AbstractRequestActivity
import ru.loginov.chemistryapplication.api.entity.ChemistrySaveUnitRequest
import ru.loginov.chemistryapplication.api.entity.ChemistryScaleUnit
import ru.loginov.chemistryapplication.api.entity.ChemistryUnit

class CreateNewUnitActivity : AbstractRequestActivity() {

    companion object {
        const val KEY_CHEMISTRY_UNIT = "unit"
    }

    private var units : List<ChemistryUnit>? = null
    private val resultUnits: Array<ChemistryUnit?> = Array(2) { null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_create_new_unit_activity)
        createRequest(0) {
            if (!it) {
                Toast.makeText(this, "Can not get units list from server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequest(index: Int): Boolean {
        when (index) {
            0 -> {
                val response = ChemistryApplication.API.getAllUnits()

                if (response.canGetResult(this)) {
                    units = response.result
                    return true
                }
            }
            1 -> {
                val name = edit_text_name.text.toString()

                if (name.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this, "Please write name", Toast.LENGTH_SHORT).show()
                    }
                    return false
                }

                val high = resultUnits[0]?.id?.let {
                    val scale = edit_text_high_scale.text.toString().toDoubleOrNull()
                    if (scale == null) {
                        runOnUiThread {
                            Toast.makeText(this, "Please write high scale value", Toast.LENGTH_SHORT).show()
                        }
                        return false
                    }

                    ChemistryScaleUnit(it, scale)
                }

                val less = resultUnits[1]?.id?.let {
                    val scale = edit_text_low_scale.text.toString().toDoubleOrNull()
                    if (scale == null) {
                        runOnUiThread {
                            Toast.makeText(this, "Please write low scale value", Toast.LENGTH_SHORT).show()
                        }
                        return false
                    }

                    ChemistryScaleUnit(it, scale)
                }

                val response = ChemistryApplication.API.createUnit(ChemistrySaveUnitRequest(name, high, less));

                if (response.canGetResult(this)) {
                    setResult(RESULT_OK, Intent().apply { putExtra(KEY_CHEMISTRY_UNIT, response.result) })
                    return true
                }
            }
        }

        return false
    }

    fun startSetHighUnit(view: View) {
        Log.i(ChemistryApplication.TAG, "startSetHighUnit: sasaf")
        chooseUnit(edit_text_high_scale_layout, button_set_high_unit, R.string.string_create_new_unit_activity_button_add_high_unit_title, 0)
    }

    fun startSetLowUnit(view: View) = chooseUnit(edit_text_log_scale_layout, button_set_low_unit, R.string.string_create_new_unit_activity_button_add_low_unit_title, 1)

    private fun chooseUnit(scaleView: View, button: Button, stringId: Int, arrayIndex: Int) {
        showUnitsDialog { index, list ->
            runOnUiThread {
                val unit = if (index < list.size) list[index] else null

                if (unit?.name == null) {
                    scaleView.visibility = View.GONE
                    button.setText(stringId)
                    resultUnits[arrayIndex] = null
                } else {
                    scaleView.visibility = View.VISIBLE
                    button.text = unit.name
                    resultUnits[arrayIndex] = unit
                }
            }
        }
    }

    private fun showUnitsDialog(callback: (index: Int, list: List<ChemistryUnit?>) -> Unit) : Boolean {
        units?.also {
            val list = it.plus(null)
            val array = list.map { it?.name ?: "Without unit" }.toTypedArray()
            MaterialAlertDialogBuilder(this)
                    .setTitle("Units")
                    .setItems(array) { dialog, index -> callback(index, list) }
                    .show()
            return true
        }

        return false
    }

    fun startSaveUnit(view: View) {
        createRequest(1) {
            if (it) {
                finish()
            }
        }
    }

}