package com.omurcansayla.fitrota

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddFacilityActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var imgView: ImageView
    private var selectedBitmap: Bitmap? = null

    private var isEditMode = false
    private var existingFacilityId: String? = null
    private var existingImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_facility)

        firestore = FirebaseFirestore.getInstance()

        val nameInput = findViewById<EditText>(R.id.editTextFacilityName)
        val descInput = findViewById<EditText>(R.id.editTextDescription)
        val priceInput = findViewById<EditText>(R.id.editTextPrice)
        val latInput = findViewById<EditText>(R.id.editTextLat)
        val lngInput = findViewById<EditText>(R.id.editTextLng)
        val categorySpinner = findViewById<Spinner>(R.id.spinnerCategory) // YENİ
        val saveButton = findViewById<Button>(R.id.buttonSaveFacility)
        val selectImageButton = findViewById<Button>(R.id.btnSelectImage)
        imgView = findViewById(R.id.imgSelectedFacility)

        // --- SPINNER (KATEGORİLER) KURULUMU ---
        val categories = arrayOf("Futbol", "Basketbol", "Tenis", "Yüzme", "Fitness", "Diğer")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        // --- DÜZENLEME MODU KONTROLÜ ---
        isEditMode = intent.getBooleanExtra("isEditMode", false)

        if (isEditMode) {
            existingFacilityId = intent.getStringExtra("facilityId")
            existingImageUrl = intent.getStringExtra("image")

            nameInput.setText(intent.getStringExtra("name"))
            descInput.setText(intent.getStringExtra("desc"))
            priceInput.setText(intent.getDoubleExtra("price", 0.0).toString())
            latInput.setText(intent.getDoubleExtra("lat", 0.0).toString())
            lngInput.setText(intent.getDoubleExtra("lng", 0.0).toString())

            // Kategoriyi Seçili Getir
            val currentCategory = intent.getStringExtra("category") ?: "Diğer"
            val spinnerPosition = adapter.getPosition(currentCategory)
            categorySpinner.setSelection(spinnerPosition)

            saveButton.text = "GÜNCELLE"

            if (existingImageUrl != null) {
                if (existingImageUrl!!.startsWith("http")) {
                    Glide.with(this).load(existingImageUrl).into(imgView)
                } else {
                    try {
                        val imageBytes = android.util.Base64.decode(existingImageUrl, android.util.Base64.DEFAULT)
                        val decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imgView.setImageBitmap(decodedBitmap)
                    } catch (e: Exception) {}
                }
            }
        }

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val inputStream = contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = resizeBitmap(originalBitmap)
                imgView.setImageBitmap(selectedBitmap)
            }
        }

        selectImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val desc = descInput.text.toString()
            val priceText = priceInput.text.toString()
            val latText = latInput.text.toString().replace(",", ".")
            val lngText = lngInput.text.toString().replace(",", ".")
            val selectedCategory = categorySpinner.selectedItem.toString() // SEÇİLEN KATEGORİ

            if (name.isNotEmpty() && priceText.isNotEmpty()) {

                var finalImageString = ""
                if (selectedBitmap != null) {
                    finalImageString = bitmapToString(selectedBitmap!!)
                } else if (isEditMode && existingImageUrl != null) {
                    finalImageString = existingImageUrl!!
                } else {
                    Toast.makeText(this, "Lütfen bir resim seçin.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val facilityId = if (isEditMode) existingFacilityId!! else UUID.randomUUID().toString()
                val price = priceText.toDoubleOrNull() ?: 0.0
                val lat = latText.toDoubleOrNull() ?: 0.0
                val lng = lngText.toDoubleOrNull() ?: 0.0

                val facilityMap = hashMapOf(
                    "facilityId" to facilityId,
                    "name" to name,
                    "description" to desc,
                    "pricePerHour" to price,
                    "imageUrl" to finalImageString,
                    "locationLat" to lat,
                    "locationLng" to lng,
                    "availableHours" to listOf("10:00", "11:00", "12:00"),
                    "category" to selectedCategory // KAYDEDİYORUZ
                )

                firestore.collection("facilities").document(facilityId).set(facilityMap)
                    .addOnSuccessListener {
                        val msg = if (isEditMode) "Tesis Güncellendi! ✅" else "Tesis Eklendi! 🎉"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Hata oluştu!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Lütfen zorunlu alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bitmapToString(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val maxSize = 600
        if (width > maxSize || height > maxSize) {
            val ratio = width.toFloat() / height.toFloat()
            if (ratio > 1) {
                width = maxSize
                height = (width / ratio).toInt()
            } else {
                height = maxSize
                width = (height * ratio).toInt()
            }
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}