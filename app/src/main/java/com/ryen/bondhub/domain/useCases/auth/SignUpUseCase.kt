package com.ryen.bondhub.domain.useCases.auth

import com.ryen.bondhub.domain.model.UserAuth
import com.ryen.bondhub.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<UserAuth> =
        authRepository.signUpWithDefaultProfile(email, password, displayName)
}