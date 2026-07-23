package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPreferencesManager
import com.example.data.local.TransactionType
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoogleSansCode
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.CategoryIcons
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefsManager = remember { UserPreferencesManager.getInstance(context) }

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    
    val accounts = remember { prefsManager.getAccounts().map { it.name }.ifEmpty { listOf("Cash", "Wallet", "Bank") } }
    val paymentMethods = remember { prefsManager.getPaymentMethods().map { it.name }.ifEmpty { listOf("Cash", "UPI", "Visa", "Mastercard", "Bank Transfer") } }

    var paymentMethod by remember { mutableStateOf(paymentMethods.firstOrNull() ?: "Cash") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var account by remember { mutableStateOf(accounts.firstOrNull() ?: "Cash") }
    var toAccount by remember { mutableStateOf(accounts.getOrNull(1) ?: accounts.firstOrNull() ?: "Bank") }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var toAccountExpanded by remember { mutableStateOf(false) }
    
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    val timePickerState = rememberTimePickerState()
    
    val categories by viewModel.categories.collectAsState()
    val availableCategories = categories.filter { it.type == type }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(account, type) {
        if (paymentMethod !in paymentMethods) {
            paymentMethod = paymentMethods.firstOrNull() ?: "Cash"
        }
        if (type == TransactionType.TRANSFER && toAccount == account) {
            toAccount = accounts.firstOrNull { it != account } ?: account
        }
    }
    
    LaunchedEffect(availableCategories) {
        if (availableCategories.isNotEmpty() && selectedCategoryId !in availableCategories.map { it.id }) {
            selectedCategoryId = availableCategories.first().id
        }
    }

    val typeColor by animateColorAsState(
        targetValue = when(type) {
            TransactionType.EXPENSE -> ExpenseRed
            TransactionType.INCOME -> IncomeGreen
            TransactionType.TRANSFER -> Color(0xFF2196F3)
        },
        animationSpec = tween(300),
        label = "typeColor"
    )

    val typeContainerColor by animateColorAsState(
        targetValue = when(type) {
            TransactionType.EXPENSE -> ExpenseRed.copy(alpha = 0.12f)
            TransactionType.INCOME -> IncomeGreen.copy(alpha = 0.12f)
            TransactionType.TRANSFER -> Color(0xFF2196F3).copy(alpha = 0.12f)
        },
        animationSpec = tween(300),
        label = "typeContainerColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        FormLeadingIcon(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.onSurface,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selector (Material 3 Segmented Button with semantic colors)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                val types = TransactionType.values()
                types.forEachIndexed { index, t ->
                    val (activeBg, activeContent, activeBorder) = when (t) {
                        TransactionType.EXPENSE -> Triple(
                            ExpenseRed.copy(alpha = 0.18f),
                            ExpenseRed,
                            ExpenseRed.copy(alpha = 0.5f)
                        )
                        TransactionType.INCOME -> Triple(
                            IncomeGreen.copy(alpha = 0.18f),
                            IncomeGreen,
                            IncomeGreen.copy(alpha = 0.5f)
                        )
                        TransactionType.TRANSFER -> Triple(
                            Color(0xFF2196F3).copy(alpha = 0.18f),
                            Color(0xFF2196F3),
                            Color(0xFF2196F3).copy(alpha = 0.5f)
                        )
                    }

                    SegmentedButton(
                        selected = type == t,
                        onClick = { type = t },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = activeBg,
                            activeContentColor = activeContent,
                            activeBorderColor = activeBorder,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        icon = {
                            SegmentedButtonDefaults.Icon(
                                active = type == t,
                                activeContent = {
                                    val typeIcon = when (t) {
                                        TransactionType.EXPENSE -> Icons.Default.TrendingDown
                                        TransactionType.INCOME -> Icons.Default.TrendingUp
                                        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                    }
                                    Icon(
                                        imageVector = typeIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                                        tint = activeContent
                                    )
                                },
                                inactiveContent = {
                                    val typeIcon = when (t) {
                                        TransactionType.EXPENSE -> Icons.Default.TrendingDown
                                        TransactionType.INCOME -> Icons.Default.TrendingUp
                                        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                    }
                                    Icon(
                                        imageVector = typeIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        },
                        label = {
                            Text(
                                t.name.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (type == t) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Amount Input Card (Hero card with animated semantic theme)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(28.dp),
                color = typeContainerColor,
                border = BorderStroke(1.dp, typeColor.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (type) {
                            TransactionType.EXPENSE -> "Expense Amount"
                            TransactionType.INCOME -> "Income Amount"
                            TransactionType.TRANSFER -> "Transfer Amount"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = typeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = prefsManager.currencySymbol,
                            style = MaterialTheme.typography.displayMedium,
                            color = typeColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GoogleSansCode
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            textStyle = MaterialTheme.typography.displayMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                fontFamily = GoogleSansCode
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (amountStr.isEmpty()) {
                                    Text(
                                        "0",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = GoogleSansCode
                                    )
                                } else {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // Section 1: Transaction Details (Title & Category)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        placeholder = { Text("e.g. Grocery Shopping") },
                        leadingIcon = { FormLeadingIcon(icon = Icons.Default.Notes) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Category Selection (If Expense or Income)
                    AnimatedVisibility(
                        visible = type != TransactionType.TRANSFER,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val selectedCat = availableCategories.find { it.id == selectedCategoryId }
                        val getCategoryDisplayName: (com.example.data.local.Category?) -> String = { cat ->
                            if (cat == null) "Select Category"
                            else if (cat.parentId != null) {
                                val parent = categories.find { it.id == cat.parentId }
                                if (parent != null) "${parent.name} › ${cat.name}" else cat.name
                            } else cat.name
                        }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = getCategoryDisplayName(selectedCat),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                leadingIcon = {
                                    FormLeadingIcon(
                                        icon = CategoryIcons.getIcon(selectedCat?.iconName),
                                        tint = selectedCat?.let { Color(it.color) } ?: MaterialTheme.colorScheme.onSurfaceVariant,
                                        containerColor = selectedCat?.let { Color(it.color).copy(alpha = 0.15f) } ?: MaterialTheme.colorScheme.surfaceContainerHighest
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(16.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                availableCategories.forEach { cat ->
                                    val isSub = cat.parentId != null
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = getCategoryDisplayName(cat), 
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (isSub) FontWeight.Normal else FontWeight.SemiBold
                                            ) 
                                        },
                                        leadingIcon = {
                                            FormLeadingIcon(
                                                icon = CategoryIcons.getIcon(cat.iconName),
                                                tint = Color(cat.color),
                                                containerColor = Color(cat.color).copy(alpha = 0.15f)
                                            )
                                        },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Account & Payment
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (type == TransactionType.TRANSFER) "Accounts" else "Account & Payment",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    if (type == TransactionType.TRANSFER) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = accountExpanded,
                                    onExpandedChange = { accountExpanded = !accountExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = account,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("From") },
                                        leadingIcon = { FormLeadingIcon(icon = Icons.Default.AccountBalance) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = accountExpanded,
                                        onDismissRequest = { accountExpanded = false },
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        accounts.forEach { acc ->
                                            DropdownMenuItem(
                                                text = { Text(acc, style = MaterialTheme.typography.bodyLarge) },
                                                leadingIcon = { FormLeadingIcon(icon = Icons.Default.AccountBalance) },
                                                onClick = {
                                                    account = acc
                                                    accountExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = toAccountExpanded,
                                    onExpandedChange = { toAccountExpanded = !toAccountExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = toAccount,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("To") },
                                        leadingIcon = { FormLeadingIcon(icon = Icons.Default.AccountBalance) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toAccountExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = toAccountExpanded,
                                        onDismissRequest = { toAccountExpanded = false },
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        accounts.filter { it != account }.forEach { acc ->
                                            DropdownMenuItem(
                                                text = { Text(acc, style = MaterialTheme.typography.bodyLarge) },
                                                leadingIcon = { FormLeadingIcon(icon = Icons.Default.AccountBalance) },
                                                onClick = {
                                                    toAccount = acc
                                                    toAccountExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = accountExpanded,
                            onExpandedChange = { accountExpanded = !accountExpanded }
                        ) {
                            OutlinedTextField(
                                value = account,
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text(
                                        if (type == TransactionType.INCOME) "Account (Credit to)" else "Account (Debit from)"
                                    )
                                },
                                leadingIcon = { FormLeadingIcon(icon = Icons.Default.AccountBalance) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(16.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = accountExpanded,
                                onDismissRequest = { accountExpanded = false },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc, style = MaterialTheme.typography.bodyLarge) },
                                        leadingIcon = { FormLeadingIcon(icon = Icons.Default.AccountBalance) },
                                        onClick = {
                                            account = acc
                                            accountExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = paymentExpanded,
                            onExpandedChange = { paymentExpanded = !paymentExpanded }
                        ) {
                            OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Payment Method") },
                                leadingIcon = { FormLeadingIcon(icon = Icons.Default.Payments) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(16.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = paymentExpanded,
                                onDismissRequest = { paymentExpanded = false },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                paymentMethods.forEach { pm ->
                                    DropdownMenuItem(
                                        text = { Text(pm, style = MaterialTheme.typography.bodyLarge) },
                                        leadingIcon = { FormLeadingIcon(icon = Icons.Default.Payments) },
                                        onClick = {
                                            paymentMethod = pm
                                            paymentExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Schedule & Notes
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Schedule & Notes",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis)),
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Date") },
                                leadingIcon = { FormLeadingIcon(icon = Icons.Default.CalendarToday) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showTimePicker = true }
                        ) {
                            OutlinedTextField(
                                value = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dateMillis)),
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Time") },
                                leadingIcon = { FormLeadingIcon(icon = Icons.Default.Schedule) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        placeholder = { Text("e.g. food, dinner") },
                        leadingIcon = { FormLeadingIcon(icon = Icons.Default.Label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Add extra details...") },
                        leadingIcon = { FormLeadingIcon(icon = Icons.Default.Description) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Button
            val isFormValid = amountStr.isNotBlank() && 
                amountStr.toDoubleOrNull() != null && 
                (type == TransactionType.TRANSFER || selectedCategoryId != null) && 
                title.isNotBlank()

            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && (type == TransactionType.TRANSFER || selectedCategoryId != null) && title.isNotBlank()) {
                        viewModel.addTransaction(
                            title = title,
                            amount = amount,
                            description = description,
                            categoryId = selectedCategoryId ?: 0,
                            type = type,
                            dateMillis = dateMillis,
                            tags = tags,
                            paymentMethod = paymentMethod,
                            account = account,
                            toAccount = if (type == TransactionType.TRANSFER) toAccount else null
                        )
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = typeColor,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dateMillis = it
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = dateMillis
                        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        cal.set(Calendar.MINUTE, timePickerState.minute)
                        dateMillis = cal.timeInMillis
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }
}

@Composable
private fun FormLeadingIcon(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .size(36.dp)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
