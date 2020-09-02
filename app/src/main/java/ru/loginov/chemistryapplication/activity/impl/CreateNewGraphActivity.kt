package ru.loginov.chemistryapplication.activity.impl

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_create_new_graph_activity.*
import org.apache.commons.io.IOUtils
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.activity.AbstractRequestActivity
import ru.loginov.chemistryapplication.adapter.ValuesAdapter
import ru.loginov.chemistryapplication.api.entity.ChemistryUnit
import java.io.StringWriter
import java.nio.charset.StandardCharsets

class CreateNewGraphActivity : AbstractRequestActivity() {

    companion object {
        private const val REQUEST_CODE_ADD_VALUE = 1
    }

    private val values = HashMap<Pair<ChemistryUnit, Double>, MutableList<Uri>>()

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_create_new_graph_activity)

        this.layoutInflater.inflate(R.layout.footer_create_new_graph_activity_list_view_values, null)?.also {
            it.setOnClickListener {
                startActivityForAddValue();
            }
            list_view_values.addFooterView(it)
        }

        list_view_values.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emptyArray())
    }

    fun startCreateGraph(view: View) {
        if (values.size < 2) {
            Toast.makeText(this, "Please add at least 2 values", Toast.LENGTH_SHORT).show()
            return;
        }

        if (edit_text_name.text?.isEmpty() == true) {
            Toast.makeText(this, "Please write name", Toast.LENGTH_SHORT).show()
        }

        createRequest(0) {
            runOnUiThread {
                if (it) {
                    setResult(Activity.RESULT_OK)
                } else {
                    setResult(Activity.RESULT_CANCELED)
                }
                finish()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_ADD_VALUE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val unit = data?.getParcelableExtra<ChemistryUnit>(AddGraphValueActivity.KEY_UNIT)
                    val value = data?.getDoubleExtra(AddGraphValueActivity.KEY_DOUBLE_VALUE, 0.0)
                    val colorsUri = data?.getParcelableExtra<Uri>(AddGraphValueActivity.KEY_COLOR_URI)

                    if (unit != null && value != null && colorsUri != null) {
                        val list = values[unit to value]
                        if (list == null) {
                            values[unit to value] = ArrayList()
                            list_view_values.adapter = ValuesAdapter(this, values.keys)
                        }

                        values[unit to value]?.add(colorsUri)
                    }
                }
            }
        }
    }

    private fun startActivityForAddValue() {
        startActivityForResult(Intent(this, AddGraphValueActivity::class.java), REQUEST_CODE_ADD_VALUE)
    }

    override fun onRequest(index: Int): Boolean {
        runOnUiThread {
            dialog = MaterialAlertDialogBuilder(this).setTitle("Loading").setView(R.layout.layout_choose_color_activity_progress_dialog).setCancelable(false).show()
        }

        val name = edit_text_name.text.toString()

        val response = ChemistryApplication.API.saveGraph(name, values.toList().map {
            val str = it.second.let { uriList ->
                StringWriter().also { writer ->
                    uriList.forEach {
                        IOUtils.copy(contentResolver.openInputStream(it), writer, StandardCharsets.UTF_8)
                    }
                }.toString()
            }

            return@map it.first to str
        })

        runOnUiThread {
            dialog?.dismiss()
        }

        return response.canGetResult()
    }
}