package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.Category
import com.example.data.local.TransactionType
import com.example.ui.ExpenseViewModel
import com.example.ui.util.CategoryIcons
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedIconName by remember { mutableStateOf("ShoppingCart") }
    var iconSearchQuery by remember { mutableStateOf("") }
    
    // Subcategory state
    var isSubcategory by remember { mutableStateOf(false) }
    var selectedParentId by remember { mutableStateOf<Int?>(null) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }
    
    // Preset M3 category colors
    val colorOptions = listOf(
        Color(0xFFE91E63), // Pink/Expense
        Color(0xFFF44336), // Red
        Color(0xFFFF9800), // Orange
        Color(0xFF4CAF50), // Green
        Color(0xFF009688), // Teal
        Color(0xFF2196F3), // Blue
        Color(0xFF3F51B5), // Indigo
        Color(0xFF9C27B0), // Purple
        Color(0xFF607D8B)  // Blue Grey
    )
    var selectedColor by remember { mutableStateOf(colorOptions.first()) }
    var filterType by remember { mutableStateOf<TransactionType?>(null) }

    val categories by viewModel.categories.collectAsState()

    // Filter main categories (parentId == null) for subcategory dropdown
    val mainCategories = remember(categories) {
        categories.filter { it.parentId == null }
    }

    // Handle initial selection for subcategory if needed
    LaunchedEffect(isSubcategory) {
        if (isSubcategory) {
            if (selectedParentId == null && mainCategories.isNotEmpty()) {
                val parent = mainCategories.first()
                selectedParentId = parent.id
                selectedType = parent.type
                selectedColor = Color(parent.color)
            }
        } else {
            selectedParentId = null
        }
    }

    val filteredIcons = remember(iconSearchQuery) {
        CategoryIcons.searchIcons(iconSearchQuery)
    }

    // Filtered categories for existing list
    val filteredMainCategories = remember(categories, filterType) {
        val rootCats = categories.filter { it.parentId == null }
        if (filterType == null) rootCats else rootCats.filter { it.type == filterType }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New Category Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isSubcategory) "Create Subcategory" else "Create Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Category Level Selection (Main Category vs Subcategory)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !isSubcategory,
                                onClick = {
                                    isSubcategory = false
                                },
                                label = { Text("Main Category") },
                                shape = CircleShape
                            )
                            
                            FilterChip(
                                selected = isSubcategory,
                                onClick = {
                                    if (mainCategories.isNotEmpty()) {
                                        isSubcategory = true
                                    }
                                },
                                enabled = mainCategories.isNotEmpty(),
                                label = { 
                                    Text(if (mainCategories.isEmpty()) "Subcategory (No parents)" else "Subcategory") 
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.SubdirectoryArrowRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = CircleShape
                            )
                        }

                        // Parent Category Selector (If Subcategory)
                        if (isSubcategory && mainCategories.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = parentDropdownExpanded,
                                onExpandedChange = { parentDropdownExpanded = !parentDropdownExpanded }
                            ) {
                                val selectedParent = mainCategories.find { it.id == selectedParentId }
                                OutlinedTextField(
                                    value = selectedParent?.name ?: "Select Parent Category",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Parent Category") },
                                    leadingIcon = {
                                        selectedParent?.let { p ->
                                            Box(
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .size(32.dp)
                                                    .background(Color(p.color).copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = CategoryIcons.getIcon(p.iconName),
                                                    contentDescription = null,
                                                    tint = Color(p.color),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } ?: Icon(Icons.Default.Category, contentDescription = null)
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    shape = CircleShape
                                )

                                ExposedDropdownMenu(
                                    expanded = parentDropdownExpanded,
                                    onDismissRequest = { parentDropdownExpanded = false },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    mainCategories.forEach { parent ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .background(Color(parent.color).copy(alpha = 0.2f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = CategoryIcons.getIcon(parent.iconName),
                                                            contentDescription = null,
                                                            tint = Color(parent.color),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = parent.name,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "(${parent.type.name.lowercase().replaceFirstChar { it.uppercase() }})",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedParentId = parent.id
                                                selectedType = parent.type
                                                selectedColor = Color(parent.color)
                                                parentDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Transaction Type Segmented Button (Only for Main Category)
                        if (!isSubcategory) {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val types = TransactionType.values()
                                types.forEachIndexed { index, t ->
                                    SegmentedButton(
                                        selected = selectedType == t,
                                        onClick = { selectedType = t },
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
                        }

                        // Category Name Input
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(if (isSubcategory) "Subcategory Name" else "Category Name") },
                            placeholder = { Text(if (isSubcategory) "e.g. Vegetables, Gym Membership..." else "e.g. Shopping, Salary...") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .size(36.dp)
                                        .background(selectedColor.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryIcons.getIcon(selectedIconName),
                                        contentDescription = null,
                                        tint = selectedColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = CircleShape
                        )

                        // Icon Selection Section with Icon Search
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Category Icon",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = iconSearchQuery,
                                onValueChange = { iconSearchQuery = it },
                                placeholder = { Text("Search icons...", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (iconSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { iconSearchQuery = "" }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Clear",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            if (filteredIcons.isEmpty()) {
                                Text(
                                    text = "No icons found for \"$iconSearchQuery\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(filteredIcons, key = { it.name }) { iconItem ->
                                        val isSelected = iconItem.name == selectedIconName
                                        Surface(
                                            onClick = { selectedIconName = iconItem.name },
                                            shape = CircleShape,
                                            color = if (isSelected) selectedColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            border = if (isSelected) BorderStroke(2.dp, selectedColor) else null,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = iconItem.icon,
                                                    contentDescription = iconItem.name,
                                                    tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Color Picker Row
                        Column {
                            Text(
                                text = "Color Badge",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(colorOptions) { color ->
                                    val isSelected = color == selectedColor
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .then(
                                                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                                else Modifier
                                            )
                                            .clickable { selectedColor = color },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add Button
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    viewModel.addCategory(
                                        name = name,
                                        type = selectedType,
                                        parentId = if (isSubcategory) selectedParentId else null,
                                        color = selectedColor.toArgb(),
                                        iconName = selectedIconName
                                    )
                                    name = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = CircleShape,
                            enabled = name.isNotBlank() && (!isSubcategory || selectedParentId != null)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSubcategory) "Add Subcategory" else "Add Category",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // Existing Categories Header & Filter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Existing Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredMainCategories.size} main categories",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == null,
                        onClick = { filterType = null },
                        label = { Text("All") },
                        shape = CircleShape
                    )
                    FilterChip(
                        selected = filterType == TransactionType.EXPENSE,
                        onClick = { filterType = if (filterType == TransactionType.EXPENSE) null else TransactionType.EXPENSE },
                        label = { Text("Expense") },
                        shape = CircleShape
                    )
                    FilterChip(
                        selected = filterType == TransactionType.INCOME,
                        onClick = { filterType = if (filterType == TransactionType.INCOME) null else TransactionType.INCOME },
                        label = { Text("Income") },
                        shape = CircleShape
                    )
                    FilterChip(
                        selected = filterType == TransactionType.TRANSFER,
                        onClick = { filterType = if (filterType == TransactionType.TRANSFER) null else TransactionType.TRANSFER },
                        label = { Text("Transfer") },
                        shape = CircleShape
                    )
                }
            }

            // Category List with Subcategories
            items(filteredMainCategories, key = { it.id }) { cat ->
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(cat.id) {
                    isVisible = true
                }
                val subCats = categories.filter { it.parentId == cat.id }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(300))
                ) {
                    val catColor = Color(cat.color)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(catColor.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = CategoryIcons.getIcon(cat.iconName),
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (subCats.isNotEmpty()) {
                                            Text(
                                                text = "${subCats.size} subcategories",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Quick Add Subcategory Button
                                    IconButton(
                                        onClick = {
                                            isSubcategory = true
                                            selectedParentId = cat.id
                                            selectedType = cat.type
                                            selectedColor = Color(cat.color)
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Subcategory",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = when(cat.type) {
                                            TransactionType.EXPENSE -> com.example.ui.theme.ExpenseRed.copy(alpha = 0.15f)
                                            TransactionType.INCOME -> com.example.ui.theme.IncomeGreen.copy(alpha = 0.15f)
                                            TransactionType.TRANSFER -> Color(0xFF2196F3).copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = cat.type.name.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = when(cat.type) {
                                                TransactionType.EXPENSE -> com.example.ui.theme.ExpenseRed
                                                TransactionType.INCOME -> com.example.ui.theme.IncomeGreen
                                                TransactionType.TRANSFER -> Color(0xFF2196F3)
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Render Subcategories
                            if (subCats.isNotEmpty()) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    subCats.forEach { subCat ->
                                        val subCatColor = Color(subCat.color)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.SubdirectoryArrowRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(subCatColor.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = CategoryIcons.getIcon(subCat.iconName),
                                                    contentDescription = null,
                                                    tint = subCatColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = subCat.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
