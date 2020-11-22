package com.ashutosh.textrecognition

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.kotlinpermissions.KotlinPermissions

class MainActivity : AppCompatActivity() {

    lateinit var outputText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textRecognizer = TextRecognizer.Builder(this).build()
        val surfaceView = findViewById<SurfaceView>(R.id.surface_camera_preview)
        outputText = findViewById<TextView>(R.id.tv_result)

        KotlinPermissions.with(this)
            .permissions(Manifest.permission.CAMERA)
            .onAccepted {

            }
            .onDenied { }
            .onForeverDenied { }
            .ask()

        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Dependencies are not loaded yet.", Toast.LENGTH_LONG).show()
            return
        }

        val mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build()


        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        mCameraSource.start(holder)
                        return
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Permission denied.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mCameraSource.stop()
            }

        })

        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                val items = detections.detectedItems

                if (items.size() <= 0) {
                    return
                }

                outputText.post {
                    val stringBuilder = StringBuilder()
                    for (i in 0 until items.size()) {
                        val item = items.valueAt(i)
                        stringBuilder.append(item.value)
                        stringBuilder.append("\n")
                    }
                    outputText.text = stringBuilder.toString()
                }
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {}
            Configuration.UI_MODE_NIGHT_YES -> {
                Toast.makeText(this, "Night", Toast.LENGTH_SHORT).show()
                outputText.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }
}