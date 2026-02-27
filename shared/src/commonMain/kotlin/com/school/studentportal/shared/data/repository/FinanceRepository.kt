package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.*
import com.school.studentportal.shared.data.network.SharedApiService

class FinanceRepository(private val apiService: SharedApiService) {
    
    suspend fun getFeeBalance(): Result<FeeBalanceResponse> {
        return apiService.getFeeBalance()
    }

    suspend fun getReceipts(): Result<FinanceStatementResponse> {
        return apiService.getReceipts()
    }

    suspend fun initiateStkPush(paymentType: String, amount: Double?): Result<ApiResponse> {
        val request = StkPushRequest(
            amount = amount,
            payment_type = paymentType
        )
        return apiService.initiateStkPush(request)
    }

    suspend fun initiateBankPayment(paymentType: String, amount: Double?): Result<PaystackInitResponse> {
        val request = StkPushRequest(
            amount = amount,
            payment_type = paymentType
        )
        return apiService.initiatePaystackPayment(request)
    }

    suspend fun initiateMpesaPayment(amount: Double, phoneNumber: String): Result<MpesaResponse> {
        return apiService.initiateMpesaPayment(amount, phoneNumber)
    }
}
