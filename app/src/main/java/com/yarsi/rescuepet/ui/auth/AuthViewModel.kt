package com.yarsi.rescuepet.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.yarsi.rescuepet.data.repository.AuthRepository
import com.yarsi.rescuepet.utils.Result

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableLiveData<Result<String>>()
    val loginState: LiveData<Result<String>> = _loginState

    private val _registerState = MutableLiveData<Result<String>>()
    val registerState: LiveData<Result<String>> = _registerState

    fun login(email: String, password: String, role: String) {
        val validationError = validateEmail(email) ?: validatePassword(password)
        if (validationError != null) {
            _loginState.value = Result.Error(validationError)
            return
        }
        _loginState.value = Result.Loading
        viewModelScope.launch {
            _loginState.value = repository.login(email, password, role)
        }
    }

    fun register(email: String, password: String, name: String) {
        val validationError = validateName(name) ?: validateEmail(email) ?: validatePassword(password)
        if (validationError != null) {
            _registerState.value = Result.Error(validationError)
            return
        }
        _registerState.value = Result.Loading
        viewModelScope.launch {
            _registerState.value = repository.register(email, password, name)
        }
    }

    private fun validateName(name: String): String? {
        return if (name.isBlank()) "Nama harus diisi" else null
    }

    private fun validateEmail(email: String): String? {
        return if (!email.matches(Regex("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$"))) "Format email tidak valid" else null
    }

    private fun validatePassword(password: String): String? {
        return if (password.length < 8) "Password minimal 8 karakter" else null
    }
}
