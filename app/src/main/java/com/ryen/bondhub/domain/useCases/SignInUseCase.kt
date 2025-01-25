package com.ryen.bondhub.domain.useCases

import com.ryen.bondhub.domain.model.User
import com.ryen.bondhub.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return repository.signIn(email, password)
    }

}