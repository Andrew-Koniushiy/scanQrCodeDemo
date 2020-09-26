package com.github.akoniushiy.scanQrCodeDemo.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.github.akoniushiy.scanQrCodeDemo.QrCodeAnalyzer
import com.github.akoniushiy.scanQrCodeDemo.R
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class ScanFragment : Fragment() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 10
        private const val TAG: String = "ScanFragment"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private var displayId: Int = -1
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService? = null
    private lateinit var previewView: PreviewView

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                previewView = requireView().findViewById(R.id.previewView)
                previewView.post {
                    displayId = previewView.display.displayId
                    setUpCamera()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previewView = view.findViewById(R.id.previewView)
        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(displayListener, null)

        if (!isCameraPermissionGranted()) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
            return
        }

        previewView.post {
            displayId = previewView.display.displayId
            setUpCamera()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            bindUseCases(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this.context))
    }

    private val onQrCodesDetected: (qrCodes: List<FirebaseVisionBarcode>) -> Unit = { qrCodes ->
        Log.d(TAG, "onQrCodesDetected, qrCodes size " + qrCodes.size)
        val firstQRCode = qrCodes.firstOrNull()
        if (firstQRCode != null) {
            if (findNavController().currentDestination?.id != R.id.resultFragment) {
                val rawValue = qrCodes[0].rawValue
                if (rawValue?.isNotEmpty()!!) {
                    imageAnalyzer?.clearAnalyzer()
                    cameraExecutor?.shutdown()
                    displayManager.unregisterDisplayListener(displayListener)
                    previewView.postDelayed({
                        findNavController().navigate(
                            ScanFragmentDirections.actionScanFragmentToResultFragment(
                                rawValue
                            )
                        )
                    }, 1000)

                }

            }
        }
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayIdValue: Int) = view?.let { view ->
            if (displayIdValue == displayId) {
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }


    private fun bindUseCases(cameraProvider: ProcessCameraProvider) {
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        val rotation = previewView.display.rotation
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor!!, QrCodeAnalyzer(onQrCodesDetected))
            }
        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(previewView.createSurfaceProvider())
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }
}