package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AccountItem(
    val id: String,
    val name: String,
    val iconName: String,
    val balance: Double = 0.0
)

data class PaymentMethodItem(
    val id: String,
    val name: String,
    val iconName: String
)

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val name: String
)

object CurrencyData {
    val currencies = listOf(
        CurrencyItem("USD", "$", "United States Dollar"),
        CurrencyItem("INR", "₹", "Indian Rupee"),
        CurrencyItem("EUR", "€", "Euro"),
        CurrencyItem("GBP", "£", "British Pound Sterling"),
        CurrencyItem("JPY", "¥", "Japanese Yen"),
        CurrencyItem("CAD", "CA$", "Canadian Dollar"),
        CurrencyItem("AUD", "AU$", "Australian Dollar"),
        CurrencyItem("CHF", "CHF", "Swiss Franc"),
        CurrencyItem("CNY", "CN¥", "Chinese Yuan"),
        CurrencyItem("BRL", "R$", "Brazilian Real"),
        CurrencyItem("MXN", "MEX$", "Mexican Peso"),
        CurrencyItem("AED", "AED", "UAE Dirham"),
        CurrencyItem("SGD", "S$", "Singapore Dollar"),
        CurrencyItem("NZD", "NZ$", "New Zealand Dollar"),
        CurrencyItem("KRW", "₩", "South Korean Won"),
        CurrencyItem("RUB", "₽", "Russian Ruble"),
        CurrencyItem("ZAR", "R", "South African Rand"),
        CurrencyItem("TRY", "₺", "Turkish Lira"),
        CurrencyItem("IDR", "Rp", "Indonesian Rupiah"),
        CurrencyItem("MYR", "RM", "Malaysian Ringgit"),
        CurrencyItem("PHP", "₱", "Philippine Peso"),
        CurrencyItem("THB", "฿", "Thai Baht"),
        CurrencyItem("VND", "₫", "Vietnamese Dong"),
        CurrencyItem("SAR", "SR", "Saudi Riyal"),
        CurrencyItem("EGP", "E£", "Egyptian Pound"),
        CurrencyItem("PKR", "Rs", "Pakistani Rupee"),
        CurrencyItem("BDT", "৳", "Bangladeshi Taka"),
        CurrencyItem("SEK", "kr", "Swedish Krona"),
        CurrencyItem("NOK", "kr", "Norwegian Krone"),
        CurrencyItem("DKK", "kr", "Danish Krone"),
        CurrencyItem("PLN", "zł", "Polish Zloty"),
        CurrencyItem("HKD", "HK$", "Hong Kong Dollar"),
        CurrencyItem("ILS", "₪", "Israeli New Shekel"),
        CurrencyItem("NGN", "₦", "Nigerian Naira"),
        CurrencyItem("KES", "KSh", "Kenyan Shilling")
    )
}

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _themeModeFlow = MutableStateFlow(prefs.getString("theme_mode", "system") ?: "system")
    val themeModeFlow: StateFlow<String> = _themeModeFlow.asStateFlow()

    var themeMode: String
        get() = prefs.getString("theme_mode", "system") ?: "system"
        set(value) {
            prefs.edit().putString("theme_mode", value).apply()
            _themeModeFlow.value = value
        }

    var isDarkMode: Boolean
        get() = themeMode == "dark"
        set(value) {
            themeMode = if (value) "dark" else "light"
        }

    var isSetupCompleted: Boolean
        get() = prefs.getBoolean("is_setup_completed", false)
        set(value) = prefs.edit().putBoolean("is_setup_completed", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "User") ?: "User"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userAvatar: String
        get() = prefs.getString("user_avatar", "Person") ?: "Person"
        set(value) = prefs.edit().putString("user_avatar", value).apply()

    var currencyCode: String
        get() = prefs.getString("currency_code", "USD") ?: "USD"
        set(value) = prefs.edit().putString("currency_code", value).apply()

    var currencySymbol: String
        get() = prefs.getString("currency_symbol", "$") ?: "$"
        set(value) = prefs.edit().putString("currency_symbol", value).apply()

    var googleAccountEmail: String?
        get() = prefs.getString("google_email", null)
        set(value) = prefs.edit().putString("google_email", value).apply()

    fun getAccounts(): List<AccountItem> {
        val json = prefs.getString("accounts_list", null) ?: return defaultAccounts()
        return try {
            val type = object : TypeToken<List<AccountItem>>() {}.type
            gson.fromJson(json, type) ?: defaultAccounts()
        } catch (e: Exception) {
            defaultAccounts()
        }
    }

    fun saveAccounts(list: List<AccountItem>) {
        val json = gson.toJson(list)
        prefs.edit().putString("accounts_list", json).apply()
    }

    fun getPaymentMethods(): List<PaymentMethodItem> {
        val json = prefs.getString("payment_methods_list", null) ?: return defaultPaymentMethods()
        return try {
            val type = object : TypeToken<List<PaymentMethodItem>>() {}.type
            gson.fromJson(json, type) ?: defaultPaymentMethods()
        } catch (e: Exception) {
            defaultPaymentMethods()
        }
    }

    fun savePaymentMethods(list: List<PaymentMethodItem>) {
        val json = gson.toJson(list)
        prefs.edit().putString("payment_methods_list", json).apply()
    }

    companion object {
        fun defaultAccounts(): List<AccountItem> = listOf(
            AccountItem("1", "Cash", "LocalAtm"),
            AccountItem("2", "Bank Account", "AccountBalance"),
            AccountItem("3", "Digital Wallet", "AccountBalanceWallet")
        )

        fun defaultPaymentMethods(): List<PaymentMethodItem> = listOf(
            PaymentMethodItem("1", "UPI", "QrCode"),
            PaymentMethodItem("2", "Visa", "CreditCard"),
            PaymentMethodItem("3", "Mastercard", "CreditCard"),
            PaymentMethodItem("4", "Cash", "Payments"),
            PaymentMethodItem("5", "Bank Transfer", "AccountBalance")
        )
    }
}
