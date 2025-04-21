package com.ryen.bondhub

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.ryen.bondhub.notifications.FCMTokenRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BHApplication: Application(){

    @Inject
    lateinit var tokenRepository: FCMTokenRepository

    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if(!task.isSuccessful){
                Log.w("BHApplication", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("BHApplication", "FCM registration token: $token")

            CoroutineScope(Dispatchers.IO).launch {
                tokenRepository.updateUserFCMToken(token)
            }
        }
    }
}