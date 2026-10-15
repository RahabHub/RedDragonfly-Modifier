package com.rahab.dragonfly.config

/**
 * 应用级配置常量。
 *
 * 学校固定为北京语言大学（BLCU），无需切换。
 *
 * @author Rahab
 */
object AppConfig {

    /** 学校编号（北京语言大学）。 */
    const val SCHOOL_NO = "10032"

    /** 学校名称。 */
    const val SCHOOL_NAME = "北京语言大学"

    /**
     * 学校服务端地址。
     *
     * 注意：原版学校列表里该地址是 http，但服务器已强制 https（302 跳转）。
     * 这里直接使用 https，避免重定向问题。
     */
    const val BASE_URL = "https://tyjx.blcu.edu.cn/appapi/"

    /** App 版本号，登录时上报，与原版保持一致。 */
    const val APP_VERSION = "1.2.22"

    /** 设备系统类型。 */
    const val SYS_TYPE = "android"

    /** 用户类型：学生。 */
    const val SUBTYPE = "student"

    // 跑步规则常量（来自 getRunParam.php，校内定向跑）。
    /** 事件编号。 */
    const val EVENT_NO = "802"

    /** 规则编号。 */
    const val RULE_ID = "5"

    /** 打卡点顺序。 */
    const val RUN_PATH = "1,12,2,10"

    /** 点位状态。 */
    const val POINT_STATUS = "3"

    /** 地图样式地址：OpenFreeMap 免费矢量瓦片，无需 key。 */
    const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

    /** 地图初始中心（北京语言大学附近）。 */
    const val MAP_INIT_LATITUDE = 39.9985
    const val MAP_INIT_LONGITUDE = 116.3506
}
