package com.github.walkandtag.ui.components

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.github.walkandtag.R
import com.github.walkandtag.util.addPathStyle
import com.github.walkandtag.util.buildBounds
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun InteractiveMapPath(
    modifier: Modifier = Modifier,
    styleUri: String = "https://raw.githubusercontent.com/go2garret/maps/main/src/assets/json/openStreetMap.json",
    path: Collection<LatLng>
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply { onCreate(null) }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDestroy() }
    }

    AndroidView(modifier = modifier, factory = { mapView }, update = { view ->
        view.getMapAsync { map ->
            map.setStyle(
                Style.Builder().fromUri(styleUri).addPathStyle(path)
            ) {
                it.addImage(
                    "start-icon",
                    AppCompatResources.getDrawable(context, R.drawable.path_start_pin)!!.toBitmap(),
                    false
                )
                it.addImage(
                    "end-icon",
                    AppCompatResources.getDrawable(context, R.drawable.path_end_pin)!!.toBitmap(),
                    false
                )
                val bounds = buildBounds(path)
                val padding = 50
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            }
        }
    })
}
