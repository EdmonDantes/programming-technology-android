package ru.loginov.chemistryapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.api.entity.ChemistryMetric
import java.util.*

class ChooseMetricAdapter(context: Context, var metrics: List<ChemistryMetric>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val metricsCheckBox: MutableMap<String, CheckBox> = TreeMap()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? = (convertView ?: inflater.inflate(R.layout.layout_adapter_choose_metric_activity, parent, false))?.
        also {
            it.findViewById<TextView>(android.R.id.text1).text = metrics[position].name
            it.findViewById<TextView>(android.R.id.text2).text = metrics[position].description

            val checkBox = it.findViewById<CheckBox>(R.id.checkbox)

            metricsCheckBox[metrics[position].tag]?.also {
                checkBox.isChecked = it.isChecked
            }

            metricsCheckBox[metrics[position].tag] = checkBox

            it.setOnClickListener { checkBox.performClick() }
        }

    override fun getItem(position: Int): ChemistryMetric {
        return metrics[position];
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    override fun getCount(): Int {
        return metrics.size
    }

    fun getCheckedMetrics(): List<ChemistryMetric> {
        return metrics.filter { metricsCheckBox[it.tag]?.isChecked ?: false}
    }
}