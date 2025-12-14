package com.example.foodyorder

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "RestaurantTimeCheck"

class RestaurantInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? = null

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.restaurant_info, null)

        val restaurant = marker.tag as? Restaurant ?: return view

        val txtName = view.findViewById<TextView>(R.id.txtRestaurantName)
        val txtHours = view.findViewById<TextView>(R.id.txtHours)
        val txtStatus = view.findViewById<TextView>(R.id.txtStatus)

        val isOpen = isRestaurantOpen(restaurant)

        txtName.text = restaurant.name

        val todayHours = getTodayHoursString(restaurant)
        txtHours.text = todayHours

        txtStatus.text = if (isOpen) "Open Now" else "Closed"
        txtStatus.setTextColor(
            if (isOpen) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        )

        return view
    }
    private fun getTodayHoursString(restaurant: Restaurant): String {
        val day = SimpleDateFormat("EEE", Locale("en", "US")).format(Date()).lowercase()
        val hours = restaurant.openHours[day]
        return hours ?: "Not Available"
    }

    private fun isRestaurantOpen(restaurant: Restaurant): Boolean {
        val openHoursToday = getTodayHoursString(restaurant)

        if (openHoursToday == "Not Available") {
            return false
        }

        return parseAndCheckTime(openHoursToday)
    }

    private fun parseAndCheckTime(openHoursToday: String): Boolean {

        val parts = openHoursToday.split("-")
        if (parts.size != 2) {
            Log.e(TAG, "Wrong format (expected HH:MM-HH:MM).")
            return false
        }

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)

        val startParts = parts[0].split(":")
        val endParts = parts[1].split(":")

        val startHour = startParts[0].toIntOrNull()
        val startMinute = startParts[1].toIntOrNull()
        val endHour = endParts[0].toIntOrNull()
        val endMinute = endParts[1].toIntOrNull()


        if (startHour == null || startMinute == null || endHour == null || endMinute == null) {
            Log.e(TAG, "Parsing error for hours and minutes")
            return false
        }

        val startTotalMinutes = (startHour * 60) + startMinute
        var endTotalMinutes = (endHour * 60) + endMinute
        if (endHour == 0 && endMinute == 0) {
            endTotalMinutes = 24 * 60
        }
        val currentTotalMinutes = (currentHour * 60) + currentMinute

        val isOpen = currentTotalMinutes in startTotalMinutes..endTotalMinutes

        return isOpen
    }

}