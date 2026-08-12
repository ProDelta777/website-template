package com.skinlens.ui

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.skinlens.domain.IngredientScanner
import com.skinlens.domain.ScannerResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(navController: NavController) {
    val context = LocalContext.current
    val scanner = remember { IngredientScanner(context) }
    val coroutineScope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var scanResult by remember { mutableStateOf<ScannerResult?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isScanning = true
            coroutineScope.launch {
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, it)
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    } else {
                        val tmpBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                        tmpBitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                    }
                    scanResult = scanner.scanImage(bitmap)
                    inputText = scanResult?.rawText ?: ""
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isScanning = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check Ingredients") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Type an ingredient list or scan a label to learn about the ingredients based on our offline knowledge base.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Ingredient List") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = {
                    coroutineScope.launch {
                        scanResult = scanner.analyzeText(inputText)
                    }
                }) {
                    Text("Check Text")
                }

                OutlinedButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                    Text("Scan Label Photo")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isScanning) {
                CircularProgressIndicator()
            } else {
                scanResult?.let { result ->
                    Text("Found Educational Matches:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (result.matchedIngredients.isEmpty()) {
                        Text("No matching ingredients found in the local database.", color = MaterialTheme.colorScheme.error)
                    } else {
                        LazyColumn {
                            items(result.matchedIngredients) { ingredient ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = ingredient.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(text = "Category: ${ingredient.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Purpose: ${ingredient.generalPurpose}", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Warnings: ${ingredient.importantWarnings}", style = MaterialTheme.typography.bodySmall)
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
