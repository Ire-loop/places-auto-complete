package com.example.places.core.domain.parser

import android.util.Log
import com.example.places.core.domain.model.LatLngLocation
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoogleMapsHtmlParser"

@Singleton
class GoogleMapsHtmlParser @Inject constructor() {

    fun robustGeocode(placeName: String): LatLngLocation? {
        try {
            val encodedPlace = URLEncoder.encode(placeName, "UTF-8")
            val urlString = "https://www.google.com/maps/place/$encodedPlace"

            // Use HttpURLConnection for better control
            val url = URI(urlString).toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            )
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val html = connection.inputStream.bufferedReader().use { it.readText() }

            /*
            * Method 1: Look for coordinates in various JSON patterns
            * for coordinates that appear as [null, null, [lat,lng]]
            */
            val coordPattern1 = Regex("""\[null,null,\[(-?\d+\.\d+),(-?\d+\.\d+)\]\]""")
            val match1 = coordPattern1.find(html)

            // Pattern for coordinates in format "lat,lng" with proper decimal numbers
            val coordPattern2 =
                Regex("""(?<![\d])(-?\d{1,2}\.\d{3,15}),(-?\d{1,3}\.\d{3,15})(?![\d])""")

            // Pattern for coordinates in viewport bounds
            val viewportPattern =
                Regex("""viewport[^}]*?(-?\d+\.\d+)[^}]*?(-?\d+\.\d+)[^}]*?(-?\d+\.\d+)[^}]*?(-?\d+\.\d+)""")

            var latitude: String? = null
            var longitude: String? = null

            // Try the first pattern
            if (match1 != null) {
                latitude = match1.groupValues[2]
                longitude = match1.groupValues[1]
            }

            // If not found, try the second pattern but validate the values
            if (latitude == null || longitude == null) {
                val matches = coordPattern2.findAll(html)
                for (match in matches) {
                    val lat = match.groupValues[2].toDoubleOrNull()
                    val lng = match.groupValues[1].toDoubleOrNull()

                    // Validate that these are reasonable coordinates
                    if (lat != null && lng != null &&
                        lat >= -90 && lat <= 90 &&
                        lng >= -180 && lng <= 180
                    ) {
                        latitude = match.groupValues[2]
                        longitude = match.groupValues[1]
                        break
                    }
                }
            }

            // If still not found, try a viewport pattern and calculate center
            if (latitude == null || longitude == null) {
                val viewportMatch = viewportPattern.find(html)
                if (viewportMatch != null) {
                    val values =
                        viewportMatch.groupValues.drop(1).mapNotNull { it.toDoubleOrNull() }
                    if (values.size >= 4) {
                        // Calculate center from viewport bounds
                        val lat = (values[0] + values[2]) / 2
                        val lng = (values[1] + values[3]) / 2
                        if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                            latitude = lat.toString()
                            longitude = lng.toString()
                        }
                    }
                }
            }

            // Method 2: Fallback to looking for specific markers in URL patterns
            if (latitude == null || longitude == null) {
                // Look for @lat, lng pattern in URLs
                val urlPattern = Regex("""@(-?\d+\.\d+),(-?\d+\.\d+)""")
                val urlMatch = urlPattern.find(html)
                if (urlMatch != null) {
                    latitude = urlMatch.groupValues[2]
                    longitude = urlMatch.groupValues[1]
                }
            }

            // Method 3: Look in static map URLs
            if (latitude == null || longitude == null) {
                val staticMapPattern =
                    Regex("""staticmap\?[^"]*center=(-?\d+\.?\d*)[%,](-?\d+\.?\d*)""")
                val staticMatch = staticMapPattern.find(html)
                if (staticMatch != null) {
                    latitude = staticMatch.groupValues[2]
                    longitude = staticMatch.groupValues[1]
                }
            }

            if (latitude != null && longitude != null) {
                // Extract address from meta-tags or title
                val addressPattern = Regex("""<meta content="([^"]+)"[^>]*property="og:title"""")
                val addressMatch = addressPattern.find(html)

                println("Address: ${addressMatch?.groupValues}")

                val address = addressMatch?.groupValues?.get(1)
                    ?: Regex("""<title>([^<]+)</title>""").find(html)?.groupValues?.get(1)
                    ?: placeName

                // Clean up address
                val cleanAddress = address
                    .replace(" - Google Maps", "")
                    .replace("Google Maps", "")
                    .replace("\\u0026", "&")
                    .replace("\\u002F", "/")
                    .trim()

                // Extract postal code (Indian format 6 digits or other formats)
                val postalCodePatterns = listOf(
                    Regex("""\b(\d{6})\b"""),           // Indian postal code
                    Regex("""\b(\d{5})\b"""),           // US ZIP code
                    Regex("""\b([A-Z]\d[A-Z] \d[A-Z]\d)\b""") // Canadian postal code
                )

                var postalCode = ""
                for (pattern in postalCodePatterns) {
                    val match = pattern.find(cleanAddress)
                    if (match != null) {
                        postalCode = match.value
                        break
                    }
                }

                Log.d(TAG, "Geocoded address: $cleanAddress")

                return LatLngLocation(latitude.toDouble(), longitude.toDouble())
            }

            return null

        } catch (e: Exception) {
            println("Error geocoding: ${e.message}")
            return null
        }
    }
}