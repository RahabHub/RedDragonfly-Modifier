package com.rahab.dragonfly.data.model

import kotlinx.serialization.Serializable

/**
 * 已保存的跑步路线。
 *
 * @author Rahab
 */
@Serializable
data class SavedRoute(
    val name: String,
    val points: List<GeoPoint>,
)
