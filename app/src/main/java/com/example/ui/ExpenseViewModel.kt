package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.Category
import com.example.data.local.Transaction
import com.example.data.local.TransactionType
import com.example.data.local.TransactionWithCategory
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.categoryDao(), db.transactionDao())
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currentCategories = db.categoryDao().getAllCategoriesSync() // need to add this
            if (currentCategories.isEmpty()) {
                val incomeCategories = listOf(
                    Category(name = "Salary", type = TransactionType.INCOME, color = 0xFF4CAF50.toInt()),
                    Category(name = "Freelance", type = TransactionType.INCOME, color = 0xFF8BC34A.toInt()),
                    Category(name = "Investments", type = TransactionType.INCOME, color = 0xFF009688.toInt())
                )
                val expenseCategories = listOf(
                    Category(name = "Groceries", type = TransactionType.EXPENSE, color = 0xFFE91E63.toInt()),
                    Category(name = "Rent", type = TransactionType.EXPENSE, color = 0xFFF44336.toInt()),
                    Category(name = "Utilities", type = TransactionType.EXPENSE, color = 0xFFFF9800.toInt()),
                    Category(name = "Entertainment", type = TransactionType.EXPENSE, color = 0xFF9C27B0.toInt()),
                    Category(name = "Transport", type = TransactionType.EXPENSE, color = 0xFF3F51B5.toInt())
                )
                
                val transferCategories = listOf(
                    Category(name = "Bank to Cash", type = TransactionType.TRANSFER, color = 0xFF607D8B.toInt()),
                    Category(name = "Cash to Bank", type = TransactionType.TRANSFER, color = 0xFF795548.toInt())
                )
                
                val allCats = incomeCategories + expenseCategories + transferCategories
                val ids = allCats.map { db.categoryDao().insert(it).toInt() }
                
                val salaryId = ids[0]
                val freelanceId = ids[1]
                val groceriesId = ids[3]
                val rentId = ids[4]
                val transportId = ids[7]
                
                db.categoryDao().insert(Category(name = "Bonus", type = TransactionType.INCOME, parentId = salaryId, color = 0xFF4CAF50.toInt()))
                val fuelId = db.categoryDao().insert(Category(name = "Fuel", type = TransactionType.EXPENSE, parentId = transportId, color = 0xFF3F51B5.toInt())).toInt()
                val taxiId = db.categoryDao().insert(Category(name = "Taxi", type = TransactionType.EXPENSE, parentId = transportId, color = 0xFF3F51B5.toInt())).toInt()
                
                val now = java.util.Calendar.getInstance()
                val random = java.util.Random()
                
                for (i in 0..15) {
                    val daysAgo = i
                    val cal = now.clone() as java.util.Calendar
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
                    val dayMillis = cal.timeInMillis
                    
                    val numTxs = random.nextInt(3) + 1
                    for (j in 0 until numTxs) {
                        val isIncome = random.nextInt(10) > 7
                        val paymentMethod = listOf("UPI", "Visa", "Mastercard", "Rupay").random(kotlin.random.Random(j + i))
                        val account = listOf("Cash", "Wallet", "Bank").random(kotlin.random.Random(j + i))
                        if (isIncome) {
                            val catId = if (random.nextBoolean()) salaryId else freelanceId
                            db.transactionDao().insert(Transaction(title = "Income source ${j+1}", description = "", amount = (random.nextInt(5000) + 1000).toDouble(), categoryId = catId, type = TransactionType.INCOME, dateMillis = dayMillis - random.nextInt(1000 * 60 * 60 * 12), tags = "", paymentMethod = paymentMethod, account = account))
                        } else {
                            val catIds = listOf(groceriesId, rentId, fuelId, taxiId)
                            val catId = catIds[random.nextInt(catIds.size)]
                            db.transactionDao().insert(Transaction(title = "Expense item ${j+1}", description = "", amount = (random.nextInt(1000) + 50).toDouble(), categoryId = catId, type = TransactionType.EXPENSE, dateMillis = dayMillis - random.nextInt(1000 * 60 * 60 * 12), tags = "", paymentMethod = paymentMethod, account = account))
                        }
                    }
                }
            }
        }
    }

    val categories = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addCategory(name: String, type: TransactionType, parentId: Int? = null, color: Int = 0xFF6200EE.toInt()) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, type = type, parentId = parentId, color = color))
        }
    }

    fun addTransaction(title: String, amount: Double, description: String, categoryId: Int, type: TransactionType, dateMillis: Long, tags: String, paymentMethod: String, account: String, toAccount: String? = null) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(title = title, amount = amount, description = description, categoryId = categoryId, dateMillis = dateMillis, type = type, tags = tags, paymentMethod = paymentMethod, account = account, toAccount = toAccount)
            )
        }
    }
    
    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }
}
