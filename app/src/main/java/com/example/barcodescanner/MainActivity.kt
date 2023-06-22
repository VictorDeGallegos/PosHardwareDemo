package com.example.barcodescanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.device.ScanManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), ScanReceiver {

    val scanManagerUtils = ScanManagerUtils()

    private var registerReceiver : Boolean = false

    private lateinit var barcodeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        barcodeTextView = findViewById(R.id.barcodeTextView)

        // Simulación de la obtención del código de barras desde el dispositivo POS
        // val barcodeData = obtenerCodigoDeBarrasDesdeDispositivoPOS()

        // Mostrar el código de barras en el TextView
        barcodeTextView.text = "No se ha leído ningún código de barras"

        registerDataReceiver(true)
        scanManagerUtils.startDecode()
    }



    private fun registerDataReceiver(register: Boolean) {
        if(register != registerReceiver){
            registerReceiver = register
            scanManagerUtils.registerDataReceiver(register, broadcastReceiver, this)
        }

    }

    /*private fun obtenerCodigoDeBarrasDesdeDispositivoPOS(): String {


        // Registra el BroadcastReceiver para recibir los resultados del escáner
        val scanReceiver = object : ScanReceiver {
            override fun onScanHandled() {
                // Lógica adicional a ejecutar cuando se maneja la lectura del código de barras

            }

            override fun onScanResult(scanResult: String) {
                // Aquí se obtiene el resultado del escáner y se muestra en la interfaz de usuario
                // Puedes actualizar la interfaz de usuario o almacenar el resultado en una variable
                // para su uso posterior
                // Por ejemplo, si deseas mostrar el resultado en un TextView llamado txtCodigoBarras:
                barcodeTextView.text = scanResult
            }
        }

        // Registra el BroadcastReceiver
        //scanManagerUtils.registerDataReceiver(true, scanReceiver, applicationContext)

        // Inicia la lectura del código de barras
        scanManagerUtils.startDecode()

        // Aquí puedes agregar lógica adicional o esperar a que se obtenga el resultado del escáner
        // Puedes utilizar eventos o bucles para esperar hasta que se obtenga el resultado
        // Por ejemplo, puedes utilizar una función callback o una variable de condición para sincronizar el proceso

        // Cuando hayas obtenido el resultado del código de barras, puedes detener la lectura del escáner
        scanManagerUtils.stopDecode()

        // Desregistra el BroadcastReceiver
        //scanManagerUtils.registerDataReceiver(false, scanReceiver, applicationContext)

        // Devuelve el código de barras obtenido (puedes cambiar esto según tus necesidades)
        // Por ejemplo, puedes almacenar el resultado en una variable y devolverla
        // return resultadoCodigoBarras

        // Devuelve el codigo de barras para mostrarlo en el TextView

        if (barcodeTextView.text.toString() == "") {
            return "No se ha leído ningún código de barras"
        } else {
            return barcodeTextView.text.toString()
        }
    }*/

    override fun onScanHandled() {
        TODO("Not yet implemented")
    }

    override fun onScanResult(scanResult: String) {
        TODO("Not yet implemented")
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { intentSafe ->
                val barcode = intentSafe.getByteArrayExtra(ScanManager.DECODE_DATA_TAG)
                val barcodeLength = intentSafe.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0)
                barcode?.let { barcodeSafe ->
                    val scanResult = String(barcodeSafe, 0, barcodeLength)
                    val handler = ScanHandler.getScanHandler(this@MainActivity)
                    val msg = handler.obtainMessage(ScanManagerUtils.MSG_SHOW_SCAN_RESULT)
                    msg.obj = scanResult
                    barcodeTextView.text = scanResult
                }
            }
        }
    }


}
