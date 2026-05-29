package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import com.example.data.local.ParcelDatabase
import com.example.data.model.ParcelBooking
import com.example.data.repository.ParcelRepository
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.BookingState
import com.example.ui.viewmodel.ParcelViewModel
import java.text.SimpleDateFormat
import java.util.*

private val DarkBackground = Color(0xFF0F141C)
private val CardBackground = Color(0xFF1B2330)
private val AccentSaffron = Color(0xFFFF8008)
private val AccentGreen = Color(0xFF00C9FF)
private val IndiaGreen = Color(0xFF4CAF50)
private val SlateTextSecondary = Color(0xFF90A4AE)

@Composable
fun ParcelTrackerTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = AccentSaffron,
        secondary = AccentGreen,
        background = DarkBackground,
        surface = CardBackground,
        onPrimary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )
    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = Typography(),
        content = content
    )
}

class MainActivity : ComponentActivity() {
    private lateinit var database: ParcelDatabase
    private lateinit var repository: ParcelRepository
    private lateinit var viewModel: ParcelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        database = ParcelDatabase.getDatabase(this)
        repository = ParcelRepository(database.parcelDao(), database.userDao())
        viewModel = ParcelViewModel(repository)

        enableEdgeToEdge()
        setContent {
            ParcelTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: ParcelViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("track") }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HeaderSection(currentUser = currentUser, onLogout = { viewModel.logout() })
        TabNavigationBar(activeTab = activeTab, onTabSelected = { activeTab = it })

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                "track" -> TrackTabScreen(viewModel = viewModel)
                "book" -> BookTabScreen(viewModel = viewModel)
                "history" -> HistoryTabScreen(viewModel = viewModel)
                "account" -> AccountTabScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HeaderSection(
    currentUser: com.example.data.model.User?,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = AccentSaffron,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SpeedPost",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = AccentSaffron
                )
                Text(
                    text = " India",
                    fontWeight = FontWeight.Light,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
            Text(
                text = "Fastest Logistics Network",
                fontSize = 11.sp,
                color = SlateTextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        if (currentUser != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2C394F))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentUser.name.split(" ").firstOrNull() ?: "User",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Log out",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onLogout() }
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE65100).copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentSaffron)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Guest Mode",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentSaffron
                )
            }
        }
    }
}

@Composable
fun TabNavigationBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf(
        TabItem("track", "Track", Icons.Default.Search),
        TabItem("book", "Book", Icons.Default.Add),
        TabItem("history", "My Shipments", Icons.Default.Star),
        TabItem("account", "Account", Icons.Default.Person)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(CardBackground.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        tabs.forEach { tab ->
            val isSelected = activeTab == tab.id
            val bg = if (isSelected) AccentSaffron.copy(alpha = 0.15f) else Color.Transparent
            val tint = if (isSelected) AccentSaffron else SlateTextSecondary

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable { onTabSelected(tab.id) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tab.title,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = tint
                )
            }
        }
    }
}

data class TabItem(val id: String, val title: String, val icon: ImageVector)

