package com.example.gencidevtest.presentation.common.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.presentation.common.converter.PriceConverter

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = { onProductClick(product) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            ) {
                // Gambar produk
                AsyncImage(
                    model = product.thumbnail.ifEmpty { "https://via.placeholder.com/200x150" },
                    contentDescription = product.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                // Stock info di pojok kanan atas
                if (product.stock <= 10) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "Only ${product.stock} left",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category & Brand
                Text(
                    text = "${product.category} • ${product.brand}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price & Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price
                    Text(
                        text = "$${PriceConverter.format(product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Rating
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⭐ ${String.format("%.1f", product.rating)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


            }
        }
    }
}