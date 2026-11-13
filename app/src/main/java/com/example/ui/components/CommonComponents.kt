package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HomeRepairService
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Plumbing
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WindPower
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BookingStatus
import com.example.data.model.BusinessAvailability
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueSoft
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.GoldenYellowSoft
import com.example.ui.theme.StatusBusyAmber
import com.example.ui.theme.StatusBusyBg
import com.example.ui.theme.StatusOfflineBg
import com.example.ui.theme.StatusOfflineRed
import com.example.ui.theme.StatusOnlineBg
import com.example.ui.theme.StatusOnlineGreen
import com.example.util.ImageUtils

fun categoryIconForKey(iconKey: String): ImageVector = when (iconKey.lowercase()) {
    "fan" -> Icons.Rounded.WindPower
    "lighting" -> Icons.Rounded.Lightbulb
    "ac" -> Icons.Rounded.AcUnit
    "wiring" -> Icons.Rounded.ElectricalServices
    "cctv" -> Icons.Rounded.Videocam
    "plumbing" -> Icons.Rounded.Plumbing
    "charger" -> Icons.Rounded.BatteryChargingFull
    "audio" -> Icons.Rounded.Headphones
    "ips" -> Icons.Rounded.Power
    "pump" -> Icons.Rounded.WaterDrop
    "generator" -> Icons.Rounded.Bolt
    "home" -> Icons.Rounded.HomeRepairService
    "electronics" -> Icons.Rounded.Cable
    else -> Icons.Rounded.Build
}

@Composable
fun BusinessStatusPill(
    status: BusinessAvailability,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val (bg, fg, dot) = when (status) {
        BusinessAvailability.ONLINE -> Triple(StatusOnlineBg, StatusOnlineGreen, StatusOnlineGreen)
        BusinessAvailability.BUSY -> Triple(StatusBusyBg, StatusBusyAmber, StatusBusyAmber)
        BusinessAvailability.OFFLINE -> Triple(StatusOfflineBg, StatusOfflineRed, StatusOfflineRed)
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dot)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = strings.businessStatusLabel(status),
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BookingStatusBadge(status: BookingStatus, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val (bg, fg) = when (status) {
        BookingStatus.PENDING -> GoldenYellowSoft to Color(0xFF8A5A00)
        BookingStatus.ACCEPTED -> ElectricBlueSoft to ElectricBlue
        BookingStatus.SCHEDULED -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        BookingStatus.IN_PROGRESS -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
        BookingStatus.COMPLETED -> StatusOnlineBg to StatusOnlineGreen
        BookingStatus.REJECTED, BookingStatus.CANCELLED -> StatusOfflineBg to StatusOfflineRed
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = strings.bookingStatusText(status),
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BrandLogoMark(modifier: Modifier = Modifier, sizeDp: Int = 44) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = modifier
            .size(sizeDp.dp)
            .border(1.dp, GoldenYellow.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_brand_logo),
            contentDescription = "Yusuf Electronics Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun EncodedOrNetworkImage(
    imageSource: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (imageSource.startsWith("data:image")) {
        val bitmap = remember(imageSource) { ImageUtils.decodeDataUriToBitmap(imageSource) }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
    } else if (imageSource.isNotBlank()) {
        AsyncImage(
            model = imageSource,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

@Composable
fun StarRatingRow(rating: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = if (i <= rating) GoldenYellow else Color(0xFFD1D5DB),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Surface(
                color = ElectricBlueSoft,
                shape = CircleShape,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DeepNavy,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorRetryCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        colors = CardDefaults.cardColors(containerColor = StatusOfflineBg),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = StatusOfflineRed
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = StatusOfflineRed,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Button(onClick = onRetry) {
                Text(strings.retryBtn)
            }
        }
    }
}
