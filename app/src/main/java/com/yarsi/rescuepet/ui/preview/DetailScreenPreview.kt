package com.yarsi.rescuepet.ui.preview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.ui.detail.DetailContent
import com.yarsi.rescuepet.ui.detail.DetailRow
import com.yarsi.rescuepet.ui.detail.DetailScreen
import com.yarsi.rescuepet.ui.detail.DetailViewModel
import com.yarsi.rescuepet.ui.theme.RescuePetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    AppwriteClient.initialize(LocalContext.current)
    RescuePetTheme {
        DetailScreen(
            viewModel = DetailViewModel(),
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailContentPreview() {
    AppwriteClient.initialize(LocalContext.current)
    RescuePetTheme {
        DetailContent(
            animal = Animal(
                id = "1",
                type = "Kucing",
                name = "Milo",
                age = 12,
                description = "Kucing lucu dan ramah, suka bermain dengan anak-anak.",
                status = "available",
                category = "adoption",
                posterName = "Budi",
                posterContact = "081234567890",
                latitude = -6.2088,
                longitude = 106.8456
            ),
            currentUserId = null,
            isUpdating = false,
            isDeleting = false,
            storageRepo = StorageRepository(),
            context = androidx.compose.ui.platform.LocalContext.current,
            onUpdateStatus = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailRowPreview() {
    RescuePetTheme {
        DetailRow(label = "Jenis", value = "Kucing")
    }
}
