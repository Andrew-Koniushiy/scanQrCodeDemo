package com.github.akoniushiy.scanQrCodeDemo

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class QrCodeAnalyzer(
    private var onQrCodesDetected: (qrCodes: List<FirebaseVisionBarcode>) -> Unit
) : ImageAnalysis.Analyzer {
    companion object {
        private const val TAG: String = "QrCodeAnalyzer"
    }


    private fun onSuccessListener(): (MutableList<FirebaseVisionBarcode>) -> Unit =
        fun(barcodes: MutableList<FirebaseVisionBarcode>) {
            onQrCodesDetected(barcodes)
        }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> FirebaseVisionImageMetadata.ROTATION_0

    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        val mediaImage = imageProxy.image
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val imageRotation = degreesToFirebaseRotation(rotationDegrees)
        if (mediaImage != null) {
            val visionImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
            detector.detectInImage(visionImage)
                .addOnSuccessListener { barcodes ->
                    onSuccessListener()(barcodes)
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Mlkit processing Failed", e)
                    imageProxy.close()
                }
        }
    }
}