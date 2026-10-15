package com.rahab.dragonfly.util

import android.content.Context
import android.os.Build
import java.util.UUID

/**
 * 设备信息工具。
 *
 * 登录接口需要上报设备品牌、型号、系统版本以及一个机器编号。
 * 机器编号首次生成后持久化，保证同一设备多次登录一致。
 *
 * @author Rahab
 */
object DeviceInfo {

    private const val PREFS_NAME = "dragonfly_prefs"
    private const val KEY_MACHINE_NO = "machine_no"

    /** 手机品牌，如 HUAWEI。 */
    fun phoneBrand(): String = Build.MANUFACTURER

    /** 手机型号，如 DCO-AL00。 */
    fun phoneModel(): String = Build.MODEL

    /** 系统版本，如 12。 */
    fun sysRelease(): String = Build.VERSION.RELEASE

    /**
     * 机器编号：32 位十六进制随机串，首次生成后持久化。
     */
    fun machineNo(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(KEY_MACHINE_NO, null)?.let { return it }

        val generated = UUID.randomUUID().toString().replace("-", "")
        prefs.edit().putString(KEY_MACHINE_NO, generated).apply()
        return generated
    }
}
