package com.example.gencidevtest.presentation.home.screen

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.presentation.cart.viewmodel.CartViewModel
import com.example.gencidevtest.presentation.common.converter.PriceConverter
import com.example.gencidevtest.presentation.home.viewmodel.ProductDetailViewModel

@SuppressLint("DefaultLocale")
@Composable
fun ProductDetailScreen(
    productId: Int,
    onBackClick: () -> Unit,
    productDetailViewModel: ProductDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val uiState by productDetailViewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    // Load product detail when screen opens
    LaunchedEffect(productId) {
        productDetailViewModel.loadProductDetail(productId)
    }


    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Custom Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Product Detail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        productDetailViewModel.loadProductDetail(productId)
                    }) {
                        Text("Try Again")
                    }
                }
            }

            uiState.product != null -> {
                val product = uiState.product!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Product Images
                        ProductImageSection(
                            images = product.images,
                            selectedIndex = selectedImageIndex,
                        )
                    }

                    item {
                        // Product Basic Info
                        ProductBasicInfo(product = product)
                    }

                    item {
                        // Add to Cart Message
                        cartUiState.addToCartMessage?.let { message ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (message.contains("success")) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    }
                                )
                            ) {
                                Text(
                                    text = message,
                                    modifier = Modifier.padding(16.dp),
                                    color = if (message.contains("success")) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            }
                        }
                    }

                    item {
                        // Product Details
                        ProductDetailsCard(product = product)
                    }

                    item {
                        // Product Specifications
                        ProductSpecificationsCard(product = product)
                    }

                    item {
                        // Add to Cart Button
                        Button(
                            onClick = {
                                cartViewModel.addToCart(product.id, 1)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !cartUiState.isAddingToCart && product.stock > 0
                        ) {
                            if (cartUiState.isAddingToCart) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (product.stock > 0) "Add to Cart" else "Out of Stock",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductImageSection(
    images: List<String>,
    selectedIndex: Int,
) {
    Column {
        // Main Image
        AsyncImage(
            model = images.getOrNull(selectedIndex) ?: images.firstOrNull()
            ?: "https://via.placeholder.com/400x300",
            contentDescription = "Product Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        if (images.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))

            // Image Thumbnails
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(images.size) { index ->
                    AsyncImage(
                        model = images[index],
                        contentDescription = "Product Image $index",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (index == selectedIndex) {
                                    Modifier
                                } else {
                                    Modifier
                                }
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ProductBasicInfo(product: Product) {
    Column {
        // Product Title
        Text(
            text = product.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category & Brand
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = product.category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (product.brand.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = product.brand,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Price & Rating Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Original Price (sebelum diskon)
                if (product.discountPercentage > 0) {
                    val originalPrice = product.price / (1 - product.discountPercentage / 100)
                    Text(
                        text = "$${PriceConverter.format(originalPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                }

                // Final Price (setelah diskon)
                Text(
                    text = "$${PriceConverter.format(product.price)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Discount label
                if (product.discountPercentage > 0) {
                    Text(
                        text = "${String.format("%.0f", product.discountPercentage)}% OFF",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", product.rating),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stock Status
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stockColor = when {
                product.stock <= 0 -> MaterialTheme.colorScheme.error
                product.stock <= 10 -> Color(0xFFFF6B35)
                else -> Color(0xFF4CAF50)
            }

            val stockText = when {
                product.stock <= 0 -> "Out of Stock"
                product.stock <= 10 -> "Only ${product.stock} left"
                else -> "In Stock (${product.stock} available)"
            }

            Surface(
                color = stockColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stockText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = stockColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ProductDetailsCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun ProductSpecificationsCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Specifications List
            SpecificationItem("SKU", product.sku)
            SpecificationItem("Brand", product.brand.ifEmpty { "N/A" })
            SpecificationItem("Category", product.category)
            SpecificationItem("Stock", "${product.stock} units")
            SpecificationItem("Weight", "${product.weight} lbs")

            if (product.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(product.tags) { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecificationItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}