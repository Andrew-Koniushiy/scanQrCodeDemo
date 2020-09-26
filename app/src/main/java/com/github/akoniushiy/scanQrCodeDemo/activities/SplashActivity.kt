package com.github.akoniushiy.scanQrCodeDemo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.akoniushiy.scanQrCodeDemo.R
import com.google.firebase.FirebaseApp

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseApp.initializeApp(this.applicationContext)
    }
}