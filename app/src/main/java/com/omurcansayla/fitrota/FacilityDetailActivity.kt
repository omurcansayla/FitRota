package com.omurcansayla.fitrota

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FacilityDetailActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facility_detail)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Ekran Elemanları
        val imgView = findViewById<ImageView>(R.id.imgDetailFacility)
        val txtName = findViewById<TextView>(R.id.txtDetailName)
        val txtDesc = findViewById<TextView>(R.id.txtDetailDescription)
        val txtPrice = findViewById<TextView>(R.id.txtDetailPrice)
        val btnReservation = findViewById<Button>(R.id.btnMakeReservation)
        val btnMap = findViewById<Button>(R.id.btnShowMap)

        // Admin Butonları
        val adminLayout = findViewById<LinearLayout>(R.id.adminPanelLayout)
        val btnEdit = findViewById<Button>(R.id.btnEditFacility)
        val btnDelete = findViewById<Button>(R.id.btnDeleteFacility)

        // Verileri Al
        val facilityId = intent.getStringExtra("facilityId")
        val name = intent.getStringExtra("name")
        val desc = intent.getStringExtra("desc")
        val price = intent.getDoubleExtra("price", 0.0)
        val imageUrl = intent.getStringExtra("image")
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)

        // Verileri Yaz
        txtName.text = name
        txtDesc.text = desc
        txtPrice.text = "$price TL / Saat"

        // Resim Yükle
        if (imageUrl != null && imageUrl.startsWith("http")) {
            Glide.with(this).load(imageUrl).into(imgView)
        } else if (imageUrl != null) {
            try {
                val imageBytes = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT)
                val decodedImage = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imgView.setImageBitmap(decodedImage)
            } catch (e: Exception) { e.printStackTrace() }
        }

        // --- ADMİN KONTROLÜ ---
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            // Admin ise butonları göster
                            adminLayout.visibility = View.VISIBLE
                        }
                    }
                }
        }

        // --- SİLME İŞLEMİ ---
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Tesisi Sil")
                .setMessage("Bu tesisi silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
                .setPositiveButton("Evet, Sil") { _, _ ->
                    if (facilityId != null) {
                        firestore.collection("facilities").document(facilityId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Tesis silindi.", Toast.LENGTH_SHORT).show()
                                finish() // Sayfayı kapat
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Hata oluştu.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        // --- GÜNCELLEME İŞLEMİ (Hazırlık) ---
        btnEdit.setOnClickListener {
            // AddFacilityActivity sayfasını 'Düzenleme Modu'nda açacağız
            val intent = Intent(this, AddFacilityActivity::class.java)
            intent.putExtra("isEditMode", true) // Düzenleme modu olduğunu belirtiyoruz
            intent.putExtra("facilityId", facilityId)
            intent.putExtra("name", name)
            intent.putExtra("desc", desc)
            intent.putExtra("price", price)
            intent.putExtra("image", imageUrl)
            intent.putExtra("lat", lat)
            intent.putExtra("lng", lng)
            startActivity(intent)
            finish() // Detay sayfasını kapat
        }

        // Harita Butonu
        btnMap.setOnClickListener {
            if (lat != 0.0 && lng != 0.0) {
                val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($name)")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                try { startActivity(mapIntent) } catch (e: Exception) { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            } else {
                Toast.makeText(this, "Konum bilgisi yok.", Toast.LENGTH_SHORT).show()
            }
        }

        // Rezervasyon Butonu
        btnReservation.setOnClickListener {
            val intent = Intent(this, ReservationActivity::class.java)
            intent.putExtra("facilityId", facilityId)
            intent.putExtra("facilityName", name)
            startActivity(intent)
        }
    }
}