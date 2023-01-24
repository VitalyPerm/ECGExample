package com.example.ecgexample

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

const val TAG = "check___"

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    private val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    ) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            Toast.makeText(this, "granted!", Toast.LENGTH_SHORT).show()
        }

    private val btAdapter by lazy {
        val bt = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bt.adapter
    }

    private val scanSettings =
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

    private val bleScanCallback: ScanCallback =
        object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                Log.d(TAG, "onScanResult: ${result?.device?.name}")
                result?.device?.let { bluetoothDevice ->
                    if (bluetoothDevice.name != null) addDevice(
                        bluetoothDevice
                    )
                }
            }
        }

    private lateinit var adapter: MainAdapter
    private lateinit var rv: RecyclerView
    private val btDevicesSet: MutableSet<BluetoothDevice> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkPermissions(bluetoothPermissions).not()) permissionsLauncher.launch(
            bluetoothPermissions
        ) else startScan()

        rv = findViewById(R.id.rv)
        adapter = MainAdapter {
            btAdapter.bluetoothLeScanner.stopScan(bleScanCallback)
            ECGRepository.btDevice = it
            val intent = Intent(this, DeviceActivity::class.java)
            startActivity(intent)
        }
        rv.adapter = adapter

    }

    private fun startScan() {
        btAdapter.bluetoothLeScanner.startScan(
            listOf(),
            scanSettings,
            bleScanCallback
        )
    }

    private fun addDevice(btDevice: BluetoothDevice) {
        btDevicesSet.add(btDevice)
        adapter.submitList(btDevicesSet.toList())
        Log.d(TAG, "addDevice: size ${btDevicesSet.size}")
    }


    private fun checkPermissions(permissions: Array<String>): Boolean {
        permissions.forEach { permission ->
            if (ActivityCompat.checkSelfPermission(
                    application, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }
}


class MainAdapter(val onClick: (BluetoothDevice) -> Unit) :
    ListAdapter<BluetoothDevice, MainViewHolder>(MainDiffCallBack) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder =
        MainViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.main_item, parent, false)
        )

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener { onClick(getItem(position)) }
    }

}

class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    @SuppressLint("MissingPermission")
    fun bind(device: BluetoothDevice) {
        val tvName = itemView.findViewById<TextView>(R.id.name)
        tvName.text = device.name
    }
}

object MainDiffCallBack : DiffUtil.ItemCallback<BluetoothDevice>() {
    override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean =
        oldItem.address == newItem.address


    override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean =
        oldItem == newItem
}