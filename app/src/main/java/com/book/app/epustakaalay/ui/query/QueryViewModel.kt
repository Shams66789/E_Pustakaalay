package com.book.app.epustakaalay.ui.query

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.book.app.epustakaalay.others.Request
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class QueryViewModel(application: Application) : AndroidViewModel(application) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val _data: MutableLiveData<List<Request>> = MutableLiveData()
    val data: LiveData<List<Request>> get() = _data

    fun fetchDataFromFirebase() {
        val dataList = ArrayList<Request>()
        database.child("Request").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val firstName = childSnapshot.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = childSnapshot.child("lastName").getValue(String::class.java) ?: ""
                    val email = childSnapshot.child("email").getValue(String::class.java) ?: ""
                    val bookDetails = childSnapshot.child("bookDetail").getValue(String::class.java) ?: ""

                    val myData = Request(firstName, lastName, email, bookDetails)
                    dataList.add(myData)
                }
                _data.value = dataList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("QueryViewModel", "Database error: ${error.message}")
                // You can also show a Toast or Snackbar to notify the user about the error
                // For example:
                Toast.makeText(getApplication(), "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}