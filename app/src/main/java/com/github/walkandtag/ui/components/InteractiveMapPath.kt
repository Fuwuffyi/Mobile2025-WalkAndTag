package com.github.walkandtag.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
fun InteractiveMapPath(
    modifier: Modifier = Modifier,
    styleUri: String = "https://raw.githubusercontent.com/go2garret/maps/main/src/assets/json/openStreetMap.json",
    path: Collection<LatLng>
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            mapView.onDestroy()
        }
    }
    AndroidView(modifier = modifier, factory = { mapView }, update = { view ->
        view.getMapAsync { mapLibreMap ->
            mapLibreMap.setStyle(
                Style.Builder().fromUri(styleUri).withSource(
                    GeoJsonSource(
                        "route-source", LineString.fromLngLats(path.map {
                            Point.fromLngLat(it.longitude, it.latitude)
                        })
                    )
                ).withLayer(
                    LineLayer("route-layer", "route-source").withProperties(
                        lineColor(Color.Red.toArgb()), lineWidth(4f)
                    )
                )
            ) { style ->
                val bounds = path.fold(LatLngBounds.Builder()) { builder, point ->
                    builder.include(point)
                }.build()
                val padding = 50
                mapLibreMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds, padding
                    )
                )
            }
        }
    })
}
