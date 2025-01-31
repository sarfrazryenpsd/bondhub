package com.ryen.bondhub.domain.useCases

import com.ryen.bondhub.domain.model.UserAuth
import com.ryen.bondhub.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(email: String, password: String, displayName: String): Result<UserAuth> {
        return repository.signUp(email, password, displayName)
    }

}