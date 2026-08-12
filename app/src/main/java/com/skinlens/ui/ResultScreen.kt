package com.skinlens.ui

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.skinlens.data.AppDatabase
import com.skinlens.data.ProgressEntity
import com.skinlens.domain.SkinAnalysisResult
import com.skinlens.domain.SkinAnalysisSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, photoUri: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var analysisResult by remember { mutableStateOf<SkinAnalysisResult?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    val decodedUri = Uri.parse(Uri.decode(photoUri))

    LaunchedEffect(decodedUri) {
        withContext(Dispatchers.IO) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, decodedUri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                val tmpBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, decodedUri)
                tmpBitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
            }
            val system = SkinAnalysisSystem()
            val result = system.analyzeSkin(bitmap)
            analysisResult = result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skin Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val result = analysisResult
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Overall Appearance Score",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${result.overallScore}/100",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This is an informational app-generated score, NOT a medical health score.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Visible Characteristics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                CharacteristicRow("Apparent Oiliness", result.oiliness.label)
                CharacteristicRow("Apparent Dryness", result.dryness.label)
                CharacteristicRow("Visible Redness", result.redness.label)
                CharacteristicRow("Visible Uneven Texture", result.texture.label)
                CharacteristicRow("Visible Dark-spot-like Areas", result.darkSpots.label)
                CharacteristicRow("Visible Blemish-like Areas", result.blemishLike.label)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Simple Educational Routine", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Morning:", fontWeight = FontWeight.SemiBold)
                Text("1. Gentle cleansing\n2. Suitable moisturizer\n3. Broad-spectrum sunscreen", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Evening:", fontWeight = FontWeight.SemiBold)
                Text("1. Gentle cleansing\n2. Optional treatment (e.g. Niacinamide or gentle Exfoliant)\n3. Moisturizer/barrier support", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "Disclaimer: This is an image-based educational assessment, not a medical diagnosis. If you have persistent, painful, rapidly changing, or concerning skin problems, consult a qualified healthcare professional.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isSaving = true

                            // Copy file to internal storage so we have permanent access
                            val savedUri = withContext(Dispatchers.IO) {
                                try {
                                    val inputStream = context.contentResolver.openInputStream(decodedUri)
                                    val fileName = "skinlens_${System.currentTimeMillis()}.jpg"
                                    val file = File(context.filesDir, fileName)
                                    val outputStream = FileOutputStream(file)
                                    inputStream?.copyTo(outputStream)
                                    inputStream?.close()
                                    outputStream.close()
                                    Uri.fromFile(file).toString()
                                } catch (e: Exception) {
                                    decodedUri.toString() // fallback
                                }
                            }

                            val dao = AppDatabase.getDatabase(context).progressDao()
                            dao.insertProgress(
                                ProgressEntity(
                                    timestamp = System.currentTimeMillis(),
                                    photoUri = savedUri,
                                    overallScore = result.overallScore,
                                    oiliness = result.oiliness.label,
                                    dryness = result.dryness.label,
                                    redness = result.redness.label,
                                    texture = result.texture.label,
                                    darkSpots = result.darkSpots.label,
                                    blemishLike = result.blemishLike.label
                                )
                            )
                            saved = true
                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving && !saved
                ) {
                    Text(if (saved) "Saved to Progress" else "Save Progress")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { navController.navigate("ingredients") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Learn about Ingredient Categories")
                }
            }
        }
    }
}

@Composable
fun CharacteristicRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
