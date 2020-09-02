package ru.loginov.chemistryapplication.activity.impl

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import kotlinx.android.synthetic.main.layout_choose_graph_activity.*
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.ChemistryApplication.API
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.activity.AbstractLoadingActivity
import ru.loginov.chemistryapplication.adapter.ChooseMetricAdapter

class ChooseMetricsActivity : AbstractLoadingActivity() {

    companion object {
        const val GRAPH_ID_KEY = "graphId"
        const val REQUEST_CODE_FOR_GET_COLORS = 1
    }

    private var graphId: Long = -1

    private val tags: MutableSet<String> = HashSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_choose_metric_activity)

        tags.clear()

        graphId = intent.getLongExtra(GRAPH_ID_KEY, -1)

        swipe_refresh_layout.setOnRefreshListener {
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
        val response = API.getAllMetrics()

        if (response.canGetResult(this)) {
            val res = response.result!!

            val toAdapter = res.filter { !it.isCollector }
            runOnUiThread {
                if (graphs_list_view.adapter != null && graphs_list_view.adapter is ChooseMetricAdapter) {
                    (graphs_list_view.adapter as ChooseMetricAdapter).also {
                        it.metrics = toAdapter
                        it.notifyDataSetChanged()
                    }
                } else {
                    graphs_list_view.adapter = ChooseMetricAdapter(this, toAdapter)
                }
            }
        }
    }

    fun onChooseMutators(view: View) {
        if (isUpdating) {
            return
        }

        if (graphs_list_view.adapter != null && graphs_list_view.adapter is ChooseMetricAdapter) {
            val metrics = (graphs_list_view.adapter as ChooseMetricAdapter).getCheckedMetrics().map { it.tag }
            if (metrics.isNotEmpty()) {
                startActivityForResult(Intent(this, ChooseImageActivity::class.java), REQUEST_CODE_FOR_GET_COLORS)
            } else {
                makeText(this, "You have chosen nothing", LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_FOR_GET_COLORS) {
            if (resultCode == Activity.RESULT_OK) {
                val colors = data?.getParcelableExtra<Uri>(ChooseColorsActivity.KEY_COLOR_URI)
                if (colors == null) {
                    makeText(this, "Can not get colors", LENGTH_SHORT).show()
                    return
                }

                startActivity(Intent(this, GetResultActivity::class.java).apply {
                    putExtra(GetResultActivity.GRAPH_ID_KEY, graphId)
                    putExtra(GetResultActivity.MUTATORS_KEY, (graphs_list_view.adapter as ChooseMetricAdapter).getCheckedMetrics().map { it.tag }.toTypedArray())
                    putExtra(GetResultActivity.COLORS_KEY, colors)
                })
            } else {
                Log.w(ChemistryApplication.TAG, "onActivityResult: Can not get colors")
                makeText(this, "Can not get colors", LENGTH_SHORT).show()
            }
        }
    }
}