@Composable
fun TrackTabScreen(viewModel: ParcelViewModel) {
    var searchCode by remember { mutableStateOf("") }
    val searchResult by viewModel.searchResult.collectAsStateWithLifecycle()
    val searchError by viewModel.searchError.collectAsStateWithLifecycle()
    val searchLoading by viewModel.searchLoading.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Instant India Package Radar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter your tracking ID beginning with 'IND' map routes instantly across transport partners.",
                        fontSize = 12.sp,
                        color = SlateTextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchCode,
                        onValueChange = { searchCode = it },
                        placeholder = { Text("e.g. IND839401732", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentSaffron) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentSaffron,
                            unfocusedBorderColor = Color(0xFF37474F),
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.trackParcel(searchCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSaffron),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (searchLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Locate Package Now", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        if (searchError != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE53935).copy(alpha = 0.15f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE53935))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = searchError ?: "", color = Color(0xFFFF8A80), fontSize = 13.sp)
                }
            }
        }

        if (searchResult != null) {
            val parcel = searchResult!!
            item {
                Text(
                    text = "Live Route Tracking Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                TrackingDetailCard(parcel = parcel)
            }
        } else if (searchResult == null && searchError == null && !searchLoading) {
            item {
                Text(
                    text = "Quick Demo Trackers (Tap to Search)",
                    fontSize = 12.sp,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("IND839401732", "IND492048210", "IND104820394").forEach { code ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBackground)
                                .clickable {
                                    searchCode = code
                                    viewModel.trackParcel(code)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = code,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingDetailCard(parcel: ParcelBooking) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ID: ${parcel.trackingNumber}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text(text = "Carrier: ${parcel.carrier}", fontSize = 12.sp, color = SlateTextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentSaffron.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = parcel.parcelType, color = AccentSaffron, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color(0xFF2C394F), thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

            val stages = listOf("Booked", "Dispatched", "In Transit", "Out for Delivery", "Delivered")
            val currentStageIndex = stages.indexOf(parcel.status).coerceAtLeast(0)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                stages.forEachIndexed { index, stage ->
                    val isActive = index <= currentStageIndex
                    val isCurrent = index == currentStageIndex

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) AccentSaffron
                                    else if (isActive) IndiaGreen
                                    else Color(0xFF37474F)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive && !isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = stage,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isActive) Color.White else SlateTextSecondary
                            )
                            if (isCurrent) {
                                Text(
                                    text = "Your package is currently in this status.",
                                    fontSize = 11.sp,
                                    color = AccentGreen
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF2C394F), thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Origin", fontSize = 10.sp, color = SlateTextSecondary)
                    Text(text = "${parcel.senderCity} (${parcel.senderPincode})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = parcel.senderName, color = SlateTextSecondary, fontSize = 11.sp)
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = AccentSaffron,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .align(Alignment.CenterVertically)
                )
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = "Destination", fontSize = 10.sp, color = SlateTextSecondary, textAlign = TextAlign.End)
                    Text(text = "${parcel.recipientCity} (${parcel.recipientPincode})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.End)
                    Text(text = parcel.recipientName, color = SlateTextSecondary, fontSize = 11.sp, textAlign = TextAlign.End)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "WEIGHT", fontSize = 9.sp, color = SlateTextSecondary)
                    Text(text = "${parcel.weightKg} kg", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "EST. DAYS", fontSize = 9.sp, color = SlateTextSecondary)
                    Text(text = "${parcel.estimatedDays} Days", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "COST", fontSize = 9.sp, color = SlateTextSecondary)
                    Text(text = "₹${"%.2f".format(parcel.priceRs)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IndiaGreen)
                }
            }
        }
    }
}

