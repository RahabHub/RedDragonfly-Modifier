package com.rahab.dragonfly.data.model

import com.rahab.dragonfly.util.CoordinateTransform
import kotlinx.serialization.Serializable
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 经纬度坐标点（WGS-84，与 OpenStreetMap/MapLibre 一致）。
 *
 * @author Rahab
 */
@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    /** 转为 GCJ-02（国测局坐标，上传时使用）。 */
    fun toGcj02(): GeoPoint {
        val (lat, lng) = CoordinateTransform.wgs84ToGcj02(latitude, longitude)
        return GeoPoint(lat, lng)
    }

    companion object {
        private const val EARTH_RADIUS_M = 6371000.0

        /** 计算两个坐标点之间的大圆距离（米），使用 Haversine 公式。 */
        fun distanceMeters(from: GeoPoint, to: GeoPoint): Double {
            val lat1 = Math.toRadians(from.latitude)
            val lat2 = Math.toRadians(to.latitude)
            val dLat = Math.toRadians(to.latitude - from.latitude)
            val dLng = Math.toRadians(to.longitude - from.longitude)

            val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLng / 2) * sin(dLng / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_M * c
        }
    }
}

/** 计算一组折线点的总里程（米）。 */
fun List<GeoPoint>.totalDistanceMeters(): Double {
    if (size < 2) return 0.0
    var total = 0.0
    for (i in 0 until size - 1) {
        total += GeoPoint.distanceMeters(this[i], this[i + 1])
    }
    return total
}
