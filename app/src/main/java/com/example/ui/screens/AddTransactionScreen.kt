package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionType
import com.example.ui.ExpenseViewModel
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
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var account by remember { mutableStateOf("Cash") }
    var toAccount by remember { mutableStateOf("Bank") }
    
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
    
    val accounts = listOf("Cash", "Wallet", "Bank")
    val paymentMethods = when(account) {
        "Cash" -> listOf("Cash")
        "Wallet" -> listOf("Wallet")
        "Bank" -> listOf("UPI", "Visa", "Mastercard", "Rupay", "Bank Transfer")
        else -> listOf("Cash")
    }
    
    LaunchedEffect(account) {
        if (paymentMethod !in paymentMethods) {
            paymentMethod = paymentMethods.firstOrNull() ?: "Cash"
        }
    }
    
    LaunchedEffect(availableCategories) {
        if (availableCategories.isNotEmpty() && selectedCategoryId !in availableCategories.map { it.id }) {
            selectedCategoryId = availableCategories.first().id
        }
    }

    val typeColor = when(type) {
        TransactionType.EXPENSE -> com.example.ui.theme.ExpenseRed
        TransactionType.INCOME -> com.example.ui.theme.IncomeGreen
        TransactionType.TRANSFER -> Color(0xFF2196F3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Transaction Type Selector (Material 3 Segmented Button)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                val types = TransactionType.values()
                types.forEachIndexed { index, t ->
                    SegmentedButton(
                        selected = type == t,
                        onClick = { type = t },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                        label = {
                            Text(
                                t.name.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            // Amount Input Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = NumberFormat.getCurrencyInstance().currency?.symbol ?: "$",
                            style = MaterialTheme.typography.displayMedium,
                            color = typeColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            textStyle = MaterialTheme.typography.displayMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (amountStr.isEmpty()) {
                                    Text(
                                        "0",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Grocery Shopping") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = CircleShape
            )

            // Category Selection (If Expense or Income)
            if (type != TransactionType.TRANSFER) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = availableCategories.find { it.id == selectedCategoryId }?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = CircleShape
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name, style = MaterialTheme.typography.bodyLarge) },
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(cat.color)) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Account / Source Selection
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
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = CircleShape
                            )
                            ExposedDropdownMenu(
                                expanded = accountExpanded,
                                onDismissRequest = { accountExpanded = false },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc, style = MaterialTheme.typography.bodyLarge) },
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
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toAccountExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = CircleShape
                            )
                            ExposedDropdownMenu(
                                expanded = toAccountExpanded,
                                onDismissRequest = { toAccountExpanded = false },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc, style = MaterialTheme.typography.bodyLarge) },
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
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = CircleShape
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    account = acc
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date & Time Selection
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
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
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
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Payment Method Selection (if not Transfer)
            if (type != TransactionType.TRANSFER) {
                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = !paymentExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = CircleShape
                    )
                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        paymentMethods.forEach { pm ->
                            DropdownMenuItem(
                                text = { Text(pm, style = MaterialTheme.typography.bodyLarge) },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                                onClick = {
                                    paymentMethod = pm
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Tags
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("e.g. food, dinner") },
                leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = CircleShape
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Add extra details...") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 4,
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
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
                    .height(52.dp),
                shape = CircleShape,
                enabled = amountStr.isNotBlank() && amountStr.toDoubleOrNull() != null && (type == TransactionType.TRANSFER || selectedCategoryId != null) && title.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
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
