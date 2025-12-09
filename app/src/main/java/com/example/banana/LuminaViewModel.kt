package com.example.banana

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

class LuminaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val historyDao = db.historyDao()

    private val _feedImages = MutableStateFlow<List<RemoteImage>>(emptyList())
    val feedImages = _feedImages.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEntity>>(emptyList())
    val history = _history.asStateFlow()

    var selectedPhotoUri by mutableStateOf<Uri?>(null)
    var selectedTemplateRes by mutableStateOf(R.drawable.ic_launcher_background)
    var generatedBitmap by mutableStateOf<Bitmap?>(null)
    var isProcessing by mutableStateOf(false)

    init {
        fetchFeed()
        loadHistory()
    }

    private fun fetchFeed() {
        viewModelScope.launch {
            try {
                val images = RetrofitInstance.api.getImages()
                _feedImages.value = images
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            historyDao.getAllHistory().collect { list ->
                _history.value = list
            }
        }
    }

    fun processImage(context: Context, onComplete: () -> Unit) {
        if (selectedPhotoUri == null) return

        isProcessing = true
        viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(1500)

            val result = combineImages(context, selectedPhotoUri!!, selectedTemplateRes)
            generatedBitmap = result

            withContext(Dispatchers.Main) {
                isProcessing = false
                onComplete()
            }
        }
    }

    fun saveResult(context: Context) {
        generatedBitmap?.let { bmp ->
            viewModelScope.launch(Dispatchers.IO) {
                val savedUri = saveImageToGallery(context, bmp)
                if (savedUri != null) {
                    historyDao.insert(HistoryEntity(imageUri = savedUri, templateName = "Custom Template"))
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Saved & Added to History!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun combineImages(context: Context, userPhotoUri: Uri, templateResId: Int): Bitmap? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(userPhotoUri)
            val userBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val templateBitmap = try {
                BitmapFactory.decodeResource(context.resources, templateResId)
            } catch (e: Exception) { null }

            if (templateBitmap == null) return userBitmap

            val resultBitmap = Bitmap.createBitmap(templateBitmap.width, templateBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resultBitmap)

            canvas.drawBitmap(templateBitmap, 0f, 0f, null)

            val targetWidth = templateBitmap.width / 2
            val scale = targetWidth.toFloat() / userBitmap.width
            val targetHeight = (userBitmap.height * scale).toInt()
            val left = (templateBitmap.width - targetWidth) / 2f
            val top = (templateBitmap.height - targetHeight) / 2f

            val dstRect = RectF(left, top, left + targetWidth, top + targetHeight)
            canvas.drawBitmap(userBitmap, null, dstRect, null)

            resultBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap): String? {
        val filename = "Lumina_AI_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        return try {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { contentResolver.openOutputStream(it) }
            fos?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

            imageUri?.toString()
        } catch (e: Exception) {
            null
        } finally {
            fos?.close()
        }
    }
}