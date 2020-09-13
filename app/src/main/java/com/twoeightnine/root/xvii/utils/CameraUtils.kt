package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import com.twoeightnine.root.xvii.lg.L
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer

private const val TAG = "camera"

fun CameraManager.getFrontCameraId(): String? {
    try {
        for (cameraId in cameraIdList) {
            val chars = getCameraCharacteristics(cameraId)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return cameraId
            }
        }
    } catch (e: CameraAccessException) {
        L.tag(TAG).throwable(e).log("unable to find front camera")
    }
    return null
}

@SuppressLint("MissingPermission")
fun CameraManager.openFrontCamera(backgroundHandler: Handler, onOpened: (CameraDevice) -> Unit) {
    val cameraId = getFrontCameraId()
    if (cameraId != null) {
        openCamera(cameraId, object : CameraDeviceStateCallback() {
            override fun onOpened(camera: CameraDevice) {
                L.tag(TAG).log("opened")
                onOpened(camera)
            }

            override fun onClosed(camera: CameraDevice) {
                super.onClosed(camera)
                L.tag(TAG).log("closed")
            }
        }, Handler(Looper.getMainLooper()))
    }
}

fun CameraManager.takePicture(
        cameraDevice: CameraDevice,
        file: File,
        backgroundHandler: Handler,
        onCaptured: () -> Unit
) {
    try {
        val characteristics = getCameraCharacteristics(cameraDevice.id)
        val jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(ImageFormat.JPEG)

        val width = jpegSizes?.getOrNull(0)?.width ?: 640
        val height = jpegSizes?.getOrNull(0)?.height ?: 480
        L.tag(TAG).log("size $width*$height")

        val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
        val outputSurfaces = arrayListOf(reader.surface)
        val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                .apply {
                    addTarget(reader.surface)
                    set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                }

        val readerListener: ImageReader.OnImageAvailableListener = object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(reader: ImageReader) {
                var image: Image? = null
                try {
                    image = reader.acquireLatestImage()
                    val buffer: ByteBuffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)
                    save(bytes)
                } catch (e: Exception) {
                    L.tag(TAG).throwable(e).log("unable to save image")
                } finally {
                    image?.close()
                }
            }

            @Throws(IOException::class)
            private fun save(bytes: ByteArray) {
                var output: OutputStream? = null
                try {
                    output = FileOutputStream(file)
                    output.write(bytes)
                } finally {
                    output?.close()
                }
            }
        }
        reader.setOnImageAvailableListener(readerListener, backgroundHandler)
        val captureListener: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                onCaptured()
            }
        }
        cameraDevice.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    session.capture(captureBuilder.build(), captureListener, backgroundHandler)
                } catch (e: CameraAccessException) {
                    L.tag(TAG).throwable(e).log("unable to capture")
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                L.tag(TAG).warn().log("configure failed")
            }
        }, backgroundHandler)
    } catch (e: CameraAccessException) {
        L.tag(TAG).throwable(e).log("error occurred during capturing")
    }
}

abstract class CameraDeviceStateCallback : CameraDevice.StateCallback() {

    override fun onDisconnected(cameraDevice: CameraDevice) {
        onClosed(cameraDevice)
    }

    override fun onError(cameraDevice: CameraDevice, error: Int) {
        L.tag(TAG).warn().log("camera callback returned error $error")
        onClosed(cameraDevice)
    }

}