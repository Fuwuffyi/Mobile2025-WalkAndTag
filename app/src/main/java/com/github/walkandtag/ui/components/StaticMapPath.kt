package com.github.walkandtag.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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

private fun latRad(lat: Double): Double {
    val sin = sin(lat * Math.PI / 180.0)
    return ln((1 + sin) / (1 - sin)) / 2.0
}

private fun calculateZoomLevel(bounds: LatLngBounds, width: Int, height: Int): Double {
    val worldWidth = 256
    val latFraction = (latRad(bounds.latitudeNorth) - latRad(bounds.latitudeSouth)) / Math.PI
    val lngFraction = (bounds.longitudeEast - bounds.longitudeWest) / 180.0
    val latZoom = (ln(height * 1.0 / worldWidth / latFraction) / ln(2.0)).toFloat()
    val lngZoom = (ln(width * 1.0 / worldWidth / lngFraction) / ln(2.0)).toFloat()
    return min(latZoom, lngZoom).toDouble() - 0.2
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun generateMapSnapshot(
    context: Context, styleUri: String, width: Int, height: Int, path: Collection<LatLng>
): Bitmap = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { continuation ->
        // Calculate bounding box for the path
        val bounds = path.fold(LatLngBounds.Builder()) { builder, point ->
            builder.include(LatLng(point.latitude, point.longitude))
            builder
        }.build()
        // Convert path to GeoJSON LineString
        val points = path.map { Point.fromLngLat(it.longitude, it.latitude) }
        val lineString = LineString.fromLngLats(points)
        // Create the line overlay
        val geoJsonSource = GeoJsonSource("route-source", lineString)
        val lineLayer = LineLayer("route-layer", "route-source").withProperties(
            lineColor(Color.Red.toArgb()),  // use .toArgb() from androidx.compose.ui.graphics.Color
            lineWidth(4f)
        )
        // Build the map style
        val styleBuilder =
            Style.Builder().fromUri(styleUri).withSource(geoJsonSource).withLayer(lineLayer)
        // Create camera options to fit the bounds
        val cameraPosition = CameraPosition.Builder().target(bounds.center)
            .zoom(calculateZoomLevel(bounds, width, height)).build()
        // Link everything together
        val options = MapSnapshotter.Options(width, height).withStyleBuilder(styleBuilder)
            .withPixelRatio(context.resources.displayMetrics.density)
            .withCameraPosition(cameraPosition)
        // Create the map snapshot
        val snapshotter = MapSnapshotter(context, options)
        snapshotter.start(callback = { snapshot ->
            continuation.resume(
                value = snapshot.bitmap
            ) { cause, _, _ ->
                snapshotter.cancel()
            }
            snapshotter.cancel()
        }, errorHandler = {
            snapshotter.cancel()
            continuation.resumeWithException(Throwable(it))
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
    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(path, styleUri) {
        isLoading = true
        error = null
        try {
            bitmapState = withContext(Dispatchers.IO) {
                generateMapSnapshot(
                    context,
                    styleUri,
                    context.resources.displayMetrics.widthPixels,
                    (context.resources.displayMetrics.widthPixels * (4.0f / 6.0f)).toInt(),
                    path
                )
            }
        } catch (e: Exception) {
            error = "Map load failed: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    Box(modifier = modifier) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error ?: "Map load error")
                }
            }

            bitmapState != null -> {
                Image(
                    bitmap = bitmapState!!.asImageBitmap(),
                    contentDescription = "Static Map",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
