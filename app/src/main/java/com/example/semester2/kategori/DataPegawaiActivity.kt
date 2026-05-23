package com.example.semester2.kategori

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.adapter.DataPegawaiAdapter
import com.example.semester2.model.ModelPegawai
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataPegawaiActivity : AppCompatActivity() {

    private lateinit var rvDataPegawai: RecyclerView
    private lateinit var fabDataPegawai: FloatingActionButton
    private lateinit var tvKosongPegawai: TextView
    private lateinit var searchViewPegawai: SearchView
    private lateinit var adapter: DataPegawaiAdapter
    
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")
    private var originalList = ArrayList<ModelPegawai>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pegawai)

        rvDataPegawai = findViewById(R.id.rvDataPegawai)
        fabDataPegawai = findViewById(R.id.fabDataPegawai)
        tvKosongPegawai = findViewById(R.id.tvKosongPegawai)
        searchViewPegawai = findViewById(R.id.searchViewPegawai)

        adapter = DataPegawaiAdapter(ArrayList())
        adapter.setOnItemClickListener(object : DataPegawaiAdapter.OnItemClickListener {
            override fun onItemClick(model: ModelPegawai) {
                val intent = Intent(this@DataPegawaiActivity, PegawaiActivity::class.java)
                intent.putExtra("EXTRA_PEGAWAI", model)
                startActivity(intent)
            }
        })
        
        rvDataPegawai.layoutManager = LinearLayoutManager(this)
        rvDataPegawai.adapter = adapter
        
        setupSearchView()
        fetchData()

        fabDataPegawai.setOnClickListener {
            val intent = Intent(this, PegawaiActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchData() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelPegawai>()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(ModelPegawai::class.java)
                        if (pegawai != null) {
                            list.add(pegawai)
                        }
                    }
                }
                originalList.clear()
                originalList.addAll(list)
                updateUI(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataPegawaiActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun setupSearchView() {
        searchViewPegawai.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        if (query.isNullOrEmpty()) {
            updateUI(originalList)
        } else {
            val filtered = originalList.filter {
                it.namaPegawai?.lowercase()?.contains(query.lowercase()) == true ||
                it.rolePegawai?.lowercase()?.contains(query.lowercase()) == true
            }
            updateUI(filtered)
        }
    }

    private fun updateUI(list: List<ModelPegawai>) {
        if (list.isEmpty()) {
            tvKosongPegawai.visibility = View.VISIBLE
            rvDataPegawai.visibility = View.GONE
        } else {
            tvKosongPegawai.visibility = View.GONE
            rvDataPegawai.visibility = View.VISIBLE
            adapter.updateData(list)
        }
    }
}
