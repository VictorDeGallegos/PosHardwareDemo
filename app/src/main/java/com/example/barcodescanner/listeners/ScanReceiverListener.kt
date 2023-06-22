package com.example.barcodescanner.listeners

interface ScanReceiverListener {
    fun onScanHandled()
    fun onScanResult(scanResult: String)
}