package com.layrin.smsclassification.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.layrin.smsclassification.R
import com.layrin.smsclassification.databinding.ActivityMainBinding
import com.layrin.smsclassification.ui.settings.SettingsFragment.Companion.DARK_THEME_KEY
import com.layrin.smsclassification.ui.settings.SettingsFragment.Companion.LIGHT_THEME_KEY
import com.layrin.smsclassification.ui.settings.SettingsFragment.Companion.SAME_AS_SYSTEM_KEY
import com.layrin.smsclassification.ui.settings.SettingsFragment.Companion.SELECTED_THEME
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    private val onDefaultAppResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (PackageManager.PERMISSION_DENIED in
                Array(PERMISSIONS_ARRAY.size) {
                    ActivityCompat.checkSelfPermission(this, PERMISSIONS_ARRAY[it])
                }
            ) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_ARRAY, 0)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setAppTheme()

        installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpNavController()
        setSupportActionBar(binding.toolbarMain)
        setupActionBarWithNavController(navController)
        setUpAppPermission()
    }

    private fun setAppTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        when (sharedPreferences.getString(SELECTED_THEME, SAME_AS_SYSTEM_KEY)) {
            LIGHT_THEME_KEY -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            DARK_THEME_KEY -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
            SAME_AS_SYSTEM_KEY -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }
    }

    private fun setUpAppPermission() {
        when {
            packageName != Telephony.Sms.getDefaultSmsPackage(this) -> {
                val intent = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    val roleManager = getSystemService(RoleManager::class.java)
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                } else {
                    val setSmsAppIntent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                    setSmsAppIntent
                }
                onDefaultAppResult.launch(intent)
            }
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_DENIED -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    0
                )
            }
        }
    }

    private fun setUpNavController() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.fragmentContainerView.id) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.startScreenFragment -> {
                    binding.appBarLayout.visibility = View.GONE
                }
                R.id.conversationFragment, R.id.contactFragment, R.id.messageFragment, R.id.settingsFragment -> {
                    binding.appBarLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.contains(PackageManager.PERMISSION_DENIED)) {
            finish()
            return
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        val PERMISSIONS_ARRAY = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    }

}