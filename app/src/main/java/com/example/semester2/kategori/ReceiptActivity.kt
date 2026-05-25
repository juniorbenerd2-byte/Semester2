package com.example.semester2.kategori

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.semester2.R
import com.example.semester2.model.ModelTrolly
import java.io.IOException
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReceiptActivity : AppCompatActivity() {

    private lateinit var containerItems: LinearLayout
    private lateinit var tvDate: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnPrint: Button
    
    private var checkoutItems: ArrayList<ModelTrolly>? = null
    private var totalHarga: Long = 0
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_receipt)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        initView()
        getDataFromIntent()
        displayReceipt()

        findViewById<ImageButton>(R.id.btnBackReceipt).setOnClickListener { finish() }
        
        btnPrint.setOnClickListener {
            checkBluetoothAndPrint()
        }
    }

    private fun initView() {
        containerItems = findViewById(R.id.containerItems)
        tvDate = findViewById(R.id.tvReceiptDate)
        tvTotal = findViewById(R.id.tvReceiptTotal)
        btnPrint = findViewById(R.id.btnPrintReceipt)
    }

    private fun getDataFromIntent() {
        checkoutItems = intent.getParcelableArrayListExtra("CHECKOUT_ITEMS")
        totalHarga = intent.getLongExtra("TOTAL_HARGA", 0)
    }

    private fun displayReceipt() {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        tvDate.text = sdf.format(Date())
        tvTotal.text = formatRupiah(totalHarga)

        containerItems.removeAllViews()
        checkoutItems?.forEach { item ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_receipt_row, containerItems, false)
            itemView.findViewById<TextView>(R.id.tvItemName).text = item.namaProduk
            itemView.findViewById<TextView>(R.id.tvItemDetails).text = "${item.jumlah} x ${formatRupiah(item.harga ?: 0)}"
            itemView.findViewById<TextView>(R.id.tvItemSubtotal).text = formatRupiah(item.totalHarga ?: 0)
            containerItems.addView(itemView)
        }
    }

    private fun checkBluetoothAndPrint() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Aktifkan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (bluetoothSocket != null && bluetoothSocket!!.isConnected) {
            sendDataToPrinter()
        } else {
            showDevicePicker()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showDevicePicker() {
        val pairedDevices = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "Tidak ada perangkat Bluetooth terpasang (paired)", Toast.LENGTH_LONG).show()
            return
        }

        val deviceNames = pairedDevices.map { it.name ?: "Unknown" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Pilih Printer Bluetooth")
            .setItems(deviceNames) { _, which ->
                connectToDevice(pairedDevices[which])
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        val dialog = AlertDialog.Builder(this)
            .setMessage("Menyambungkan ke ${device.name}...")
            .setCancelable(false)
            .show()

        Thread {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                
                Handler(Looper.getMainLooper()).post {
                    dialog.dismiss()
                    Toast.makeText(this, "Terhubung ke Printer", Toast.LENGTH_SHORT).show()
                    sendDataToPrinter()
                }
            } catch (e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    dialog.dismiss()
                    Toast.makeText(this, "Gagal konek: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun sendDataToPrinter() {
        try {
            val os = outputStream ?: return
            val nota = StringBuilder()
            val line = "--------------------------------\n"
            
            nota.append("\n      TOKO JUNIOR\n")
            nota.append(line)
            nota.append("Tgl: ${tvDate.text}\n")
            nota.append(line)
            
            checkoutItems?.forEach { item ->
                nota.append("${item.namaProduk}\n")
                nota.append("${item.jumlah} x ${formatRupiahSimple(item.harga ?: 0)} = ${formatRupiahSimple(item.totalHarga ?: 0)}\n")
            }
            
            nota.append(line)
            nota.append("TOTAL:          ${formatRupiahSimple(totalHarga)}\n")
            nota.append(line)
            nota.append("    Terima Kasih\n\n\n\n\n")

            os.write(nota.toString().toByteArray())
            os.flush()
            Toast.makeText(this, "Berhasil mencetak nota", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal print: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(number: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(number).replace("Rp", "Rp ").replace(",00", "")
    }

    private fun formatRupiahSimple(number: Long): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(number)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { bluetoothSocket?.close() } catch (e: Exception) {}
    }
}
