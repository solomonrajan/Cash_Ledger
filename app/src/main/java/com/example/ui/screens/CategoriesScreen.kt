package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.Category
import com.example.data.local.TransactionType
import com.example.ui.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("New Category Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape
            )
            
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == TransactionType.EXPENSE, 
                    onClick = { type = TransactionType.EXPENSE }, 
                    label = { Text("Expense") },
                    shape = CircleShape
                )
                FilterChip(
                    selected = type == TransactionType.INCOME, 
                    onClick = { type = TransactionType.INCOME }, 
                    label = { Text("Income") },
                    shape = CircleShape
                )
            }
            
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addCategory(name, type)
                        name = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape
            ) {
                Text("Add Category")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Existing Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(categories, key = { it.id }) { cat ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(cat.id) {
                        isVisible = true
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isVisible,
                        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                                androidx.compose.animation.slideInVertically(initialOffsetY = { 50 }, animationSpec = androidx.compose.animation.core.tween(300))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = CircleShape
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text(cat.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                    Text(cat.type.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
