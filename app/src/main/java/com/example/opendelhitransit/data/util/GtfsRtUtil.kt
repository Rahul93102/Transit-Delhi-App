package com.example.opendelhitransit.data.util

import android.util.Log
import com.example.opendelhitransit.data.model.BusLocation
import okhttp3.ResponseBody
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date

/**
 * A utility class to handle GTFS-RT Protocol Buffer binary data.
 * This is a simplified implementation that directly parses the binary data
 * without relying on generated protobuf classes.
 */
object GtfsRtUtil {
    private const val TAG = "GtfsRtUtil"
    
    // Wire types in Protocol Buffers
    private const val WIRE_TYPE_VARINT = 0
    private const val WIRE_TYPE_64BIT = 1
    private const val WIRE_TYPE_LENGTH_DELIMITED = 2
    private const val WIRE_TYPE_32BIT = 5
    
    // Field IDs from the proto file
    private const val ENTITY_LIST_FIELD = 2L
    private const val VEHICLE_FIELD = 4L
    private const val VEHICLE_ID_FIELD = 1L
    private const val VEHICLE_LABEL_FIELD = 2L
    private const val TRIP_FIELD = 1L
    private const val TRIP_ROUTE_ID_FIELD = 5L
    private const val TRIP_ID_FIELD = 1L
    private const val POSITION_FIELD = 2L
    private const val POSITION_LATITUDE_FIELD = 1L
    private const val POSITION_LONGITUDE_FIELD = 2L
    private const val POSITION_BEARING_FIELD = 3L
    private const val POSITION_SPEED_FIELD = 5L
    private const val TIMESTAMP_FIELD = 5L
    
    fun parseVehiclePositions(responseBody: ResponseBody): List<BusLocation> {
        val busLocations = mutableListOf<BusLocation>()
        
        try {
            val bytes = responseBody.bytes()
            
            // Simple implementation to extract key vehicle data
            // This is a simplified parser - production code would use generated classes
            
            var position = 0
            while (position < bytes.size) {
                // Parse field tag (field ID and wire type)
                val tag = readVarint(bytes, position)
                position += getVarintSize(tag)
                
                val fieldId = tag shr 3
                val wireType = (tag and 0x7).toInt()
                
                // Look for entity list (field ID 2)
                if (fieldId == ENTITY_LIST_FIELD && wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                    val length = readVarint(bytes, position).toInt()
                    position += getVarintSize(readVarint(bytes, position))
                    
                    // Process entity
                    val entityBytes = bytes.copyOfRange(position, position + length)
                    val busLocation = parseEntity(entityBytes)
                    if (busLocation != null) {
                        busLocations.add(busLocation)
                    }
                    
                    position += length
                } else {
                    // Skip other fields
                    position = skipField(bytes, position, wireType)
                }
            }
            
            Log.d(TAG, "Parsed ${busLocations.size} bus locations")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing protocol buffer data: ${e.message}")
            e.printStackTrace()
        }
        
        return busLocations
    }
    
