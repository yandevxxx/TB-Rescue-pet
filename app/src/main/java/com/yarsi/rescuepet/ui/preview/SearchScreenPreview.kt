package com.yarsi.rescuepet.ui.preview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.ui.search.AnimalWithDistance
import com.yarsi.rescuepet.ui.search.NearbyAnimalCard
import com.yarsi.rescuepet.ui.search.SearchScreen
import com.yarsi.rescuepet.ui.search.SearchViewModel
import com.yarsi.rescuepet.ui.theme.RescuePetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    AppwriteClient.initialize(LocalContext.current)
    RescuePetTheme {
        SearchScreen(
            viewModel = SearchViewModel(),
            onBack = {},
            onAnimalClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NearbyAnimalCardPreview() {
    AppwriteClient.initialize(LocalContext.current)
    RescuePetTheme {
        NearbyAnimalCard(
            item = AnimalWithDistance(
                animal = Animal(
                    id = "1",
                    type = "Anjing",
                    name = "Max",
                    age = 24,
                    description = "Anjing Setia.",
                    status = "available",
                    category = "lost",
                    posterName = "Siti",
                    posterContact = "081234567890",
                    latitude = -6.2088,
                    longitude = 106.8456
                ),
                distanceKm = 1.5
            ),
            storageRepo = StorageRepository(),
            onClick = {}
        )
    }
}
