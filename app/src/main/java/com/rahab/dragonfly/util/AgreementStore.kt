package com.rahab.dragonfly.util

import android.content.Context

/**
 * 首次使用协议同意状态持久化。
 *
 * @author Rahab
 */
object AgreementStore {

    private const val PREFS_NAME = "dragonfly_prefs"
    private const val KEY_ACCEPTED = "agreement_accepted"

    /** 是否已同意协议。 */
    fun isAccepted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ACCEPTED, false)
    }

    /** 标记为已同意。 */
    fun markAccepted(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ACCEPTED, true).apply()
    }
}
