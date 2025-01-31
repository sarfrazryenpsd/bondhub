package com.ryen.bondhub.domain.model

data class UserAuth(
    val uid: String,
    val email: String,
    val displayName: String?,
)
