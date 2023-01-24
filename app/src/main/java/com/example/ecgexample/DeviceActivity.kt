package com.example.ecgexample

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast


object ECGRepository {
    var btDevice: BluetoothDevice? = null
}

@SuppressLint("MissingPermission")
class DeviceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        val intent = Intent(this, ECGService::class.java)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, ECGService::class.java).apply {
            putExtra(ECGService.STOP, true)
        }
        startService(intent)
    }
}