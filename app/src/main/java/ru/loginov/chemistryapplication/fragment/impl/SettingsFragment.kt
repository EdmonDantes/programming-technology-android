package ru.loginov.chemistryapplication.fragment.impl

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import ru.loginov.chemistryapplication.ChemistryApplication
import ru.loginov.chemistryapplication.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_hierarchy, rootKey)

        findPreference<EditTextPreference>("server_port")?.also {
            it.setOnBindEditTextListener {
                it.inputType == InputType.TYPE_CLASS_NUMBER
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(ChemistryApplication.LISTENER)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(ChemistryApplication.LISTENER)
    }
}