package com.rahab.dragonfly.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rahab.dragonfly.data.AuthRepository
import com.rahab.dragonfly.data.model.LoginData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 登录页视图状态。
 */
data class LoginUiState(
    val studentno: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginData: LoginData? = null,
)

/**
 * 登录页 ViewModel。
 *
 * @author Rahab
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** 学号输入变化。 */
    fun onStudentnoChange(value: String) {
        _uiState.update { it.copy(studentno = value, error = null) }
    }

    /** 密码输入变化。 */
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    /** 提交登录。 */
    fun login() {
        val state = _uiState.value
        if (state.isLoading) return

        // 简单校验：学号与密码不能为空。
        if (state.studentno.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "请输入学号和密码") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.login(state.studentno.trim(), state.password)) {
                is AuthRepository.LoginResult.Success ->
                    _uiState.update { it.copy(isLoading = false, loginData = result.data) }
                is AuthRepository.LoginResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}
