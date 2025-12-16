package com.omurcansayla.fitrota

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omurcansayla.fitrota.model.Booking

class BookingAdapter(private val bookingList: ArrayList<Booking>) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtFacilityName: TextView = itemView.findViewById(R.id.txtBookingFacilityName)
        val txtDate: TextView = itemView.findViewById(R.id.txtBookingDate)
        val txtTime: TextView = itemView.findViewById(R.id.txtBookingTime)
        // txtStatus satırını buradan sildik
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val currentItem = bookingList[position]

        holder.txtFacilityName.text = currentItem.facilityName
        holder.txtDate.text = "📅 ${currentItem.date}"
        holder.txtTime.text = "⏰ ${currentItem.time}"

        // Renklendirme ve durum yazdırma kodlarını tamamen sildik.
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }
}