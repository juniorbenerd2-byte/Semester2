package com.example.semester2.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.semester2.model.ModelLayanan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataLayananViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    private val myRef = database.getReference("users_data").child(userId).child("layanan")
    
    val layananList = MutableLiveData<ArrayList<ModelLayanan>>()
    private var originalLayananList = ArrayList<ModelLayanan>()
    
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
        
        valueEventListener?.let { myRef.removeEventListener(it) }

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                val list = ArrayList<ModelLayanan>()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val layanan = dataSnapshot.getValue(ModelLayanan::class.java)
                        if (layanan != null) {
                            list.add(layanan)
                        }
                    }
                }
                originalLayananList.clear()
                originalLayananList.addAll(list)
                filterList(searchQuery.value)
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("DataLayananViewModel", "Database error: ${error.message}")
            }
        }

        myRef.addValueEventListener(valueEventListener!!)
    }

    fun filterList(query: String?) {
        searchQuery.value = query
        if (query.isNullOrEmpty()) {
            layananList.value = originalLayananList
            isSearchEmpty.value = originalLayananList.isEmpty()
        } else {
            val filteredList = originalLayananList.filter {
                it.namaLayanan?.lowercase()?.contains(query.lowercase()) == true
            }
            layananList.value = ArrayList(filteredList)
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
