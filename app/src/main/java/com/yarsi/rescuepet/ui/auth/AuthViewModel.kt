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

    fun login(email: String, password: String) {
        _loginState.value = Result.Loading
        viewModelScope.launch {
            _loginState.value = repository.login(email, password)
        }
    }

    fun register(email: String, password: String, name: String) {
        _registerState.value = Result.Loading
        viewModelScope.launch {
            _registerState.value = repository.register(email, password, name)
        }
    }
}
