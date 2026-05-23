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
import com.example.semester2.adapter.DataPegawaiAdapter
import com.example.semester2.model.ModelPegawai
import com.example.semester2.viewModels.DataPegawaiViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataPegawaiActivity : AppCompatActivity() {

    private val viewModel: DataPegawaiViewModel by viewModels()
    private lateinit var rvDataPegawai: RecyclerView
    private lateinit var fabDataPegawai: FloatingActionButton
    private lateinit var tvKosongPegawai: TextView
    private lateinit var searchViewPegawai: SearchView
    private lateinit var adapter: DataPegawaiAdapter

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
        observeViewModel()

        fabDataPegawai.setOnClickListener {
            val intent = Intent(this, PegawaiActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearchView() {
        searchViewPegawai.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        viewModel.pegawaiList.observe(this) { list ->
            if (list.isNullOrEmpty()) {
                tvKosongPegawai.visibility = View.VISIBLE
                rvDataPegawai.visibility = View.GONE
            } else {
                tvKosongPegawai.visibility = View.GONE
                rvDataPegawai.visibility = View.VISIBLE
                adapter.updateData(list)
            }
        }
    }
}
