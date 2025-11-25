package com.example.foodyorder

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RestaurantInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? = null

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.restaurant_info, null)

        val restaurant = marker.tag as? Restaurant ?: return view

        val txtName = view.findViewById<TextView>(R.id.txtRestaurantName)
        val txtHours = view.findViewById<TextView>(R.id.txtHours)
        val txtStatus = view.findViewById<TextView>(R.id.txtStatus)

        txtName.text = restaurant.name
        txtHours.text = getTodayHours(restaurant)
        txtStatus.text = if (isRestaurantOpen(restaurant)) "Open Now" else "Closed"

        txtStatus.setTextColor(
            if (isRestaurantOpen(restaurant)) Color.GREEN else Color.RED
        )

        return view
    }

    private fun getTodayHours(restaurant: Restaurant): String {
        val day = SimpleDateFormat("EEE", Locale.getDefault()).format(Date()).lowercase()
        return restaurant.openHours[day] ?: "Not Available"
    }

    private fun isRestaurantOpen(restaurant: Restaurant): Boolean {
        val today = getTodayHours(restaurant)
        if (!today.contains("-")) return false

        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val (start, end) = today.split("-")
        return now >= start && now <= end
    }
}
