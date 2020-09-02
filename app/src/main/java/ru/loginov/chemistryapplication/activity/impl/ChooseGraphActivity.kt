package ru.loginov.chemistryapplication.activity.impl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_choose_graph_activity.*
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.activity.AbstractLoadingActivity
import ru.loginov.chemistryapplication.adapter.ChooseGraphAdapter
import ru.loginov.chemistryapplication.adapter.EmptyAdapter

class ChooseGraphActivity : AbstractLoadingActivity(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    companion object {
        const val REQUEST_CODE_CREATE_GRAPH = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_choose_graph_activity)

        graphs_list_view.onItemClickListener = this
        graphs_list_view.onItemLongClickListener = this

        swipe_refresh_layout.setOnRefreshListener {
            graphs_list_view.adapter = EmptyAdapter(this)
            update()
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

    override fun onUpdate() {
        val response = ChemistryApplication.API.getAllGraph()

        if (response.canGetResult(this)) {
            val res = response.result!!.toMutableList()

            runOnUiThread {
                if (graphs_list_view.adapter != null && graphs_list_view.adapter is ChooseGraphAdapter) {
                    (graphs_list_view.adapter as ChooseGraphAdapter).also {
                        it.graphs = res
                        it.notifyDataSetChanged()
                    }
                } else {
                    graphs_list_view.adapter = ChooseGraphAdapter(this, res)
                }
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (isUpdating) {
            return
        }

        parent?.adapter?.also {
            val graphId = it.getItemId(position);
            Intent(this, ChooseMetricsActivity::class.java).also {
                it.putExtra(ChooseMetricsActivity.GRAPH_ID_KEY, graphId)
                startActivity(it)
            }
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
        if (isUpdating) {
            return false;
        }

        parent?.adapter?.getItemId(position)?.also { graphId ->
            MaterialAlertDialogBuilder(this)
                    .setTitle("Menu")
                    .setItems(arrayOf("Delete")) {_, index ->
                        if (index == 0) {
                            ChemistryApplication.EXECUTOR.submit {
                                ChemistryApplication.API.removeGraph(graphId)
                                update()
                            }
                        }
                    }.show()
            return true
        }



        return false;
    }

    fun onCreateGraph(view: View?) {
        if (isUpdating) {
            return
        }

        startActivityForResult(Intent(this, CreateNewGraphActivity::class.java), REQUEST_CODE_CREATE_GRAPH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CREATE_GRAPH) {
            if (resultCode == Activity.RESULT_OK) {
                update()
            }
        }
    }

    fun startSettings(item: MenuItem) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }


}