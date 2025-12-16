package com.omurcansayla.fitrota

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omurcansayla.fitrota.model.Booking
import java.util.Calendar
import java.util.UUID

class ReservationActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val titleText = findViewById<TextView>(R.id.txtFacilityNameTitle)
        val dateInput = findViewById<EditText>(R.id.editTextDate)

        // Yeni Kutular
        val startTimeInput = findViewById<EditText>(R.id.editTextStartTime)
        val endTimeInput = findViewById<EditText>(R.id.editTextEndTime)

        val confirmButton = findViewById<Button>(R.id.btnConfirmReservation)

        val facilityName = intent.getStringExtra("facilityName") ?: "Tesis"
        val facilityId = intent.getStringExtra("facilityId") ?: ""
        titleText.text = "$facilityName İçin Rezervasyon"

        // 1. TARİH SEÇİCİ
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dateInput.setText(formattedDate)
            }, year, month, day)
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        // 2. BAŞLANGIÇ SAATİ SEÇİCİ
        startTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog(this, { _, hour, minute ->
                val formattedTime = String.format("%02d:%02d", hour, minute)
                startTimeInput.setText(formattedTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }

        // 3. BİTİŞ SAATİ SEÇİCİ
        endTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog(this, { _, hour, minute ->
                val formattedTime = String.format("%02d:%02d", hour, minute)
                endTimeInput.setText(formattedTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }

        // 4. KAYDETME (ARALIK BİRLEŞTİRME)
        confirmButton.setOnClickListener {
            val date = dateInput.text.toString()
            val start = startTimeInput.text.toString()
            val end = endTimeInput.text.toString()

            if (date.isNotEmpty() && start.isNotEmpty() && end.isNotEmpty()) {

                // İki saati birleştirip tek bir metin yapıyoruz
                // Örnek: "14:00 - 15:00"
                val timeRange = "$start - $end"

                saveBooking(facilityId, facilityName, date, timeRange)
            } else {
                Toast.makeText(this, "Lütfen Tarih ve Saat aralığını eksiksiz girin.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBooking(facilityId: String, facilityName: String, date: String, timeRange: String) {
        val userId = auth.currentUser?.uid ?: return
        val bookingId = UUID.randomUUID().toString()

        val newBooking = Booking(
            bookingId = bookingId,
            userId = userId,
            facilityId = facilityId,
            facilityName = facilityName,
            date = date,
            time = timeRange, // Birleştirilmiş aralık buraya gidiyor
            status = "Bekliyor"
        )

        firestore.collection("bookings").document(bookingId).set(newBooking)
            .addOnSuccessListener {
                Toast.makeText(this, "Randevu Oluşturuldu! ⚽", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata oluştu!", Toast.LENGTH_SHORT).show()
            }
    }
}