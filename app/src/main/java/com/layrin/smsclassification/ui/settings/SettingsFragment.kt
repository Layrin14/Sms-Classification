package com.layrin.smsclassification.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.layrin.smsclassification.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment :
    PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

        // swipe preference
        val swipeSwitch = findPreference<SwitchPreference>(SWIPE_MODE_KEY)

        swipeSwitch?.setOnPreferenceChangeListener { preference, checked ->
            preference.sharedPreferences?.edit()
                ?.putBoolean(ENABLE_SWIPE, checked as Boolean)
                ?.apply()
            true
        }

        // theme preference
        val themeList = findPreference<ListPreference>(THEME_KEY)
        themeList?.apply {
            when (preferenceManager.sharedPreferences?.getString(SELECTED_THEME,
                SAME_AS_SYSTEM_KEY)) {
                LIGHT_THEME_KEY -> setIcon(R.drawable.ic_light_mode)
                DARK_THEME_KEY -> setIcon(R.drawable.ic_dark_mode)
                SAME_AS_SYSTEM_KEY -> setIcon(R.drawable.ic_same_as_system)
            }
        }
        themeList?.setOnPreferenceChangeListener { preference, selectedTheme ->
            val icon = setTheme(selectedTheme.toString())
            preference.setIcon(icon)
            preference.sharedPreferences?.edit()
                ?.putString(SELECTED_THEME, selectedTheme.toString())
                ?.apply()
            true
        }
    }

    private fun setTheme(key: String): Int {
        return when (key) {
            LIGHT_THEME_KEY -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.drawable.ic_light_mode
            }
            DARK_THEME_KEY -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                R.drawable.ic_dark_mode
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                R.drawable.ic_same_as_system
            }
        }
    }

    companion object {
        const val THEME_KEY = "action_theme"
        const val LIGHT_THEME_KEY = "action_light"
        const val DARK_THEME_KEY = "action_dark"
        const val SAME_AS_SYSTEM_KEY = "action_system"
        private const val SWIPE_MODE_KEY = "action_swipe"
        const val ENABLE_SWIPE = "swipe"
        const val SELECTED_THEME = "theme"
    }
}