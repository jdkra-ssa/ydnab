package com.ydnab.ydnad.util

import com.ydnab.ydnad.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Categories {

    private val fallbackMap: LinkedHashMap<String, List<String>> = linkedMapOf(
        "Housing" to listOf("Rent/Mortgage", "Utilities", "Maintenance", "Insurance"),
        "Food" to listOf("Groceries", "Restaurants", "Coffee", "Fast Food"),
        "Transport" to listOf("Gas", "Car Payment", "Public Transit", "Parking", "Maintenance"),
        "Healthcare" to listOf("Doctor", "Pharmacy", "Dental", "Vision"),
        "Entertainment" to listOf("Streaming", "Movies", "Games", "Hobbies"),
        "Shopping" to listOf("Clothing", "Electronics", "Household"),
        "Personal Care" to listOf("Haircut", "Gym", "Personal Care"),
        "Education" to listOf("Tuition", "Books", "Courses"),
        "Other" to listOf("Miscellaneous", "Gifts", "Fees")
    )

    private var loadedMap: LinkedHashMap<String, List<String>> = fallbackMap

    val names: List<String> get() = loadedMap.keys.toList()

    fun subCategories(category: String): List<String> = loadedMap[category] ?: emptyList()

    suspend fun load() {
        try {
            val result = withContext(Dispatchers.IO) { fetchFromApi() }
            if (result.isNotEmpty()) {
                loadedMap = result
            }
        } catch (_: Exception) {
            // keep fallback map
        }
    }

    private fun fetchFromApi(): LinkedHashMap<String, List<String>> {
        val url = URL(BuildConfig.API_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("x-api-key", BuildConfig.API_KEY)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        check(connection.responseCode == HttpURLConnection.HTTP_OK) {
            "HTTP ${connection.responseCode}"
        }

        val responseText = connection.inputStream.bufferedReader().readText()
        connection.disconnect()

        val jsonData = JSONObject(responseText)
        if (!jsonData.has("body")) return linkedMapOf()

        val bodyArray = JSONArray(jsonData.getString("body"))
        val items = (0 until bodyArray.length()).map { bodyArray.getJSONObject(it) }
        val sorted = items.sortedBy { it.optString("SK") }

        val map = LinkedHashMap<String, MutableList<String>>()
        for (item in sorted) {
            val sk = item.optString("SK")
            if (sk.isEmpty()) continue
            val colonIdx = sk.indexOf(':')
            if (colonIdx < 0) continue
            val category = sk.substring(0, colonIdx).trim()
            val subCategory = sk.substring(colonIdx + 1).trim()
            if (category.isEmpty() || subCategory.isEmpty()) continue
            map.getOrPut(category) { mutableListOf() }.add(subCategory)
        }

        return LinkedHashMap<String, List<String>>(map)
    }
}
