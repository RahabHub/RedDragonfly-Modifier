package com.rahab.dragonfly.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rahab.dragonfly.data.AuthRepository
import com.rahab.dragonfly.data.SchoolRepository
import com.rahab.dragonfly.data.model.LoginData
import com.rahab.dragonfly.data.model.School
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
 * 拉取学校列表供用户选择，登录时使用所选学校。
 *
 * @author Rahab
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val schoolRepository = SchoolRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** 学校列表。 */
    private val _schools = MutableStateFlow<List<School>>(emptyList())
    val schools: StateFlow<List<School>> = _schools.asStateFlow()

    /** 当前选中的学校。 */
    private val _selectedSchool = MutableStateFlow<School?>(null)
    val selectedSchool: StateFlow<School?> = _selectedSchool.asStateFlow()

    init {
        loadSchools()
    }

    /** 拉取学校列表。 */
    private fun loadSchools() {
        viewModelScope.launch {
            val list = schoolRepository.fetchSchools()
            _schools.value = list
            if (_selectedSchool.value == null) {
                _selectedSchool.value = list.firstOrNull()
            }
        }
    }

    /** 选择学校。 */
    fun onSchoolSelected(school: School) {
        _selectedSchool.value = school
        _uiState.update { it.copy(error = null) }
    }

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

        val school = _selectedSchool.value
        if (school == null) {
            _uiState.update { it.copy(error = "请选择学校") }
            return
        }
        if (state.studentno.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "请输入学号和密码") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(school, state.studentno.trim(), state.password)) {
                is AuthRepository.LoginResult.Success ->
                    _uiState.update { it.copy(isLoading = false, loginData = result.data) }
                is AuthRepository.LoginResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}
