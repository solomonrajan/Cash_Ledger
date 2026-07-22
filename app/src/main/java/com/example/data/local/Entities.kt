package com.example.data.local

import androidx.room.*

enum class TransactionType { INCOME, EXPENSE, TRANSFER }

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: TransactionType,
    val parentId: Int? = null,
    val color: Int = 0xFF6200EE.toInt(),
    val iconName: String = "Category"
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val description: String,
    val categoryId: Int,
    val dateMillis: Long,
    val type: TransactionType,
    val tags: String,
    val paymentMethod: String,
    val account: String,
    val toAccount: String? = null
)

data class TransactionWithCategory(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)
