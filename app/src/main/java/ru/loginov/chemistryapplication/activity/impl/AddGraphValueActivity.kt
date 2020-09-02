package ru.loginov.chemistryapplication.activity.impl

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_add_graph_value_activity.*
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.api.entity.ChemistryUnit

class AddGraphValueActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_UNIT = 1
        private const val REQUEST_CODE_COLOR_FILE = 2

        const val KEY_UNIT = "unit"
        const val KEY_COLOR_URI = "colorUri"
        const val KEY_DOUBLE_VALUE = "value"
    }

    private var unit: ChemistryUnit? = null
    private var color: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_add_graph_value_activity)
    }

    fun startSetUnit(view: View) {
        startActivityForResult(Intent(this, UnitsActivity::class.java), REQUEST_CODE_UNIT)
    }

    fun startSetColor(view: View) {
        startActivityForResult(Intent(this, ChooseImageActivity::class.java), REQUEST_CODE_COLOR_FILE)
    }

    fun startReturnResult(view: View) {
        if (unit == null) {
            Toast.makeText(this, "Please, chose unit", Toast.LENGTH_SHORT).show()
            return
        }

        if (color == null) {
            Toast.makeText(this, "Please, choose color", Toast.LENGTH_SHORT).show()
            return
        }

        val number = edit_text_number_value.text.toString().toDoubleOrNull()
        if (number == null) {
            Toast.makeText(this, "Please, write value", Toast.LENGTH_SHORT).show();
            return
        }

        setResult(Activity.RESULT_OK, Intent().apply { putExtra(KEY_UNIT, unit!!); putExtra(KEY_COLOR_URI, color!!); putExtra(KEY_DOUBLE_VALUE, number) })
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_UNIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    text_view_unit_layout.visibility = View.VISIBLE
                    unit = data?.getParcelableExtra(UnitsActivity.KEY_UNIT)
                    updateUnitView()
                }
            }
            REQUEST_CODE_COLOR_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    color = data?.getParcelableExtra(ChooseColorsActivity.KEY_COLOR_URI)
                    updateColorView()
                }
            }
        }
    }

    private fun updateUnitView() {
        if (unit == null) {
            text_view_unit_layout.visibility = View.GONE
        } else {
            text_view_unit_layout.visibility = View.VISIBLE
            text_view_unit.setText(unit!!.name)
        }
    }

    private fun updateColorView() {
        if (color == null) {
            button_set_color.setText(R.string.title_in_add_graph_value_activity_button_set_color)
        } else {
            button_set_color.setText(R.string.title_in_add_graph_value_activity_button_set_color_update_state)
        }
    }
}