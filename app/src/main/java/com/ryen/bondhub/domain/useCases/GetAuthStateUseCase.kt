package com.ryen.bondhub.domain.useCases

import com.ryen.bondhub.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    operator fun invoke(): Flow<Boolean> = authRepository.isUserAuthenticated()
}