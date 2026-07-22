package com.example.data.repository

import com.example.data.local.Category
import com.example.data.local.CategoryDao
import com.example.data.local.Transaction
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionType
import com.example.data.local.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<TransactionWithCategory>> = transactionDao.getAllTransactions()

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsByType(type)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Int) {
        transactionDao.deleteTransaction(id)
    }
}
