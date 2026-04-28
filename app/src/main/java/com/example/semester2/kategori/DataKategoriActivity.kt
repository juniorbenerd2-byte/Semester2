package com.example.semester2.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.adapter.DataKategoriAdapter
import com.example.semester2.viewModels.DataKategoriViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataKategoriActivity : AppCompatActivity() {

    private val viewModel: DataKategoriViewModel by viewModels()
    private lateinit var rvDataKategori: RecyclerView
    private lateinit var fabDataKategori: FloatingActionButton
    private lateinit var tvKosong: TextView
    private lateinit var searchView: SearchView
    private lateinit var adapter: DataKategoriAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_kategori)

        rvDataKategori = findViewById(R.id.rvDataKategori)
        fabDataKategori = findViewById(R.id.fabDataKategori)
        tvKosong = findViewById(R.id.tvKosong)
        searchView = findViewById(R.id.searchView)

        adapter = DataKategoriAdapter(ArrayList())
        adapter.setOnItemClickListener(object : DataKategoriAdapter.OnItemClickListener {
            override fun onItemClick(model: ModelKategori) {
                val intent = Intent(this@DataKategoriActivity, KategoriActivity::class.java)
                intent.putExtra("EXTRA_KATEGORI", model)
                startActivity(intent)
            }
        })
        rvDataKategori.layoutManager = LinearLayoutManager(this)
        rvDataKategori.adapter = adapter
        
        setupSearchView()
        observeViewModel()

        fabDataKategori.setOnClickListener {
            val intent = Intent(this, KategoriActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterList(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.kategoriList.observe(this) { list ->
            if (list.isNullOrEmpty()) {
                tvKosong.visibility = View.VISIBLE
                rvDataKategori.visibility = View.GONE
            } else {
                tvKosong.visibility = View.GONE
                rvDataKategori.visibility = View.VISIBLE
                adapter.updateData(list)
            }
        }
    }
}
