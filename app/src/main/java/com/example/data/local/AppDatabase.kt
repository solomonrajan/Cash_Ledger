package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType) = value.name

    @TypeConverter
    fun toTransactionType(value: String) = enumValueOf<TransactionType>(value)
}

@Database(entities = [Category::class, Transaction::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                SQLiteDatabase.loadLibs(context)
                val passphrase = getEncryptionKey(context)
                val factory = SupportFactory(passphrase)
                
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker.db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = db
                db
            }
        }

        private fun getEncryptionKey(context: Context): ByteArray {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "db_keys",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            var keyBase64 = sharedPreferences.getString("sqlcipher_key", null)
            if (keyBase64 == null) {
                val random = SecureRandom()
                val key = ByteArray(32)
                random.nextBytes(key)
                keyBase64 = android.util.Base64.encodeToString(key, android.util.Base64.NO_WRAP)
                sharedPreferences.edit().putString("sqlcipher_key", keyBase64).apply()
                return key
            }
            return android.util.Base64.decode(keyBase64, android.util.Base64.NO_WRAP)
        }
    }
}
