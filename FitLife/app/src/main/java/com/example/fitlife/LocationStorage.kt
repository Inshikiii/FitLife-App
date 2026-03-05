package com.example.fitlife

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object LocationStorage {

    private const val PREF_NAME = "fitlife_locations"
    private const val KEY_LOCATIONS = "locations"

    fun saveLocation(context: Context, location: WorkoutLocation) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val array = JSONArray(prefs.getString(KEY_LOCATIONS, "[]"))

        val obj = JSONObject()
        obj.put("name", location.name)
        obj.put("lat", location.latitude)
        obj.put("lng", location.longitude)

        array.put(obj)
        prefs.edit().putString(KEY_LOCATIONS, array.toString()).apply()
    }

    fun getLocations(context: Context): List<WorkoutLocation> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_LOCATIONS, "[]")
        val array = JSONArray(json)

        val list = mutableListOf<WorkoutLocation>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                WorkoutLocation(
                    obj.getString("name"),
                    obj.getDouble("lat"),
                    obj.getDouble("lng")
                )
            )
        }
        return list
    }
    fun deleteLocation(context: Context, latitude: Double, longitude: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_LOCATIONS, "[]")
        val array = JSONArray(json)

        val newArray = JSONArray()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val lat = obj.getDouble("lat")
            val lng = obj.getDouble("lng")

            // keep everything EXCEPT the tapped marker
            if (lat != latitude || lng != longitude) {
                newArray.put(obj)
            }
        }

        prefs.edit().putString(KEY_LOCATIONS, newArray.toString()).apply()
    }

}
