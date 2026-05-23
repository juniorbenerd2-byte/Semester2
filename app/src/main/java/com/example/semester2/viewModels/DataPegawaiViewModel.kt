package com.example.semester2.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.semester2.model.ModelPegawai
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataPegawaiViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")
    
    val pegawaiList = MutableLiveData<ArrayList<ModelPegawai>>()
    private var originalPegawaiList = ArrayList<ModelPegawai>()
    
    private val searchQuery = MutableLiveData<String?>()
    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    private var valueEventListener: ValueEventListener? = null

    init {
        getData()
    }

    fun getData() {
        isLoading.value = true
        
        // Remove existing listener if any before adding a new one
        valueEventListener?.let { myRef.removeEventListener(it) }

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                val list = ArrayList<ModelPegawai>()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(ModelPegawai::class.java)
                        if (pegawai != null) {
                            list.add(pegawai)
                        }
                    }
                }
                originalPegawaiList.clear()
                originalPegawaiList.addAll(list)
                
                // If there's a filter query active, re-apply it; otherwise post original list
                filterList(searchQuery.value)
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("DataPegawaiViewModel", "Database error: ${error.message}")
            }
        }

        myRef.addValueEventListener(valueEventListener!!)
    }

    fun filterList(query: String?) {
        searchQuery.value = query
        if (query.isNullOrEmpty()) {
            pegawaiList.value = originalPegawaiList
            isSearchEmpty.value = originalPegawaiList.isEmpty()
        } else {
            val filteredList = originalPegawaiList.filter {
                it.namaPegawai?.lowercase()?.contains(query.lowercase()) == true ||
                it.rolePegawai?.lowercase()?.contains(query.lowercase()) == true
            }
            pegawaiList.value = ArrayList(filteredList)
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up Firebase listener to prevent memory leaks when ViewModel is destroyed
        valueEventListener?.let {
            myRef.removeEventListener(it)
        }
    }
}