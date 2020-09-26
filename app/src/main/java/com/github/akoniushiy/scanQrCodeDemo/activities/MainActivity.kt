package com.github.akoniushiy.scanQrCodeDemo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import com.github.akoniushiy.scanQrCodeDemo.R

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        appBarConfiguration =
            AppBarConfiguration(HashSet<Int>())

        setupActionBarWithNavController(
            this,
            getNavController(),
            appBarConfiguration
        )


    }

    private fun getNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = getNavController()
        val result = navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
        if (!result && !isTopLevelDestination(
                navController,
                appBarConfiguration
            )
        ) {
            super.onBackPressed()
        }
        return result
    }

    private fun isTopLevelDestination(
        navController: NavController,
        configuration: AppBarConfiguration
    ): Boolean {
        return matchDestinations(
            navController.currentDestination!!,
            configuration.topLevelDestinations
        )
    }

    private fun matchDestinations(
        destination: NavDestination,
        destinationIds: Set<Int?>
    ): Boolean {
        var currentDestination: NavDestination? = destination
        do {
            if (destinationIds.contains(currentDestination!!.id)) {
                return true
            }
            currentDestination = currentDestination.parent
        } while (currentDestination != null)
        return false
    }

    override fun onBackPressed() {
        if (!isTopLevelDestination(getNavController(), appBarConfiguration))
            super.onBackPressed()
    }

    override fun finish() {
        super.finish()
        ActivityNavigator.applyPopAnimationsToPendingTransition(this)
    }
}