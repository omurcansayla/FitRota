package com.omurcansayla.fitrota

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omurcansayla.fitrota.model.Facility

class FacilityAdapter(private val facilityList: ArrayList<Facility>) : RecyclerView.Adapter<FacilityAdapter.FacilityViewHolder>() {

    class FacilityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textViewFacilityName)
        val priceText: TextView = itemView.findViewById(R.id.textViewFacilityPrice)
        val imageView: ImageView = itemView.findViewById(R.id.imageViewFacility)
        val btnFavorite: ImageView = itemView.findViewById(R.id.btnFavorite) // Yıldız İkonu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_facility, parent, false)
        return FacilityViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
        val currentItem = facilityList[position]
        val context = holder.itemView.context

        // Firebase Araçları
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        holder.nameText.text = currentItem.name
        holder.priceText.text = "${currentItem.pricePerHour} TL / Saat"

        // Resim Yükleme
        if (currentItem.imageUrl.startsWith("http")) {
            Glide.with(context).load(currentItem.imageUrl).into(holder.imageView)
        } else {
            try {
                val imageBytes = android.util.Base64.decode(currentItem.imageUrl, android.util.Base64.DEFAULT)
                val decodedImage = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.imageView.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        // --- FAVORİ KONTROLÜ (Başlangıçta Dolu mu Boş mu?) ---
        if (currentUser != null) {
            val favRef = firestore.collection("users").document(currentUser.uid)
                .collection("favorites").document(currentItem.facilityId)

            favRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Favorilerde VARSA -> Dolu Yıldız
                    holder.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                    holder.btnFavorite.tag = "fav" // Durumu aklımızda tutalım
                } else {
                    // Favorilerde YOKSA -> Boş Yıldız
                    holder.btnFavorite.setImageResource(android.R.drawable.star_big_off)
                    holder.btnFavorite.tag = "not_fav"
                }
            }
        }

        // --- YILDIZA TIKLAYINCA ---
        holder.btnFavorite.setOnClickListener {
            if (currentUser != null) {
                val favRef = firestore.collection("users").document(currentUser.uid)
                    .collection("favorites").document(currentItem.facilityId)

                if (holder.btnFavorite.tag == "fav") {
                    // Zaten favoriymiş, TIKLAYINCA SİL (Çıkar)
                    favRef.delete().addOnSuccessListener {
                        holder.btnFavorite.setImageResource(android.R.drawable.star_big_off)
                        holder.btnFavorite.tag = "not_fav"
                        Toast.makeText(context, "Favorilerden çıkarıldı", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Favori değilmiş, TIKLAYINCA EKLE
                    val favData = hashMapOf(
                        "facilityId" to currentItem.facilityId,
                        "name" to currentItem.name,
                        "addedAt" to com.google.firebase.Timestamp.now()
                    )

                    favRef.set(favData).addOnSuccessListener {
                        holder.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                        holder.btnFavorite.tag = "fav"
                        Toast.makeText(context, "Favorilere eklendi! ⭐", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Lütfen giriş yapın", Toast.LENGTH_SHORT).show()
            }
        }

        // Karta Tıklayınca Detaya Git (Eski Özellik)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, FacilityDetailActivity::class.java)
            intent.putExtra("facilityId", currentItem.facilityId)
            intent.putExtra("name", currentItem.name)
            intent.putExtra("desc", currentItem.description)
            intent.putExtra("price", currentItem.pricePerHour)
            intent.putExtra("image", currentItem.imageUrl)
            intent.putExtra("lat", currentItem.locationLat)
            intent.putExtra("lng", currentItem.locationLng)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return facilityList.size
    }
}