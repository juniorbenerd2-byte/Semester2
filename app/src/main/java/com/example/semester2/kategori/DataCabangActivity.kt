package com.example.semester2.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.adapter.DetailCabangAdapter
import com.example.semester2.model.ModelCabang
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DataCabangActivity : AppCompatActivity() {

    private lateinit var rvDataCabang: RecyclerView
    private lateinit var tvKosongCabang: TextView
    private lateinit var fabTambahCabang: FloatingActionButton
    private lateinit var adapter: DetailCabangAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_cabang)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Sesi berakhir", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rvDataCabang = findViewById(R.id.rvDataCabang)
        tvKosongCabang = findViewById(R.id.tvKosongCabang)
        fabTambahCabang = findViewById(R.id.fabTambahCabang)

        database = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("cabang")

        adapter = DetailCabangAdapter(ArrayList())
        rvDataCabang.layoutManager = LinearLayoutManager(this)
        rvDataCabang.adapter = adapter

        adapter.setOnItemClickListener(object : DetailCabangAdapter.OnItemClickListener {
            override fun onItemClick(model: ModelCabang) {
                val intent = Intent(this@DataCabangActivity, CabangActivity::class.java)
                intent.putExtra("EXTRA_CABANG", model)
                startActivity(intent)
            }
        })

        fabTambahCabang.setOnClickListener {
            startActivity(Intent(this, CabangActivity::class.java))
        }

        fetchDataCabang()
    }

    private fun fetchDataCabang() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelCabang>()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(ModelCabang::class.java)
                        if (cabang != null) {
                            list.add(cabang)
                        }
                    }
                }

                if (list.isEmpty()) {
                    tvKosongCabang.visibility = View.VISIBLE
                    rvDataCabang.visibility = View.GONE
                } else {
                    tvKosongCabang.visibility = View.GONE
                    rvDataCabang.visibility = View.VISIBLE
                    adapter.updateData(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
