package com.example.barcodescanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.barcodescanner.listeners.ScanReceiverListener


class MainActivity : AppCompatActivity(), ScanReceiverListener {

    val scanManagerHandler = ScanManagerHandler(this)

    private lateinit var barcodeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        barcodeTextView = findViewById(R.id.barcodeTextView)

        barcodeTextView.text = "No se ha leído ningún código de barras"

        // Set the value of register
        scanManagerHandler.setRegister(true)

        // Set the intent filters and set the register
        scanManagerHandler.registerDataReceiver()
    }



    override fun onScanHandled() {
        TODO("Not yet implemented")
    }

    // This function is called when the scanner returns a result
    override fun onScanResult(scanResult: String) {
        barcodeTextView.text = scanResult
    }


}
