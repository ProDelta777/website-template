package com.skinlens.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.skinlens.domain.PhotoQualityChecker
import kotlinx.coroutines.launch
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(navController: NavController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var qualityError by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val qualityChecker = remember { PhotoQualityChecker() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            qualityError = null
            isAnalyzing = true

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

                    val result = qualityChecker.checkQuality(bitmap)
                    when (result) {
                        is PhotoQualityChecker.QualityResult.Good -> {
                            // Proceed to result screen and pass the URI
                            val uriString = Uri.encode(it.toString())
                            navController.navigate("result/$uriString") {
                                popUpTo("analysis") { inclusive = true }
                            }
                        }
                        is PhotoQualityChecker.QualityResult.Poor -> {
                            qualityError = result.reason
                        }
                    }
                } catch (e: Exception) {
                    qualityError = "Failed to load or process image."
                } finally {
                    isAnalyzing = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analyze Skin") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isAnalyzing) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Analyzing photo quality...")
            } else {
                Text(
                    text = "Upload a clear face photo.",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "For best results, use natural, even lighting and ensure your face is centered.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = { photoPickerLauncher.launch("image/*") }) {
                    Text("Select Photo")
                }

                if (qualityError != null) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = qualityError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
