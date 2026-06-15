package com.yarsi.rescuepet.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val deleteState by viewModel.deleteState.observeAsState()
    val currentUserId by viewModel.currentUserId.observeAsState()
    val isUpdating by remember { derivedStateOf { updateState is Result.Loading } }
    val isDeleting by remember { derivedStateOf { deleteState is Result.Loading } }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val storageRepo = remember { StorageRepository() }
    val context = LocalContext.current

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is Result.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Status berhasil diperbarui") }
            }
            is Result.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> {}
        }
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is Result.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Postingan berhasil dihapus") }
                kotlinx.coroutines.delay(800)
                onBack()
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
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
                        isDeleting = isDeleting,
                        storageRepo = storageRepo,
                        context = context,
                        onUpdateStatus = { newStatus ->
                            viewModel.updateStatus(animal!!.id, newStatus)
                        },
                        onDelete = {
                            viewModel.deleteAnimal(animal!!.id)
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
    isDeleting: Boolean,
    storageRepo: StorageRepository,
    context: android.content.Context,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isOwner = currentUserId != null && currentUserId == animal.posterId
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                .background(Color.LightGray, RoundedCornerShape(12.dp))
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
                    "found" -> "Sudah Ditemukan"
                    "reunited" -> "Sudah Ditemukan"
                    else -> animal.status
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        DetailRow("Jenis", animal.type)
        DetailRow("Usia", if (animal.age > 0) "${animal.age} bulan" else "Belum diketahui")
        if (animal.posterName.isNotEmpty()) {
            DetailRow("Diposting", animal.posterName)
        }
        if (animal.description.isNotEmpty()) {
            Text(
                text = animal.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (animal.posterContact.isNotEmpty()) {
            val masked = if (isOwner) animal.posterContact else maskContact(animal.posterContact)
            DetailRow("Kontak", masked)
        }
        if (animal.latitude != 0.0 || animal.longitude != 0.0) {
            DetailRow(
                label = "Lokasi",
                value = "${"%.4f".format(animal.latitude)}, ${"%.4f".format(animal.longitude)}",
                onClick = {
                    val uri = Uri.parse("geo:${animal.latitude},${animal.longitude}?q=${animal.latitude},${animal.longitude}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            )
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
                if (animal.category == "adoption") {
                    Button(
                        onClick = { onUpdateStatus("adopted") },
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating
                    ) {
                        Text("Tandai Teradopsi")
                    }
                }
                if (animal.category == "lost") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("reunited") },
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating
                    ) {
                        Text("Tandai Ditemukan")
                    }
                }
            }
        }

        if (isOwner) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDeleting,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(if (isDeleting) "Menghapus..." else "Hapus Postingan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Postingan") },
            text = { Text("Apakah Anda yakin ingin menghapus postingan ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

private fun maskContact(contact: String): String {
    if (contact.length <= 4) return contact
    val first = contact.take(4)
    val last = if (contact.length >= 8) contact.takeLast(4) else ""
    val mid = contact.length - 4 - last.length
    return first + "*".repeat(mid.coerceAtLeast(0)) + last
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetailScreenPreview() {
    RescuePetTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail Hewan") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.fillMaxWidth().height(250.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                )
                Text("Milo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Adopsi", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Tersedia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
                DetailLabel("Jenis", "Kucing")
                DetailLabel("Usia", "6 bulan")
                DetailLabel("Diposting", "Budi")
                Text("Kucing jantan lucu, sudah divaksin", style = MaterialTheme.typography.bodyMedium)
                DetailLabel("Kontak", "081234567890")
            }
        }
    }
}

@Composable
private fun DetailLabel(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DetailRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = if (onClick != null) Modifier
            .fillMaxWidth()
            .clickable { onClick() } else Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
