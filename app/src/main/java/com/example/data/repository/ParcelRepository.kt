package com.example.data.repository

import com.example.data.local.ParcelDao
import com.example.data.local.UserDao
import com.example.data.model.ParcelBooking
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class ParcelRepository(
    private val parcelDao: ParcelDao,
    private val userDao: UserDao
) {
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserById(id: Long): User? {
        return userDao.getUserById(id)
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    fun getAllBookings(): Flow<List<ParcelBooking>> = parcelDao.getAllBookings()

    fun getBookingsByUser(userId: Long): Flow<List<ParcelBooking>> = parcelDao.getBookingsByUser(userId)

    suspend fun getBookingByTrackingNumber(trackingNumber: String): ParcelBooking? {
        return parcelDao.getBookingByTrackingNumber(trackingNumber)
    }

    suspend fun insertBooking(booking: ParcelBooking): Long {
        return parcelDao.insertBooking(booking)
    }

    suspend fun updateBooking(booking: ParcelBooking) {
        parcelDao.updateBooking(booking)
    }

    suspend fun deleteBooking(booking: ParcelBooking) {
        parcelDao.deleteBooking(booking)
    }
}
