package com.example.barcodescanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.device.ScanManager
import android.device.scanner.configuration.PropertyID
import android.device.scanner.configuration.Triggering
import android.os.Handler
import android.os.Looper
import android.os.Message
import timber.log.Timber
import java.lang.ref.WeakReference

class ScanManagerUtils {

    private val scanManger = ScanManager()
    private var broadcastIsRegistered = false

    companion object {
        val DECODE_TRIGGER_MODE_HOST = Triggering.HOST
        val DECODE_TRIGGER_MODE_CONTINUOUS = Triggering.CONTINUOUS
        val DECODE_TRIGGER_MODE_PAUSE = Triggering.PULSE

        const val DECODE_OUTPUT_MODE_INTENT = 0
        const val DECODE_OUTPUT_MODE_FOCUS = 1
        const val MSG_SHOW_SCAN_RESULT = 1
        const val KEY_CODE_SCANNER_LEFT = 520
        const val KEY_CODE_SCANNER_RIGHT = 521
        const val ACTION_CAPTURE_IMAGE = "scanner_capture_image_result"
    }

    init {
        openScanner()
    }

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

    private fun updateLockTriggerState(state : Boolean) {
        val currentState = getLockTriggerState()
        if (state != currentState){
            if (state){
                scanManger.lockTrigger()
            }
            else {
                scanManger.unlockTrigger()
            }
        }
    }

    fun startDecode() : Boolean {
        return if (getLockTriggerState()){
            false
        }
        else {
            scanManger.startDecode()
            true
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

    fun registerDataReceiver(register: Boolean, broadcastReceiver: BroadcastReceiver, context: Context?) {
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
            context?.registerReceiver(broadcastReceiver, filter)
        }
        else {
            stopDecode()
            try {
                //try catch is added because if the broadcastReceiver is not registered the app crash.
                if(broadcastIsRegistered){
                    context?.unregisterReceiver(broadcastReceiver)
                }
            }catch (e: Exception){
                Timber.e(e,"Unregister receiver Error")
            }
        }
        broadcastIsRegistered = register
    }

}



class ScanHandler: Handler(Looper.getMainLooper()) {
    private lateinit var scanReceiver: WeakReference<ScanReceiver>

    companion object {
        fun getScanHandler(scanReceiver: ScanReceiver): ScanHandler {
            val handler = ScanHandler()
            handler.scanReceiver = WeakReference(scanReceiver)
            return handler
        }
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        scanReceiver.get()?.onScanHandled()
        when(msg.what) {
            ScanManagerUtils.MSG_SHOW_SCAN_RESULT -> {
                val scanResult = msg.obj as String
                scanReceiver.get()?.onScanResult(scanResult)
            }
        }
    }

}

interface ScanReceiver {
    fun onScanHandled()
    fun onScanResult(scanResult: String)
}