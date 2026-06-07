package com.yarsi.rescuepet.ui.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.ui.theme.RescuePetTheme
import com.yarsi.rescuepet.utils.Result
import kotlinx.coroutines.launch

class AnimalDetailActivity : ComponentActivity() {
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animalId = intent.getStringExtra("animal_id") ?: run {
            finish(); return
        }
        viewModel.loadAnimal(animalId)
        setContent {
            RescuePetTheme {
                DetailScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val animal by viewModel.animal.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val updateState by viewModel.updateState.observeAsState()
    val currentUserId by viewModel.currentUserId.observeAsState()
    val isUpdating by remember { derivedStateOf { updateState is Result.Loading } }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val storageRepo = remember { StorageRepository() }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is Result.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Status berhasil diperbarui") }
                viewModel.animal.value?.let { a ->
                    viewModel.loadAnimal(a.id)
                }
            }
            is Result.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Hewan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading && animal == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null && animal == null -> {
                    Text(
                        text = error ?: "Terjadi kesalahan",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                animal != null -> {
                    DetailContent(
                        animal = animal!!,
                        currentUserId = currentUserId,
                        isUpdating = isUpdating,
                        storageRepo = storageRepo,
                        onUpdateStatus = { newStatus ->
                            viewModel.updateStatus(animal!!.id, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    animal: Animal,
    currentUserId: String?,
    isUpdating: Boolean,
    storageRepo: StorageRepository,
    onUpdateStatus: (String) -> Unit
) {
    val isOwner = currentUserId != null && currentUserId == animal.posterId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = if (animal.imageId.isNotEmpty()) {
                storageRepo.getImageUrl(animal.imageId)
            } else {
                null
            },
            contentDescription = animal.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = animal.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = when (animal.category) {
                    "adoption" -> "Adopsi"
                    "lost" -> "Hilang"
                    "found" -> "Ditemukan"
                    else -> animal.category
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = when (animal.status) {
                    "available" -> "Tersedia"
                    "adopted" -> "Teradopsi"
                    "found" -> "Ditemukan"
                    else -> animal.status
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        DetailRow("Jenis", animal.type)
        if (animal.age > 0) DetailRow("Usia", "${animal.age} bulan")
        if (animal.description.isNotEmpty()) {
            Text(
                text = animal.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (animal.posterContact.isNotEmpty()) {
            DetailRow("Kontak", animal.posterContact)
        }
        if (animal.latitude != 0.0 || animal.longitude != 0.0) {
            DetailRow("Lokasi", "${"%.4f".format(animal.latitude)}, ${"%.4f".format(animal.longitude)}")
        }

        if (isOwner && animal.status == "available") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Update Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onUpdateStatus("adopted") },
                    modifier = Modifier.weight(1f),
                    enabled = !isUpdating
                ) {
                    Text("Teradopsi")
                }
                OutlinedButton(
                    onClick = { onUpdateStatus("found") },
                    modifier = Modifier.weight(1f),
                    enabled = !isUpdating
                ) {
                    Text("Ditemukan")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
