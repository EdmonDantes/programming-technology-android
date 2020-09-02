package ru.loginov.chemistryapplication.activity.impl

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_settings_activity.*
import ru.loginov.chemistryapplication.R
import ru.loginov.chemistryapplication.fragment.impl.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_settings_activity)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        supportFragmentManager.beginTransaction().replace(R.id.settings_container, SettingsFragment()).commit()
    }


}