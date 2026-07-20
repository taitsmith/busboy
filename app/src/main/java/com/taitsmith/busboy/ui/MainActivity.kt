package com.taitsmith.busboy.ui

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.databinding.ActivityMainBinding
import com.taitsmith.busboy.di.SettingsRepository
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import com.taitsmith.busboy.viewmodels.NearbyViewModel
import dagger.hilt.android.AndroidEntryPoint
import im.delight.android.location.SimpleLocation
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_FINE_LOCATION = 6

    @Inject lateinit var settingsRepository: SettingsRepository

    //activity-scoped, the same instances the fragments obtain via activityViewModels(); reset on switch.
    private val byIdViewModel: ByIdViewModel by viewModels()
    private val nearbyViewModel: NearbyViewModel by viewModels()

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var mainActivityViewModel: MainActivityViewModel

    private var nearbyStatusUpdateTv: TextView? = null
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        bottomNavigationView = binding.mainTabLayout
        nearbyStatusUpdateTv = binding.nearbyStatusUpdater
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel!!.uiState.collect { uiState ->
                    when (uiState) {
                        is MainActivityViewModel.LoadingState.Loading -> hideUi(true)
                        is MainActivityViewModel.LoadingState.StatusUpdate -> updateStatus(uiState.msg)
                        is MainActivityViewModel.LoadingState.Success -> hideUi(false)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //collect the raw DataStore flow (not the eagerly-seeded StateFlow, whose synthetic
                //AC_TRANSIT seed can look like a switch on cold start). Its first emission is the real
                //persisted agency; drop(1) skips it so only genuine user switches trigger a reset.
                settingsRepository.selectedAgency.drop(1).collect { onAgencyChanged(it) }
            }
        }

        setTabListeners()
    }

    private fun setTabListeners() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.byId       -> navController.navigate(R.id.byIdFragment)
                R.id.nearby     -> navController.navigate(R.id.nearbyFragment)
                R.id.favorites  -> navController.navigate(R.id.favoritesFragment)
                R.id.settings   -> navController.navigate(R.id.settingsFragment)
                R.id.help       -> showHelp()
            }
            true
        }
    }

    private fun onAgencyChanged(agency: Agency) {
        //the By-Id / Nearby view models are activity-scoped, so they outlive the nav reset and would
        //otherwise re-render the previous agency's stale results. clear them before navigating.
        byIdViewModel.reset()
        nearbyViewModel.reset()

        val options = NavOptions.Builder()
            .setPopUpTo(navController.graph.startDestinationId, true)
            .build()
        navController.navigate(navController.graph.startDestinationId, null, options)
        bottomNavigationView.selectedItemId = R.id.byId

        val name = when (agency) {
            Agency.AC_TRANSIT -> getString(R.string.agency_ac_transit)
            Agency.CTA        -> getString(R.string.agency_cta)
        }
        showSnackbar(getString(R.string.snackbar_agency_switched, name))
    }

    private fun updateStatus(s: String) {
        hideUi(false)
        when (s) {
            "NO_LOC_ENABLED"        -> askToEnableLoc()
            "NO_PERMISSION"         -> ActivityCompat.requestPermissions(
                                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                            PERMISSION_REQUEST_FINE_LOCATION)
            "404"                   -> showSnackbar(R.string.snackbar_404)
            "BAD_INPUT"             -> showSnackbar(R.string.snackbar_bad_input)
            "NULL_PRED_RESPONSE"    -> showSnackbar(R.string.snackbar_no_predictions)
            "NULL_BUS_COORDS"       -> showSnackbar(R.string.snackbar_null_bus_coords)
            "CALL_FAILURE"          -> showSnackbar(R.string.snackbar_network_error)
            "BAD_DISTANCE"          -> showSnackbar(R.string.snackbar_bad_distance)
            "NULL_LOCATION"         -> showSnackbar(R.string.snackbar_loc_null)
            "WAITING_ON_LOCATION"   -> showSnackbar(R.string.snackbar_loc_null)
            "DIRECTION_FAILURE"     -> showSnackbar(R.string.snackbar_direction_failure)
            "NO_WAYPOINTS"          -> showSnackbar(R.string.snackbar_no_waypoints)
            "NO_SERVICE_SCHEDULED"  -> showSnackbar(R.string.snackbar_no_service_scheduled)
            "UNKNOWN"               -> showSnackbar(R.string.snackbar_unknown_error)
            "FAVORITE_ADDED"        -> showSnackbar(R.string.snackbar_favorite_added)
            "STOP_DELETED"          -> showSnackbar(R.string.snackbar_favorite_deleted)
        }
    }

    private fun showSnackbar(message: Int) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun hideUi(shouldHide: Boolean) {
        if (shouldHide) {
            binding.navHostFragment.visibility  = View.INVISIBLE
            binding.progressBar.visibility      = View.VISIBLE
        } else {
            binding.navHostFragment.visibility  = View.VISIBLE
            binding.progressBar.visibility      = View.INVISIBLE
            nearbyStatusUpdateTv?.visibility    = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NearbyViewModel.locationPermGranted.value = true
            } else {
                //permission denied- tell the user why Nearby won't work instead of failing silently.
                showSnackbar(R.string.snackbar_location_disabled)
            }
        }
    }
}