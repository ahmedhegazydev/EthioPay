package com.kbyai.idcardrecognition.payment.idcardrecognition

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kbyai.idcardrecognition.R
import com.kbyai.idsdk.IDSDK
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.preview.Frame
import io.fotoapparat.preview.FrameProcessor
import io.fotoapparat.selector.back
import io.fotoapparat.view.CameraView
import org.json.JSONObject


class CameraActivityKt : ComponentActivity() {

    private fun returnResultAndFinish(json: String) {
        val data = Intent().putExtra("result_json", json)
        setResult(RESULT_OK, data)
        finish()
    }

    private var goodFrames = 0
    private val PASS_QUALITY = 60
    private val NEED_GOOD_FRAMES = 2


    private val TAG = "ID_CARD_SCAN"

    private val PREVIEW_WIDTH = 720
    private val PREVIEW_HEIGHT = 1280

    private lateinit var cameraView: CameraView
    private lateinit var faceView: FaceView
    private lateinit var fotoapparat: Fotoapparat
    private lateinit var context: Context

    private var documenName: String = ""
    private var positionRect: Rect = Rect()
    private var recognized = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")


        setContentView(R.layout.activity_camera_kt)


        context = this
        cameraView = findViewById(R.id.preview)
        faceView = findViewById(R.id.faceView)

        fotoapparat = Fotoapparat.with(this)
            .into(cameraView)
            .lensPosition(back())
            .frameProcessor(FaceFrameProcessor())
            .previewResolution { Resolution(PREVIEW_HEIGHT, PREVIEW_WIDTH) }
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            Log.d(TAG, "Camera permission not granted → requesting")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            Log.d(TAG, "Camera permission granted → starting camera")
            fotoapparat.start()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        recognized = false

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fotoapparat.start()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause → stopping camera")
        fotoapparat.stop()
        faceView.setDocumentInfos(null, "")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult")

        if (requestCode == 1 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Camera permission granted after request → starting camera")
            fotoapparat.start()
        }
    }

    inner class FaceFrameProcessor : FrameProcessor {

        override fun process(frame: Frame) {

            if (recognized) return

            Log.d(TAG, "Frame received: ${frame.size.width}x${frame.size.height}")

            try {
                val bitmap = IDSDK.yuv2Bitmap(
                    frame.image,
                    frame.size.width,
                    frame.size.height,
                    6
                )

                Log.d(TAG, "Bitmap created: ${bitmap.width}x${bitmap.height}")


                val result = IDSDK.idcardRecognition(bitmap)
                if (result.isNullOrBlank() || result == "null") {
                    Log.d(TAG, "SDK returned null/blank → skip frame")
                    return
                }

                Log.d(TAG, "SDK raw result: $result")

                val jsonResult = JSONObject(result)

                val positionObj = jsonResult.getJSONObject("Position")
                val hasMrz = jsonResult.has("MRZ")
                val quality = jsonResult.getInt("Quality")
                val documentName = jsonResult.getString("Document Name")

                Log.d(
                    TAG,
                    "Detected document=$documentName | quality=$quality | hasMRZ=$hasMrz"
                )

                val x1 = positionObj.getInt("x1")
                val y1 = positionObj.getInt("y1")
                val x2 = positionObj.getInt("x2")
                val y2 = positionObj.getInt("y2")

                positionRect = Rect(x1, y1, x2, y2)
                documenName = "$documentName $quality"

//                if (quality > 80 && (documentName != "Unknown" || hasMrz)) {
                val PASS_QUALITY = 60
                if (quality >= PASS_QUALITY && (documentName != "Unknown" || hasMrz)) {

                    recognized = true
                    Log.d(TAG, "✅ ID CARD RECOGNIZED")

                    runOnUiThread {
                        faceView.setFrameSize(Size(bitmap.width, bitmap.height))
                        faceView.setDocumentInfos(positionRect, documentName)
                    }

                    // ✅ رجّع النتيجة مباشرة بدل فتح ResultActivity
//                    runOnUiThread {
//                        returnResultAndFinish(jsonResult.toString())
//                    }

                    val minimal = JSONObject().apply {
                        put("Document Number", jsonResult.optString("Document Number", ""))
                        put("Document Name", jsonResult.optString("Document Name", ""))
                        put("Quality", jsonResult.optInt("Quality", 0))
                        put("Date of Birth-Arabic (Egypt)", jsonResult.optString("Date of Birth-Arabic (Egypt)", ""))
                        put("Given Names-Arabic (Egypt)", jsonResult.optString("Given Names-Arabic (Egypt)", ""))
                        put("Surname-Arabic (Egypt)", jsonResult.optString("Surname-Arabic (Egypt)", ""))
                    }
                    returnResultAndFinish(minimal.toString())

                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during frame processing", e)
                documenName = ""
                positionRect = Rect()
            }

            runOnUiThread {
                faceView.setFrameSize(Size(frame.size.width, frame.size.height))
                faceView.setDocumentInfos(positionRect, documenName)
            }
        }
    }

//    override fun onBackPressed() {
//        setResult(RESULT_CANCELED)
//        super.onBackPressed()
//    }


}
