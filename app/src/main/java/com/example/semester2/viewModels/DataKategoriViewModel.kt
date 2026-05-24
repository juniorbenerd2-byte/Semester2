package com.example.semester2.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.semester2.kategori.ModelKategori
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataKategoriViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    private val myRef = database.getReference("users_data").child(userId).child("kategori")
    
    val kategoriList = MutableLiveData<ArrayList<ModelKategori>>()
    private var originalKategoriList = ArrayList<ModelKategori>()
    
    private val searchQuery = MutableLiveData<String?>()
    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    private var valueEventListener: ValueEventListener? = null

    init {
        if (userId.isNotEmpty()) {
            getData()
        }
    }

    fun getData() {
        isLoading.value = true
        
        // Remove existing listener if any
        valueEventListener?.let { myRef.removeEventListener(it) }

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                val list = ArrayList<ModelKategori>()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                        if (kategori != null) {
                            list.add(kategori)
                        }
                    }
                }
                originalKategoriList.clear()
                originalKategoriList.addAll(list)
                
                // Re-apply filter if query is active
                filterList(searchQuery.value)
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("DataKategoriViewModel", "Database error: ${error.message}")
            }
        }

        myRef.addValueEventListener(valueEventListener!!)
    }

    fun filterList(query: String?) {
        searchQuery.value = query
        if (query.isNullOrEmpty()) {
            kategoriList.value = originalKategoriList
            isSearchEmpty.value = originalKategoriList.isEmpty()
        } else {
            val filteredList = originalKategoriList.filter {
                it.namaKategori?.lowercase()?.contains(query.lowercase()) == true
            }
            kategoriList.value = ArrayList(filteredList)
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        valueEventListener?.let {
            myRef.removeEventListener(it)
        }
    }
}