    private fun parseEntity(bytes: ByteArray): BusLocation? {
        try {
            var vehicleId = ""
            var label = ""
            var routeId = ""
            var tripId = ""
            var latitude = 0.0
            var longitude = 0.0
            var speed = 0f
            var timestamp = 0L
            
            var position = 0
            while (position < bytes.size) {
                val tag = readVarint(bytes, position)
                position += getVarintSize(tag)
                
                val fieldId = tag shr 3
                val wireType = (tag and 0x7).toInt()
                
                // Look for vehicle field (field ID 4)
                if (fieldId == VEHICLE_FIELD && wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                    val length = readVarint(bytes, position).toInt()
                    position += getVarintSize(readVarint(bytes, position))
                    
                    // Process vehicle data 
                    val vehicleBytes = bytes.copyOfRange(position, position + length)
                    val result = parseVehicle(vehicleBytes)
                    
                    vehicleId = result["vehicleId"] as String? ?: ""
                    label = result["label"] as String? ?: ""
                    routeId = result["routeId"] as String? ?: ""
                    tripId = result["tripId"] as String? ?: ""
                    latitude = result["latitude"] as Double? ?: 0.0
                    longitude = result["longitude"] as Double? ?: 0.0
                    speed = result["speed"] as Float? ?: 0f
                    timestamp = result["timestamp"] as Long? ?: 0L
                    
                    position += length
                } else {
                    // Skip other fields
                    position = skipField(bytes, position, wireType)
                }
            }
            
            if (vehicleId.isNotEmpty() && latitude != 0.0 && longitude != 0.0) {
                return BusLocation(
                    vehicleId = vehicleId,
                    routeId = routeId,
                    tripId = tripId,
                    latitude = latitude,
                    longitude = longitude,
                    bearing = 0f, // Default value if not available
                    speed = speed,
                    timestamp = Date(timestamp * 1000) // Convert UNIX timestamp to Date
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing entity: ${e.message}")
        }
        
        return null
    }
    
    private fun parseVehicle(bytes: ByteArray): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        var position = 0
        while (position < bytes.size) {
            val tag = readVarint(bytes, position)
            position += getVarintSize(tag)
            
            val fieldId = tag shr 3
            val wireType = (tag and 0x7).toInt()
            
            when (fieldId) {
                // Trip field
                TRIP_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        
                        val tripBytes = bytes.copyOfRange(position, position + length)
                        val tripResult = parseTripInfo(tripBytes)
                        result.putAll(tripResult)
                        
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                // Position field
                POSITION_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        
                        val posBytes = bytes.copyOfRange(position, position + length)
                        val posResult = parsePositionInfo(posBytes)
                        result.putAll(posResult)
                        
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                // Vehicle descriptor field
                8L -> { // vehicle descriptor field
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        
                        val vehBytes = bytes.copyOfRange(position, position + length)
                        val vehResult = parseVehicleInfo(vehBytes)
                        result.putAll(vehResult)
                        
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                // Timestamp field
                TIMESTAMP_FIELD -> {
                    if (wireType == WIRE_TYPE_VARINT) {
                        val timestamp = readVarint(bytes, position)
                        position += getVarintSize(timestamp)
                        result["timestamp"] = timestamp
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                else -> {
                    position = skipField(bytes, position, wireType)
                }
            }
        }
        
        return result
    }
    
    private fun parseTripInfo(bytes: ByteArray): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        var position = 0
        while (position < bytes.size) {
            val tag = readVarint(bytes, position)
            position += getVarintSize(tag)
            
            val fieldId = tag shr 3
            val wireType = (tag and 0x7).toInt()
            
            when (fieldId) {
                TRIP_ROUTE_ID_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        
                        val routeId = String(bytes, position, length)
                        result["routeId"] = routeId
                        
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                TRIP_ID_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        
                        val tripId = String(bytes, position, length)
                        result["tripId"] = tripId
                        
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                else -> {
                    position = skipField(bytes, position, wireType)
                }
            }
        }
        
        return result
    }
    
    private fun parsePositionInfo(bytes: ByteArray): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        var position = 0
        while (position < bytes.size) {
            val tag = readVarint(bytes, position)
            position += getVarintSize(tag)
            
            val fieldId = tag shr 3
            val wireType = (tag and 0x7).toInt()
            
            when (fieldId) {
                POSITION_LATITUDE_FIELD -> {
                    if (wireType == WIRE_TYPE_32BIT) {
                        val latitude = readFloat(bytes, position)
                        position += 4
                        result["latitude"] = latitude.toDouble()
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                POSITION_LONGITUDE_FIELD -> {
                    if (wireType == WIRE_TYPE_32BIT) {
                        val longitude = readFloat(bytes, position)
                        position += 4
                        result["longitude"] = longitude.toDouble()
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                POSITION_BEARING_FIELD -> {
                    if (wireType == WIRE_TYPE_32BIT) {
                        val bearing = readFloat(bytes, position)
                        position += 4
                        result["bearing"] = bearing
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                POSITION_SPEED_FIELD -> {
                    if (wireType == WIRE_TYPE_32BIT) {
                        val speed = readFloat(bytes, position)
                        position += 4
                        result["speed"] = speed
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                else -> {
                    position = skipField(bytes, position, wireType)
                }
            }
        }
        
        return result
    }
    
    private fun parseVehicleInfo(bytes: ByteArray): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        var position = 0
        while (position < bytes.size) {
            val tag = readVarint(bytes, position)
            position += getVarintSize(tag)
            
            val fieldId = tag shr 3
            val wireType = (tag and 0x7).toInt()
            
            when (fieldId) {
                VEHICLE_ID_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        
                        val vehicleId = String(bytes, position, length)
                        result["vehicleId"] = vehicleId
                        
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                VEHICLE_LABEL_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        
                        val label = String(bytes, position, length)
                        result["label"] = label
                        
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                
                else -> {
                    position = skipField(bytes, position, wireType)
                }
            }
        }
        
        return result
    }
    
    // Helper function to skip over a field based on wire type
    private fun skipField(bytes: ByteArray, position: Int, wireType: Int): Int {
        var newPosition = position
        
        when (wireType) {
            WIRE_TYPE_VARINT -> {
                val value = readVarint(bytes, newPosition)
                newPosition += getVarintSize(value)
            }
            WIRE_TYPE_64BIT -> {
                newPosition += 8
            }
            WIRE_TYPE_LENGTH_DELIMITED -> {
                val length = readVarint(bytes, newPosition).toInt()
                newPosition += getVarintSize(readVarint(bytes, newPosition))
                newPosition += length
            }
            WIRE_TYPE_32BIT -> {
                newPosition += 4
            }
            else -> {
                // Unknown wire type
                Log.w(TAG, "Unknown wire type: $wireType")
            }
        }
        
        return newPosition
    }
    
    // Read a variable-length integer from the byte array
    private fun readVarint(bytes: ByteArray, position: Int): Long {
        var result: Long = 0
        var shift = 0
        var currentPos = position
        
        while (currentPos < bytes.size) {
            val b = bytes[currentPos++].toLong() and 0xFF
            result = result or ((b and 0x7F) shl shift)
            if (b and 0x80 == 0L) {
                break
            }
            shift += 7
            if (shift >= 64) {
                throw RuntimeException("Malformed varint")
            }
        }
        
        return result
    }
    
    // Get the size of a varint in bytes
    private fun getVarintSize(value: Long): Int {
        var tempValue = value
        var size = 0
        
        do {
            tempValue = tempValue shr 7
            size++
        } while (tempValue != 0L)
        
        return size
    }
    
    // Read a 32-bit float from the byte array
    private fun readFloat(bytes: ByteArray, position: Int): Float {
        return ByteBuffer.wrap(bytes, position, 4).order(ByteOrder.LITTLE_ENDIAN).float
    }
} 