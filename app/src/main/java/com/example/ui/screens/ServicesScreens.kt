package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BusinessSettings
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.categoryIconForKey
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueSoft
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.GoldenYellowSoft
import com.example.ui.theme.StatusOnlineBg
import com.example.ui.theme.StatusOnlineGreen

@Composable
fun ServicesCatalogScreen(
    categories: List<ServiceCategory>,
    services: List<ServiceItem>,
    initialCategoryId: String?,
    onOpenServiceDetail: (ServiceItem) -> Unit,
    onBookService: (ServiceItem) -> Unit,
    onContactUs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isBangla = strings.isBangla
    var selectedCategoryId by remember(initialCategoryId) { mutableStateOf(initialCategoryId) }
    var searchQuery by remember { mutableStateOf("") }

    val activeCategories = categories.filter { it.status == "active" }
    val activeCategoryMap = activeCategories.associateBy { it.id }

    val filteredServices = services.filter { svc ->
        val matchesCategory = if (selectedCategoryId == null) {
            activeCategories.isEmpty() || svc.categoryId in activeCategoryMap
        } else {
            svc.categoryId == selectedCategoryId
        }
        val matchesQuery = searchQuery.isBlank() || run {
            val q = searchQuery.trim().lowercase()
            val cat = activeCategoryMap[svc.categoryId]
            svc.nameEn.lowercase().contains(q) ||
                svc.nameBn.lowercase().contains(q) ||
                svc.descriptionEn.lowercase().contains(q) ||
                svc.descriptionBn.lowercase().contains(q) ||
                (cat?.nameEn?.lowercase()?.contains(q) == true) ||
                (cat?.nameBn?.lowercase()?.contains(q) == true)
        }
        matchesCategory && matchesQuery
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(color = DeepNavy, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.categoriesTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(strings.searchPlaceholder) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ElectricBlue) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GoldenYellow,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("services_search_input")
                )
            }
        }

        if (activeCategories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text(strings.allCategoriesChip) }
                    )
                }
                items(activeCategories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
                        label = { Text(cat.localizedName(isBangla)) },
                        leadingIcon = {
                            Icon(
                                imageVector = categoryIconForKey(cat.iconKey),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        if (filteredServices.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    icon = Icons.Rounded.ElectricalServices,
                    message = if (services.isEmpty()) strings.catalogEmptyMessage else strings.noSearchResults
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredServices, key = { it.id }) { svc ->
                    val cat = activeCategoryMap[svc.categoryId]
                    ServiceCatalogCard(
                        service = svc,
                        category = cat,
                        isBangla = isBangla,
                        onOpenDetail = { onOpenServiceDetail(svc) },
                        onBook = { onBookService(svc) },
                        onContact = onContactUs
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceCatalogCard(
    service: ServiceItem,
    category: ServiceCategory?,
    isBangla: Boolean,
    onOpenDetail: () -> Unit,
    onBook: () -> Unit,
    onContact: () -> Unit
) {
    val strings = LocalAppStrings.current
    val available = service.availability == "available"
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ElectricBlueSoft,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIconForKey(category?.iconKey ?: "electrical"),
                            contentDescription = null,
                            tint = DeepNavy
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (category != null) {
                        Text(
                            text = category.localizedName(isBangla),
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricBlue
                        )
                    }
                    Text(
                        text = service.localizedName(isBangla),
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepNavy
                    )
                }
                Surface(
                    color = if (available) StatusOnlineBg else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (available) strings.availableBadge else strings.unavailableBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (available) StatusOnlineGreen else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = service.localizedDescription(isBangla),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (service.localizedPrice(isBangla).isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = GoldenYellowSoft,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${strings.estimatedPriceTitle}: ${service.localizedPrice(isBangla)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepNavy,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onBook,
                    enabled = available,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.bookServiceBtn)
                }
                OutlinedButton(
                    onClick = onContact,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.contactBtn)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    service: ServiceItem,
    category: ServiceCategory?,
    businessSettings: BusinessSettings,
    onBack: () -> Unit,
    onBookThisService: () -> Unit,
    onSendMessage: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isBangla = strings.isBangla
    val context = LocalContext.current
    val available = service.availability == "available"

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.serviceDetailsTitle) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DeepNavy, ElectricBlue)))
                    .padding(22.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GoldenYellow,
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = categoryIconForKey(category?.iconKey ?: "electrical"),
                                    contentDescription = null,
                                    tint = DeepNavy,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            if (category != null) {
                                Text(
                                    text = category.localizedName(isBangla),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GoldenYellow
                                )
                            }
                            Text(
                                text = service.localizedName(isBangla),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        color = if (available) StatusOnlineBg else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (available) strings.availableBadge else strings.unavailableBadge,
                            color = if (available) StatusOnlineGreen else Color(0xFFDC2626),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = service.localizedDescription(isBangla),
                            style = MaterialTheme.typography.bodyLarge,
                            color = DeepNavy
                        )
                        if (service.localizedPrice(isBangla).isNotBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Surface(
                                color = GoldenYellowSoft,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = strings.estimatedPriceTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = service.localizedPrice(isBangla),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepNavy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                val features = service.localizedFeatures(isBangla)
                if (features.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = strings.featuresTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepNavy
                            )
                            features.forEach { feat ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(text = feat, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onBookThisService,
                    enabled = available,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("detail_book_service_button")
                ) {
                    Text(strings.bookServiceBtn, style = MaterialTheme.typography.titleMedium)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onSendMessage,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_send_message_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.sendMessageBtn)
                    }

                    val phone = businessSettings.phoneNumber.trim()
                    OutlinedButton(
                        onClick = {
                            if (phone.isNotBlank()) {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                            }
                        },
                        enabled = phone.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_call_now_button")
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.callNowBtn)
                    }
                }
            }
        }
    }
}
