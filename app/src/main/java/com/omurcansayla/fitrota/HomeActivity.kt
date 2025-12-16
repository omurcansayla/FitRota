package com.omurcansayla.fitrota

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omurcansayla.fitrota.model.Facility
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FacilityAdapter

    // Listeler
    private lateinit var fullFacilityList: ArrayList<Facility> // Tüm veriler burada duracak
    private lateinit var displayedList: ArrayList<Facility>    // Ekranda görünenler burada

    // Filtre Durumu
    private var currentCategory = "Tümü"
    private var currentSearchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI Elemanları
        val welcomeText = findViewById<TextView>(R.id.textViewWelcome)
        val addFacilityButton = findViewById<Button>(R.id.buttonAddFacilityPage)
        val logoutButton = findViewById<Button>(R.id.buttonLogout)
        val profileButton = findViewById<Button>(R.id.buttonGoProfile)
        val searchView = findViewById<SearchView>(R.id.searchViewFacility)

        // Kategori Butonları
        val btnAll = findViewById<Button>(R.id.btnCatAll)
        val btnFootball = findViewById<Button>(R.id.btnCatFootball)
        val btnBasketball = findViewById<Button>(R.id.btnCatBasketball)
        val btnTennis = findViewById<Button>(R.id.btnCatTennis)
        val btnSwim = findViewById<Button>(R.id.btnCatSwimming)

        // Buton Listesi (Renk değiştirmek için)
        val categoryButtons = listOf(btnAll, btnFootball, btnBasketball, btnTennis, btnSwim)

        addFacilityButton.visibility = android.view.View.GONE

        // Kullanıcı Bilgisi
        val currentUser = auth.currentUser
        if (currentUser != null) {
            welcomeText.text = "Merhaba..."
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        if (!name.isNullOrEmpty()) welcomeText.text = "Merhaba, $name 👋"

                        val role = document.getString("role")
                        if (role == "admin") addFacilityButton.visibility = android.view.View.VISIBLE
                    }
                }
        }

        // --- LİSTE KURULUMU ---
        recyclerView = findViewById(R.id.recyclerViewFacilities)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fullFacilityList = arrayListOf()
        displayedList = arrayListOf()
        adapter = FacilityAdapter(displayedList)
        recyclerView.adapter = adapter

        // --- VERİLERİ ÇEK ---
        getFacilitiesFromFirestore()

        // --- ARAMA İŞLEMİ ---
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { return false }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchText = newText ?: ""
                filterList() // Her harf girildiğinde filtrele
                return true
            }
        })

        // --- KATEGORİ İŞLEMİ ---
        fun setCategory(selectedBtn: Button, category: String) {
            currentCategory = category

            // Renkleri Sıfırla (Hepsini Gri Yap)
            for (btn in categoryButtons) {
                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDBDBD"))
            }
            // Seçileni Turuncu Yap
            selectedBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F57C00"))

            filterList() // Filtrele
        }

        btnAll.setOnClickListener { setCategory(btnAll, "Tümü") }
        btnFootball.setOnClickListener { setCategory(btnFootball, "Futbol") }
        btnBasketball.setOnClickListener { setCategory(btnBasketball, "Basketbol") }
        btnTennis.setOnClickListener { setCategory(btnTennis, "Tenis") }
        btnSwim.setOnClickListener { setCategory(btnSwim, "Yüzme") }

        // Diğer Butonlar
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        addFacilityButton.setOnClickListener { startActivity(Intent(this, AddFacilityActivity::class.java)) }
        profileButton.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
    }

    private fun getFacilitiesFromFirestore() {
        firestore.collection("facilities").addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener

            fullFacilityList.clear()
            if (value != null) {
                for (document in value) {
                    val facility = document.toObject(Facility::class.java)
                    fullFacilityList.add(facility)
                }
                // İlk açılışta filtreyi çalıştır (Tümü görünür)
                filterList()
            }
        }
    }

    // --- FİLTRELEME MANTIĞI ---
    private fun filterList() {
        val filtered = ArrayList<Facility>()

        val searchTextLower = currentSearchText.lowercase(Locale.getDefault())

        for (item in fullFacilityList) {
            // 1. İsim Araması Kontrolü
            val matchesSearch = item.name.lowercase(Locale.getDefault()).contains(searchTextLower)

            // 2. Kategori Kontrolü
            val matchesCategory = if (currentCategory == "Tümü") true else item.category == currentCategory

            // İkisi de uyuyorsa listeye ekle
            if (matchesSearch && matchesCategory) {
                filtered.add(item)
            }
        }

        // Listeyi güncelle
        displayedList.clear()
        displayedList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }
}