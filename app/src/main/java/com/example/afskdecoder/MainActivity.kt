package com.example.afskdecoder

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private var mConfig: FSKConfig? = null
    private var mDecoder: FSKDecoder? = null

    private var mPCMFeeder = Runnable {
        try {
            //open input stream to the WAV file
            val input = resources.assets.open("fsk.wav")

            //get information about the WAV file
            val info: WavToPCM.WavInfo = WavToPCM.readHeader(input)

            //get the raw PCM data
            val pcm = ByteBuffer.wrap(WavToPCM.readWavPcm(info, input))

            //the decoder has 1 second buffer (equals to sample rate),
            //so we have to fragment the entire file,
            //to prevent buffer overflow or rejection
            var buffer = ByteArray(1024)

            //feed signal little by little... another way to do that is to
            //check the returning value of appendSignal(), it returns the
            //remaining space in the decoder signal buffer
            while (pcm.hasRemaining()) {
                if (pcm.remaining() > 1024) {
                    pcm[buffer]
                } else {
                    buffer = ByteArray(pcm.remaining())
                    pcm[buffer]
                }
                mDecoder!!.appendSignal(buffer)
                Thread.sleep(100)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /// INIT FSK CONFIG


        /// INIT FSK CONFIG
        try {
            mConfig = FSKConfig(
                FSKConfig.SAMPLE_RATE_29400,
                FSKConfig.PCM_8BIT,
                FSKConfig.CHANNELS_MONO,
                FSKConfig.SOFT_MODEM_MODE_4,
                FSKConfig.THRESHOLD_20P
            )
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        /// INIT FSK DECODER

        var text2 : StringBuilder = StringBuilder()
        /// INIT FSK DECODER
        mDecoder = FSKDecoder(mConfig
        ) { newData ->
            val text = String(newData!!)
            text2.append(text)
            runOnUiThread {
                val view = findViewById<TextView>(R.id.result)
                view.text = view.text.toString() + text
            }
        }

        ///


        ///
        Thread(mPCMFeeder).start()
    }

    override fun onDestroy() {
        mDecoder!!.stop()
        super.onDestroy()
    }

}