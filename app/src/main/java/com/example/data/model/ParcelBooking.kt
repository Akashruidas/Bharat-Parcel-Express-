package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "parcel_bookings")
data class ParcelBooking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0, // 0 for guest/unlogged bookings
    val trackingNumber: String,
    val senderName: String,
    val senderPhone: String,
    val senderPincode: String,
    val senderCity: String,
    val recipientName: String,
    val recipientPhone: String,
    val recipientPincode: String,
    val recipientCity: String,
    val weightKg: Double,
    val parcelType: String, // "Documents", "Electronics", "Clothing", "Fragile", "Others"
    val priceRs: Double,
    val status: String, // "Booked", "Dispatched", "In Transit", "Out for Delivery", "Delivered"
    val estimatedDays: Int,
    val bookedDate: Long = System.currentTimeMillis(),
    val lastUpdateDate: Long = System.currentTimeMillis(),
    val carrier: String // "Delhivery", "BlueDart", "India Post", "DTDC"
) : Serializable
