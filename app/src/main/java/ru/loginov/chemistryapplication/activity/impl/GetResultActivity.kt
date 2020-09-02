package ru.loginov.chemistryapplication.activity.impl

import android.net.Uri
import android.os.Bundle
import android.widget.SimpleAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_get_result_activity.*
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomStringUtils
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.activity.AbstractRequestActivity
import ru.loginov.chemistryapplication.api.entity.ShortMetricDescription
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.*

class GetResultActivity : AbstractRequestActivity() {

    companion object {
        const val GRAPH_ID_KEY = "graphId"
        const val MUTATORS_KEY = "mutators"
        const val COLORS_KEY = "colors"

        private const val FIRST_MUTATOR_TAG = "collector_first"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_get_result_activity)

        swipe_refresh_layout.setOnRefreshListener {
            createRequest(0)
        }

        createRequest(0)
    }

    override fun onRequest(index: Int): Boolean {

        runOnUiThread {
            swipe_refresh_layout.isRefreshing = true
        }

        val graphId = intent.getLongExtra(GRAPH_ID_KEY, -1)
        val mutators = intent.getStringArrayExtra(MUTATORS_KEY)
        val colors = intent.getParcelableExtra<Uri>(COLORS_KEY)

        var ret = false;

        if (graphId > 0 && mutators != null && colors != null) {
            val metrics = LinkedList<ShortMetricDescription>()
            mutators.forEach {
                val name = RandomStringUtils.randomAlphabetic(10)
                metrics.add(ShortMetricDescription(name, it))
                metrics.add(ShortMetricDescription(RandomStringUtils.randomAlphabetic(10), FIRST_MUTATOR_TAG, listOf(name)))
            }

            if (colors.path == null) {
                Toast.makeText(this, "Can not get colors data", Toast.LENGTH_SHORT).show()
            }

            StringWriter().also {
                IOUtils.copy(contentResolver.openInputStream(colors), it, StandardCharsets.UTF_8)
                ret = it.toString().let { colorsString ->
                    val response = ChemistryApplication.API.getValueFromMetric(graphId, metrics, colorsString)

                    if (response.canGetResult(this)) {
                        val result = response.result

                        if (result != null && result.isNotEmpty()) {
                            runOnUiThread {
                                result_list_view.adapter = SimpleAdapter(this, result.filter { it.value.second != null }.map { mapOf("name" to it.key, "description" to "${it.value.second} ${it.value.first.name}") }, android.R.layout.simple_list_item_2, arrayOf("name", "description"), intArrayOf(android.R.id.text1, android.R.id.text2))
                                result_list_view.visibility
                            }
                            true
                        } else {
                            Toast.makeText(this, "Can not get results", Toast.LENGTH_SHORT).show()
                            false
                        }
                    } else {
                        false
                    }
                }
            }
        } else {
            Toast.makeText(this, "Can not create \"get results\" request. Wrong arguments", Toast.LENGTH_SHORT).show()
        }

        runOnUiThread {
            swipe_refresh_layout.isRefreshing = false
        }

        return ret;
    }
}