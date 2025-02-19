package com.ryen.bondhub.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object AuthValidation {
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_NAME_LENGTH = 50
    private const val MIN_NAME_LENGTH = 2

    fun validateFullName(fullName: String): ValidationResult {
        val trimmedName = fullName.trim()
        return when {
            trimmedName.isEmpty() -> ValidationResult.Error("Name cannot be empty")
            trimmedName.length < MIN_NAME_LENGTH -> ValidationResult.Error("Name must be at least $MIN_NAME_LENGTH characters")
            trimmedName.length > MAX_NAME_LENGTH -> ValidationResult.Error("Name cannot exceed $MAX_NAME_LENGTH characters")
            !trimmedName.matches("^[a-zA-Z]+([ ][a-zA-Z]+)*$".toRegex()) ->
                ValidationResult.Error("Name can only contain letters and single spaces between words")
            else -> ValidationResult.Success
        }
    }

    fun validateEmail(email: String): ValidationResult {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return when {
            email.isEmpty() -> ValidationResult.Error("Email cannot be empty")
            !email.matches(emailRegex) -> ValidationResult.Error("Invalid email format")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Error("Password cannot be empty")
            password.length < MIN_PASSWORD_LENGTH -> ValidationResult.Error("Password must be at least $MIN_PASSWORD_LENGTH characters")
            !password.matches(".*[A-Z].*".toRegex()) -> ValidationResult.Error("Password must contain at least one uppercase letter")
            !password.matches(".*[a-z].*".toRegex()) -> ValidationResult.Error("Password must contain at least one lowercase letter")
            !password.matches(".*[0-9].*".toRegex()) -> ValidationResult.Error("Password must contain at least one number")
            !password.matches(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*".toRegex()) ->
                ValidationResult.Error("Password must contain at least one special character")
            else -> ValidationResult.Success
        }
    }

    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isEmpty() -> ValidationResult.Error("Confirm password cannot be empty")
            password != confirmPassword -> ValidationResult.Error("Passwords do not match")
            else -> ValidationResult.Success
        }
    }

    fun handleFirebaseAuthError(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthWeakPasswordException -> "Password is too weak"
            is FirebaseAuthInvalidCredentialsException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Invalid email format"
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                    else -> "Invalid credentials"
                }
            }
            is FirebaseAuthInvalidUserException -> {
                when (exception.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                    "ERROR_USER_DISABLED" -> "This account has been disabled"
                    else -> "Account error"
                }
            }
            is FirebaseAuthUserCollisionException -> "An account already exists with this email"
            is FirebaseTooManyRequestsException -> "Too many attempts. Please try again later"
            is FirebaseNetworkException -> "Network error. Please check your connection"
            else -> "Authentication failed. Please try again"
        }
    }

    // Utility function to validate all sign-up fields at once
    fun validateSignUpFields(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): ValidationResult {
        validateFullName(fullName).let { if (it is ValidationResult.Error) return it }
        validateEmail(email).let { if (it is ValidationResult.Error) return it }
        validatePassword(password).let { if (it is ValidationResult.Error) return it }
        validatePasswordMatch(password, confirmPassword).let { if (it is ValidationResult.Error) return it }
        return ValidationResult.Success
    }

    // Utility function to validate all sign-in fields at once
    fun validateSignInFields(
        email: String,
        password: String
    ): ValidationResult {
        validateEmail(email).let { if (it is ValidationResult.Error) return it }
        // For sign-in, we only check if password is not empty
        if (password.isEmpty()) return ValidationResult.Error("Password cannot be empty")
        return ValidationResult.Success
    }
}