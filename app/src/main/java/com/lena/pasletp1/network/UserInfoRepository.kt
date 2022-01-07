package com.lena.pasletp1.network

import com.lena.pasletp1.user.UserInfo
import okhttp3.MultipartBody

class UserInfoRepository(private val userUpdated: (UserInfo) -> Unit) {

    private val userWebService = Api.userWebService

    suspend fun refresh() {
        val response = userWebService.getInfo()
        if (response.isSuccessful) {
            response.body()?.let(userUpdated)
        }
    }

    suspend fun update(user: UserInfo) {
        val response = userWebService.update(user)
        if (response.isSuccessful) {
            response.body()?.let(userUpdated)
        }
    }

    suspend fun updateAvatar(avatar: MultipartBody.Part) {
        val response = userWebService.updateAvatar(avatar)
        if (response.isSuccessful) {
            response.body()?.let(userUpdated)
        }
    }

}