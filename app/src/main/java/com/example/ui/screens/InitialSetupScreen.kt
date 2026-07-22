package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.auth.DriveBackupHelper
import com.example.data.local.*
import com.example.ui.ExpenseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSetupScreen(
    viewModel: ExpenseViewModel,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { UserPreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 6

    // Form states
    var userName by remember { mutableStateOf(prefsManager.userName.ifEmpty { "User" }) }
    var userAvatar by remember { mutableStateOf(prefsManager.userAvatar) }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }

    var selectedCurrency by remember {
        mutableStateOf(
            CurrencyData.currencies.find { it.code == prefsManager.currencyCode }
                ?: CurrencyData.currencies.first()
        )
    }

    var accountsList by remember { mutableStateOf(prefsManager.getAccounts().toMutableList()) }
    var paymentMethodsList by remember { mutableStateOf(prefsManager.getPaymentMethods().toMutableList()) }

    // Categories state setup
    var selectedCategories by remember {
        mutableStateOf(
            mutableListOf(
                Category(name = "Salary", type = TransactionType.INCOME, color = 0xFF4CAF50.toInt(), iconName = "Payments"),
                Category(name = "Freelance", type = TransactionType.INCOME, color = 0xFF8BC34A.toInt(), iconName = "Work"),
                Category(name = "Investments", type = TransactionType.INCOME, color = 0xFF009688.toInt(), iconName = "Savings"),
                Category(name = "Groceries", type = TransactionType.EXPENSE, color = 0xFFE91E63.toInt(), iconName = "ShoppingCart"),
                Category(name = "Rent", type = TransactionType.EXPENSE, color = 0xFFF44336.toInt(), iconName = "Home"),
                Category(name = "Utilities", type = TransactionType.EXPENSE, color = 0xFFFF9800.toInt(), iconName = "Lightbulb"),
                Category(name = "Entertainment", type = TransactionType.EXPENSE, color = 0xFF9C27B0.toInt(), iconName = "Movie"),
                Category(name = "Transport", type = TransactionType.EXPENSE, color = 0xFF3F51B5.toInt(), iconName = "DirectionsCar"),
                Category(name = "Bank to Cash", type = TransactionType.TRANSFER, color = 0xFF607D8B.toInt(), iconName = "AccountBalance"),
                Category(name = "Cash to Bank", type = TransactionType.TRANSFER, color = 0xFF795548.toInt(), iconName = "AccountBalance")
            )
        )
    }

    var googleAccountEmail by remember { mutableStateOf<String?>(null) }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                googleAccountEmail = account?.email
                prefsManager.googleAccountEmail = account?.email
                backupStatus = "Signed in as ${account?.email}. Drive Backup linked!"
            } catch (e: Exception) {
                backupStatus = "Google Sign-In skipped or failed."
            }
        }
        isSigningIn = false
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cash Ledger Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Step $currentStep of $totalSteps",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { currentStep / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = CircleShape,
                            modifier = Modifier.testTag("setup_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                // Save preferences
                                prefsManager.userName = userName.trim().ifEmpty { "User" }
                                prefsManager.userAvatar = selectedAvatarUri?.toString() ?: userAvatar
                                prefsManager.currencyCode = selectedCurrency.code
                                prefsManager.currencySymbol = selectedCurrency.symbol
                                prefsManager.saveAccounts(accountsList)
                                prefsManager.savePaymentMethods(paymentMethodsList)
                                prefsManager.isSetupCompleted = true

                                // Save categories to Room DB
                                coroutineScope.launch {
                                    selectedCategories.forEach { cat ->
                                        viewModel.addCategory(
                                            name = cat.name,
                                            type = cat.type,
                                            color = cat.color,
                                            iconName = cat.iconName
                                        )
                                    }
                                    onSetupComplete()
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("setup_next_button")
                    ) {
                        Text(if (currentStep == totalSteps) "Get Started" else "Next")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "SetupSteps"
            ) { step ->
                when (step) {
                    1 -> Step1UserNameAndAvatar(
                        userName = userName,
                        onNameChange = { userName = it },
                        selectedAvatar = userAvatar,
                        onAvatarSelect = { userAvatar = it; selectedAvatarUri = null },
                        selectedAvatarUri = selectedAvatarUri,
                        onUriSelect = { selectedAvatarUri = it }
                    )
                    2 -> Step2CurrencySelection(
                        selectedCurrency = selectedCurrency,
                        onCurrencySelect = { selectedCurrency = it }
                    )
                    3 -> Step3AccountsSetup(
                        accounts = accountsList,
                        onAccountsChanged = { accountsList = it.toMutableList() }
                    )
                    4 -> Step4CategoriesSetup(
                        categories = selectedCategories,
                        onCategoriesChanged = { selectedCategories = it.toMutableList() }
                    )
                    5 -> Step5PaymentMethodsSetup(
                        paymentMethods = paymentMethodsList,
                        onPaymentMethodsChanged = { paymentMethodsList = it.toMutableList() }
                    )
                    6 -> Step6GoogleBackupAndFinish(
                        googleEmail = googleAccountEmail,
                        backupStatus = backupStatus,
                        isSigningIn = isSigningIn,
                        onSignInClick = {
                            isSigningIn = true
                            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                            )
                                .requestEmail()
                                .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.appdata"))
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(client.signInIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PixelHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun Step1UserNameAndAvatar(
    userName: String,
    onNameChange: (String) -> Unit,
    selectedAvatar: String,
    onAvatarSelect: (String) -> Unit,
    selectedAvatarUri: Uri?,
    onUriSelect: (Uri?) -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onUriSelect(uri)
        }
    }

    val avatarPresets = listOf("Person", "Face", "AccountCircle", "Star", "Palette", "Smile", "Workspace", "Fitness")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            PixelHeader(
                icon = Icons.Default.Person,
                title = "Welcome to Cash Ledger",
                subtitle = "Set up your user profile for Cash Ledger."
            )
        }

        item {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { photoPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedAvatarUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedAvatarUri),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = getAvatarVector(selectedAvatar),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Choose photo from device", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = userName,
                onValueChange = onNameChange,
                label = { Text("Your Name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_name_input")
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Choose an avatar icon",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                avatarPresets.take(4).forEach { name ->
                    AvatarBadgeItem(
                        name = name,
                        isSelected = selectedAvatarUri == null && selectedAvatar == name,
                        onClick = { onAvatarSelect(name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                avatarPresets.drop(4).forEach { name ->
                    AvatarBadgeItem(
                        name = name,
                        isSelected = selectedAvatarUri == null && selectedAvatar == name,
                        onClick = { onAvatarSelect(name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AvatarBadgeItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getAvatarVector(name),
            contentDescription = name,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}

private fun getAvatarVector(name: String): ImageVector = when (name) {
    "Face" -> Icons.Default.Face
    "AccountCircle" -> Icons.Default.AccountCircle
    "Star" -> Icons.Default.Star
    "Palette" -> Icons.Default.Palette
    "Smile" -> Icons.Default.SentimentSatisfiedAlt
    "Workspace" -> Icons.Default.Work
    "Fitness" -> Icons.Default.FitnessCenter
    else -> Icons.Default.Person
}

@Composable
private fun Step2CurrencySelection(
    selectedCurrency: CurrencyItem,
    onCurrencySelect: (CurrencyItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) CurrencyData.currencies
        else CurrencyData.currencies.filter {
            it.code.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.symbol.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        PixelHeader(
            icon = Icons.Default.Payments,
            title = "Choose Currency",
            subtitle = "Select your primary currency. All account totals will use this symbol."
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search currency by code or name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) } }
            } else null,
            shape = CircleShape,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList) { currency ->
                val isSelected = currency.code == selectedCurrency.code
                Surface(
                    onClick = { onCurrencySelect(currency) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currency.symbol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "${currency.code} - ${currency.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Symbol: ${currency.symbol}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { onCurrencySelect(currency) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step3AccountsSetup(
    accounts: List<AccountItem>,
    onAccountsChanged: (List<AccountItem>) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddEditAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, iconName ->
                val newAcc = AccountItem(id = System.currentTimeMillis().toString(), name = name, iconName = iconName)
                onAccountsChanged(accounts + newAcc)
                showAddDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PixelHeader(
                icon = Icons.Default.AccountBalance,
                title = "Setup Accounts",
                subtitle = "Set up your primary spending and saving accounts."
            )
        }

        items(accounts) { acc ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getAccountIcon(acc.iconName),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = acc.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (accounts.size > 1) {
                        IconButton(onClick = { onAccountsChanged(accounts.filter { it.id != acc.id }) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Account")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Step4CategoriesSetup(
    categories: List<Category>,
    onCategoriesChanged: (List<Category>) -> Unit
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, type, iconName ->
                val newCat = Category(name = name, type = type, iconName = iconName)
                onCategoriesChanged(categories + newCat)
                showAddCategoryDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PixelHeader(
                icon = Icons.Default.Category,
                title = "Setup Categories",
                subtitle = "Pre-configured expense & income categories. Toggle or add yours."
            )
        }

        items(categories) { cat ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(cat.color).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getAccountIcon(cat.iconName),
                                contentDescription = null,
                                tint = Color(cat.color)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(cat.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(cat.type.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    IconButton(onClick = { onCategoriesChanged(categories.filter { it != cat }) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { showAddCategoryDialog = true },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Category")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Step5PaymentMethodsSetup(
    paymentMethods: List<PaymentMethodItem>,
    onPaymentMethodsChanged: (List<PaymentMethodItem>) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddPaymentMethodDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, iconName ->
                val newPm = PaymentMethodItem(id = System.currentTimeMillis().toString(), name = name, iconName = iconName)
                onPaymentMethodsChanged(paymentMethods + newPm)
                showAddDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PixelHeader(
                icon = Icons.Default.Payments,
                title = "Payment Methods",
                subtitle = "Configure payment modes (UPI, Cards, Cash, Bank Transfer)."
            )
        }

        items(paymentMethods) { pm ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getPaymentIcon(pm.iconName),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(pm.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }

                    if (paymentMethods.size > 1) {
                        IconButton(onClick = { onPaymentMethodsChanged(paymentMethods.filter { it.id != pm.id }) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Payment Method")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Step6GoogleBackupAndFinish(
    googleEmail: String?,
    backupStatus: String?,
    isSigningIn: Boolean,
    onSignInClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            PixelHeader(
                icon = Icons.Default.CloudUpload,
                title = "Google Drive Backup",
                subtitle = "Optionally sign in with Google to automatically backup your expense records to Drive."
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Device Storage First",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your financial data is saved 100% locally on your phone database. Cloud backup is optional and can be enabled or skipped anytime in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            if (googleEmail != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Linked: $googleEmail",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onSignInClick,
                    enabled = !isSigningIn,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign in with Google", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                }
            }

            backupStatus?.let { status ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AddEditAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("AccountBalance") }
    val iconOptions = listOf("AccountBalance", "AccountBalanceWallet", "LocalAtm", "CreditCard", "Savings", "Payments", "Receipt", "Work")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. Savings Bank)") },
                    singleLine = true
                )
                Text("Select Icon", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconOptions.take(4).forEach { iconName ->
                        IconButton(onClick = { selectedIcon = iconName }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (selectedIcon == iconName) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    getAccountIcon(iconName),
                                    contentDescription = null,
                                    tint = if (selectedIcon == iconName) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconOptions.drop(4).forEach { iconName ->
                        IconButton(onClick = { selectedIcon = iconName }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (selectedIcon == iconName) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    getAccountIcon(iconName),
                                    contentDescription = null,
                                    tint = if (selectedIcon == iconName) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedIcon) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddPaymentMethodDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("CreditCard") }
    val iconOptions = listOf("QrCode", "CreditCard", "Payments", "AccountBalance", "Payment", "Smartphone")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment Method") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Apple Pay / GPay)") },
                    singleLine = true
                )
                Text("Select Icon", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconOptions.forEach { iconName ->
                        IconButton(onClick = { selectedIcon = iconName }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (selectedIcon == iconName) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    getPaymentIcon(iconName),
                                    contentDescription = null,
                                    tint = if (selectedIcon == iconName) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedIcon) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: TransactionType, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedIcon by remember { mutableStateOf("ShoppingCart") }
    val iconOptions = listOf("ShoppingCart", "Home", "Lightbulb", "Movie", "DirectionsCar", "Payments", "Work", "Savings")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("Expense") }
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("Income") }
                    )
                }
                Text("Icon", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconOptions.take(4).forEach { iconName ->
                        IconButton(onClick = { selectedIcon = iconName }) {
                            Icon(getAccountIcon(iconName), contentDescription = null)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedType, selectedIcon) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

fun getAccountIcon(name: String): ImageVector = when (name) {
    "AccountBalance" -> Icons.Default.AccountBalance
    "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
    "LocalAtm" -> Icons.Default.LocalAtm
    "CreditCard" -> Icons.Default.CreditCard
    "Savings" -> Icons.Default.Savings
    "Payments" -> Icons.Default.Payments
    "Receipt" -> Icons.Default.Receipt
    "Work" -> Icons.Default.Work
    "ShoppingCart" -> Icons.Default.ShoppingCart
    "Home" -> Icons.Default.Home
    "Lightbulb" -> Icons.Default.Lightbulb
    "Movie" -> Icons.Default.Movie
    "DirectionsCar" -> Icons.Default.DirectionsCar
    else -> Icons.Default.Category
}

fun getPaymentIcon(name: String): ImageVector = when (name) {
    "QrCode" -> Icons.Default.QrCode
    "CreditCard" -> Icons.Default.CreditCard
    "Payments" -> Icons.Default.Payments
    "AccountBalance" -> Icons.Default.AccountBalance
    "Payment" -> Icons.Default.Payment
    "Smartphone" -> Icons.Default.Smartphone
    else -> Icons.Default.Payments
}
