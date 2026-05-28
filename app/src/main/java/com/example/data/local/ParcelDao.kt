package com.example.data.local

import androidx.room.*
import com.example.data.model.ParcelBooking
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcel_bookings ORDER BY bookedDate DESC")
    fun getAllBookings(): Flow<List<ParcelBooking>>

    @Query("SELECT * FROM parcel_bookings WHERE userId = :userId ORDER BY bookedDate DESC")
    fun getBookingsByUser(userId: Long): Flow<List<ParcelBooking>>

    @Query("SELECT * FROM parcel_bookings WHERE trackingNumber = :trackingNumber LIMIT 1")
    suspend fun getBookingByTrackingNumber(trackingNumber: String): ParcelBooking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: ParcelBooking): Long

    @Update
    suspend fun updateBooking(booking: ParcelBooking)

    @Delete
    suspend fun deleteBooking(booking: ParcelBooking)
}
