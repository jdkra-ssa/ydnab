package com.ydnab.ydnad.data

import com.ydnab.ydnad.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

object TransactionApiService {

    suspend fun fetchTransactions(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int = 50,
        offset: Int = 0
    ): List<Transaction> = withContext(Dispatchers.IO) {
        val safeLimit = if (limit <= 0) 50 else limit
        val safeOffset = if (offset < 0) 0 else offset

        val url = URL(
            "https://6uq5hs41sc.execute-api.us-west-2.amazonaws.com/Prod" +
                "?start_date=${startDate}&end_date=${endDate}" +
                "&limit=$safeLimit&offset=$safeOffset"
        )

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("x-api-key", BuildConfig.API_KEY)
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        val responseText = try {
            connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }

        val json = JSONObject(responseText)

        val bodyArray: JSONArray = if (json.has("body")) {
            JSONArray(json.getString("body"))
        } else {
            JSONArray()
        }

        // Collect transaction items only (SK starts with YYYY-MM-DD), sort chronologically
        val dateSkRegex = Regex("""^\d{4}-\d{2}-\d{2}""")
        val items = (0 until bodyArray.length())
            .map { bodyArray.getJSONObject(it) }
            .filter { dateSkRegex.containsMatchIn(it.optString("SK")) }
        val sorted = items.sortedBy { it.optString("SK") }

        // Apply offset/limit
        val page = sorted.drop(safeOffset).take(safeLimit)

        page.map { item ->
            val sk = item.optString("SK")
            val datePart = sk.substringBefore("#")

            val category = item.optString("Category").replace(",", "")
            val subCategory = item.optString("SubCategory").replace(",", "")
            val memo = item.optString("Memo").replace(",", "")

            val outflow = item.optString("Outflow", "0").replace(",", "").toDoubleOrNull() ?: 0.0
            val inflow = item.optString("Inflow", "0").replace(",", "").toDoubleOrNull() ?: 0.0
            val amount = (inflow - outflow).round2()

            Transaction(
                date = LocalDate.parse(datePart),
                category = category,
                subCategory = subCategory,
                memo = memo,
                amount = amount
            )
        }
    }

    private fun Double.round2(): Double = Math.round(this * 100) / 100.0
}
