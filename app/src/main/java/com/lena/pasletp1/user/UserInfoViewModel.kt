package com.lena.pasletp1.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lena.pasletp1.network.UserInfoRepository
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class UserInfoViewModel : ViewModel() {

    private val userInfo = MutableStateFlow(
        UserInfo("email",
            "firstName",
            "lastName",
            null)
    )

    private val repository = UserInfoRepository {
        userInfo.value = it
    }

    fun collect(collector: FlowCollector<UserInfo>) {
        viewModelScope.launch {
            userInfo.collect(collector)
        }
    }

    fun update(user: UserInfo) {
        viewModelScope.launch {
            repository.update(user)
            repository.refresh()
        }
    }

    fun updateAvatar(avatar: MultipartBody.Part) {
        viewModelScope.launch {
            repository.updateAvatar(avatar)
            repository.refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refresh()
        }
    }
}