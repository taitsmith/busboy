package com.taitsmith.busboy.ui

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.taitsmith.busboy.api.StopPredictionResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.databinding.ActivityMainBinding
import com.taitsmith.busboy.utils.OnItemClickListener
import com.taitsmith.busboy.utils.OnItemLongClickListener
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.viewmodels.FavoritesViewModel
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import com.taitsmith.busboy.viewmodels.NearbyViewModel
import dagger.hilt.android.AndroidEntryPoint
import im.delight.android.location.SimpleLocation

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnItemClickListener, OnItemLongClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    lateinit var prediction: StopPredictionResponse.BustimeResponse.Prediction

    private var nearbyStatusUpdateTv: TextView? = null

    var nearbyFragment: NearbyFragment? = null
    var byIdFragment: ByIdFragment? = null
    var favoritesFragment: FavoritesFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        acTransitApiKey = getString(R.string.ac_transit_key)
        mainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        bottomNavigationView = binding.mainTabLayout
        nearbyStatusUpdateTv = binding.nearbyStatusUpdater
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        nearbyFragment = NearbyFragment()
        bottomNavigationView.setupWithNavController(navController)
        byIdFragment = ByIdFragment()
        favoritesFragment = FavoritesFragment()

        setObservers()
        setTabListeners()
    }

    private fun setTabListeners() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.byId -> {
                    navController.popBackStack()
                    navController.navigate(R.id.byIdFragment)
                }
                R.id.nearby -> {
                    navController.popBackStack()
                    navController.navigate(R.id.nearbyFragment)
                }
                R.id.favorites -> navController.navigate(R.id.favoritesFragment)
                R.id.help -> showHelp()
            }
            true
        }
    }

    private fun setObservers() {
        MainActivityViewModel.mutableStatusMessage.observe(this) { s: String -> getStatusMessage(s) }
        MainActivityViewModel.mutableErrorMessage.observe(this) { s: String -> getErrorMessage(s) }
        mutableBus.observe(this) {
            mainActivityViewModel!!.getWaypoints(
                prediction.rt!!
            )
        }
        mutableNearbyStatusUpdater.observe(this) { s -> updateNearbyStatusText(s) }
    }


    private fun getErrorMessage(s: String) {
        hideUi(false)
        when (s) {
            "NO_PERMISSION" -> ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION)
            "404" -> Snackbar.make(
                binding.root, R.string.snackbar_404,
                Snackbar.LENGTH_LONG).show()
            "NO_LOC_ENABLED" -> askToEnableLoc()
            "BAD_INPUT" -> Snackbar.make(
                binding.root, R.string.snackbar_bad_input,
                Snackbar.LENGTH_LONG).show()
            "NULL_PRED_RESPONSE" -> Snackbar.make(
                binding.root, R.string.snackbar_no_predictions,
                Snackbar.LENGTH_LONG).show()
            "NULL_BUS_COORDS" -> Snackbar.make(
                binding.root, R.string.snackbar_null_bus_coords,
                Snackbar.LENGTH_LONG).show()
            "CALL_FAILURE" -> Snackbar.make(
                binding.root, R.string.snackbar_network_error,
                Snackbar.LENGTH_LONG).show()
            "BAD_DISTANCE" -> Snackbar.make(
                binding.root, R.string.snackbar_bad_distance,
                Snackbar.LENGTH_LONG).show()
            "NO_FAVORITE_STOPS" -> Snackbar.make(
                binding.root, R.string.snackbar_no_favorites,
                Snackbar.LENGTH_LONG).show()
        }
    }

    private fun getStatusMessage(s: String) {
        when (s) {
            "HELP_REQUESTED" -> showHelp()
            "DIRECTION_POLYLINE_READY" -> {
                val action = NearbyFragmentDirections.actionNearbyFragmentToMapsFragment("directions")
                navController.navigate(action)
            }
            "ROUTE_POLYLINE_READY" -> {
                val action = ByIdFragmentDirections.actionByIdFragmentToMapsFragment("route")
                navController.navigate(action)
            }
            "FAVORITE_ADDED" -> Snackbar.make(
                binding.root, R.string.snackbar_favorite_added,
                Snackbar.LENGTH_LONG).show()
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
        MainActivityViewModel.mutableStatusMessage.removeObservers(this)
        MainActivityViewModel.mutableErrorMessage.removeObservers(this)
        mainActivityViewModel = null
        binding.unbind()
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
            .setNegativeButton(R.string.dialog_no_loc_negative) { _: DialogInterface?, _: Int ->
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
                NearbyViewModel.loc.beginUpdates()
                enableNearbySearch = true
            }
        }
    }

    //the five following are for listviews on nearby, by id && favorites fragments
    override fun onNearbyItemSelected(position: Int) {
        val s = NearbyViewModel.stopList[position]!!.stopId
        val action = NearbyFragmentDirections.actionNearbyFragmentToByIdFragment(s!!)
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"
        ByIdViewModel.predictionList.clear()
        navController.navigate(action)
    }

    override fun onIdItemSelected(position: Int) {
        prediction = ByIdViewModel.predictionList[position]
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"
        mainActivityViewModel!!.getBusLocation(prediction.vid!!)
    }

    override fun onFavoriteItemSelected(position: Int) {
        val s = FavoritesViewModel.favoriteStops[position].stopId
        val action = FavoritesFragmentDirections.actionFavoritesFragmentToByIdFragment(s!!)
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"
        ByIdViewModel.predictionList.clear()
        navController.navigate(action)
    }

    override fun onNearbyLongClick(position: Int) {
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"
        val (_, _, _, latitude, longitude) = NearbyViewModel.stopList[position]!!
        val start =
            NearbyViewModel.loc.latitude.toString() + "," + NearbyViewModel.loc.longitude.toString()
        val end = (latitude!!).toString() + "," + (longitude!!).toString()
        mainActivityViewModel!!.getDirectionsToStop(start, end)
    }

    override fun onIdLongClick(position: Int) {}

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 6
        var mainActivityViewModel: MainActivityViewModel? = null
        var enableNearbySearch = false
        lateinit var acTransitApiKey: String
        var mutableBus: MutableLiveData<Bus> = MutableLiveData()
        var mutableNearbyStatusUpdater: MutableLiveData<String> = MutableLiveData()
    }
}