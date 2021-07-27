package com.example.test

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.deepspeech.libdeepspeech.DeepSpeechModel
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var deepspeech: DeepSpeechModel

    private val modelPath by lazy { File(applicationContext.externalCacheDir, "deepspeech.tflite").path }
    private val audioPath by lazy { File(applicationContext.externalCacheDir, "audio.wav").path }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newModel(modelPath)
        doInference(audioPath)
    }

    private fun newModel(tfliteModelPath: String) {
        log("Creating model...")
        deepspeech = DeepSpeechModel(tfliteModelPath)
        log("Model creation Done")
    }

    private fun doInference(audioFilePath: String) {
        log("Starting inference...")
        try {
            // TODO : Assert variables
            val wave = RandomAccessFile(audioFilePath, "r")
            wave.seek(20)
            val audioFormat = CharOperations.readLEChar(wave)

            wave.seek(22)
            val numChannels = CharOperations.readLEChar(wave)

            wave.seek(24)
            val sampleRate = CharOperations.readLEInt(wave)

            wave.seek(34)
            val bitsPerSample = CharOperations.readLEChar(wave)

            wave.seek(40)
            val bufferSize = CharOperations.readLEInt(wave)

            wave.seek(44)
            val bytes = ByteArray(bufferSize)
            wave.readFully(bytes)

            val shorts = ShortArray(bytes.size / 2)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)

            log("Assertions Passed!")
            log("Running inference")

            val infStartTime = System.currentTimeMillis()
            val decoded = deepspeech.stt(shorts, shorts.size)
            val infEndTime = System.currentTimeMillis() - infStartTime

            log("Inference completed in $infEndTime")
            log("Inference Result: $decoded")
        }
        catch (e: Exception) {
            log("--------------------------------------------")
            log("Error occured while trying to run inference!")
            e.printStackTrace()
        }
    }

    private fun log(s: String) {
        Log.d("DeepSpeech-Test", s)
    }
}