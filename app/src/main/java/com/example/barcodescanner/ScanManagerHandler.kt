package com.example.barcodescanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanManager
import android.device.ScanManager.BARCODE_LENGTH_TAG
import android.device.ScanManager.DECODE_DATA_TAG
import android.device.scanner.configuration.PropertyID
import android.device.scanner.configuration.Triggering
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.barcodescanner.ScanManagerHandler.Companion.MSG_SHOW_SCAN_RESULT
import com.example.barcodescanner.listeners.ScanReceiverListener
import timber.log.Timber
import java.lang.ref.WeakReference

class ScanManagerHandler(
   private val context: Context,
){
    val scanReciever by lazy { context as ScanReceiverListener }
    private val scanManger = ScanManager()
    private var broadcastIsRegistered = false
    private var register = false

    companion object {
        val DECODE_TRIGGER_MODE_PAUSE = Triggering.PULSE
        const val DECODE_OUTPUT_MODE_INTENT = 0
        const val MSG_SHOW_SCAN_RESULT = 1
        const val ACTION_CAPTURE_IMAGE = "scanner_capture_image_result"

    }

    init {
        openScanner()
    }

    fun setRegister(register: Boolean) {this.register = register}

    fun getScanOutputMode() : Int = scanManger.outputMode

    fun getLockTriggerState() : Boolean = scanManger.triggerLockState

    fun getScannerState() : Boolean = scanManger.scannerState

    private fun getParameterString(ids : Array<Int>) : Array<String> = scanManger.getParameterString(ids.toIntArray())

    fun getBeepState() = scanManger.beepState

    private fun getTriggerMode() : Triggering = scanManger.triggerMode

    private fun setTrigger(mode : Triggering) {
        val currentMode = getTriggerMode()
        if (mode != currentMode){
            scanManger.triggerMode = mode
        }
    }

    fun setScanOutputMode(mode : Int) {
        val currentMode = getScanOutputMode()
        if (mode != currentMode){
            scanManger.switchOutputMode(mode)
        }
    }

    fun stopDecode() {
        scanManger.stopDecode()
    }

    fun closeScanner() : Boolean {
        stopDecode()
        return scanManger.closeScanner()
    }

    fun openScanner() : Boolean {
        var state = scanManger.scannerState
        if (!state){
            state = scanManger.openScanner()
            if (!state){
                return false
            }
        }
        scanManger.enableAllSymbologies(true)
        setTrigger(DECODE_TRIGGER_MODE_PAUSE)
        setScanOutputMode(DECODE_OUTPUT_MODE_INTENT)
        return state
    }

    fun registerDataReceiver() {
        if (register) {
            val filter = IntentFilter()
            val idBuf = arrayOf(
                PropertyID.WEDGE_INTENT_ACTION_NAME,
                PropertyID.WEDGE_INTENT_DATA_STRING_TAG
            )
            val valueBuf = getParameterString(idBuf)
            if (valueBuf[0] != "") {
                filter.addAction(valueBuf[0])
            } else {
                filter.addAction(ScanManager.ACTION_DECODE)
            }
            filter.addAction(ACTION_CAPTURE_IMAGE)
            context.registerReceiver(broadcastReceiver, filter)
        }
        else {
            stopDecode()
            try {
                //try catch is added because if the broadcastReceiver is not registered the app crash.
                if(broadcastIsRegistered){
                    context.unregisterReceiver(broadcastReceiver)
                }
            }catch (e: Exception){
                Timber.e(e,"Unregister receiver Error")
            }
        }
        broadcastIsRegistered = register
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { intentSafe ->
                val barcode = intentSafe.getByteArrayExtra(DECODE_DATA_TAG)
                val barcodeLength = intentSafe.getIntExtra(BARCODE_LENGTH_TAG, 0)
                barcode?.let { barcodeSafe ->
                    val scanResult = String(barcodeSafe, 0, barcodeLength)
                    val handler = ScanHandler.getScanHandler(context as ScanReceiverListener)
                    val msg = handler.obtainMessage(MSG_SHOW_SCAN_RESULT)
                    msg.obj = scanResult
                    scanReciever.onScanResult(scanResult)
                }
            }
        }
    }

}



class ScanHandler: Handler(Looper.getMainLooper()) {
    private lateinit var scanReceiver: WeakReference<ScanReceiverListener>

    companion object {
        fun getScanHandler(scanReceiver: ScanReceiverListener): ScanHandler {
            val handler = ScanHandler()
            handler.scanReceiver = WeakReference(scanReceiver)
            return handler
        }
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        scanReceiver.get()?.onScanHandled()
        when(msg.what) {
            MSG_SHOW_SCAN_RESULT -> {
                val scanResult = msg.obj as String
                scanReceiver.get()?.onScanResult(scanResult)
            }
        }
    }
}
