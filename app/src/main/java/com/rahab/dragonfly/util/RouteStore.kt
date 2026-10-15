package com.rahab.dragonfly.util

import android.content.Context
import com.rahab.dragonfly.data.model.SavedRoute
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 跑步路线本地持久化。
 *
 * 使用 SharedPreferences 存储 JSON 列表。
 *
 * @author Rahab
 */
object RouteStore {

    private const val PREFS_NAME = "dragonfly_prefs"
    private const val KEY_ROUTES = "saved_routes"

    private val json = Json { ignoreUnknownKeys = true }

    /** 保存（按名称覆盖同名路线）。 */
    fun save(context: Context, route: SavedRoute) {
        val routes = loadAll(context).toMutableList()
        routes.removeAll { it.name == route.name }
        routes.add(route)
        write(context, routes)
    }

    /** 读取全部已保存路线。 */
    fun loadAll(context: Context): List<SavedRoute> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROUTES, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<SavedRoute>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** 删除指定名称的路线。 */
    fun delete(context: Context, name: String) {
        val routes = loadAll(context).filter { it.name != name }
        write(context, routes)
    }

    private fun write(context: Context, routes: List<SavedRoute>) {
        val raw = json.encodeToString(routes)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_ROUTES, raw).apply()
    }
}
