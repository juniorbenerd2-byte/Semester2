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
import com.example.semester2.adapter.DataLayananAdapter
import com.example.semester2.model.ModelLayanan
import com.example.semester2.viewModels.DataLayananViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataLayananActivity : AppCompatActivity() {

    private val viewModel: DataLayananViewModel by viewModels()
    private lateinit var rvDataLayanan: RecyclerView
    private lateinit var fabDataLayanan: FloatingActionButton
    private lateinit var tvKosong: TextView
    private lateinit var searchView: SearchView
    private lateinit var adapter: DataLayananAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_layanan)

        rvDataLayanan = findViewById(R.id.rvDataLayanan)
        fabDataLayanan = findViewById(R.id.fabDataLayanan)
        tvKosong = findViewById(R.id.tvKosong)
        searchView = findViewById(R.id.searchView)

        adapter = DataLayananAdapter(ArrayList())
        adapter.setOnItemClickListener(object : DataLayananAdapter.OnItemClickListener {
            override fun onItemClick(model: ModelLayanan) {
                val intent = Intent(this@DataLayananActivity, LayananActivity::class.java)
                startActivity(intent)
            }
        })
        rvDataLayanan.layoutManager = LinearLayoutManager(this)
        rvDataLayanan.adapter = adapter
        
        setupSearchView()
        observeViewModel()

        fabDataLayanan.setOnClickListener {
            val intent = Intent(this, LayananActivity::class.java)
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
        viewModel.layananList.observe(this) { list ->
            if (list.isNullOrEmpty()) {
                tvKosong.visibility = View.VISIBLE
                rvDataLayanan.visibility = View.GONE
            } else {
                tvKosong.visibility = View.GONE
                rvDataLayanan.visibility = View.VISIBLE
                adapter.updateData(list)
            }
        }
    }
}
