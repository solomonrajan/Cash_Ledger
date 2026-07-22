package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionType
import com.example.data.local.TransactionWithCategory
import com.example.ui.ExpenseViewModel
import com.example.ui.util.CategoryIcons
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

import com.example.ui.theme.GoogleSansCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCategories: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var placeholderText by remember { mutableStateOf("Cash Ledger") }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        placeholderText = "Search transactions"
    }

    var selectedDateFilter by remember { mutableStateOf("This Month") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var selectedPaymentFilter by remember { mutableStateOf("All") }
    
    val filteredTransactions = remember(transactions, searchQuery, selectedDateFilter, selectedTypeFilter, selectedPaymentFilter) {
        val now = Calendar.getInstance()
        transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.transaction.dateMillis }
            val matchesDate = when (selectedDateFilter) {
                "This Month" -> cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                "Last Month" -> {
                    val lastMonth = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                    cal.get(Calendar.YEAR) == lastMonth.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == lastMonth.get(Calendar.MONTH)
                }
                else -> true
            }
            
            val matchesType = when (selectedTypeFilter) {
                "Income" -> tx.transaction.type == TransactionType.INCOME
                "Expense" -> tx.transaction.type == TransactionType.EXPENSE
                else -> true
            }
            
            val paymentMethod = tx.transaction.paymentMethod
            
            val matchesPayment = if (selectedPaymentFilter == "All") true else paymentMethod == selectedPaymentFilter
            
            val matchesSearch = tx.category.name.contains(searchQuery, ignoreCase = true) ||
                    tx.transaction.title.contains(searchQuery, ignoreCase = true) ||
                    tx.transaction.description.contains(searchQuery, ignoreCase = true) ||
                    tx.transaction.amount.toString().contains(searchQuery)
                    
            matchesDate && matchesType && matchesPayment && matchesSearch
        }
    }
    
    val balance = filteredTransactions.sumOf { 
        when (it.transaction.type) {
            TransactionType.INCOME -> it.transaction.amount
            TransactionType.EXPENSE -> -it.transaction.amount
            TransactionType.TRANSFER -> 0.0
        }
    }
    val income = filteredTransactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
    val expense = filteredTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
    
    val formatter = NumberFormat.getCurrencyInstance()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 2.dp
                ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(placeholderText) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile & Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
            }
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onNavigateToAdd, 
                modifier = Modifier.testTag("add_fab"),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(36.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 120.dp
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        var dateExpanded by remember { mutableStateOf(false) }
                        Box {
                            FilterChip(
                                selected = selectedDateFilter != "All Time",
                                onClick = { dateExpanded = true },
                                label = { Text(selectedDateFilter) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                shape = androidx.compose.foundation.shape.CircleShape,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                            DropdownMenu(
                                expanded = dateExpanded, 
                                onDismissRequest = { dateExpanded = false },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                listOf("All Time", "This Month", "Last Month").forEach { filter ->
                                    DropdownMenuItem(text = { Text(filter) }, onClick = { selectedDateFilter = filter; dateExpanded = false })
                                }
                            }
                        }
                    }
                    item {
                        var typeExpanded by remember { mutableStateOf(false) }
                        Box {
                            FilterChip(
                                selected = selectedTypeFilter != "All",
                                onClick = { typeExpanded = true },
                                label = { Text(if (selectedTypeFilter == "All") "Type" else selectedTypeFilter) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                shape = androidx.compose.foundation.shape.CircleShape,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                            DropdownMenu(
                                expanded = typeExpanded, 
                                onDismissRequest = { typeExpanded = false },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                listOf("All", "Income", "Expense").forEach { filter ->
                                    DropdownMenuItem(text = { Text(filter) }, onClick = { selectedTypeFilter = filter; typeExpanded = false })
                                }
                            }
                        }
                    }
                    item {
                        var paymentExpanded by remember { mutableStateOf(false) }
                        Box {
                            FilterChip(
                                selected = selectedPaymentFilter != "All",
                                onClick = { paymentExpanded = true },
                                label = { Text(if (selectedPaymentFilter == "All") "Payment" else selectedPaymentFilter) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                shape = androidx.compose.foundation.shape.CircleShape,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                            DropdownMenu(
                                expanded = paymentExpanded, 
                                onDismissRequest = { paymentExpanded = false },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                listOf("All", "Cash", "UPI", "Visa", "Mastercard", "Rupay").forEach { filter ->
                                    DropdownMenuItem(text = { Text(filter) }, onClick = { selectedPaymentFilter = filter; paymentExpanded = false })
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Expressive Net Worth Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(32.dp)
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Net Worth",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            formatter.format(balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontFamily = GoogleSansCode
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = com.example.ui.theme.IncomeGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Income", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatter.format(income), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.IncomeGreen, fontFamily = GoogleSansCode)
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = com.example.ui.theme.ExpenseRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Expense", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatter.format(expense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ExpenseRed, fontFamily = GoogleSansCode)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val maxExpense = filteredTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.maxOfOrNull { it.transaction.amount } ?: 0.0
                        
                        Column(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("Largest Expense", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(formatter.format(maxExpense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer, fontFamily = GoogleSansCode)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Transactions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${filteredTransactions.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer, fontFamily = GoogleSansCode)
                                }
                            }
                        }
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No transactions found.",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val groupedTransactions = filteredTransactions.groupBy { tx ->
                    val cal = Calendar.getInstance().apply { timeInMillis = tx.transaction.dateMillis }
                    SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(cal.time)
                }
                
                groupedTransactions.forEach { (date, txs) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = GoogleSansCode,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
                        )
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                txs.forEachIndexed { index, tx ->
                                    TransactionRowItem(tx = tx)
                                    if (index < txs.size - 1) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                            modifier = Modifier.padding(start = 80.dp, end = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}@Composable
fun RowScope.TransactionItemContent(tx: TransactionWithCategory, showDate: Boolean = false) {
    val formatter = NumberFormat.getCurrencyInstance()
    val icon = CategoryIcons.getIcon(tx.category.iconName)
    val tagColorBase = Color(
        red = (tx.category.name.hashCode() * 123 % 255) / 255f,
        green = (tx.category.name.hashCode() * 321 % 255) / 255f,
        blue = (tx.category.name.hashCode() * 213 % 255) / 255f
    )
    val isLight = MaterialTheme.colorScheme.surface.red > 0.5f
    val tagBgColor = tagColorBase.copy(alpha = if (isLight) 0.2f else 0.4f)
    val tagTextColor = if (isLight) tagColorBase.copy(alpha = 1f) else Color.White
    val iconBgColor = when (tx.transaction.type) {
        TransactionType.INCOME -> com.example.ui.theme.IncomeGreen
        TransactionType.EXPENSE -> com.example.ui.theme.ExpenseRed
        TransactionType.TRANSFER -> Color(0xFF2196F3)
    }
    val iconColor = Color.White

    Box(
        modifier = Modifier.size(48.dp).background(iconBgColor, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
    }
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
        Text(tx.category.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        val title = tx.transaction.title
        val paymentMethod = tx.transaction.paymentMethod
        
        val desc = if (title.isNotBlank()) "${tx.category.type.name.lowercase(Locale.getDefault())} • $title" else tx.category.type.name.lowercase(Locale.getDefault())
        Text(desc.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val formatStr = if (showDate) "MMM dd, yyyy • hh:mm a" else "hh:mm a"
            val timeStr = SimpleDateFormat(formatStr, Locale.getDefault()).format(Date(tx.transaction.dateMillis))
            Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontFamily = GoogleSansCode)
            
            Box(modifier = Modifier.background(tagBgColor, androidx.compose.foundation.shape.CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text("#${tx.category.name.lowercase(Locale.getDefault()).replace(" ", "")}", style = MaterialTheme.typography.labelSmall, color = tagTextColor, fontWeight = FontWeight.Bold)
            }
            
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, androidx.compose.foundation.shape.CircleShape).padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val officialLogoUrl = when (paymentMethod) {
                    "Visa" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Visa_Inc._logo.svg/200px-Visa_Inc._logo.svg.png"
                    "Mastercard" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Mastercard-logo.svg/200px-Mastercard-logo.svg.png"
                    "UPI" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/UPI-Logo-vector.svg/200px-UPI-Logo-vector.svg.png"
                    "Rupay" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/Rupay-Logo.png/200px-Rupay-Logo.png"
                    else -> null
                }
                
                if (officialLogoUrl != null) {
                    coil.compose.AsyncImage(
                        model = officialLogoUrl,
                        contentDescription = paymentMethod,
                        modifier = Modifier.height(10.dp).widthIn(max = 24.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    Text(paymentMethod, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                } else {
                    val pmIcon = Icons.Default.Payments
                    val pmColor = MaterialTheme.colorScheme.onSecondaryContainer
                    Icon(pmIcon, contentDescription = null, tint = pmColor, modifier = Modifier.size(10.dp))
                    Text(paymentMethod, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    Text(
        formatter.format(tx.transaction.amount),
        color = when (tx.transaction.type) {
            TransactionType.INCOME -> com.example.ui.theme.IncomeGreen
            TransactionType.EXPENSE -> com.example.ui.theme.ExpenseRed
            TransactionType.TRANSFER -> Color(0xFF2196F3)
        },
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyLarge,
        fontFamily = GoogleSansCode
    )
}

@Composable
fun TransactionRowItem(tx: TransactionWithCategory) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        TransactionDetailsDialog(tx = tx, onDismiss = { showDialog = false }, onEdit = { /* TODO */ })
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransactionItemContent(tx = tx)
    }
}

@Composable
fun TransactionItem(tx: TransactionWithCategory) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        TransactionRowItem(tx = tx)
    }
}

@Composable
fun TransactionDetailsDialog(tx: TransactionWithCategory, onDismiss: () -> Unit, onEdit: () -> Unit) {
    val formatter = NumberFormat.getCurrencyInstance()
    val timeFormatter = SimpleDateFormat("EEEE, dd MMMM yyyy • hh:mm a", Locale.getDefault())
    val isIncome = tx.category.type == TransactionType.INCOME
    val isTransfer = tx.category.type == TransactionType.TRANSFER
    val amountColor = when {
        isIncome -> com.example.ui.theme.IncomeGreen
        isTransfer -> Color(0xFF2196F3)
        else -> com.example.ui.theme.ExpenseRed
    }
    val amountPrefix = when {
        isIncome -> "+"
        isTransfer -> ""
        else -> "-"
    }
    val title = tx.transaction.title
    val paymentMethod = tx.transaction.paymentMethod

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Transaction Details", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$amountPrefix${formatter.format(tx.transaction.amount)}",
                    style = MaterialTheme.typography.displaySmall,
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansCode,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                HorizontalDivider()
                
                DetailRow("Category", tx.category.name, CategoryIcons.getIcon(tx.category.iconName))
                DetailRow("Title", title.ifBlank { "No title" }, Icons.Default.Notes)
                if (tx.transaction.description.isNotBlank()) {
                    DetailRow("Description", tx.transaction.description, Icons.Default.Notes)
                }
                if (tx.transaction.tags.isNotBlank()) {
                    DetailRow("Tags", tx.transaction.tags, Icons.Default.Label)
                }
                DetailRow("Payment Method", paymentMethod, Icons.Default.Payments)
                DetailRow("Date", timeFormatter.format(Date(tx.transaction.dateMillis)), Icons.Default.CalendarToday)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onEdit()
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


