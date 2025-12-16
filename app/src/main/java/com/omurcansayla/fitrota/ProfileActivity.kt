package com.omurcansayla.fitrota

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omurcansayla.fitrota.model.Booking

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingList: ArrayList<Booking>
    private lateinit var adapter: BookingAdapter // Adaptör tanımlı

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Ekran Elemanları
        val nameInput = findViewById<EditText>(R.id.editTextProfileName)
        val emailInput = findViewById<EditText>(R.id.editTextProfileEmail)
        val passInput = findViewById<EditText>(R.id.editTextProfilePassword)
        val btnUpdate = findViewById<Button>(R.id.btnUpdateProfile)
        val btnFavorites = findViewById<Button>(R.id.btnGoToFavorites)

        btnFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        // --- LİSTE AYARLARI (BAĞLANTI BURADA YAPILIYOR) ---
        recyclerView = findViewById(R.id.recyclerViewBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)
        bookingList = arrayListOf()

        // Adaptörü oluştur ve listeye bağla
        adapter = BookingAdapter(bookingList)
        recyclerView.adapter = adapter
        // ---------------------------------------------------

        val currentUser = auth.currentUser

        if (currentUser != null) {
            emailInput.setText(currentUser.email)

            // 1. MEVCUT BİLGİLERİ GETİR
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        nameInput.setText(name)
                    }
                }

            // 2. RANDEVULARI GETİR
            loadUserBookings(currentUser.uid)
        }

        // 3. GÜNCELLEME İŞLEMİ
        btnUpdate.setOnClickListener {
            val newName = nameInput.text.toString()
            val newPass = passInput.text.toString()

            if (currentUser != null && newName.isNotEmpty()) {
                // İsim Güncelle
                firestore.collection("users").document(currentUser.uid)
                    .update("name", newName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profil Güncellendi! ✅", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Hata oluştu!", Toast.LENGTH_SHORT).show()
                    }

                // Şifre Güncelle
                if (newPass.isNotEmpty()) {
                    if (newPass.length >= 6) {
                        currentUser.updatePassword(newPass)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Şifre Değişti! 🔑", Toast.LENGTH_SHORT).show()
                                passInput.setText("")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Şifre Hatası: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Şifre en az 6 karakter olmalı.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadUserBookings(userId: String) {
        firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                bookingList.clear()
                if (result.isEmpty) {
                    // Eğer hiç randevu yoksa kullanıcıya bilgi verebiliriz (Opsiyonel)
                    // Toast.makeText(this, "Henüz randevunuz yok.", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in result) {
                        try {
                            val bookingId = document.getString("bookingId") ?: ""
                            val fId = document.getString("facilityId") ?: ""
                            val fName = document.getString("facilityName") ?: ""
                            val uId = document.getString("userId") ?: ""
                            val date = document.getString("date") ?: ""
                            val time = document.getString("time") ?: ""
                            val status = document.getString("status") ?: ""

                            // NOT: Model dosyanla buradaki sıralama uyuşmalı.
                            // Genelde: (id, userId, facilityId, name, date, time, PRICE(0.0), status)
                            val booking = Booking(bookingId, uId, fId, fName, date, time, 0.0, status)

                            bookingList.add(booking)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    // Listeyi yenile ki veriler görünsün
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veriler alınamadı.", Toast.LENGTH_SHORT).show()
            }
    }
}