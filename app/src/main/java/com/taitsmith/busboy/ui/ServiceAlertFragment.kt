package com.taitsmith.busboy.ui

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    @Composable
    fun ServiceAlertList(alerts: List<ServiceAlert>) {
        Column(
            Modifier.fillMaxWidth()
        ) {
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
        Surface (
            shape = MaterialTheme.shapes.medium,
            onClick = { isExpanded = !isExpanded },
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth(),
            color = Color(0xFFDCE5DC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Row {
                    Text(
                        alert.nm.toString(),
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column (
                        Modifier.fillMaxWidth()
                    ) {
                        Row {
                            Text(
                                Html.fromHtml(alert.dtl, 1).toString()
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                "Cause: " + alert.cse
                            )
                            Text(
                                "Effect: " + alert.efct
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Text("Lines impacted: ")
                            alert.srvc.forEachIndexed { i, v ->
                                if (i < alert.srvc.size - 1) Text(v.rt.toString() + ", ")
                                else Text(v.rt.toString())
                            }
                        }
                    }
                }

            }
        }
    }
    @Preview
    @Composable
    fun AlertCardPreview() {
        val alert = ServiceAlert()

        alert.nm    = "Preview Alert"
        alert.dtl   = "Be careful, this is a test alert"
        alert.cse   = "Turkey in road"
        alert.efct  = "The bus is slow"
        alert.prty  = "Medium"

        alert.srvc = arrayListOf(
            ServiceAlert.ImpactedServices(
                rt      = "51A",
                rtdir   = "NB"
            ),
            ServiceAlert.ImpactedServices(
                rt      = "6",
                rtdir   = "NB"
            )
        )
        AlertCard(alert)
    }
}




