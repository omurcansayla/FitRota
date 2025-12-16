package com.omurcansayla.fitrota

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omurcansayla.fitrota.model.Facility

class FavoritesActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FacilityAdapter
    private lateinit var favoriteList: ArrayList<Facility>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(this)
        favoriteList = arrayListOf()
        adapter = FacilityAdapter(favoriteList)
        recyclerView.adapter = adapter

        loadFavorites()
    }

    private fun loadFavorites() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 1. Önce kullanıcının favorilediği ID'leri çek
            firestore.collection("users").document(currentUser.uid)
                .collection("favorites").get()
                .addOnSuccessListener { documents ->
                    val favIds = ArrayList<String>()
                    for (doc in documents) {
                        favIds.add(doc.id) // Belge ID'si tesis ID'sidir
                    }

                    if (favIds.isNotEmpty()) {
                        // 2. Şimdi bu ID'lere sahip tesisleri getir
                        fetchFacilitiesByIds(favIds)
                    } else {
                        Toast.makeText(this, "Henüz favoriniz yok.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun fetchFacilitiesByIds(favIds: ArrayList<String>) {
        // Not: Basit olması için tüm tesisleri çekip filtreleyeceğiz.
        // (Gerçek projelerde "whereIn" kullanılır ama 10'dan fazla olunca patlar, bu yöntem öğrenci projesi için daha güvenli)

        firestore.collection("facilities").get()
            .addOnSuccessListener { result ->
                favoriteList.clear()
                for (document in result) {
                    val facility = document.toObject(Facility::class.java)

                    // Eğer bu tesisin ID'si favori listemizde varsa ekle
                    if (favIds.contains(facility.facilityId)) {
                        favoriteList.add(facility)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }
}