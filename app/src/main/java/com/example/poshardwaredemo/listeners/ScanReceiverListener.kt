package com.example.poshardwaredemo.listeners

interface ScanReceiverListener {
    fun onScanHandled()
    fun onScanResult(scanResult: String)
}