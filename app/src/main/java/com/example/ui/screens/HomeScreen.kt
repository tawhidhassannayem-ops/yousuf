package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Booking
import com.example.data.model.BusinessSettings
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceItem
import com.example.data.model.ServiceReview
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.BrandLogoMark
import com.example.ui.components.BusinessStatusPill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StarRatingRow
import com.example.ui.components.categoryIconForKey
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DeepNavyLight
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueSoft
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.GoldenYellowSoft

@Composable
fun HomeScreen(
    customerName: String,
    businessSettings: BusinessSettings,
    categories: List<ServiceCategory>,
    services: List<ServiceItem>,
    recentBookings: List<Booking>,
    reviews: List<ServiceReview>,
    unreadNotificationCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAllServices: (categoryId: String?) -> Unit,
    onOpenServiceDetail: (ServiceItem) -> Unit,
    onOpenBookingForm: (ServiceItem) -> Unit,
    onOpenContactUs: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenMyBookings: () -> Unit,
    isAdmin: Boolean = false,
    onOpenAdminConsole: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isBangla = strings.isBangla

    val activeCategories = categories.filter { it.status == "active" }
    val activeCategoryIds = activeCategories.map { it.id }.toSet()
    val visibleServices = services.filter { it.categoryId in activeCategoryIds || activeCategories.isEmpty() }

    val popularServices = visibleServices.filter { it.isPopular }.ifEmpty { visibleServices.take(6) }

    val matchingServices = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        val q = searchQuery.trim().lowercase()
        visibleServices.filter { svc ->
            svc.nameEn.lowercase().contains(q) ||
                svc.nameBn.lowercase().contains(q) ||
                svc.descriptionEn.lowercase().contains(q) ||
                svc.descriptionBn.lowercase().contains(q)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 28.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1, 2, 3: Brand Logo + Welcome Customer + Notification Icon + Language Toggle
        item {
            Surface(
                color = DeepNavy,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            BrandLogoMark(sizeDp = 46)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.brandName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldenYellow,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${strings.welcomePrefix} ${customerName.ifBlank { "Customer" }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = false,
                                onClick = onToggleLanguage,
                                label = {
                                    Text(
                                        text = strings.switchLangAction,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                modifier = Modifier.testTag("home_language_toggle")
                            )
                            Spacer(Modifier.width(4.dp))
                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier.testTag("home_notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationCount > 0) {
                                            Badge(containerColor = GoldenYellow, contentColor = DeepNavy) {
                                                Text(unreadNotificationCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Notifications,
                                        contentDescription = strings.notificationsTitle,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Live business availability indicator + Admin Panel switch (for admin users only)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BusinessStatusPill(status = businessSettings.availabilityEnum())
                        Text(
                            text = if (isBangla) businessSettings.workingHoursBn else businessSettings.workingHoursEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAdmin) {
                            Surface(
                                color = GoldenYellow,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clickable(onClick = onOpenAdminConsole)
                                    .testTag("home_admin_console_chip")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = DeepNavy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = strings.adminBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepNavy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // 4. Global Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = strings.searchPlaceholder,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = strings.searchPlaceholder,
                                tint = ElectricBlue
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = GoldenYellow,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }
            }
        }

        // Inline search results when typing on Home
        if (searchQuery.isNotBlank()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = "${strings.allServices} (${matchingServices.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepNavy
                    )
                    Spacer(Modifier.height(10.dp))
                    if (matchingServices.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Rounded.Search,
                            message = strings.noSearchResults
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            matchingServices.take(8).forEach { svc ->
                                ServiceCompactRowCard(
                                    service = svc,
                                    isBangla = isBangla,
                                    onClick = { onOpenServiceDetail(svc) },
                                    onBook = { onOpenBookingForm(svc) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Promotional Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(DeepNavy, ElectricBlue)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Surface(
                                color = GoldenYellow,
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = DeepNavy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "YUSUF ELECTRONICS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepNavy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = strings.promoTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = strings.promoSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onOpenAllServices(null) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldenYellow,
                                        contentColor = DeepNavy
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("home_promo_cta")
                                ) {
                                    Text(strings.promoCta, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onOpenMessages,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.navMessages)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Popular Services Section
        item {
            SectionHeader(
                title = strings.popularServices,
                actionText = strings.seeAll,
                onAction = { onOpenAllServices(null) }
            )
            if (popularServices.isEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyStateCard(
                        icon = Icons.Rounded.ElectricalServices,
                        message = strings.catalogEmptyMessage
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularServices, key = { it.id }) { svc ->
                        val cat = activeCategories.firstOrNull { it.id == svc.categoryId }
                        PopularServiceCard(
                            service = svc,
                            iconKey = cat?.iconKey ?: "electrical",
                            isBangla = isBangla,
                            onClick = { onOpenServiceDetail(svc) },
                            onBook = { onOpenBookingForm(svc) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // 7. All Services Section (Dynamic Categories)
        item {
            SectionHeader(
                title = strings.allServices,
                actionText = strings.seeAll,
                onAction = { onOpenAllServices(null) }
            )
            if (activeCategories.isEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyStateCard(
                        icon = Icons.Rounded.ElectricalServices,
                        message = strings.catalogEmptyMessage
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    activeCategories.chunked(2).forEach { pair ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pair.forEach { cat ->
                                CategoryTileCard(
                                    category = cat,
                                    isBangla = isBangla,
                                    onClick = { onOpenAllServices(cat.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // 8 & 9. Emergency Contact + Contact Yusuf Electronics Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, GoldenYellow.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GoldenYellowSoft,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.SupportAgent, contentDescription = null, tint = DeepNavy)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.emergencyContact,
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepNavy
                            )
                            Text(
                                text = if (businessSettings.phoneNumber.isNotBlank()) {
                                    businessSettings.phoneNumber
                                } else {
                                    strings.notConfiguredYet
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BusinessStatusPill(status = businessSettings.availabilityEnum())
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onOpenContactUs,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("home_contact_us_button")
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.contactYusufBtn)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // 10. Recent Bookings Section
        item {
            SectionHeader(
                title = strings.recentBookings,
                actionText = strings.seeAll,
                onAction = onOpenMyBookings
            )
            if (recentBookings.isEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyStateCard(
                        icon = Icons.Rounded.CalendarMonth,
                        message = strings.noBookingsYet
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    recentBookings.take(3).forEach { booking ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onOpenMyBookings)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = booking.localizedServiceName(isBangla),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DeepNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${booking.bookingCode} • ${booking.preferredDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                BookingStatusBadge(status = booking.statusEnum())
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // 11. Customer Reviews Section
        item {
            SectionHeader(title = strings.customerReviews, actionText = null, onAction = {})
            if (reviews.isEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyStateCard(
                        icon = Icons.Rounded.RateReview,
                        message = strings.noReviewsYet
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reviews.take(10), key = { it.id }) { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.width(260.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                StarRatingRow(rating = rev.rating.toInt())
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = rev.comment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DeepNavy,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = rev.customerName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = DeepNavy
        )
        if (actionText != null) {
            TextButton(onClick = onAction) {
                Text(actionText, color = ElectricBlue)
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PopularServiceCard(
    service: ServiceItem,
    iconKey: String,
    isBangla: Boolean,
    onClick: () -> Unit,
    onBook: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = ElectricBlueSoft,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIconForKey(iconKey),
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    }
                }
                Surface(
                    color = GoldenYellowSoft,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (service.availability == "available") strings.availableBadge else strings.unavailableBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = service.localizedName(isBangla),
                style = MaterialTheme.typography.titleSmall,
                color = DeepNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(42.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = service.localizedPrice(isBangla),
                style = MaterialTheme.typography.bodySmall,
                color = ElectricBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onBook,
                enabled = service.availability == "available",
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.bookServiceBtn, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CategoryTileCard(
    category: ServiceCategory,
    isBangla: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                color = DeepNavy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = categoryIconForKey(category.iconKey),
                        contentDescription = null,
                        tint = GoldenYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = category.localizedName(isBangla),
                style = MaterialTheme.typography.titleSmall,
                color = DeepNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = category.localizedDescription(isBangla),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ServiceCompactRowCard(
    service: ServiceItem,
    isBangla: Boolean,
    onClick: () -> Unit,
    onBook: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.localizedName(isBangla),
                    style = MaterialTheme.typography.titleSmall,
                    color = DeepNavy
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = service.localizedPrice(isBangla),
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricBlue
                )
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onBook,
                enabled = service.availability == "available",
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.bookServiceBtn)
            }
        }
    }
}
