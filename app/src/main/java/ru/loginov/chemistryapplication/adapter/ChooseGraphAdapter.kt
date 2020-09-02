package ru.loginov.chemistryapplication.adapter

import android.content.Context
import android.widget.ArrayAdapter
import ru.loginov.chemistryapplication.api.entity.ChemistryGraph

class ChooseGraphAdapter(context: Context, var graphs: MutableList<ChemistryGraph>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {

    override fun getItem(position: Int): String? {
        return graphs[position].name
    }

    override fun getCount(): Int {
        return graphs.size
    }

    override fun getPosition(item: String?): Int {
        return graphs.indexOfFirst { it.name == item }
    }

    override fun getItemId(position: Int): Long {
        return graphs[position].id.toLong()
    }

    fun addGraph(graph: ChemistryGraph) {
        graphs.add(graph)
        notifyDataSetChanged()
    }
}