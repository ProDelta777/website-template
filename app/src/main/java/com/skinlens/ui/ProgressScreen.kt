package com.skinlens.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.skinlens.data.AppDatabase
import com.skinlens.data.ProgressEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).progressDao() }
    val progressList by dao.getAllProgress().collectAsState(initial = emptyList())
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Progress") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete All")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (progressList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No progress saved yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(progressList) { item ->
                    ProgressCard(item = item)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete All Data") },
                text = { Text("Are you sure you want to delete all locally stored progress data? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            dao.deleteAllProgress()
                            showDeleteConfirm = false
                        }
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ProgressCard(item: ProgressEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.timestamp))
            Text(text = date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = Uri.parse(item.photoUri),
                    contentDescription = "Saved Photo",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Score: ${item.overallScore}/100", fontWeight = FontWeight.Bold)
                    Text("Oiliness: ${item.oiliness}", style = MaterialTheme.typography.bodySmall)
                    Text("Redness: ${item.redness}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
