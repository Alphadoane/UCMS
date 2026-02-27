package com.example.android.data.repository

import android.content.Context
import com.example.android.data.network.ApiService
import com.example.android.data.network.NetworkModule
import timber.log.Timber

/**
 * Finance Repository
 * Handles Fee Payments, Balances, and Receipts.
 */
class FinanceRepository(context: Context? = null) {

    private val api: ApiService by lazy {
        NetworkModule.apiInstance ?: throw IllegalStateException("API not initialized. Call StudentRepository.init(context)")
    }

    suspend fun getFeeBalance(userId: String): Map<String, Any> {
        return try {
             val res = api.getFeeBalance()
              if (res.isSuccessful) {
                  val body = res.body()
                  val bal = body?.balance ?: 0.0
                  val paid = body?.total_paid ?: 0.0
                  val billed = body?.total_billed ?: 0.0
                  mapOf("balance" to bal, "currency" to "KES", "billed" to billed, "paid" to paid)
              } else {
                 mapOf("balance" to 0.0, "currency" to "KES", "billed" to 0.0, "paid" to 0.0)
             }
        } catch(e: Exception) { mapOf("balance" to 0.0, "currency" to "KES", "billed" to 0.0, "paid" to 0.0) }
    }

    suspend fun initiateStkPush(paymentType: String, amount: Double?): Result<String> {
        return try {
            val res = api.initiateStkPush(ApiService.StkPushRequest(paymentType, amount))
            if (res.isSuccessful) {
                Result.success("STK Push initiated successfully")
            } else {
                Result.failure(Exception(res.errorBody()?.string() ?: "Failed to initiate payment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initiateBankPayment(paymentType: String, amount: Double?): Result<ApiService.PaystackInitResponse> {
        return try {
            val res = api.initiatePaystackPayment(ApiService.StkPushRequest(paymentType, amount))
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception(res.errorBody()?.string() ?: "Failed to initiate bank payment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceipts(userId: String): List<Map<String, Any>> {
        return try {
            val res = api.getReceipts()
            if (res.isSuccessful) {
                res.body()?.receipts?.map {
                    mapOf(
                        "id" to it.receipt_no,
                        "amount" to it.amount,
                        "date" to it.date,
                        "description" to it.description,
                        "method" to (it.method ?: "N/A"),
                        "ref" to (it.ref ?: it.receipt_no)
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getUserProfile(type: String): com.example.android.data.model.User? {
        // Mock implementation
        return com.example.android.data.model.User(id="fin1", email="student@example.com", firstName="Student", lastName="User", role=com.example.android.data.model.UserRole.STUDENT)
    }
}
