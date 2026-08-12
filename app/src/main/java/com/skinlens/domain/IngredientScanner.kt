package com.skinlens.domain

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class IngredientDbEntry(
    val name: String,
    val category: String,
    val generalPurpose: String,
    val suitableSkinConcerns: List<String>,
    val generalUsageInformation: String,
    val possibleIrritationConsiderations: String,
    val importantWarnings: String,
    val beginnersIntroduceGradually: Boolean
)

data class ScannerResult(
    val matchedIngredients: List<IngredientDbEntry>,
    val rawText: String
)

class IngredientScanner(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var knowledgeBase: List<IngredientDbEntry> = emptyList()

    init {
        loadKnowledgeBase()
    }

    private fun loadKnowledgeBase() {
        try {
            val jsonString = context.assets.open("ingredients.json").bufferedReader().use { it.readText() }
            knowledgeBase = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            knowledgeBase = emptyList()
        }
    }

    suspend fun scanImage(bitmap: Bitmap): ScannerResult = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()
        val rawText = result.text

        analyzeText(rawText)
    }

    suspend fun analyzeText(text: String): ScannerResult = withContext(Dispatchers.Default) {
        val lowerText = text.lowercase()

        // Find ingredients from knowledge base that are mentioned in the text
        val matches = knowledgeBase.filter { ingredient ->
            lowerText.contains(ingredient.name.lowercase())
        }

        ScannerResult(
            matchedIngredients = matches,
            rawText = text
        )
    }
}
