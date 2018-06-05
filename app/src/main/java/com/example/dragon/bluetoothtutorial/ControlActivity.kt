package com.example.dragon.bluetoothtutorial


import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.control_layout.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.util.*



class ControlActivity:AppCompatActivity() {
    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        val Forward = "8"
        val Back = "2"
        val Left = "4"
        val Right = "6"
        val Stop = "0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val initialValue = mutableListOf<Int>()
        val queue = Queue<Int>(initialValue)
        queue.enqueue(8)
        queue.enqueue(0)
        queue.enqueue(8)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        m_address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)
        ConnectToDevice(this).execute()
        forward.setOnClickListener { sendCommand(Forward) }
        back.setOnClickListener { sendCommand(Back) }
        left.setOnClickListener { sendCommand(Left) }
        right.setOnClickListener { sendCommand( Right) }
        stop.setOnClickListener { sendCommand(Stop) }
        disconnect.setOnClickListener { sendCommand(Stop); disconnect() }
        route.setOnClickListener {
            launch {
                if (queue.hasNext()) {
                    val arr = this.toString()
                    sendCommand(arr)
                    delay(4000)
                }
            }
        }
    }

    class Queue<T>(list: MutableList<T>) : Iterator<T> {
        var itCounter: Int = 0
        var command: MutableList<T> = list
        private fun isEmpty(): Boolean = this.command.isEmpty()
        private fun count(): Int = this.command.count()
        override fun toString() = this.command.toString()
        fun enqueue(element: T) {
            this.command.add(element)
        }

        fun dequeue(): T? {
            if (this.isEmpty()) {
                return null
            } else {
                return this.command.removeAt(1)
            }
        }
        override fun hasNext(): Boolean {
            val hasNext = itCounter < count()
            if (!hasNext) itCounter = 0
            return hasNext
        }
        override fun next(): T {
            if (hasNext()) {
                val topPos: Int = (count() - 1) - itCounter
                itCounter++
                return this.command[topPos]
            } else {
                throw NoSuchElementException("No such element")
            }
        }

    }

        private fun sendCommand(input: String) {
            if (m_bluetoothSocket != null) {
                try {
                    m_bluetoothSocket!!.outputStream.write(input.toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        private fun disconnect() {
            if (m_bluetoothSocket != null) {
                try {
                    stop.setOnClickListener { sendCommand("0") }
                    m_bluetoothSocket!!.close()
                    m_bluetoothSocket = null
                    m_isConnected = false
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            finish()
        }

        private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
            private var connectSuccess: Boolean = true
            private val context: Context

            init {
                this.context = c
            }

            override fun onPreExecute() {
                super.onPreExecute()
                m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
            }

            override fun doInBackground(vararg p0: Void?): String? {
                try {
                    if (m_bluetoothSocket == null || !m_isConnected) {
                        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                        val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                        m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                        m_bluetoothSocket!!.connect()
                    }
                } catch (e: IOException) {
                    connectSuccess = false
                    e.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                if (!connectSuccess) {
                    Log.i("data", "couldn't connect")
                } else {
                    m_isConnected = true
                }
                m_progress.dismiss()
            }
        }
    }
