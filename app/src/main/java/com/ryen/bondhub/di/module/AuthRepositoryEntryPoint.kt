package com.ryen.bondhub.di.module

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.ryen.bondhub.domain.repository.AuthRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface AuthRepositoryEntryPoint {
    fun getAuthRepository(): AuthRepository
}

val LocalAuthRepository = staticCompositionLocalOf<AuthRepository> {
    error("No AuthRepository provided")
}

@Composable
fun ProvideAuthRepository(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        AuthRepositoryEntryPoint::class.java
    )

    CompositionLocalProvider(
        LocalAuthRepository provides entryPoint.getAuthRepository()
    ) {
        content()
    }
}