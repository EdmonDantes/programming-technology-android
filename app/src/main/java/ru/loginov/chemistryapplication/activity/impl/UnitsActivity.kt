package ru.loginov.chemistryapplication.activity.impl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_units_activity.*
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.activity.AbstractLoadingActivity
import ru.loginov.chemistryapplication.adapter.UnitAdapter
import ru.loginov.chemistryapplication.api.entity.ChemistryUnit

class UnitsActivity : AbstractLoadingActivity() {

    companion object {
        const val KEY_UNIT = "unitId"

        const val REQUEST_CODE_CREATE_NEW_UNIT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_units_activity)

        swipe_refresh_layout.setOnRefreshListener {
            update() {
                runOnUiThread {
                    swipe_refresh_layout.isRefreshing = false
                }
            }
        }

        list_view_units.setOnItemClickListener { adapterView, view, position: Int, id: Long ->
            if (isUpdating) {
                return@setOnItemClickListener
            }

            (adapterView.adapter as? UnitAdapter)?.also {adapter ->
                setResult(Activity.RESULT_OK, Intent().apply { putExtra(KEY_UNIT, adapter.units[position]) })
                finish()
            }
        }

       list_view_units.setOnItemLongClickListener { parent, view, position, id ->
           if (isUpdating) {
               false;
           } else {
                if (parent?.adapter is UnitAdapter) {
                    (parent.adapter as? UnitAdapter)?.let {
                        val unit = it.units[position]
                        unit.id?.let { id ->

                            MaterialAlertDialogBuilder(this)
                                    .setTitle("Menu")
                                    .setItems(arrayOf("Delete")) {_, _ ->
                                        ChemistryApplication.EXECUTOR.submit {
                                            val response = ChemistryApplication.API.removeUnit(id)
                                            if (response.needForce) {
                                                runOnUiThread {
                                                    MaterialAlertDialogBuilder(this).setTitle("Warning")
                                                            .setCancelable(false)
                                                            .setMessage(R.string.string_units_activity_dialog_delete_warning_message)
                                                            .setPositiveButton(R.string.string_units_activity_dialog_delete_warning_positive_button_title) { d, _ ->
                                                                ChemistryApplication.EXECUTOR.submit {
                                                                    val result = ChemistryApplication.API.removeUnit(id, true)
                                                                    result.canGetResult()
                                                                    d.dismiss()
                                                                    update()
                                                                }
                                                            }.setNegativeButton(R.string.string_units_activity_dialog_delete_warning_negative_button_title) { d, _ ->
                                                                d.dismiss()
                                                                update()
                                                            }.show()
                                                }
                                            } else {
                                                update()
                                            }
                                        }
                                    }.show()

                            true
                        }  ?: false
                    } ?: false
                } else {
                    false
                }
           }
       }

        update()
    }

    override fun onUpdate() {
        val response = ChemistryApplication.API.getAllUnits()

        if (response.canGetResult(this)) {
            response.result!!.toMutableList().also {
                runOnUiThread {
                    if (list_view_units.adapter != null && list_view_units.adapter is UnitAdapter) {
                        (list_view_units.adapter as UnitAdapter).units = it
                        (list_view_units.adapter as UnitAdapter).notifyDataSetChanged()
                    } else {
                        list_view_units.adapter = UnitAdapter(this, it)
                    }
                }
            }
        }
    }

    override fun onPreUpdate() {
        runOnUiThread {
            swipe_refresh_layout.isRefreshing = true
        }
    }

    override fun onPostUpdate() {
        runOnUiThread {
            swipe_refresh_layout.isRefreshing = false
        }
    }

    fun onCreateUnit(view: View) {
        if (isUpdating) {
            Toast.makeText(this, "Wait for end of updating", Toast.LENGTH_SHORT).show()
            return
        }

        startActivityForResult(Intent(this, CreateNewUnitActivity::class.java), REQUEST_CODE_CREATE_NEW_UNIT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CREATE_NEW_UNIT && resultCode == RESULT_OK) {
            val unit = data?.getParcelableExtra<ChemistryUnit>(CreateNewUnitActivity.KEY_CHEMISTRY_UNIT)

            unit?.also {
                if (list_view_units.adapter != null && list_view_units.adapter is UnitAdapter) {
                    (list_view_units.adapter as UnitAdapter).addUnit(it)
                } else {
                    update()
                }
            }
        }
    }
}