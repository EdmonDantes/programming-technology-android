package ru.loginov.chemistryapplication.adapter

import android.content.Context
import android.widget.ArrayAdapter

class EmptyAdapter(context: Context) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {

    override fun getCount(): Int = 0
}