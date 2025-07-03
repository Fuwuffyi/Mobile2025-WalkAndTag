package com.github.walkandtag.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.match
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

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
