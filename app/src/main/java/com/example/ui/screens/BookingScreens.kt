package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceItem
import com.example.data.model.UserProfile
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EncodedOrNetworkImage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueSoft
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.GoldenYellowSoft
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingFormScreen(
    service: ServiceItem,
    category: ServiceCategory?,
    userProfile: UserProfile?,
    isBusy: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSubmitBooking: (
        customerName: String,
        phone: String,
        address: String,
        cityArea: String,
        description: String,
        imageUrls: List<String>,
        preferredDate: String,
        preferredTime: String,
        onBooked: (String) -> Unit
    ) -> Unit,
    onDoneAfterSuccess: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isBangla = strings.isBangla
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler(onBack = onBack)

    var customerName by remember(userProfile) { mutableStateOf(userProfile?.fullName ?: "") }
    var phone by remember(userProfile) { mutableStateOf(userProfile?.phone ?: "") }
    var address by remember(userProfile) { mutableStateOf(userProfile?.address ?: "") }
    var cityArea by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var preferredDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    val timeSlots = remember {
        listOf("09:00 AM - 12:00 PM", "12:00 PM - 03:00 PM", "03:00 PM - 06:00 PM", "06:00 PM - 09:00 PM")
    }
    var preferredTime by remember { mutableStateOf(timeSlots.first()) }
    val attachedImages = remember { mutableStateListOf<String>() }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var submittedBookingCode by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && attachedImages.size < 3) {
                scope.launch {
                    val encoded = ImageUtils.encodeUriToBoundedDataUri(context, uri)
                    if (encoded != null) attachedImages.add(encoded)
                }
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            preferredDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) { Text(strings.confirmBtn) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(strings.cancelBtn) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (submittedBookingCode != null) {
        AlertDialog(
            onDismissRequest = onDoneAfterSuccess,
            icon = {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(40.dp))
            },
            title = { Text(strings.bookingSubmittedTitle) },
            text = {
                Column {
                    Text(strings.bookingSubmittedMsg)
                    Spacer(Modifier.height(10.dp))
                    Surface(color = GoldenYellowSoft, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            text = "ID: $submittedBookingCode",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepNavy,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDoneAfterSuccess, colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)) {
                    Text(strings.myBookingsTitle)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.bookServiceTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Selected Category & Service summary
            Card(
                colors = CardDefaults.cardColors(containerColor = ElectricBlueSoft),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (category != null) {
                        Text(
                            text = "${strings.serviceCategoryField}: ${category.localizedName(isBangla)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricBlue
                        )
                    }
                    Text(
                        text = "${strings.selectedServiceField}: ${service.localizedName(isBangla)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepNavy
                    )
                }
            }

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it; validationError = null },
                label = { Text(strings.customerNameField) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("booking_name_input")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; validationError = null },
                label = { Text(strings.phoneNumberField) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("booking_phone_input")
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it; validationError = null },
                label = { Text(strings.completeAddressField) },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("booking_address_input")
            )

            OutlinedTextField(
                value = cityArea,
                onValueChange = { cityArea = it; validationError = null },
                label = { Text(strings.cityAreaField) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("booking_area_input")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; validationError = null },
                label = { Text(strings.problemDescField) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().testTag("booking_desc_input")
            )

            // Preferred Date
            OutlinedTextField(
                value = preferredDate,
                onValueChange = {},
                readOnly = true,
                label = { Text(strings.preferredDateField) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = strings.preferredDateField)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            // Preferred Time Slots
            Text(
                text = strings.preferredTimeField,
                style = MaterialTheme.typography.labelLarge,
                color = DeepNavy
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timeSlots.forEach { slot ->
                    FilterChip(
                        selected = preferredTime == slot,
                        onClick = { preferredTime = slot },
                        label = { Text(slot, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Optional Problem Images
            Text(
                text = strings.problemImagesLabel,
                style = MaterialTheme.typography.labelLarge,
                color = DeepNavy
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(attachedImages) { img ->
                    Box(modifier = Modifier.size(84.dp)) {
                        EncodedOrNetworkImage(
                            imageSource = img,
                            contentDescription = "Problem photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        IconButton(
                            onClick = { attachedImages.remove(img) },
                            modifier = Modifier.align(Alignment.TopEnd).size(26.dp)
                        ) {
                            Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(50)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.padding(2.dp))
                            }
                        }
                    }
                }
                if (attachedImages.size < 3) {
                    item {
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(84.dp)
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(strings.addPhotoBtn)
                        }
                    }
                }
            }

            val err = validationError ?: errorMessage
            if (err != null) {
                Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(6.dp))
            Button(
                onClick = {
                    if (customerName.trim().isEmpty() ||
                        phone.trim().length < 5 ||
                        address.trim().length < 3 ||
                        cityArea.trim().length < 2 ||
                        description.trim().length < 3
                    ) {
                        validationError = strings.validationBookingFields
                    } else {
                        onSubmitBooking(
                            customerName.trim(),
                            phone.trim(),
                            address.trim(),
                            cityArea.trim(),
                            description.trim(),
                            attachedImages.toList(),
                            preferredDate,
                            preferredTime
                        ) { code ->
                            submittedBookingCode = code
                        }
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_booking_button")
            ) {
                if (isBusy) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(strings.submitBookingBtn, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun MyBookingsScreen(
    bookings: List<Booking>,
    isBusy: Boolean,
    onCancelBooking: (String) -> Unit,
    onSubmitReview: (booking: Booking, rating: Long, comment: String, onDone: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isBangla = strings.isBangla
    var reviewTarget by remember { mutableStateOf<Booking?>(null) }
    var rating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    if (reviewTarget != null) {
        val target = reviewTarget!!
        AlertDialog(
            onDismissRequest = { reviewTarget = null },
            title = { Text(strings.rateServiceTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = target.localizedServiceName(isBangla),
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepNavy
                    )
                    Text(text = strings.ratingPrompt, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (star in 1..5) {
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) GoldenYellow else Color(0xFFD1D5DB),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        placeholder = { Text(strings.reviewCommentHint) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("review_comment_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewComment.trim().length >= 2) {
                            onSubmitReview(target, rating.toLong(), reviewComment.trim()) {
                                reviewTarget = null
                                reviewComment = ""
                                rating = 5
                            }
                        }
                    },
                    enabled = !isBusy && reviewComment.trim().length >= 2,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text(strings.submitReviewBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewTarget = null }) { Text(strings.cancelBtn) }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(color = DeepNavy, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = strings.myBookingsTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(18.dp)
            )
        }

        if (bookings.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    icon = Icons.Rounded.CalendarMonth,
                    message = strings.noBookingsYet
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings, key = { it.id }) { booking ->
                    val status = booking.statusEnum()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = booking.bookingCode,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                BookingStatusBadge(status = status)
                            }
                            Text(
                                text = booking.localizedServiceName(isBangla),
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepNavy
                            )
                            Text(
                                text = "${booking.preferredDate} • ${booking.preferredTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${booking.address}, ${booking.cityArea}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = booking.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeepNavy
                            )

                            if (booking.imageUrls.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(booking.imageUrls) { img ->
                                        EncodedOrNetworkImage(
                                            imageSource = img,
                                            contentDescription = "Booking image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                            }

                            if (booking.adminNotes.isNotBlank()) {
                                Surface(color = ElectricBlueSoft, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${strings.adminNotesLabel}: ${booking.adminNotes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepNavy,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (status in listOf(BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.SCHEDULED)) {
                                    OutlinedButton(
                                        onClick = { onCancelBooking(booking.id) },
                                        enabled = !isBusy,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(strings.cancelBookingBtn)
                                    }
                                }
                                if (status == BookingStatus.COMPLETED && !booking.hasReview) {
                                    Button(
                                        onClick = { reviewTarget = booking },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow, contentColor = DeepNavy),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(strings.writeReviewBtn, fontWeight = FontWeight.Bold)
                                    }
                                } else if (booking.hasReview) {
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            text = strings.reviewedBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF16A34A),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
