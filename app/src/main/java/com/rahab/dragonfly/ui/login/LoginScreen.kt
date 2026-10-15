package com.rahab.dragonfly.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rahab.dragonfly.config.AppConfig
import com.rahab.dragonfly.ui.theme.Coral100
import com.rahab.dragonfly.ui.theme.Coral500
import com.rahab.dragonfly.ui.theme.Mist100
import com.rahab.dragonfly.ui.theme.Teal700
import com.rahab.dragonfly.ui.theme.Teal900

/**
 * 登录页。
 *
 * 学校固定为北京语言大学，用户输入学号与密码即可登录。
 *
 * @author Rahab
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (com.rahab.dragonfly.data.model.LoginData) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // 登录成功后回调跳转。
    state.loginData?.let { data ->
        onLoginSuccess(data)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Teal700, Teal900),
                ),
            ),
    ) {
        // 顶部装饰光斑
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-80).dp)
                .clip(RoundedCornerShape(140.dp))
                .background(Coral500.copy(alpha = 0.18f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 品牌区
            Text(
                text = "红蜻蜓轨迹",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = AppConfig.SCHOOL_NAME,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                fontSize = 15.sp,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(40.dp))

            // 登录卡片
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = androidx.compose.ui.graphics.Color.White,
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = "登录",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Teal900,
                    )

                    OutlinedTextField(
                        value = state.studentno,
                        onValueChange = viewModel::onStudentnoChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("学号") },
                        leadingIcon = { Icon(Icons.Outlined.Person, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        colors = fieldColors(),
                    )

                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("密码") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff
                                    else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.login()
                        }),
                        colors = fieldColors(),
                    )

                    // 错误提示
                    AnimatedVisibility(
                        visible = state.error != null,
                        enter = fadeIn(tween(200)),
                    ) {
                        Text(
                            text = state.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Teal700,
                            contentColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = androidx.compose.ui.graphics.Color.White,
                                strokeWidth = 2.5.dp,
                            )
                        } else {
                            Text("登 录", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 底部说明
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 },
            ) {
                Text(
                    text = "学校已锁定 · 北京语言大学",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

/** 输入框统一样式。 */
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Teal700,
    focusedLabelColor = Teal700,
    cursorColor = Teal700,
    unfocusedBorderColor = Mist100,
    focusedContainerColor = Coral100.copy(alpha = 0.05f),
)
