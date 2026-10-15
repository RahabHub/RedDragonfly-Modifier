package com.rahab.dragonfly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rahab.dragonfly.data.model.LoginData
import com.rahab.dragonfly.ui.agreement.AgreementScreen
import com.rahab.dragonfly.ui.login.LoginScreen
import com.rahab.dragonfly.ui.login.LoginViewModel
import com.rahab.dragonfly.ui.map.MapScreen
import com.rahab.dragonfly.ui.map.MapViewModel
import com.rahab.dragonfly.ui.theme.DragonflyTheme
import com.rahab.dragonfly.util.AgreementStore

/**
 * 应用入口 Activity。
 *
 * 页面顺序：首次使用协议 → 登录 → 地图选点。
 *
 * @author Rahab
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DragonflyTheme {
                DragonflyRoot()
            }
        }
    }
}

/**
 * 根组合：按「协议 → 登录 → 地图」的顺序切换。
 */
@Composable
private fun DragonflyRoot() {
    val context = LocalContext.current
    var agreementAccepted by remember { mutableStateOf(AgreementStore.isAccepted(context)) }

    val loginViewModel: LoginViewModel = viewModel()
    var loginData by remember { mutableStateOf<LoginData?>(null) }

    val current = loginData
    when {
        // 首次使用：先同意协议。
        !agreementAccepted -> AgreementScreen(
            onAgreed = {
                AgreementStore.markAccepted(context)
                agreementAccepted = true
            },
        )

        // 已登录：进入地图选点。
        current != null -> {
            val mapViewModel: MapViewModel = viewModel()
            MapScreen(
                viewModel = mapViewModel,
                loginData = current,
            )
        }

        // 未登录：进入登录页。
        else -> LoginScreen(
            viewModel = loginViewModel,
            onLoginSuccess = { data -> loginData = data },
        )
    }
}
