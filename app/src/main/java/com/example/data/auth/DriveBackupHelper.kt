package com.example.data.auth

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class DriveBackupHelper(private val context: Context) {
    
    suspend fun backupDatabase(account: GoogleSignInAccount): Result<String> = withContext(Dispatchers.IO) {
        try {
            val scope = "oauth2:https://www.googleapis.com/auth/drive.file"
            val gAccount = account.account ?: return@withContext Result.failure(Exception("No account found"))
            val token = GoogleAuthUtil.getToken(context, gAccount, scope)
            
            val dbFile = context.getDatabasePath("expense_tracker.db")
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            val metadata = """{"name": "expense_tracker_backup.db"}"""
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("metadata", null, metadata.toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull()))
                .addFormDataPart("file", dbFile.name, dbFile.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                .addHeader("Authorization", "Bearer ${"$"}token")
                .post(body)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Backup successful")
            } else {
                val errorBody = response.body?.string() ?: ""
                Result.failure(Exception("Upload failed: ${"$"}{response.code} - ${"$"}errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
