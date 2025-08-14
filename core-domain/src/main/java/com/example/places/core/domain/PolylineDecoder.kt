package com.example.places.core.domain

import android.util.Log
import com.google.android.gms.maps.model.LatLng

private const val TAG = "PolylineDecoder"

/**
 * Polyline decoder for Google Routes API
 * Implements Google's polyline encoding algorithm with error handling
 */
object PolylineDecoder {

    /**
     * Decodes an encoded polyline string into a list of LatLng coordinates
     * @param encodedPolyline The encoded polyline string from Google Routes API
     * @param precision The decimal precision (default: 5 for Google Maps)
     * @return List of LatLng coordinates, empty list on error
     */
    @JvmStatic
    fun decode(
        encodedPolyline: String?,
        precision: Int = 5
    ): List<LatLng> {
        Log.d(TAG, "Decoding polyline - length: ${encodedPolyline?.length}, precision: $precision")
        
        // Input validation
        if (encodedPolyline.isNullOrBlank()) {
            Log.w(TAG, "Polyline is null or blank")
            return emptyList()
        }

        return try {
            val result = decodePolylineInternal(encodedPolyline, precision)
            Log.d(TAG, "Successfully decoded ${result.size} coordinates")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding polyline: ${e.message}, polyline: ${encodedPolyline.take(100)}...", e)
            emptyList()
        }
    }

    private fun decodePolylineInternal(
        encoded: String,
        precision: Int
    ): List<LatLng> {
        Log.d(TAG, "Starting polyline decoding - string length: ${encoded.length}, precision: $precision")
        
        val coordinates = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        val factor = Math.pow(10.0, precision.toDouble()).toInt()

        Log.d(TAG, "Decoding factor: $factor")

        while (index < len) {
            val startIndex = index
            
            // Decode latitude delta
            val latResult = decodeValue(encoded, index)
            lat += latResult.value
            index = latResult.nextIndex

            Log.v(TAG, "Decoded lat delta: ${latResult.value} at index $startIndex->${latResult.nextIndex}")

            // Check bounds after latitude decoding
            if (index >= len) {
                Log.w(TAG, "Reached end of string after latitude decoding at index $index")
                break
            }

            val lngStartIndex = index
            
            // Decode longitude delta
            val lngResult = decodeValue(encoded, index)
            lng += lngResult.value
            index = lngResult.nextIndex

            Log.v(TAG, "Decoded lng delta: ${lngResult.value} at index $lngStartIndex->${lngResult.nextIndex}")

            // Convert to double and create LatLng
            val latLng = LatLng(
                lat.toDouble() / factor,
                lng.toDouble() / factor
            )
            coordinates.add(latLng)
            
            if (coordinates.size <= 5 || coordinates.size % 100 == 0) {
                Log.d(TAG, "Point ${coordinates.size}: ${latLng.latitude}, ${latLng.longitude}")
            }
        }

        Log.d(TAG, "Completed decoding - ${coordinates.size} points extracted")
        return coordinates
    }

    private data class DecodeResult(val value: Int, val nextIndex: Int)

    private fun decodeValue(encoded: String, startIndex: Int): DecodeResult {
        var index = startIndex
        var shift = 0
        var result = 0

        do {
            if (index >= encoded.length) {
                throw IllegalArgumentException("Invalid polyline: unexpected end of string")
            }

            val b = encoded[index++].code - 63
            result = result or ((b and 0x1F) shl shift)
            shift += 5
        } while (b >= 0x20)

        // Apply two's complement if negative
        val deltaValue = if (result and 1 != 0) {
            (result shr 1).inv()
        } else {
            result shr 1
        }

        return DecodeResult(deltaValue, index)
    }

    /**
     * Simplify polyline using Douglas-Peucker algorithm
     * Reduces number of points while maintaining visual accuracy
     * @param points Original polyline points
     * @param tolerance Simplification tolerance (0.0001 = ~7-11 meters)
     */
    fun simplifyPolyline(
        points: List<LatLng>,
        tolerance: Double = 0.0001
    ): List<LatLng> {
        Log.d(TAG, "Simplifying polyline - original points: ${points.size}, tolerance: $tolerance")
        
        if (points.size <= 2) {
            Log.d(TAG, "Polyline has ${points.size} points, no simplification needed")
            return points
        }

        val simplified = douglasPeucker(points, tolerance)
        Log.d(TAG, "Polyline simplified from ${points.size} to ${simplified.size} points (${String.format("%.1f", (1 - simplified.size.toDouble() / points.size) * 100)}% reduction)")
        
        return simplified
    }

    private fun douglasPeucker(points: List<LatLng>, epsilon: Double): List<LatLng> {
        if (points.size <= 2) return points

        // Find point with maximum distance from line
        var maxDistance = 0.0
        var maxIndex = 0

        for (i in 1 until points.size - 1) {
            val distance = perpendicularDistance(
                points[i],
                points.first(),
                points.last()
            )
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        return if (maxDistance > epsilon) {
            // Recursive simplification
            val left = douglasPeucker(points.subList(0, maxIndex + 1), epsilon)
            val right = douglasPeucker(points.subList(maxIndex, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(points.first(), points.last())
        }
    }

    private fun perpendicularDistance(
        point: LatLng,
        lineStart: LatLng,
        lineEnd: LatLng
    ): Double {
        val A = point.latitude - lineStart.latitude
        val B = point.longitude - lineStart.longitude
        val C = lineEnd.latitude - lineStart.latitude
        val D = lineEnd.longitude - lineStart.longitude

        val dot = A * C + B * D
        val lenSq = C * C + D * D

        if (lenSq == 0.0) {
            return kotlin.math.sqrt(A * A + B * B)
        }

        val param = dot / lenSq
        val xx: Double
        val yy: Double

        when {
            param < 0 -> {
                xx = lineStart.latitude
                yy = lineStart.longitude
            }
            param > 1 -> {
                xx = lineEnd.latitude
                yy = lineEnd.longitude
            }
            else -> {
                xx = lineStart.latitude + param * C
                yy = lineStart.longitude + param * D
            }
        }

        val dx = point.latitude - xx
        val dy = point.longitude - yy
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}