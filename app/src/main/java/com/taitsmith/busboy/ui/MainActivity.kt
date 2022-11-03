package com.taitsmith.busboy.ui

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.databinding.ActivityMainBinding
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableErrorMessage
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableStatusMessage
import com.taitsmith.busboy.viewmodels.NearbyViewModel
import dagger.hilt.android.AndroidEntryPoint
import im.delight.android.location.SimpleLocation

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private var nearbyStatusUpdateTv: TextView? = null
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        acTransitApiKey = getString(R.string.ac_transit_key)
        mainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        bottomNavigationView = binding.mainTabLayout
        nearbyStatusUpdateTv = binding.nearbyStatusUpdater
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        setObservers()
        setTabListeners()
    }

    private fun setTabListeners() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.byId       -> navController.navigate(R.id.byIdFragment)
                R.id.nearby     -> navController.navigate(R.id.nearbyFragment)
                R.id.favorites  -> navController.navigate(R.id.favoritesFragment)
                R.id.help       -> showHelp()
            }
            true
        }
    }

    private fun setObservers() {
        mutableStatusMessage.observe(this) { s: String -> getStatusMessage(s) }
        mutableErrorMessage.observe(this) { s: String -> getErrorMessage(s) }
        mutableNearbyStatusUpdater.observe(this) { s -> updateNearbyStatusText(s) }
    }

    private fun getErrorMessage(s: String) {
        hideUi(false)
        when (s) {
            "NO_LOC_ENABLED" -> askToEnableLoc()
            "NO_PERMISSION" -> ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION)
            "404" -> showSnackbar(R.string.snackbar_404)
            "BAD_INPUT" -> showSnackbar(R.string.snackbar_bad_input)
            "NULL_PRED_RESPONSE" -> showSnackbar(R.string.snackbar_no_predictions)
            "NULL_BUS_COORDS" -> showSnackbar(R.string.snackbar_null_bus_coords)
            "CALL_FAILURE" -> showSnackbar(R.string.snackbar_network_error)
            "BAD_DISTANCE" -> showSnackbar(R.string.snackbar_bad_distance)
            "NO_FAVORITE_STOPS" -> showSnackbar(R.string.snackbar_no_favorites)
        }
    }

    private fun showSnackbar(message: Int) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun getStatusMessage(s: String) {
        when (s) {
            "HELP_REQUESTED" -> showHelp()
            "FAVORITE_ADDED" -> showSnackbar(R.string.snackbar_favorite_added)
            "STOP_DELETED" -> showSnackbar(R.string.snackbar_favorite_deleted)
            "LOADING" -> hideUi(true)
            "LOADED" -> hideUi(false)
        }
    }

    private fun hideUi(shouldHide: Boolean) {
        if (shouldHide) {
            binding.navHostFragment.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.navHostFragment.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
            nearbyStatusUpdateTv!!.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mutableStatusMessage.removeObservers(this)
        mutableErrorMessage.removeObservers(this)
        mainActivityViewModel = null
        _binding = null
    }

    private fun askToEnableLoc() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setCancelable(false)
        builder.setMessage(R.string.dialog_no_location)
            .setPositiveButton(R.string.dialog_no_loc_positive) { _: DialogInterface?, _: Int ->
                SimpleLocation.openSettings(
                    this
                )
            }
            .setNegativeButton(R.string.dialog_no_thanks) { _: DialogInterface?, _: Int ->
                Snackbar.make(
                    binding.root, R.string.snackbar_location_disabled,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            .create()
            .show()
    }

    private fun showHelp() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setMessage(R.string.dialog_help)
            .setPositiveButton(R.string.dialog_got_it, null)
            .create()
            .show()
    }

    private fun updateNearbyStatusText(s: String) {
        nearbyStatusUpdateTv!!.visibility = View.VISIBLE
        nearbyStatusUpdateTv!!.text = getString(R.string.nearby_status_update, s)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NearbyViewModel.locationPermGranted.value = true
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 6

        var mainActivityViewModel: MainActivityViewModel? = null
        var mutableNearbyStatusUpdater: MutableLiveData<String> = MutableLiveData()

        lateinit var acTransitApiKey: String
        lateinit var prediction: Prediction
    }
}