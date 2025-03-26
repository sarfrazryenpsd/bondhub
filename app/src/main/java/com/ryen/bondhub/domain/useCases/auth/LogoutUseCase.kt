package com.ryen.bondhub.domain.useCases.auth

import com.ryen.bondhub.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(): Result<Unit> {
        return repository.logout()
    }

}