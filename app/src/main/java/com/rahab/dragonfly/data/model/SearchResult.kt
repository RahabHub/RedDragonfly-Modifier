package com.rahab.dragonfly.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 地点搜索结果（Nominatim / OpenStreetMap 地理编码）。
 *
 * @author Rahab
 */
@Serializable
data class SearchResult(
    val lat: String = "",
    val lon: String = "",

    @SerialName("display_name")
    val displayName: String = "",
) {
    val latitude: Double get() = lat.toDoubleOrNull() ?: 0.0
    val longitude: Double get() = lon.toDoubleOrNull() ?: 0.0
}
