package com.ryen.bondhub.domain.useCases

import com.ryen.bondhub.domain.model.User
import com.ryen.bondhub.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(email: String, password: String, displayName: String): Result<User> {
        return repository.signUp(email, password, displayName)
    }

}