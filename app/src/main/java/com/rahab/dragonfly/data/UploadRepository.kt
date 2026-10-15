package com.rahab.dragonfly.data

import android.content.Context
import com.rahab.dragonfly.config.AppConfig
import com.rahab.dragonfly.data.api.ApiClient
import com.rahab.dragonfly.data.model.GeoPoint
import com.rahab.dragonfly.data.model.RunRule
import com.rahab.dragonfly.data.model.totalDistanceMeters
import com.rahab.dragonfly.util.Crypto
import com.rahab.dragonfly.util.DeviceInfo
import com.rahab.dragonfly.util.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 跑步数据上传仓库。
 *
 * 负责拉取跑步规则、把地图轨迹转换为服务端要求的 location/datalog 字段，
 * 计算签名（两个 HMAC-SHA256 与两个 SHA256）后上传。
 *
 * @author Rahab
 */
class UploadRepository(private val context: Context) {

    private val api = ApiClient(AppConfig.BASE_URL)

    sealed interface UploadResult {
        data class Success(val message: String) : UploadResult
        data class Failure(val message: String) : UploadResult
    }

    /**
     * 拉取当前跑步规则（取第一条）。
     */
    suspend fun getRunRule(studentno: String): RunRule? = withContext(Dispatchers.IO) {
        try {
            val response = api.getRunParam(
                mapOf(
                    "schoolno" to AppConfig.SCHOOL_NO,
                    "studentno" to studentno,
                ),
            )
            if (response.isSuccess) response.firstRule else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 上传跑步数据。
     */
    suspend fun upload(
        uid: String,
        studentno: String,
        uploadToken: String,
        points: List<GeoPoint>,
        paceMinPerKm: Float,
        dateMillis: Long,
        rule: RunRule,
    ): UploadResult = withContext(Dispatchers.IO) {
        try {
            val distanceM = points.totalDistanceMeters()
            if (distanceM < 1.0) {
                return@withContext UploadResult.Failure("请先在地图上绘制轨迹")
            }

            val usetimeSec = (distanceM / 1000.0 * paceMinPerKm * 60.0).toInt()
            val speedKmh = distanceM / usetimeSec * 3.6

            val (beginSec, endSec) = resolveRunTime(dateMillis, usetimeSec)

            val track = buildTrack(points, paceMinPerKm, beginSec, usetimeSec)
            val location = track.joinToString("@") { it.locationField() }
            val datalog = buildDatalog(track, points, studentno)

            val params = buildUploadParams(
                uid = uid,
                studentno = studentno,
                uploadToken = uploadToken,
                distanceM = distanceM,
                speedKmh = speedKmh,
                usetimeSec = usetimeSec,
                beginSec = beginSec,
                endSec = endSec,
                location = location,
                datalog = datalog,
                timestampSec = System.currentTimeMillis() / 1000,
                rule = rule,
            )

            val response = api.uploadRunData(params)
            if (response.isSuccess) {
                UploadResult.Success(response.msg.ifBlank { "上传成功" })
            } else {
                UploadResult.Failure(response.msg.ifBlank { "上传失败" })
            }
        } catch (e: Exception) {
            UploadResult.Failure(
                if (NetworkError.isConnectionError(e)) "未连接校园网，请检查网络"
                else (e.message ?: "网络错误，请稍后重试")
            )
        }
    }

    /** 解析跑步起止时间：选定日期 + 当前时刻。 */
    private fun resolveRunTime(dateMillis: Long, usetimeSec: Int): Pair<Long, Long> {
        val now = Calendar.getInstance()
        val runDay = Calendar.getInstance().apply { timeInMillis = dateMillis }
        runDay.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
        runDay.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
        runDay.set(Calendar.SECOND, now.get(Calendar.SECOND))
        val endSec = runDay.timeInMillis / 1000
        return (endSec - usetimeSec) to endSec
    }

    /** 组装上传表单参数并计算签名。 */
    private fun buildUploadParams(
        uid: String,
        studentno: String,
        uploadToken: String,
        distanceM: Double,
        speedKmh: Double,
        usetimeSec: Int,
        beginSec: Long,
        endSec: Long,
        location: String,
        datalog: String,
        timestampSec: Long,
        rule: RunRule,
    ): Map<String, String> {
        val distance = distanceM.toInt().toString()
        val speed = String.format(Locale.US, "%.15f", speedKmh)

        val locationHash = Crypto.sha256(location)
        val datalogHash = Crypto.sha256(datalog)

        val canonical = buildString {
            append("v=1\n")
            append("uid=$uid\n")
            append("schoolno=${AppConfig.SCHOOL_NO}\n")
            append("studentno=$studentno\n")
            append("begintime=$beginSec\n")
            append("endtime=$endSec\n")
            append("distance=$distance\n")
            append("speed=$speed\n")
            append("usetime=$usetimeSec\n")
            append("eventno=${rule.eventno}\n")
            append("ruleid=${rule.ruleid}\n")
            append("path=${AppConfig.RUN_PATH}\n")
            append("pointstatus=${AppConfig.POINT_STATUS}\n")
            append("platform=android\n")
            append("appversion=${AppConfig.APP_VERSION}\n")
            append("location_hash=$locationHash\n")
            append("datalog_enc=gzip_base64url\n")
            append("datalog_hash=$datalogHash\n")
            append("timestamp=$timestampSec")
        }

        val payloadSign = Crypto.hmacSha256(uploadToken, canonical)
        val sign = Crypto.hmacSha256(
            uploadToken,
            "$uploadToken$beginSec$endSec$timestampSec",
        )

        return mapOf(
            "begintime" to beginSec.toString(),
            "endtime" to endSec.toString(),
            "uid" to uid,
            "schoolno" to AppConfig.SCHOOL_NO,
            "distance" to distance,
            "speed" to speed,
            "studentno" to studentno,
            "atttype" to "",
            "eventno" to rule.eventno,
            "location" to location,
            "datalog" to datalog,
            "datalog_enc" to "gzip_base64url",
            "pointstatus" to AppConfig.POINT_STATUS,
            "usetime" to usetimeSec.toString(),
            "path" to AppConfig.RUN_PATH,
            "ruleid" to rule.ruleid,
            "platform" to "android",
            "appversion" to AppConfig.APP_VERSION,
            "device_model" to "${DeviceInfo.phoneBrand()} ${DeviceInfo.phoneModel()}",
            "upload_token" to uploadToken,
            "timestamp" to timestampSec.toString(),
            "sign" to sign,
            "payload_debug_canonical" to canonical,
            "payload_sign" to payloadSign,
        )
    }

    /** 内部轨迹点（GCJ-02 + 时间戳）。 */
    private data class TrackPoint(
        val lat: Double,
        val lng: Double,
        val tsMs: Long,
        val speedMS: Double,
    ) {
        fun locationField(): String = String.format(
            Locale.US,
            "%.14f,%.14f;%d;null;null;%.1f;fused:1.2;78.0;1",
            lat, lng, tsMs, speedMS,
        )
    }

    /** 把轨迹点展开成带时间戳的 GCJ-02 轨迹点（每约 5 米一个点）。 */
    private fun buildTrack(
        points: List<GeoPoint>,
        paceMinPerKm: Float,
        beginSec: Long,
        usetimeSec: Int,
    ): List<TrackPoint> {
        val gcj = points.map { it.toGcj02() }
        val speedMS = 1000.0 / (paceMinPerKm * 60.0)
        val total = gcj.totalDistanceMeters()
        val secPerMeter = usetimeSec / total

        val track = ArrayList<TrackPoint>()
        val stepMeters = 5.0
        var cumulativeM = 0.0
        for (i in 0 until gcj.size - 1) {
            val a = gcj[i]
            val b = gcj[i + 1]
            val segM = GeoPoint.distanceMeters(a, b)
            val n = maxOf(1, (segM / stepMeters).toInt())
            for (k in 0 until n) {
                val t = k.toDouble() / n
                val lat = a.latitude + (b.latitude - a.latitude) * t
                val lng = a.longitude + (b.longitude - a.longitude) * t
                val elapsedSec = (cumulativeM + segM * k / n) * secPerMeter
                val tsMs = beginSec * 1000 + (elapsedSec * 1000).toLong()
                track.add(TrackPoint(lat, lng, tsMs, speedMS))
            }
            cumulativeM += segM
        }
        return track
    }

    /** 构造跑步日志（模拟原版 run log 结构），并 gzip+base64url 编码。 */
    private fun buildDatalog(
        track: List<TrackPoint>,
        points: List<GeoPoint>,
        studentno: String,
    ): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.getDefault())
        val lines = ArrayList<String>()
        val startMs = track.firstOrNull()?.tsMs ?: System.currentTimeMillis()

        lines.add(
            "${fmt.format(Date(startMs))} [LIFECYCLE] run_session_start | " +
                "studentId=$studentno, provider=fused, platform=android",
        )

        // 每约 8 个点输出一条 loc_point + track_point_write，模拟原版日志。
        val sampleStep = maxOf(1, track.size / 8)
        track.forEachIndexed { index, p ->
            if (index % sampleStep == 0 || index == track.lastIndex) {
                val ts = fmt.format(Date(p.tsMs))
                lines.add(
                    "$ts [LOC   ] loc_point | reason=accepted_track_point, provider=fused, " +
                        "tsMs=${p.tsMs}, accuracyM=1.2, gcjLat=${p.lat}, gcjLng=${p.lng}, " +
                        "isMock=false, pointNum=${index + 1}, stepFreq=0",
                )
                lines.add(
                    "$ts [TRACK ] track_point_write | pointNum=${index + 1}, provider=fused, " +
                        "smoothedLat=${p.lat}, smoothedLon=${p.lng}",
                )
            }
        }

        lines.add(
            "${fmt.format(Date(track.lastOrNull()?.tsMs ?: startMs))} [LIFECYCLE] " +
                "run_session_end | points=${points.size}",
        )
        return Crypto.gzipBase64Url(lines.joinToString("\n"))
    }
}
