package com.github.walkandtag.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.snapshotter.MapSnapshotter
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import kotlin.coroutines.resumeWithException
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sin

// @TODO(): Clean up code and try to remove ExperimentalCoroutinesApi
private fun latRad(lat: Double): Double {
    val sinLat = sin(lat * Math.PI / 180.0f)
    return ln((1.0f + sinLat) / (1.0f - sinLat)) / 2.0f
}

private fun calculateZoomLevel(bounds: LatLngBounds, width: Int, height: Int): Double {
    val worldWidth = 256.0f
    val latFraction = (latRad(bounds.latitudeNorth) - latRad(bounds.latitudeSouth)) / Math.PI
    val lngFraction = (bounds.longitudeEast - bounds.longitudeWest) / 180.0f
    val latZoom = (ln(height * 1.0f / worldWidth / latFraction) / ln(2.0f)).toFloat()
    val lngZoom = (ln(width * 1.0f / worldWidth / lngFraction) / ln(2.0f)).toFloat()
    return min(latZoom, lngZoom).toDouble() - 0.2f
}

// Replace when MapSnapshotter is stable with suspend functions
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun generateMapSnapshot(
    context: Context, styleUri: String, width: Int, height: Int, path: Collection<LatLng>
): Bitmap = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { cont ->
        val bounds = path.fold(LatLngBounds.Builder()) { builder, point ->
            builder.include(point)
        }.build()
        val lineString = LineString.fromLngLats(path.map {
            Point.fromLngLat(it.longitude, it.latitude)
        })
        val style =
            Style.Builder().fromUri(styleUri).withSource(GeoJsonSource("route-source", lineString))
                .withLayer(
                    LineLayer("route-layer", "route-source").withProperties(
                        lineColor(Color.Red.toArgb()), lineWidth(4f)
                    )
                )
        val cameraPosition = CameraPosition.Builder().target(bounds.center)
            .zoom(calculateZoomLevel(bounds, width, height)).build()
        val options = MapSnapshotter.Options(width, height)
            .withPixelRatio(context.resources.displayMetrics.density)
            .withCameraPosition(cameraPosition).withStyleBuilder(style)
        val snapshotter = MapSnapshotter(context, options)
        snapshotter.start({ snapshot ->
            cont.resume(snapshot.bitmap) { _, _, _ -> snapshotter.cancel() }
            snapshotter.cancel()
        }, {
            snapshotter.cancel()
            cont.resumeWithException(Throwable(it))
        })
    }
}

@Composable
fun StaticMapPath(
    modifier: Modifier = Modifier,
    styleUri: String = "https://raw.githubusercontent.com/go2garret/maps/main/src/assets/json/openStreetMap.json",
    path: Collection<LatLng>
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(path, styleUri) {
        isLoading = true
        error = null
        try {
            bitmap = withContext(Dispatchers.IO) {
                val display = context.resources.displayMetrics
                val width = display.widthPixels
                val height = (width * 2 / 3f).toInt() // 4:6 = 2:3 ratio
                generateMapSnapshot(context, styleUri, width, height, path)
            }
        } catch (e: Exception) {
            error = "Map load failed: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(modifier = modifier) {
        when {
            isLoading -> LoadingScreen()
            error != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "Map load error")
            }

            bitmap != null -> Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Static Map",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
