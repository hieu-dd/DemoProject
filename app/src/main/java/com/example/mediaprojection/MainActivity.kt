package com.example.mediaprojection

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception
import java.nio.ByteBuffer


@SuppressLint("WrongConstant")
class MainActivity : AppCompatActivity() {

    private val STATE_RESULT_CODE = "result_code"
    private val STATE_RESULT_DATA = "result_data"

    private val REQUEST_MEDIA_PROJECTION = 1

    private var mScreenDensity = 0

    private var mResultCode = 0
    private var mResultData: Intent? = null

    private var mMediaProjection: MediaProjection? = null
    val imageReader: ImageReader by lazy {
        //ImageReader.newInstance(windowWidth, windowHeight, ImageFormat.JPEG , 2) //ImageFormat.RGB_565
        ImageReader.newInstance(400, 400, PixelFormat.RGBA_8888, 5);
    }

    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mButtonToggle: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE)
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA)
        }

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        mMediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mButtonToggle = findViewById(R.id.toggle) as Button
        mButtonToggle?.setOnClickListener {
            if (mVirtualDisplay == null) {
                startScreenCapture()
            } else {
                stopScreenCapture()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode)
            outState.putParcelable(STATE_RESULT_DATA, mResultData)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != RESULT_OK) {
                return
            }
            mResultCode = resultCode
            mResultData = data
            setUpMediaProjection()
            setUpVirtualDisplay()
        }
    }

    override fun onPause() {
        super.onPause()
        stopScreenCapture()
    }

    override fun onDestroy() {
        super.onDestroy()
        tearDownMediaProjection()
    }

    private fun setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager!!.getMediaProjection(mResultCode, mResultData!!)
    }

    private fun tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    private fun startScreenCapture() {
        if (mMediaProjection != null) {
            setUpVirtualDisplay()
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection()
            setUpVirtualDisplay()
        } else {
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                mMediaProjectionManager!!.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION
            )
        }
    }

    private fun setUpVirtualDisplay() {
        mVirtualDisplay = mMediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            400,
            400,
            mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )

        var image: Image? = null
        while (image == null) {
            image = try {
                imageReader.acquireLatestImage()
            } catch (e: Exception) {
                null
            }
        }

        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width

        var bitmap: Bitmap? =
            Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap?.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, width, height)
        image.close()
        findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)
        mButtonToggle?.setText("R.string.stop")
    }

    private fun stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mVirtualDisplay = null
        mButtonToggle?.setText("R.string.start")
    }
}