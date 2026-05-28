package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ParcelBooking
import com.example.data.model.User
import com.example.data.repository.ParcelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.random.Random

sealed interface BookingState {
    object Idle : BookingState
    object Loading : BookingState
    data class Success(val trackingNumber: String) : BookingState
    data class Error(val message: String) : BookingState
}

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}

class ParcelViewModel(private val repository: ParcelRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _searchResult = MutableStateFlow<ParcelBooking?>(null)
    val searchResult: StateFlow<ParcelBooking?> = _searchResult.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _searchLoading = MutableStateFlow(false)
    val searchLoading: StateFlow<Boolean> = _searchLoading.asStateFlow()

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState.asStateFlow()

    val userBookings: StateFlow<List<ParcelBooking>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getBookingsByUser(user.id)
        } else {
            repository.getAllBookings()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.getAllBookings().first().let { currentList ->
                if (currentList.isEmpty()) {
                    seedDummyBookings()
                }
            }
        }
    }

    private suspend fun seedDummyBookings() {
        val dummyParcels = listOf(
            ParcelBooking(
                trackingNumber = "IND839401732",
                senderName = "Arjun Sharma",
                senderPhone = "9876543210",
                senderPincode = "400001",
                senderCity = "Mumbai",
                recipientName = "Priya Patel",
                recipientPhone = "9123456780",
                recipientPincode = "380001",
                recipientCity = "Ahmedabad",
                weightKg = 1.2,
                parcelType = "Documents",
                priceRs = 180.0,
                status = "In Transit",
                estimatedDays = 2,
                carrier = "BlueDart"
            ),
            ParcelBooking(
                trackingNumber = "IND492048210",
                senderName = "Vikram Rao",
                senderPhone = "9440123456",
                senderPincode = "560001",
                senderCity = "Bengaluru",
                recipientName = "Aditi Sen",
                recipientPhone = "9830123456",
                recipientPincode = "700001",
                recipientCity = "Kolkata",
                weightKg = 0.5,
                parcelType = "Electronics",
                priceRs = 250.0,
                status = "Delivered",
                estimatedDays = 3,
                carrier = "Delhivery"
            ),
            ParcelBooking(
                trackingNumber = "IND104820394",
                senderName = "Sanjay Dutt",
                senderPhone = "8123456789",
                senderPincode = "600001",
                senderCity = "Chennai",
                recipientName = "Rajesh Verma",
                recipientPhone = "9001234567",
                recipientPincode = "411001",
                recipientCity = "Pune",
                weightKg = 3.5,
                parcelType = "Others",
                priceRs = 320.0,
                status = "Out for Delivery",
                estimatedDays = 4,
                carrier = "India Post"
            )
        )
        for (parcel in dummyParcels) {
            repository.insertBooking(parcel)
        }
    }

    fun trackParcel(trackingNum: String) {
        val cleanNum = trackingNum.trim().uppercase()
        if (cleanNum.isEmpty()) {
            _searchError.value = "Please enter a tracking number."
            _searchResult.value = null
            return
        }

        _searchLoading.value = true
        _searchError.value = null

        viewModelScope.launch {
            val res = repository.getBookingByTrackingNumber(cleanNum)
            _searchLoading.value = false
            if (res != null) {
                _searchResult.value = res
            } else {
                _searchResult.value = null
                _searchError.value = "No package found with ID '$cleanNum'"
            }
        }
    }

    fun clearSearchResult() {
        _searchResult.value = null
        _searchError.value = null
    }

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user != null) {
                val hashed = hashPassword(pass)
                if (user.passwordHash == hashed) {
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Incorrect password. Please try again.")
                }
            } else {
                _authState.value = AuthState.Error("User with this email does not exist.")
            }
        }
    }

    fun signup(name: String, email: String, pass: String) {
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("All fields are required.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val trimmedEmail = email.trim().lowercase()
            val existing = repository.getUserByEmail(trimmedEmail)
            if (existing != null) {
                _authState.value = AuthState.Error("Account with this email already exists.")
            } else {
                val newUser = User(
                    name = name.trim(),
                    email = trimmedEmail,
                    passwordHash = hashPassword(pass)
                )
                val id = repository.insertUser(newUser)
                val registeredUser = newUser.copy(id = id)
                _currentUser.value = registeredUser
                _authState.value = AuthState.Authenticated
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun bookParcel(
        senderName: String,
        senderPhone: String,
        senderPincode: String,
        senderCity: String,
        recipientName: String,
        recipientPhone: String,
        recipientPincode: String,
        recipientCity: String,
        weightKg: Double,
        parcelType: String,
        carrier: String
    ) {
        _bookingState.value = BookingState.Loading
        viewModelScope.launch {
            try {
                val randNum = Random.nextInt(100000000, 999999999)
                val trackingNum = "IND$randNum"

                val price = calculatePrice(weightKg, carrier, parcelType)
                val deliveryDays = when (carrier) {
                    "BlueDart" -> 2
                    "Delhivery" -> 3
                    "DTDC" -> 4
                    else -> 5
                }

                val newBooking = ParcelBooking(
                    userId = _currentUser.value?.id ?: 0,
                    trackingNumber = trackingNum,
                    senderName = senderName.trim(),
                    senderPhone = senderPhone.trim(),
                    senderPincode = senderPincode.trim(),
                    senderCity = senderCity.trim(),
                    recipientName = recipientName.trim(),
                    recipientPhone = recipientPhone.trim(),
                    recipientPincode = recipientPincode.trim(),
                    recipientCity = recipientCity.trim(),
                    weightKg = weightKg,
                    parcelType = parcelType,
                    priceRs = price,
                    status = "Booked",
                    estimatedDays = deliveryDays,
                    carrier = carrier
                )

                repository.insertBooking(newBooking)
                _bookingState.value = BookingState.Success(trackingNum)
            } catch (e: Exception) {
                _bookingState.value = BookingState.Error(e.localizedMessage ?: "Failed to book parcel")
            }
        }
    }

    fun clearBookingState() {
        _bookingState.value = BookingState.Idle
    }

    fun calculatePrice(weightKg: Double, carrier: String, parcelType: String): Double {
        val baseFee = 80.0
        val weightCharge = weightKg * 50.0
        val carrierMultiplier = when (carrier) {
            "BlueDart" -> 1.5
            "Delhivery" -> 1.2
            "DTDC" -> 1.1
            else -> 0.8
        }
        val typeAddon = when (parcelType) {
            "Fragile" -> 50.0
            "Electronics" -> 30.0
            else -> 0.0
        }
        return (baseFee + weightCharge) * carrierMultiplier + typeAddon
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
