package com.rahab.dragonfly.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rahab.dragonfly.config.AppConfig
import com.rahab.dragonfly.data.UploadRepository
import com.rahab.dragonfly.data.model.GeoPoint
import com.rahab.dragonfly.data.model.LoginData
import com.rahab.dragonfly.data.model.RunRule
import com.rahab.dragonfly.data.model.SavedRoute
import com.rahab.dragonfly.data.model.School
import com.rahab.dragonfly.data.model.totalDistanceMeters
import com.rahab.dragonfly.util.RouteStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 地图页视图状态。
 */
data class MapUiState(
    val points: List<GeoPoint> = emptyList(),
    /** 配速（分钟/公里），范围 2~10。 */
    val paceMinPerKm: Float = 6f,
    /** 选定的跑步日期（当天零点毫秒时间戳）。 */
    val dateMillis: Long = System.currentTimeMillis(),
    /** 是否正在上传。 */
    val isUploading: Boolean = false,
    /** 上传结果提示（成功或失败信息）。 */
    val uploadMessage: String? = null,
    /** 上传是否成功（null 表示尚无结果）。 */
    val uploadSuccess: Boolean? = null,
    /** 规则要求的最低里程（米），尚未拉取到规则时为 null。 */
    val minDistanceM: Int? = null,
)

/**
 * 地图页 ViewModel。
 *
 * 管理用户在地图上点选的轨迹点、配速、日期，以及跑步规则拉取与数据上传。
 *
 * @author Rahab
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val uploadRepository = UploadRepository(application)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    /** 已保存的路线列表。 */
    private val _savedRoutes = MutableStateFlow<List<SavedRoute>>(emptyList())
    val savedRoutes: StateFlow<List<SavedRoute>> = _savedRoutes.asStateFlow()

    /** 登录用户信息（由 MapScreen 注入）。 */
    private var user: LoginData? = null

    /** 用户选择的学校（由 MapScreen 注入）。 */
    private var school: School? = null

    /** 当前跑步规则（登录后拉取）。 */
    private var rule: RunRule? = null

    /** 轨迹总里程（米）。 */
    val totalDistanceMeters: Double
        get() = _uiState.value.points.totalDistanceMeters()

    /** 按当前配速估算的完成用时（秒）。 */
    val estimatedSeconds: Int
        get() {
            val km = totalDistanceMeters / 1000.0
            return (km * _uiState.value.paceMinPerKm * 60).toInt()
        }

    /** 注入登录用户信息与学校，并拉取跑步规则。 */
    fun setUser(data: LoginData, selectedSchool: School) {
        user = data
        school = selectedSchool
        viewModelScope.launch {
            val fetched = uploadRepository.getRunRule(selectedSchool, data.studentno)
            rule = fetched
            _uiState.update { it.copy(minDistanceM = fetched?.minDistanceM) }
        }
    }

    /** 新增一个轨迹点。 */
    fun addPoint(point: GeoPoint) {
        _uiState.update { it.copy(points = it.points + point, uploadMessage = null) }
    }

    /** 撤销最后一个轨迹点。 */
    fun undoLastPoint() {
        _uiState.update { state ->
            if (state.points.isEmpty()) state
            else state.copy(points = state.points.dropLast(1))
        }
    }

    /** 清空所有轨迹点。 */
    fun clearPoints() {
        _uiState.update { it.copy(points = emptyList()) }
    }

    /** 更新配速。 */
    fun onPaceChange(value: Float) {
        _uiState.update { it.copy(paceMinPerKm = value) }
    }

    /** 更新跑步日期。 */
    fun onDateChange(dateMillis: Long) {
        _uiState.update { it.copy(dateMillis = dateMillis) }
    }

    /** 刷新已保存路线列表。 */
    fun refreshSavedRoutes() {
        _savedRoutes.value = RouteStore.loadAll(getApplication())
    }

    /** 保存当前轨迹为一条路线。 */
    fun saveRoute(name: String) {
        val points = _uiState.value.points
        if (points.size < 2 || name.isBlank()) return
        RouteStore.save(getApplication(), SavedRoute(name = name.trim(), points = points))
        refreshSavedRoutes()
    }

    /** 应用一条已保存路线。 */
    fun loadRoute(route: SavedRoute) {
        _uiState.update { it.copy(points = route.points, uploadMessage = null, uploadSuccess = null) }
    }

    /** 删除一条已保存路线。 */
    fun deleteRoute(name: String) {
        RouteStore.delete(getApplication(), name)
        refreshSavedRoutes()
    }

    /** 上传跑步数据。 */
    fun upload() {
        val state = _uiState.value
        val loginData = user ?: run {
            _uiState.update { it.copy(uploadMessage = "登录信息缺失，请重新登录") }
            return
        }
        val currentSchool = school ?: run {
            _uiState.update { it.copy(uploadMessage = "学校信息缺失，请重新登录") }
            return
        }
        if (state.isUploading) return
        if (state.points.size < 2) {
            _uiState.update { it.copy(uploadMessage = "请先在地图上绘制轨迹") }
            return
        }

        // 规则兜底：拉取失败时使用默认值。
        val currentRule = rule ?: RunRule(
            ruleid = AppConfig.RULE_ID,
            eventno = AppConfig.EVENT_NO,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadMessage = null) }
            val result = uploadRepository.upload(
                school = currentSchool,
                uid = loginData.userid,
                studentno = loginData.studentno,
                uploadToken = loginData.uploadToken,
                points = state.points,
                paceMinPerKm = state.paceMinPerKm,
                dateMillis = state.dateMillis,
                rule = currentRule,
            )
            when (result) {
                is UploadRepository.UploadResult.Success ->
                    _uiState.update {
                        it.copy(isUploading = false, uploadMessage = result.message, uploadSuccess = true)
                    }
                is UploadRepository.UploadResult.Failure ->
                    _uiState.update {
                        it.copy(isUploading = false, uploadMessage = result.message, uploadSuccess = false)
                    }
            }
        }
    }
}
