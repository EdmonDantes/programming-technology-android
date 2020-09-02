package ru.loginov.chemistryapplication.adapter

import android.content.Context
import android.widget.ArrayAdapter
import ru.loginov.chemistryapplication.api.entity.ChemistryUnit

class UnitAdapter(context: Context, var units: MutableList<ChemistryUnit>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {

    override fun getItem(position: Int): String? {
        return units[position].name
    }

    override fun getCount(): Int {
        return units.size
    }

    override fun getPosition(item: String?): Int {
        return units.indexOfFirst { it.name == item }
    }

    override fun getItemId(position: Int): Long {
        return units[position].id?.toLong() ?: -1
    }

    fun addUnit(unit: ChemistryUnit) {
        units.add(unit)
        notifyDataSetChanged()
    }
}