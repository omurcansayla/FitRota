package com.omurcansayla.fitrota

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Üstteki bildirim çubuğunu gizleyerek tam ekran yapıyoruz
        supportActionBar?.hide()

        // 3 Saniye (3000 milisaniye) gecikme
        Handler(Looper.getMainLooper()).postDelayed({

            // Süre bitince MainActivity'e geç
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)


            finish()

        }, 2000) // Buradaki 2000 sayısını değiştirerek süreyi ayarlayabiliriz
    }
}