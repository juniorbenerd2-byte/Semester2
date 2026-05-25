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
import com.example.semester2.model.ModelTrolly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PrinterActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val deviceList = ArrayList<BluetoothDevice>()
    private lateinit var adapter: DeviceAdapter
    
    private lateinit var tvStatus: TextView
    private lateinit var tvName: TextView
    private lateinit var btnScan: Button
    
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
        setupRecyclerView()
        checkPermissions()

        findViewById<ImageButton>(R.id.btnBackPrinter).setOnClickListener { finish() }
        
        btnScan.setOnClickListener { startDiscovery() }

        findViewById<Button>(R.id.btnTestPrint).setOnClickListener {
            if (bluetoothSocket == null || !bluetoothSocket!!.isConnected) {
                Toast.makeText(this, "Hubungkan printer thermal dahulu", Toast.LENGTH_SHORT).show()
            } else {
                fetchActiveTrollyAndPrint()
            }
        }
    }

    private fun initView() {
        tvStatus = findViewById(R.id.tvPrinterStatus)
        tvName = findViewById(R.id.tvPrinterName)
        btnScan = findViewById(R.id.btnScanPrinter)
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvBluetoothDevices)
        adapter = DeviceAdapter(deviceList) { connectToDevice(it) }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun fetchActiveTrollyAndPrint() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference
        
        db.child("users_data").child(userId).child("kategori")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(katSnapshot: DataSnapshot) {
                    val activeKategori = HashSet<String>()
                    for (ds in katSnapshot.children) {
                        val nama = ds.child("namaKategori").getValue(String::class.java)
                        val status = ds.child("statusKategori").getValue(String::class.java)
                        if (nama != null && status == "Aktif") activeKategori.add(nama)
                    }

                    db.child("users_data").child(userId).child("trolly")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(trollySnapshot: DataSnapshot) {
                                val printList = ArrayList<ModelTrolly>()
                                for (ds in trollySnapshot.children) {
                                    val item = ds.getValue(ModelTrolly::class.java)
                                    if (item != null && activeKategori.contains(item.namaProduk)) {
                                        printList.add(item)
                                    }
                                }

                                if (printList.isEmpty()) {
                                    Toast.makeText(this@PrinterActivity, "Tidak ada item aktif untuk dicetak", Toast.LENGTH_SHORT).show()
                                } else {
                                    sendToPrinter(printList)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun sendToPrinter(items: List<ModelTrolly>) {
        try {
            val os = outputStream ?: return
            val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
            val date = sdf.format(Date())
            
            var total = 0L
            val nota = StringBuilder()
            
            nota.append("\n      TOKO JUNIOR\n")
            nota.append("--------------------------------\n")
            nota.append("Tgl: $date\n")
            nota.append("--------------------------------\n")
            
            for (item in items) {
                val subtotal = item.totalHarga ?: 0L
                total += subtotal
                nota.append("${item.namaProduk}\n")
                nota.append("${item.jumlah} x ${formatRupiahSimple(item.harga ?: 0)} = ${formatRupiahSimple(subtotal)}\n")
            }
            
            nota.append("--------------------------------\n")
            nota.append("TOTAL:          ${formatRupiahSimple(total)}\n")
            nota.append("--------------------------------\n")
            nota.append("    Terima Kasih\n\n\n\n\n")

            os.write(nota.toString().toByteArray())
            os.flush()
            Toast.makeText(this, "Nota Berhasil Dicetak", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal print: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiahSimple(number: Long): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(number)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions()
            return
        }

        tvStatus.text = "Menghubungkan..."
        Thread {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                } else {
                    bluetoothAdapter.cancelDiscovery()
                }

                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                
                Handler(Looper.getMainLooper()).post {
                    tvStatus.text = "Terhubung"
                    tvName.text = device.name ?: "Printer"
                    Toast.makeText(this, "Koneksi Berhasil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    tvStatus.text = "Gagal"
                    Toast.makeText(this, "Gagal konek printer: ${e.message}", Toast.LENGTH_SHORT).show()
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
        if (!bluetoothAdapter.isEnabled) { 
            Toast.makeText(this, "Aktifkan Bluetooth Terlebih Dahulu", Toast.LENGTH_SHORT).show()
            return 
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions()
            return
        }

        deviceList.clear()
        bluetoothAdapter.bondedDevices?.forEach { deviceList.add(it) }
        adapter.notifyDataSetChanged()
        
        bluetoothAdapter.startDiscovery()
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        Toast.makeText(this, "Mencari perangkat...", Toast.LENGTH_SHORT).show()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { 
            unregisterReceiver(receiver)
            bluetoothSocket?.close() 
        } catch (e: Exception) {}
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
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.checkSelfPermission(this@PrinterActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else true
            
            holder.name.text = if (hasPermission) device.name ?: "Printer" else "Printer (Izin diperlukan)"
            holder.address.text = device.address
            holder.itemView.setOnClickListener { onClick(device) }
        }
        override fun getItemCount() = devices.size
    }
}
