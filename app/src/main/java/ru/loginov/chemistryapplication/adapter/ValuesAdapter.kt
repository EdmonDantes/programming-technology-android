package ru.loginov.chemistryapplication.adapter

import android.content.Context
import android.widget.ArrayAdapter
import ru.loginov.chemistryapplication.api.entity.ChemistryUnit

class ValuesAdapter(context: Context, values: Set<Pair<ChemistryUnit, Double>>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {

    val list = values.toList().sortedBy { it.second }

    override fun getItem(position: Int): String? = list[position].let { it.second.toString() + " " + it.first.name }

    override fun getCount(): Int = list.size
}