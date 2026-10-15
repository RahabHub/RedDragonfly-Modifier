package com.rahab.dragonfly.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rahab.dragonfly.config.AppConfig
import com.rahab.dragonfly.data.SearchRepository
import com.rahab.dragonfly.data.model.GeoPoint
import com.rahab.dragonfly.data.model.LoginData
import com.rahab.dragonfly.data.model.SearchResult
import com.rahab.dragonfly.ui.theme.Ink600
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

private const val LINE_SOURCE_ID = "route-line-source"
private const val LINE_LAYER_ID = "route-line-layer"
private const val POINT_SOURCE_ID = "route-point-source"
private const val POINT_LAYER_ID = "route-point-layer"
private const val LINE_COLOR = "#0F766E"
private const val POINT_COLOR = "#14B8A6"

/**
 * 地图选点页。
 *
 * 嵌入 MapLibre 地图（开源免 key），支持地点搜索、点击添加轨迹点、折线与点标记实时绘制，
 * 以及路线保存/应用。底部叠加 [RouteControlPanel] 提供日期、配速与轨迹操作。
 *
 * @author Rahab
 */
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    loginData: LoginData,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val searchRepository = remember { SearchRepository() }

    // 注入登录用户信息，并刷新已保存路线。
    LaunchedEffect(loginData) {
        viewModel.setUser(loginData)
    }
    LaunchedEffect(Unit) {
        viewModel.refreshSavedRoutes()
    }

    // 持有地图实例与已添加的 source。
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var lineSource by remember { mutableStateOf<GeoJsonSource?>(null) }
    var pointSource by remember { mutableStateOf<GeoJsonSource?>(null) }

    // 搜索状态。
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }

    // 点位变化时刷新 source 的 GeoJSON。
    LaunchedEffect(state.points, lineSource, pointSource) {
        lineSource?.setGeoJson(buildLineGeoJson(state.points))
        pointSource?.setGeoJson(buildPointsGeoJson(state.points))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                mapInstance = map
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(AppConfig.MAP_INIT_LATITUDE, AppConfig.MAP_INIT_LONGITUDE))
                    .zoom(16.0)
                    .build()
                map.setStyle(AppConfig.MAP_STYLE_URL) { style ->
                    lineSource = style.addRouteLine()
                    pointSource = style.addRoutePoints()
                }
            },
            onMapClick = { latLng ->
                viewModel.addPoint(GeoPoint(latLng.latitude, latLng.longitude))
            },
        )

        // 顶部搜索栏。
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索地点", color = Ink600) },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (searching) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text,
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = {
                        scope.launch {
                            searching = true
                            searchResults = searchRepository.search(searchQuery)
                            searching = false
                        }
                    },
                ),
            )

            // 搜索结果列表。
            if (searchResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 6.dp,
                ) {
                    Column {
                        searchResults.forEach { result ->
                            Text(
                                text = result.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        mapInstance?.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(result.latitude, result.longitude),
                                                16.0,
                                            ),
                                        )
                                        searchResults = emptyList()
                                        searchQuery = ""
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }

        RouteControlPanel(
            state = state,
            viewModel = viewModel,
        )
    }
}

/** 向样式中添加轨迹折线 source 与 layer。 */
private fun Style.addRouteLine(): GeoJsonSource {
    val source = GeoJsonSource(LINE_SOURCE_ID, buildLineGeoJson(emptyList()))
    addSource(source)
    addLayer(
        LineLayer(LINE_LAYER_ID, LINE_SOURCE_ID).withProperties(
            PropertyFactory.lineColor(LINE_COLOR),
            PropertyFactory.lineWidth(5f),
        ),
    )
    return source
}

/** 向样式中添加轨迹点 source 与 layer。 */
private fun Style.addRoutePoints(): GeoJsonSource {
    val source = GeoJsonSource(POINT_SOURCE_ID, buildPointsGeoJson(emptyList()))
    addSource(source)
    addLayer(
        CircleLayer(POINT_LAYER_ID, POINT_SOURCE_ID).withProperties(
            PropertyFactory.circleRadius(8f),
            PropertyFactory.circleColor(POINT_COLOR),
            PropertyFactory.circleStrokeWidth(2f),
            PropertyFactory.circleStrokeColor("#FFFFFF"),
        ),
    )
    return source
}

/**
 * MapLibre 地图视图封装，处理 MapView 生命周期与点击回调。
 */
@Composable
private fun MapLibreView(
    modifier: Modifier,
    onMapReady: (MapLibreMap) -> Unit,
    onMapClick: (LatLng) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).also { mv ->
                mv.onCreate(null)
                mv.getMapAsync { map ->
                    map.addOnMapClickListener { point ->
                        onMapClick(point)
                        true
                    }
                    onMapReady(map)
                }
                mapView = mv
            }
        },
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView?.onStart()
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_STOP -> mapView?.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDestroy()
        }
    }
}

/** 构建折线 GeoJSON。 */
private fun buildLineGeoJson(points: List<GeoPoint>): String {
    if (points.size < 2) {
        return """{"type":"FeatureCollection","features":[]}"""
    }
    val coordinates = points.joinToString(",") { "[${it.longitude},${it.latitude}]" }
    return """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"LineString","coordinates":[$coordinates]},"properties":{}}]}"""
}

/** 构建轨迹点 GeoJSON。 */
private fun buildPointsGeoJson(points: List<GeoPoint>): String {
    val features = points.joinToString(",") { p ->
        """{"type":"Feature","geometry":{"type":"Point","coordinates":[${p.longitude},${p.latitude}]},"properties":{}}"""
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}
