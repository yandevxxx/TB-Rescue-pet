package com.yarsi.rescuepet.ui.preview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.yarsi.rescuepet.AnimalCard
import com.yarsi.rescuepet.CategoryChip
import com.yarsi.rescuepet.DashboardScreen
import com.yarsi.rescuepet.StatusChip
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.ui.home.HomeViewModel
import com.yarsi.rescuepet.ui.theme.RescuePetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    AppwriteClient.initialize(LocalContext.current)
    RescuePetTheme {
        DashboardScreen(
            viewModel = HomeViewModel(),
            onAddAnimal = {},
            onSearchNearby = {},
            onAnimalClick = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnimalCardPreview() {
    AppwriteClient.initialize(LocalContext.current)
    RescuePetTheme {
        AnimalCard(
            animal = Animal(
                id = "1",
                type = "Kucing",
                name = "Milo",
                age = 12,
                description = "Kucing lucu dan ramah, suka bermain dengan anak-anak.",
                status = "available",
                category = "adoption",
                posterName = "Budi",
                posterContact = "081234567890"
            ),
            storageRepo = StorageRepository(),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusChipPreview() {
    RescuePetTheme {
        StatusChip(status = "available")
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryChipPreview() {
    RescuePetTheme {
        CategoryChip(category = "adoption")
    }
}
