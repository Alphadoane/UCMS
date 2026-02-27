package com.school.studentportal.shared.data.repository

import com.school.studentportal.shared.data.model.Book
import com.school.studentportal.shared.data.model.BookCategory
import com.school.studentportal.shared.data.network.SharedApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LibraryRepository(private val api: SharedApiService) {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    suspend fun refreshBooks(category: BookCategory? = null): Result<List<Book>> {
        val result = api.getBooks(category?.name)
        if (result.isSuccess) {
            _books.value = result.getOrNull() ?: emptyList()
        }
        return result
    }

    suspend fun uploadBook(title: String, author: String, category: BookCategory, pdfBytes: ByteArray, fileName: String, coverBytes: ByteArray? = null, coverName: String? = null): Result<Unit> {
        return api.uploadBook(title, author, category.name, pdfBytes, fileName, coverBytes, coverName)
    }

    suspend fun deleteBook(bookId: Int): Result<Unit> {
        return api.deleteBook(bookId)
    }
}
