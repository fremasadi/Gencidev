package com.example.gencidevtest.presentation.common.components.card

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gencidevtest.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    category: Category,
    isSelected: Boolean = false,
    onCategoryClick: (Category) -> Unit,

    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = { onCategoryClick(category) },
        label = {
            Text(
                text = category.name,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = isSelected,
        modifier = modifier.padding(end = 8.dp)
    )
}