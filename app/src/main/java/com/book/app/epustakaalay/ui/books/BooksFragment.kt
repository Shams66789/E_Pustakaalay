package com.book.app.epustakaalay.ui.books

import android.R
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.book.app.epustakaalay.databinding.FragmentBooksBinding
import com.book.app.epustakaalay.ui.books.BooksViewModel.SaveState
import com.book.app.epustakaalay.others.Book
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BooksFragment : Fragment() {
    private lateinit var selectCategory: String
    private var _binding: FragmentBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BooksViewModel
    private lateinit var progressBar: ProgressBar

    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val fullFileName = getFileNameFromUri(uri)
            //TextView5
            val editableName : Editable = Editable.Factory.getInstance().newEditable(fullFileName)
            binding.textView5.setText(editableName)

            //Tittle
            val fileNameWithoutExtension = fullFileName.removeSuffix(".pdf")
            binding.textInputLayout.editText?.setText(fileNameWithoutExtension)
            // Call the uploadPdf function and handle callbacks
            viewModel.uploadPdf(it,
                { pdfUrl, imageUrl ->
                    // Update the PDF URL in the TextInputEditText field
                    binding.textInputLayout3.editText?.setText(pdfUrl)
                    binding.pdfURLEditText.isEnabled = false
                    // Update the Image URL in the TextInputEditText field
                    binding.textInputLayout2.editText?.setText(imageUrl)
                    binding.imageURLEditText.isEnabled = false
                }
            )
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBooksBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = ViewModelProvider(this).get(BooksViewModel::class.java)
        progressBar = binding.progressBar2

        // DropDown
        val filledExposedDropdown = binding.filledExposedDropdown
        val options = arrayOf("Class 9&10 CBSE", "Class 9&10 ICSE", "Class 11&12 NEET", "Class 11&12 JEE")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, options)
        filledExposedDropdown.setAdapter(adapter)
        filledExposedDropdown.setOnItemClickListener { parent, view, position, id ->
            // Retrieve the selected item
            selectCategory = parent.getItemAtPosition(position).toString()
            // You can also do whatever you need with the selected option here
        }

        // Date
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        val editableDate: Editable = Editable.Factory.getInstance().newEditable(formattedDate)
        binding.date.editText?.text = editableDate
        binding.dateEditText.isEnabled = false   //Disabling edit

        // Floating action button click listener
        binding.floatingActionButton.setOnClickListener {
            openFilePicker()
        }

        // Button click listener
        binding.button.setOnClickListener {
            // Retrieve data from TextInputEditText fields
            val tittle = binding.textInputLayout.editText?.text.toString()
            val pdfUrl = binding.textInputLayout3.editText?.text.toString()
            val imageUrl = binding.textInputLayout2.editText?.text.toString()

            // Create a Book object with the retrieved data
            val book = Book(tittle, selectCategory, formattedDate, pdfUrl, imageUrl)

            // Send the book data to the ViewModel to save in Firebase Realtime Database
            viewModel.saveBook(book)
        }


        //send successful
        viewModel.saveState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is SaveState.Loading -> showLoading(true)
                is SaveState.Success -> {
                    showLoading(false)
                    clearTexts()
                }
                is SaveState.Error -> {
                    showLoading(false)
                    // Handle error state
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        })

        return root
    }

    private fun openFilePicker() {
        pickPdfLauncher.launch("application/pdf")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }



    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val fileNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (fileNameIndex != -1) {
                    return it.getString(fileNameIndex)
                }
            }
        }
        return "Unknown File"
    }

    private fun clearTexts() {
        binding.textInputLayout.editText?.text = null
        binding.textInputLayout1.editText?.text = null
        binding.textInputLayout2.editText?.text = null
        binding.textInputLayout3.editText?.text = null
        binding.textView5.text = " "
    }
}