package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconItem(
    val name: String,
    val icon: ImageVector,
    val tags: List<String> = emptyList()
)

object CategoryIcons {
    val availableIcons = listOf(
        CategoryIconItem("ShoppingCart", Icons.Default.ShoppingCart, listOf("shop", "store", "buy", "groceries", "market")),
        CategoryIconItem("Restaurant", Icons.Default.Restaurant, listOf("food", "dining", "eat", "meal", "dinner", "lunch")),
        CategoryIconItem("Fastfood", Icons.Default.Fastfood, listOf("burger", "snack", "junk", "drink")),
        CategoryIconItem("Coffee", Icons.Default.Coffee, listOf("cafe", "tea", "drink", "starbucks", "breakfast")),
        CategoryIconItem("LocalBar", Icons.Default.LocalBar, listOf("drinks", "alcohol", "beer", "wine", "party", "pub")),
        CategoryIconItem("DirectionsCar", Icons.Default.DirectionsCar, listOf("car", "auto", "taxi", "vehicle", "fuel")),
        CategoryIconItem("DirectionsBus", Icons.Default.DirectionsBus, listOf("bus", "transit", "transport", "commute")),
        CategoryIconItem("TwoWheeler", Icons.Default.TwoWheeler, listOf("bike", "motorcycle", "scooter")),
        CategoryIconItem("Flight", Icons.Default.Flight, listOf("travel", "plane", "vacation", "trip", "airline")),
        CategoryIconItem("Home", Icons.Default.Home, listOf("house", "rent", "mortgage", "living")),
        CategoryIconItem("AccountBalance", Icons.Default.AccountBalance, listOf("bank", "government", "institution", "finance")),
        CategoryIconItem("Payments", Icons.Default.Payments, listOf("cash", "money", "salary", "income", "pay")),
        CategoryIconItem("Savings", Icons.Default.Savings, listOf("piggy", "invest", "deposit", "future")),
        CategoryIconItem("CreditCard", Icons.Default.CreditCard, listOf("card", "bank", "debt", "visa")),
        CategoryIconItem("Receipt", Icons.Default.Receipt, listOf("bill", "invoice", "tax", "utility")),
        CategoryIconItem("Lightbulb", Icons.Default.Lightbulb, listOf("electricity", "utilities", "idea", "power")),
        CategoryIconItem("Wifi", Icons.Default.Wifi, listOf("internet", "broadband", "network", "data")),
        CategoryIconItem("PhoneAndroid", Icons.Default.PhoneAndroid, listOf("mobile", "cell", "recharge", "phone")),
        CategoryIconItem("Tv", Icons.Default.Tv, listOf("television", "cable", "streaming")),
        CategoryIconItem("Subscriptions", Icons.Default.Subscriptions, listOf("netflix", "youtube", "service", "recurring")),
        CategoryIconItem("Movie", Icons.Default.Movie, listOf("cinema", "theater", "film", "entertainment")),
        CategoryIconItem("SportsEsports", Icons.Default.SportsEsports, listOf("gaming", "games", "playstation", "xbox")),
        CategoryIconItem("Headphones", Icons.Default.Headphones, listOf("music", "audio", "spotify", "podcast")),
        CategoryIconItem("FitnessCenter", Icons.Default.FitnessCenter, listOf("gym", "workout", "exercise", "health", "sports")),
        CategoryIconItem("MedicalServices", Icons.Default.MedicalServices, listOf("doctor", "hospital", "health", "medicine", "pharmacy")),
        CategoryIconItem("LocalHospital", Icons.Default.LocalHospital, listOf("clinic", "emergency", "health")),
        CategoryIconItem("Work", Icons.Default.Work, listOf("job", "office", "business", "salary", "freelance")),
        CategoryIconItem("School", Icons.Default.School, listOf("education", "tuition", "college", "university", "books", "study")),
        CategoryIconItem("Book", Icons.Default.Book, listOf("reading", "literature", "library")),
        CategoryIconItem("ChildCare", Icons.Default.ChildCare, listOf("baby", "kids", "family", "daycare")),
        CategoryIconItem("Pets", Icons.Default.Pets, listOf("dog", "cat", "vet", "animal", "pet food")),
        CategoryIconItem("CardGiftcard", Icons.Default.CardGiftcard, listOf("gift", "present", "birthday", "donation")),
        CategoryIconItem("Celebration", Icons.Default.Celebration, listOf("party", "festival", "holiday", "event")),
        CategoryIconItem("BeachAccess", Icons.Default.BeachAccess, listOf("vacation", "resort", "hotel", "beach", "summer")),
        CategoryIconItem("Hotel", Icons.Default.Hotel, listOf("stay", "lodging", "resort")),
        CategoryIconItem("Build", Icons.Default.Build, listOf("tools", "repair", "maintenance", "fix")),
        CategoryIconItem("Handyman", Icons.Default.Handyman, listOf("service", "renovation", "plumbing")),
        CategoryIconItem("LocalGasStation", Icons.Default.LocalGasStation, listOf("fuel", "petrol", "gas", "diesel")),
        CategoryIconItem("LocalLaundryService", Icons.Default.LocalLaundryService, listOf("laundry", "dry cleaning", "clothes", "washing")),
        CategoryIconItem("CleaningServices", Icons.Default.CleaningServices, listOf("maid", "housekeeping", "clean")),
        CategoryIconItem("Spa", Icons.Default.Spa, listOf("salon", "beauty", "massage", "barber", "wellness")),
        CategoryIconItem("SelfImprovement", Icons.Default.SelfImprovement, listOf("yoga", "meditation", "mindfulness")),
        CategoryIconItem("Store", Icons.Default.Store, listOf("shop", "vendor", "retail")),
        CategoryIconItem("Security", Icons.Default.Security, listOf("insurance", "shield", "safety")),
        CategoryIconItem("Category", Icons.Default.Category, listOf("general", "other", "misc"))
    )

    fun getIcon(iconName: String?): ImageVector {
        return availableIcons.firstOrNull { it.name.equals(iconName, ignoreCase = true) }?.icon
            ?: Icons.Default.Category
    }

    fun searchIcons(query: String): List<CategoryIconItem> {
        if (query.isBlank()) return availableIcons
        val cleanQuery = query.trim().lowercase()
        return availableIcons.filter { item ->
            item.name.lowercase().contains(cleanQuery) ||
                    item.tags.any { tag -> tag.lowercase().contains(cleanQuery) }
        }
    }
}
