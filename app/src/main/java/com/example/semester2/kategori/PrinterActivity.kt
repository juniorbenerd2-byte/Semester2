package com.example.semester2.kategori

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.model.ModelReport
import com.example.semester2.model.ModelTrolly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException
import java.io.OutputStream
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class PrinterActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val deviceList = ArrayList<BluetoothDevice>()
    private lateinit var deviceAdapter: DeviceAdapter
    
    private lateinit var tvStatus: TextView
    private lateinit var tvName: TextView
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvKosong: TextView
    private lateinit var cardDeviceList: View
    
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_printer)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        initView()
        setupBluetoothList()
        setupHistoryList()
        checkPermissions()

        findViewById<ImageButton>(R.id.btnBackPrinter).setOnClickListener { finish() }
        
        findViewById<Button>(R.id.btnScanPrinter).setOnClickListener { 
            cardDeviceList.visibility = if (cardDeviceList.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            if (cardDeviceList.visibility == View.VISIBLE) startDiscovery()
        }

        fetchReceiptHistory()
    }

    private fun initView() {
        tvStatus = findViewById(R.id.tvPrinterStatus)
        tvName = findViewById(R.id.tvPrinterName)
        rvHistory = findViewById(R.id.rvReceiptHistory)
        tvKosong = findViewById(R.id.tvKosongNota)
        cardDeviceList = findViewById(R.id.cardDeviceList)
    }

    private fun setupBluetoothList() {
        val rv = findViewById<RecyclerView>(R.id.rvBluetoothDevices)
        deviceAdapter = DeviceAdapter(deviceList) { connectToDevice(it) }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = deviceAdapter
    }

    private fun setupHistoryList() {
        rvHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchReceiptHistory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val myRef = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("report")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelReport>()
                for (ds in snapshot.children) {
                    val report = ds.getValue(ModelReport::class.java)
                    if (report != null) list.add(report)
                }
                list.reverse() // Tampilkan yang terbaru di atas

                if (list.isEmpty()) {
                    tvKosong.visibility = View.VISIBLE
                    rvHistory.visibility = View.GONE
                } else {
                    tvKosong.visibility = View.GONE
                    rvHistory.visibility = View.VISIBLE
                    rvHistory.adapter = ReceiptHistoryAdapter(list)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun printReceipt(report: ModelReport) {
        if (bluetoothSocket == null || !bluetoothSocket!!.isConnected) {
            Toast.makeText(this, "Hubungkan printer dahulu", Toast.LENGTH_SHORT).show()
            cardDeviceList.visibility = View.VISIBLE
            return
        }

        try {
            val os = outputStream ?: return
            val nota = StringBuilder()
            
            nota.append("\n      TOKO JUNIOR\n")
            nota.append("--------------------------------\n")
            nota.append("Tgl: ${report.tanggalReport}\n")
            nota.append("ID : ${report.idReport?.takeLast(6)}\n")
            nota.append("--------------------------------\n")
            
            report.items?.forEach { item ->
                nota.append("${item.namaProduk}\n")
                nota.append("${item.jumlah} x ${formatRupiah(item.harga ?: 0)} = ${formatRupiah(item.totalHarga ?: 0)}\n")
            }
            
            nota.append("--------------------------------\n")
            nota.append("TOTAL:          ${formatRupiah(report.totalPenjualan ?: 0)}\n")
            nota.append("--------------------------------\n")
            nota.append("    Terima Kasih\n\n\n\n\n")

            os.write(nota.toString().toByteArray())
            os.flush()
            Toast.makeText(this, "Nota Berhasil Dicetak", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal print: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(number: Long): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(number)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        tvStatus.text = "Menghubungkan..."
        Thread {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter.cancelDiscovery()
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                Handler(Looper.getMainLooper()).post {
                    tvStatus.text = "Terhubung"
                    tvName.text = device.name ?: "Printer"
                    cardDeviceList.visibility = View.GONE
                    Toast.makeText(this, "Printer Siap!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    tvStatus.text = "Gagal Terhubung"
                    Toast.makeText(this, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        val missing = permissions.filter { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        deviceList.clear()
        bluetoothAdapter.bondedDevices?.forEach { deviceList.add(it) }
        deviceAdapter.notifyDataSetChanged()
        bluetoothAdapter.startDiscovery()
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device)
                    deviceAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (e: Exception) {}
        try { bluetoothSocket?.close() } catch (e: Exception) {}
    }

    // ADAPTER UNTUK RIWAYAT NOTA
    inner class ReceiptHistoryAdapter(private val list: List<ModelReport>) : RecyclerView.Adapter<ReceiptHistoryAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTgl: TextView = view.findViewById(R.id.tv_tanggal_report)
            val tvTotal: TextView = view.findViewById(R.id.tv_total_penjualan_report)
            val btnPrint: View = view.findViewById(R.id.report_card)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_reportcard, parent, false))
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvTgl.text = item.tanggalReport
            holder.tvTotal.text = "Rp ${formatRupiah(item.totalPenjualan ?: 0)}"
            holder.itemView.setOnClickListener { printReceipt(item) }
        }
        override fun getItemCount() = list.size
    }

    inner class DeviceAdapter(private val devices: List<BluetoothDevice>, private val onClick: (BluetoothDevice) -> Unit) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(android.R.id.text1)
            val address: TextView = view.findViewById(android.R.id.text2)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false))
        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = devices[position]
            holder.name.text = device.name ?: "Unknown Device"
            holder.address.text = device.address
            holder.itemView.setOnClickListener { onClick(device) }
        }
        override fun getItemCount() = devices.size
    }
}