@Composable
fun BookTabScreen(viewModel: ParcelViewModel) {
    var sName by remember { mutableStateOf("") }
    var sPhone by remember { mutableStateOf("") }
    var sPincode by remember { mutableStateOf("") }
    var sCity by remember { mutableStateOf("") }

    var rName by remember { mutableStateOf("") }
    var rPhone by remember { mutableStateOf("") }
    var rPincode by remember { mutableStateOf("") }
    var rCity by remember { mutableStateOf("") }

    var weightInput by remember { mutableStateOf("1.0") }
    var selectedType by remember { mutableStateOf("Documents") }
    var selectedCarrier by remember { mutableStateOf("BlueDart") }

    val bookingState by viewModel.bookingState.collectAsStateWithLifecycle()

    LaunchedEffect(sPincode) {
        val pin = sPincode.trim()
        if (pin.length == 6) {
            val autoCity = when (pin.take(3)) {
                "400" -> "Mumbai"
                "560" -> "Bengaluru"
                "700" -> "Kolkata"
                "110" -> "Delhi"
                "600" -> "Chennai"
                "380" -> "Ahmedabad"
                "411" -> "Pune"
                "500" -> "Hyderabad"
                else -> ""
            }
            if (autoCity.isNotEmpty()) sCity = autoCity
        }
    }

    LaunchedEffect(rPincode) {
        val pin = rPincode.trim()
        if (pin.length == 6) {
            val autoCity = when (pin.take(3)) {
                "400" -> "Mumbai"
                "560" -> "Bengaluru"
                "700" -> "Kolkata"
                "110" -> "Delhi"
                "600" -> "Chennai"
                "380" -> "Ahmedabad"
                "411" -> "Pune"
                "500" -> "Hyderabad"
                else -> ""
            }
            if (autoCity.isNotEmpty()) rCity = autoCity
        }
    }

    val finalPrice = remember(weightInput, selectedCarrier, selectedType) {
        val wVal = weightInput.toDoubleOrNull() ?: 1.0
        viewModel.calculatePrice(wVal, selectedCarrier, selectedType)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Book India Domestic Delivery",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        when (val state = bookingState) {
            is BookingState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentSaffron)
                    }
                }
            }
            is BookingState.Success -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, IndiaGreen)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IndiaGreen, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Booking Successful!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your parcel has been routed through our systems. Tracking Code:",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkBackground)
                                    .padding(horizontal = 24.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = state.trackingNumber,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AccentSaffron,
                                    letterSpacing = 2.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.trackParcel(state.trackingNumber)
                                        viewModel.clearBookingState()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentSaffron)
                                ) {
                                    Text("Track Route", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.clearBookingState() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.Gray)
                                ) {
                                    Text("Book Another")
                                }
                            }
                        }
                    }
                }
            }
            is BookingState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = "Error Occurred", color = Color.Red, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = state.message, color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.clearBookingState() }) {
                                Text("Try again")
                            }
                        }
                    }
                }
            }
            BookingState.Idle -> {
                item {
                    FormGroupCard(title = "Sender Details (Origin)") {
                        OutlinedTextField(
                            value = sName,
                            onValueChange = { sName = it },
                            label = { Text("Sender Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sPhone,
                                onValueChange = { sPhone = it },
                                label = { Text("Contact No") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                            val pinMax = 6
                            OutlinedTextField(
                                value = sPincode,
                                onValueChange = { if (it.length <= pinMax) sPincode = it },
                                label = { Text("PIN (e.g. 400001)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = sCity,
                            onValueChange = { sCity = it },
                            label = { Text("Sender City Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                item {
                    FormGroupCard(title = "Recipient Details (Destination)") {
                        OutlinedTextField(
                            value = rName,
                            onValueChange = { rName = it },
                            label = { Text("Recipient Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = rPhone,
                                onValueChange = { rPhone = it },
                                label = { Text("Contact No") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                            val pinMax = 6
                            OutlinedTextField(
                                value = rPincode,
                                onValueChange = { if (it.length <= pinMax) rPincode = it },
                                label = { Text("PIN (e.g. 560001)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = rCity,
                            onValueChange = { rCity = it },
                            label = { Text("Recipient City Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                item {
                    FormGroupCard(title = "Package Specification") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("Dead Weight (KG)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(text = "Package Contents", fontSize = 11.sp, color = SlateTextSecondary)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Documents", "Electronics", "Fragile").forEach { type ->
                                        val isSel = selectedType == type
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSel) AccentSaffron else DarkBackground)
                                                .clickable { selectedType = type }
                                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = type,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "Preferred Indian Logistics Partner", fontSize = 12.sp, color = SlateTextSecondary)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("BlueDart", "Delhivery", "DTDC", "India Post").forEach { brand ->
                                val isSel = selectedCarrier == brand
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) AccentSaffron else DarkBackground)
                                        .clickable { selectedCarrier = brand }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = brand,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else SlateTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF1B5E20))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "COMPUTED PRICE (INR)", fontSize = 10.sp, color = SlateTextSecondary)
                                Text(
                                    text = "₹${"%.2f".format(finalPrice)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndiaGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "DELIVERY TIME", fontSize = 10.sp, color = SlateTextSecondary)
                                val days = when (selectedCarrier) {
                                    "BlueDart" -> "1-2 Days Express"
                                    "Delhivery" -> "2-3 Days Standard"
                                    "DTDC" -> "3-4 Days Express"
                                    else -> "4-5 Days Economy"
                                }
                                Text(text = days, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AccentGreen)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (sName.isNotEmpty() && sPincode.isNotEmpty() && rName.isNotEmpty() && rPincode.isNotEmpty()) {
                                viewModel.bookParcel(
                                    senderName = sName,
                                    senderPhone = sPhone,
                                    senderPincode = sPincode,
                                    senderCity = sCity,
                                    recipientName = rName,
                                    recipientPhone = rPhone,
                                    recipientPincode = rPincode,
                                    recipientCity = rCity,
                                    weightKg = weightInput.toDoubleOrNull() ?: 1.0,
                                    parcelType = selectedType,
                                    carrier = selectedCarrier
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSaffron),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Initiate Security Booking", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FormGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AccentGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun HistoryTabScreen(viewModel: ParcelViewModel) {
    val bookings by viewModel.userBookings.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Shipment Ledger",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (bookings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "No parcel bookings found", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            itemsIndexed(bookings) { index, item ->
                HistoryRowCard(
                    parcel = item,
                    index = index,
                    onTrack = { viewModel.trackParcel(item.trackingNumber) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HistoryRowCard(parcel: ParcelBooking, index: Int, onTrack: () -> Unit) {
    var animatedAlpha by remember { mutableStateOf(0f) }
    var animatedOffsetY by remember { mutableStateOf(40f) }

    val alpha by animateFloatAsState(
        targetValue = animatedAlpha,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = animatedOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "offsetY"
    )

    LaunchedEffect(key1 = parcel.trackingNumber) {
        delay((index * 50L).coerceAtMost(300L))
        animatedAlpha = 1f
        animatedOffsetY = 0f
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                alpha = alpha,
                translationY = offsetY
            )
            .clickable { onTrack() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "ID: ${parcel.trackingNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${parcel.senderCity} to ${parcel.recipientCity}", fontSize = 12.sp, color = SlateTextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = parcel.carrier, fontSize = 12.sp, color = AccentGreen)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when (parcel.status) {
                    "Delivered" -> IndiaGreen
                    "In Transit" -> AccentGreen
                    "Out for Delivery" -> AccentSaffron
                    else -> Color.White
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = parcel.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "₹${"%.0f".format(parcel.priceRs)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun AccountTabScreen(viewModel: ParcelViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val isSystemAdmin = currentUser?.email == "akashruidas838@gmail.com" || isAdminUnlocked

    var showAdminDashboard by remember { mutableStateOf(false) }
    var activeAuthTab by remember { mutableStateOf(0) } // 0 for Customer, 1 for Admin

    var secretPasscodeVal by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf(false) }

    var isLoginMode by remember { mutableStateOf(true) }
    var nameVal by remember { mutableStateOf("") }
    var emailVal by remember { mutableStateOf("") }
    var passVal by remember { mutableStateOf("") }

    // If Admin mode is unlocked and they chose to see the admin dashboard, open it.
    if (showAdminDashboard && isSystemAdmin) {
        AdminConsoleView(viewModel = viewModel, onExit = { showAdminDashboard = false })
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile state (Logged-in customer / Admin operator)
            if (currentUser != null || isAdminUnlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Header depending on current mode
                if (isAdminUnlocked && currentUser == null) {
                    // Logged in strictly as Admin Operator
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = AccentSaffron,
                        modifier = Modifier
                            .size(80.dp)
                            .background(AccentSaffron.copy(alpha = 0.1f), CircleShape)
                            .padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "System Admin Operator", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Secure Passcode Authentication active", fontSize = 13.sp, color = SlateTextSecondary)
                } else {
                    // Logged in as Customer (who might or might not have system admin privileges)
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = currentUser?.name ?: "Valued Customer", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = currentUser?.email ?: "", fontSize = 14.sp, color = SlateTextSecondary)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Operational Role", color = SlateTextSecondary, fontSize = 13.sp)
                            Text(
                                text = if (isSystemAdmin) "System Administrator" else "Standard Dispatcher",
                                color = if (isSystemAdmin) AccentSaffron else IndiaGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0x11FFFFFF), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Authorization Type", color = SlateTextSecondary, fontSize = 13.sp)
                            Text(
                                text = if (isAdminUnlocked) "Secure Token Only" else "User Credentials",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Launch Developer Console if they have the system admin status
                if (isSystemAdmin) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E251B)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AccentSaffron),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdminDashboard = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AccentSaffron, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Developer Console", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tap to manage system & bookings", color = SlateTextSecondary, fontSize = 11.sp)
                                }
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AccentSaffron)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Log out actions
                Button(
                    onClick = {
                        viewModel.logout()
                        viewModel.forceAdminUnlock(false)
                        showAdminDashboard = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Deauthorize & Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                // Completely Separated Login View using Segmented Tabs
                Spacer(modifier = Modifier.height(8.dp))
                
                // High-End Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Customer Portal", "Admin Access").forEachIndexed { index, title ->
                        val isSelected = activeAuthTab == index
                        val tabColor = if (isSelected) {
                            if (index == 0) AccentGreen else AccentSaffron
                        } else {
                            Color.Transparent
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tabColor)
                                .clickable { 
                                    activeAuthTab = index
                                    passcodeError = false
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.Person else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else SlateTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.White else SlateTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (activeAuthTab == 0) {
                    // CUSTOMER LOGIN / SIGNUP CARD
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = if (isLoginMode) "Customer Account Sign In" else "Create Customer Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isLoginMode) "Securely log into your courier console." else "Register account details to preserve shipping list.",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (!isLoginMode) {
                                OutlinedTextField(
                                    value = nameVal,
                                    onValueChange = { nameVal = it },
                                    label = { Text("Display Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            OutlinedTextField(
                                value = emailVal,
                                onValueChange = { emailVal = it },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = passVal,
                                onValueChange = { passVal = it },
                                label = { Text("Access Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (authState is AuthState.Error) {
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    if (isLoginMode) {
                                        viewModel.login(emailVal, passVal)
                                    } else {
                                        viewModel.signup(nameVal, emailVal, passVal)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (authState is AuthState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(
                                        text = if (isLoginMode) "Identify & Login" else "Create Profile",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isLoginMode) "New customer? Configure Account" else "Already setup? Credentials Login",
                                color = AccentGreen,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isLoginMode = !isLoginMode
                                        viewModel.resetAuthState()
                                    },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // SECURE DEVELOPMENT ADMIN TERMINAL
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, AccentSaffron.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AccentSaffron,
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(AccentSaffron.copy(alpha = 0.1f), CircleShape)
                                    .padding(14.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                text = "System Operator Terminal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Secure gateway restricted only to the system administrator. Enter the master security passcode key below.",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedTextField(
                                value = secretPasscodeVal,
                                onValueChange = { 
                                    secretPasscodeVal = it
                                    passcodeError = false
                                },
                                label = { Text("Passcode Key") },
                                trailingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SlateTextSecondary) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = passcodeError,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentSaffron,
                                    unfocusedBorderColor = Color(0x33FFFFFF)
                                )
                            )
                            
                            if (passcodeError) {
                                Text(
                                    text = "Invalid Passcode. Authorization Denied.",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    textAlign = TextAlign.Start
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val success = viewModel.tryUnlockAdmin(secretPasscodeVal)
                                    if (success) {
                                        secretPasscodeVal = ""
                                        passcodeError = false
                                        showAdminDashboard = true
                                    } else {
                                        passcodeError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentSaffron),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Authorize Admin Console",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsoleView(
    viewModel: ParcelViewModel,
    onExit: () -> Unit
) {
    val bookings by viewModel.adminBookings.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("All") }
    var parcelToEdit by remember { mutableStateOf<ParcelBooking?>(null) }

    // Stats calculations
    val totalBookings = bookings.size
    val totalRevenue = bookings.sumOf { it.priceRs }
    val bookedCount = bookings.count { it.status == "Booked" }
    val transitCount = bookings.count { it.status == "In Transit" || it.status == "Dispatched" }
    val deliveredCount = bookings.count { it.status == "Delivered" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, AccentSaffron),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = AccentSaffron,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Admin Console",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "System Management & Fleet Control",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier.background(Color(0x22FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Admin",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Stats Section
        item {
            Text(
                text = "System Analytics",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AccentSaffron,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Bookings Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Parcels", color = SlateTextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$totalBookings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    // Revenue Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Revenue", color = SlateTextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${totalRevenue.toInt()}", color = IndiaGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Booked", color = SlateTextSecondary, fontSize = 11.sp)
                            Text("$bookedCount", color = AccentSaffron, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Transit", color = SlateTextSecondary, fontSize = 11.sp)
                            Text("$transitCount", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Delivered", color = SlateTextSecondary, fontSize = 11.sp)
                            Text("$deliveredCount", color = IndiaGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Parcel Type Distribution Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentSaffron,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Parcel Type Distribution",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Analytics",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val allTypes = listOf("Documents", "Electronics", "Clothing", "Fragile", "Others")
                    val counts = allTypes.associateWith { type -> 
                        bookings.count { it.parcelType.equals(type, ignoreCase = true) } 
                    }
                    val total = counts.values.sum().coerceAtLeast(1)
                    
                    val colors = listOf(
                        AccentSaffron,
                        AccentGreen,
                        Color(0xFF9575CD),
                        IndiaGreen,
                        Color(0xFF29B6F6)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        allTypes.forEachIndexed { idx, type ->
                            val count = counts[type] ?: 0
                            val pct = count.toFloat() / total
                            val color = colors[idx % colors.size]
                            
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(color, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = type,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "$count (${(pct * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        color = SlateTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = pct,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = color,
                                    trackColor = Color(0xFF1E2638)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by ID, sender or receiver...", color = SlateTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentSaffron,
                    unfocusedBorderColor = CardBackground,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Booked", "In Transit", "Delivered").forEach { status ->
                    val isSelected = selectedFilterStatus == status
                    Card(
                        onClick = { selectedFilterStatus = status },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) AccentSaffron else CardBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = status,
                                color = if (isSelected) Color.White else SlateTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Parcel List Header
        item {
            Text(
                text = "Manage Shipments (" + bookings.size + ")",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AccentSaffron,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        // Filtering Bookings
        val filteredBookings = bookings.filter { booking ->
            val matchesSearch = booking.trackingNumber.contains(searchQuery, ignoreCase = true) ||
                    booking.senderName.contains(searchQuery, ignoreCase = true) ||
                    booking.recipientName.contains(searchQuery, ignoreCase = true) ||
                    booking.recipientCity.contains(searchQuery, ignoreCase = true)

            val matchesFilter = selectedFilterStatus == "All" ||
                    (selectedFilterStatus == "In Transit" && (booking.status == "In Transit" || booking.status == "Dispatched" || booking.status == "Out for Delivery")) ||
                    booking.status.equals(selectedFilterStatus, ignoreCase = true)

            matchesSearch && matchesFilter
        }

        if (filteredBookings.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SlateTextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No parcel bookings match this filter.",
                            color = SlateTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredBookings) { booking ->
                AdminParcelRow(
                    booking = booking,
                    isEditing = parcelToEdit?.id == booking.id,
                    onToggleEdit = {
                        parcelToEdit = if (parcelToEdit?.id == booking.id) null else booking
                    },
                    onUpdateStatus = { newStatus ->
                        viewModel.updateBookingAdmin(booking.copy(status = newStatus, lastUpdateDate = System.currentTimeMillis()))
                        parcelToEdit = null
                    },
                    onUpdateCarrier = { newCarrier ->
                        viewModel.updateBookingAdmin(booking.copy(carrier = newCarrier, lastUpdateDate = System.currentTimeMillis()))
                    },
                    onDelete = {
                        viewModel.deleteBookingAdmin(booking)
                        parcelToEdit = null
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AdminParcelRow(
    booking: ParcelBooking,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateCarrier: (String) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (booking.status) {
        "Booked" -> AccentSaffron
        "Dispatched", "In Transit" -> AccentGreen
        "Out for Delivery" -> Color(0xFF9575CD)
        "Delivered" -> IndiaGreen
        else -> SlateTextSecondary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isEditing) AccentSaffron else Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleEdit() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Line Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = booking.trackingNumber,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${booking.parcelType} • ₹${booking.priceRs.toInt()}",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = booking.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // From -> To
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("FROM", color = SlateTextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(booking.senderName, color = Color.White, fontSize = 12.sp, maxLines = 1)
                    Text(booking.senderCity, color = SlateTextSecondary, fontSize = 11.sp)
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = SlateTextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp).size(16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("TO", color = SlateTextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(booking.recipientName, color = Color.White, fontSize = 12.sp, maxLines = 1)
                    Text(booking.recipientCity, color = SlateTextSecondary, fontSize = 11.sp)
                }
            }

            // Expanded Editor
            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Update Status",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Status selection buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Booked", "Dispatched", "In Transit").forEach { status ->
                            Button(
                                onClick = { onUpdateStatus(status) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (booking.status == status) statusColor else Color(0xFF2B3340)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text(status, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Out for Delivery", "Delivered").forEach { status ->
                            Button(
                                onClick = { onUpdateStatus(status) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (booking.status == status) statusColor else Color(0xFF2B3340)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text(status, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Carrier Fleet",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Delhivery", "BlueDart", "India Post", "DTDC").forEach { carrier ->
                        val isSelected = booking.carrier == carrier
                        Card(
                            onClick = { onUpdateCarrier(carrier) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AccentGreen.copy(alpha = 0.2f) else Color(0xFF2B3340)
                            ),
                            border = BorderStroke(1.dp, if (isSelected) AccentGreen else Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = carrier,
                                    fontSize = 10.sp,
                                    color = if (isSelected) AccentGreen else Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Button
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Purge Booking Record", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
