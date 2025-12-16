package com.omurcansayla.fitrota

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase başlat
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Ekran elemanlarını bul
        val nameInput = findViewById<EditText>(R.id.editTextRegisterName)
        val emailInput = findViewById<EditText>(R.id.editTextRegisterEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextRegisterPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegisterConfirm)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

                // 1. Kullanıcıyı Oluştur
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->

                        val userId = result.user?.uid

                        if (userId != null) {
                            // 2. Veritabanına İSİM ile birlikte kaydet
                            val userMap = hashMapOf(
                                "userId" to userId,
                                "name" to name,   // ÖNEMLİ: İsim burada kaydediliyor
                                "email" to email,
                                "role" to "user"
                            )

                            firestore.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                    // Ana sayfaya gönder
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Veritabanı Hatası: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Kayıt Hatası: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}