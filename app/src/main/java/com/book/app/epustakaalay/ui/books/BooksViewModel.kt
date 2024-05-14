package com.book.app.epustakaalay.ui.books


import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.book.app.epustakaalay.others.Book
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class BooksViewModel(application: Application) : AndroidViewModel(application) {
    private val storageRef = FirebaseStorage.getInstance().reference
    private val databaseRef = FirebaseDatabase.getInstance().reference

    val saveState = MutableLiveData<SaveState>()

    fun uploadPdf(
        pdfUri: Uri,
        pdfUrlCallback: (String?, String?) -> Unit,
        errorCallback: ((String) -> Unit)? = null
    ) {
        // Upload the selected PDF file to Firebase Storage
        val pdfFileName = "${System.currentTimeMillis()}_${pdfUri.lastPathSegment}"
        val pdfRef = storageRef.child("Books/$pdfFileName")
        pdfRef.putFile(pdfUri)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded PDF file
                pdfRef.downloadUrl.addOnSuccessListener { pdfUrl ->
                    // Upload the first page of the PDF as an image
                    uploadPdfFirstPageAsImage(pdfUri, getApplication(),
                        { imageUrl ->
                            // Update the PDF URL in the TextInputEditText field
                            pdfUrlCallback(pdfUrl.toString(), imageUrl)
                        },
                        { errorMessage ->
                            // Handle error if errorCallback is provided
                            errorCallback?.invoke(errorMessage)
                        }
                    )
                }.addOnFailureListener {
                    errorCallback?.invoke("Failed to retrieve PDF URL.")
                }
            }
            .addOnFailureListener { exception ->
                // Handle error if errorCallback is provided
                errorCallback?.invoke(exception.message ?: "Unknown error occurred.")
            }
    }

    private fun uploadPdfFirstPageAsImage(
        pdfUri: Uri,
        context: Context,
        imageUrlCallback: (String?) -> Unit,
        errorCallback: (String) -> Unit
    ) {
        try {
            val parcelFileDescriptor: ParcelFileDescriptor? =
                context.contentResolver.openFileDescriptor(pdfUri, "r")
            parcelFileDescriptor?.let { pfd ->
                val pdfRenderer = PdfRenderer(pfd)
                val page: PdfRenderer.Page = pdfRenderer.openPage(0)

                // Adjust bitmap creation settings
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,  // Double the width for better resolution
                    page.height * 2, // Double the height for better resolution
                    Bitmap.Config.ARGB_8888
                )

                // Set anti-aliasing and filter options for smoother rendering
                val paint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                }

                // Render the page onto the bitmap
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE) // Set canvas background to white
                canvas.drawBitmap(bitmap, 0f, 0f, paint)

                // Adjust rendering mode for display
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Save the bitmap to a temporary file
                val imagePath = saveBitmap(bitmap, context)

                imagePath?.let { path ->
                    // Upload the image to Firebase Storage
                    val imageFileName = "${System.currentTimeMillis()}_${pdfUri.lastPathSegment}"
                    val imageRef = storageRef.child("images/$imageFileName")
                    imageRef.putFile(Uri.fromFile(File(path)))
                        .addOnSuccessListener { taskSnapshot ->
                            // Get the download URL of the uploaded image
                            imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                // Call the callback with the image URL
                                imageUrlCallback(imageUrl.toString())
                            }.addOnFailureListener {
                                errorCallback("Failed to retrieve image URL.")
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle error
                            errorCallback(exception.message ?: "Unknown error occurred.")
                        }
                } ?: errorCallback("Failed to save bitmap.")
            }
        } catch (e: IOException) {
            errorCallback("Failed to upload PDF first page as image.")
        }
    }

    private fun saveBitmap(bitmap: Bitmap, context: Context): String? {
        val imagePath = context.getExternalFilesDir(null)?.absolutePath + "/first_page.jpg"
        val file = File(imagePath)
        try {
            val outputStream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return file.absolutePath
    }

    sealed class SaveState {
        object Loading : SaveState()
        data class Success(val message: String) : SaveState()
        data class Error(val error: String) : SaveState()
    }


    fun saveBook(book: Book) {
        saveState.value = SaveState.Loading
        // Save the book data in Firebase Realtime Database
        val bookRef = databaseRef.child("Books").push()
        bookRef.setValue(book)
            .addOnSuccessListener {
                // Data saved successfully
                saveState.value = SaveState.Success("Data Send Successfully")
            }
            .addOnFailureListener { exception ->
                // Handle error
                saveState.value = SaveState.Error(exception.message ?: "Unknown error occurred.")
            }
    }
}