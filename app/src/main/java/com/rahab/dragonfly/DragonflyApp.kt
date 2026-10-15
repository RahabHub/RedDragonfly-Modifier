package com.rahab.dragonfly

import android.app.Application
import org.maplibre.android.MapLibre

/**
 * 应用入口。
 *
 * 负责初始化 MapLibre 地图引擎。
 *
 * @author Rahab
 */
class DragonflyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }
}
