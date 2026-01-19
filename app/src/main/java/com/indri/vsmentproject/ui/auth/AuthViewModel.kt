package com.indri.vsmentproject.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.indri.vsmentproject.data.repository.AuthRepository
import com.indri.vsmentproject.data.utils.Resource

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    // Tambahkan di AuthViewModel.kt
    private val _registerResult = MutableLiveData<Resource<Unit>>()
    val registerResult: LiveData<Resource<Unit>> = _registerResult

    fun register(email: String, pass: String, nama: String) {
        repository.registerManager(email, pass, nama) { result ->
            _registerResult.postValue(result)
        }
    }
    private val _loginResult = MutableLiveData<Resource<String>>()
    val loginResult: LiveData<Resource<String>> = _loginResult

    fun login(email: String, pass: String) {
        repository.login(email, pass) { result ->
            _loginResult.postValue(result)
        }
    }

    fun logout() {
        repository.logout()
    }
}