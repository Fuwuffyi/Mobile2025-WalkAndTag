package com.github.walkandtag.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.literal
import com.mapbox.mapboxsdk.style.expressions.Expression.match
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

fun buildBounds(path: Collection<LatLng>): LatLngBounds =
    path.fold(LatLngBounds.Builder()) { builder, point -> builder.include(point) }.build()

fun createRouteGeoJson(path: Collection<LatLng>): GeoJsonSource =
    GeoJsonSource("route-source", LineString.fromLngLats(path.map {
        Point.fromLngLat(it.longitude, it.latitude)
    }))

fun createPinsGeoJson(path: Collection<LatLng>): GeoJsonSource {
    val features = mutableListOf<Feature>()
    if (path.isNotEmpty()) {
        features += Feature.fromGeometry(
            Point.fromLngLat(
                path.first().longitude, path.first().latitude
            )
        ).apply {
            addStringProperty("type", "start")
        }
        features += Feature.fromGeometry(
            Point.fromLngLat(
                path.last().longitude, path.last().latitude
            )
        ).apply {
            addStringProperty("type", "end")
        }
    }
    return GeoJsonSource("pins-source", FeatureCollection.fromFeatures(features))
}

fun createRouteLayer(): LineLayer = LineLayer("route-layer", "route-source").withProperties(
    lineColor(Color.Red.toArgb()), lineWidth(4f)
)

fun createPinsLayer(): SymbolLayer = SymbolLayer("pins-layer", "pins-source").withProperties(
    iconImage(
        match(
            get("type"),
            literal("start"),
            literal("start-icon"),
            literal("end"),
            literal("end-icon"),
            literal("default-icon")
        )
    ), iconAllowOverlap(true), iconIgnorePlacement(true)
)

fun Style.Builder.addPathStyle(path: Collection<LatLng>): Style.Builder =
    this.withSource(createRouteGeoJson(path)).withSource(createPinsGeoJson(path))
        .withLayer(createRouteLayer()).withLayer(createPinsLayer())
