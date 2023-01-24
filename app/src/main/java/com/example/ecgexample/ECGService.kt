package com.example.ecgexample

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


@SuppressLint("MissingPermission")
class ECGService : Service() {

    companion object {
        const val STOP = "stop"
    }

    private val notificationId = 5

    private val channelName = "channel"

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var btGatt: BluetoothGatt? = null


    private val btGattCallback = object : BluetoothGattCallback() {


        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.d(TAG, "onConnectionStateChange: status - $status state - $newState")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.d(TAG, "onServicesDiscovered: ")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d(TAG, "onCharacteristicChanged: ")
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.d(TAG, "onDescriptorWrite: ")
        }
    }


    private lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var notificationManager: NotificationManagerCompat

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = NotificationCompat.Builder(this, channelName).apply {
            setContentTitle("Service running")
            setOngoing(true)
            setSilent(true)
            setShowWhen(false)
            setSmallIcon(R.drawable.ic_notify)
        }

        notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelName,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        btGatt = ECGRepository.btDevice?.connectGatt(
            this,
            true,
            btGattCallback,
            BluetoothDevice.TRANSPORT_AUTO
        )
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val stopIntent = intent?.getBooleanExtra(STOP, false) ?: false
        if (stopIntent) {
            stopForeground(true)
            stopSelf()
        } else {
            startForeground(notificationId, notificationBuilder.build())
            Toast.makeText(this, "startForeground - ${ECGRepository.btDevice?.name}", Toast.LENGTH_SHORT).show()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        btGatt?.disconnect()
        btGatt?.close()
    }
}