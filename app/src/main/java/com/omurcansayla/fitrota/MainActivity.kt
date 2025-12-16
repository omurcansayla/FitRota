package com.omurcansayla.fitrota

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Eğer kullanıcı zaten giriş yapmışsa direkt Ana Sayfaya (HomeActivity) at
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // XML'deki ID'lerin doğru olduğundan emin ol (Senin projendeki ID'leri tahmin ederek yazdım)
        // Eğer hata verirse ID'leri kontrol et (editTextEmail, editTextPassword vb.)
        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val registerRedirect = findViewById<TextView>(R.id.textViewRegister)

        // YENİ EKLENEN ŞİFREMİ UNUTTUM YAZISI
        val forgotPasswordText = findViewById<TextView>(R.id.textViewForgotPassword)

        // --- GİRİŞ YAP BUTONU ---
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Lütfen alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- KAYIT OL SAYFASINA GİT ---
        registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // --- YENİ: ŞİFREMİ UNUTTUM TIKLAMA OLAYI ---
        forgotPasswordText.setOnClickListener {
            val emailText = EditText(this)
            emailText.hint = "E-posta adresinizi girin"

            // Eğer giriş kutusunda mail yazılıysa onu buraya otomatik al
            if (emailInput.text.isNotEmpty()) {
                emailText.setText(emailInput.text.toString())
            }

            AlertDialog.Builder(this)
                .setTitle("Şifre Sıfırlama")
                .setMessage("Şifre sıfırlama bağlantısı göndermek için mail adresinizi girin.")
                .setView(emailText)
                .setPositiveButton("GÖNDER") { _, _ ->
                    val email = emailText.text.toString().trim()
                    if (email.isNotEmpty()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Sıfırlama linki mailinize gönderildi! 📩", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Lütfen mail adresini girin.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }
    }
}