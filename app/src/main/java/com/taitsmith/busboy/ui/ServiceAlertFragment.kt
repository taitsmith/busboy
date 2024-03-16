package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.taitsmith.busboy.data.ServiceAlert
import com.taitsmith.busboy.databinding.FragmentServiceAlertBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ServiceAlertFragment : Fragment() {

    private val args: ServiceAlertFragmentArgs by navArgs()

    private var _binding: FragmentServiceAlertBinding? = null
    private val binding  get() = _binding!!

    private lateinit var alertList: List<ServiceAlert>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceAlertBinding.inflate(inflater, container, false)
        val composeView = binding.serviceAlertComposeView

        alertList = args.serviceAlertResponse.bustimeResponse?.sb!!

        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    ServiceAlertList(alerts = alertList)
                }
            }
        }

        return binding.root
    }
}

@Composable
fun ServiceAlertList(alerts: List<ServiceAlert>) {
    Column {
        alerts.forEach {alert ->
            AlertCard(alert)
        }
    }
}

@Composable
fun AlertCard(alert: ServiceAlert) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    Card(
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                alert.nm.toString()
            )
        }
    }
}
