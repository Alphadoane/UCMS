package com.example.android.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.android.data.repository.AdmissionRepository
import com.example.android.data.repository.AuthRepository
import com.school.studentportal.shared.ui.screens.LoginScreen
import com.school.studentportal.shared.data.model.AdmissionApplication
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLogin: (String, String, (String) -> Unit, () -> Unit) -> Unit,
    onSignUp: (String, String, String, String?, (String) -> Unit, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val admissionRepository = remember { AdmissionRepository() }
    val scope = rememberCoroutineScope()

    com.school.studentportal.shared.ui.screens.LoginScreen(
        onLogin = onLogin,
        onSignUp = onSignUp,
        onRequestPasswordReset = { email, callback ->
            scope.launch {
                val result = authRepository.requestPasswordReset(email)
                callback(result)
            }
        },
        onVerifyPasswordReset = { email, otp, newPass, callback ->
            scope.launch {
                val result = authRepository.verifyPasswordReset(email, otp, newPass)
                callback(result)
            }
        },
        onCheckApplicationStatus = { query, callback ->
            scope.launch {
                val result = admissionRepository.checkStatus(query)
                // Map the result to Shared Model
                val sharedResult = result.map { app ->
                     AdmissionApplication(
                        id = app.id,
                        application_id = app.application_id,
                        first_name = app.first_name,
                        last_name = app.last_name,
                        national_id = app.national_id,
                        current_phase = app.current_phase,
                        tracking_id = app.tracking_id,
                        documents = emptyList(), // Skipping documents mapping for now
                        missing_documents = app.missing_documents,
                        rejection_reason = app.rejection_reason,
                        email = app.email,
                        credential_password = app.credential_password,
                        student_reg_number = app.student_reg_number
                    )
                }
                callback(sharedResult)
            }
        }
    )
}
