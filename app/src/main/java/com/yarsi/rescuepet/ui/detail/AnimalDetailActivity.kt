package com.yarsi.rescuepet.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.ui.home.CategoryChip
import com.yarsi.rescuepet.ui.home.StatusChip
import com.yarsi.rescuepet.ui.theme.RescuePetTheme
import com.yarsi.rescuepet.utils.Result
import com.yarsi.rescuepet.utils.maskContact
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.net.toUri
import androidx.activity.result.contract.ActivityResultContracts

class AnimalDetailActivity : ComponentActivity() {
    private val viewModel: DetailViewModel by viewModels()
    private var animalId: String? = null

    private val editResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        animalId?.let { viewModel.loadAnimal(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animalId = intent.getStringExtra("animal_id") ?: run {
            finish(); return
        }
        viewModel.loadAnimal(animalId!!)
        setContent {
            RescuePetTheme {
                DetailScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onEdit = { id ->
                        val intent = Intent(this, com.yarsi.rescuepet.ui.post.PostAnimalActivity::class.java).apply {
                            putExtra("animal_id", id)
                        }
                        editResultLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit = {}
) {
    val animal by viewModel.animal.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val updateState by viewModel.updateState.observeAsState()
    val deleteState by viewModel.deleteState.observeAsState()
    val currentUserId by viewModel.currentUserId.observeAsState()
    val currentRole by viewModel.currentRole.observeAsState()
    val address by viewModel.address.observeAsState()
    val isUpdating by remember { derivedStateOf { updateState is Result.Loading } }
    val isDeleting by remember { derivedStateOf { deleteState is Result.Loading } }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Memuat data...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                error != null && animal == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Terjadi kesalahan",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.reloadAnimal() }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                animal != null -> {
                    DetailContent(
                        animal = animal!!,
                        currentUserId = currentUserId,
                        currentRole = currentRole,
                        isUpdating = isUpdating,
                        isDeleting = isDeleting,
                        imageUrl = if (animal!!.imageId.isNotEmpty()) viewModel.getImageUrl(animal!!.imageId) else null,
                        address = address,
                        context = context,
                        onUpdateStatus = { newStatus ->
                            viewModel.updateStatus(animal!!.id, newStatus)
                        },
                        onDelete = {
                            viewModel.deleteAnimal(animal!!.id)
                        },
                        onEdit = {
                            onEdit(animal!!.id)
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("UseKtx")
@Composable
fun DetailContent(
    animal: Animal,
    currentUserId: String?,
    currentRole: String?,
    isUpdating: Boolean,
    isDeleting: Boolean,
    imageUrl: String?,
    address: String?,
    context: android.content.Context,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
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
        val imageModifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp))
        if (!imageUrl.isNullOrEmpty()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = animal.name,
                modifier = imageModifier,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 3.dp)
                    }
                },
                error = {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            )
        } else {
            Box(
                modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }

        Text(
            text = animal.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(animal.status)
            CategoryChip(animal.category)
        }

        HorizontalDivider()
        DetailRow("Jenis", animal.type)
        DetailRow("Usia", if (animal.age > 0) "${animal.age} bulan" else "Belum diketahui")
        if (animal.posterName.isNotEmpty()) {
            DetailRow("Diposting oleh", animal.posterName)
        }
        if (animal.description.isNotEmpty()) {
            HorizontalDivider()
            Text(
                text = "Deskripsi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = animal.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider()
        if (animal.posterContact.isNotEmpty()) {
            val masked = if (isOwner) animal.posterContact else maskContact(animal.posterContact)
            DetailRow("Kontak", masked)
        }
        if (animal.latitude != 0.0 || animal.longitude != 0.0) {
            DetailRow(
                label = "Lokasi",
                value = address ?: "${"%.4f".format(Locale.US, animal.latitude)}, ${"%.4f".format(Locale.US, animal.longitude)}",
                onClick = {
                    val uri = "geo:${animal.latitude},${animal.longitude}?q=${animal.latitude},${animal.longitude}".toUri()
                    context.run { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                }
            )
        }

        if (!isOwner && currentRole == "Pencari" && animal.status == "available" && animal.posterContact.isNotEmpty()) {
            HorizontalDivider()
            Button(
                onClick = {
                    val contact = animal.posterContact
                    val isPhone = Regex("^[+]?[0-9]{8,15}$").matches(contact)
                    val isEmail = Regex("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$").matches(contact)
                    if (isPhone) {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = "tel:${contact}".toUri()
                        }
                        context.startActivity(dialIntent)
                    } else if (isEmail) {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:${contact}".toUri()
                        }
                        context.startActivity(emailIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Hubungi Pemosting")
            }
        }

        if (isOwner && animal.status == "available") {
            HorizontalDivider()
            Text(
                text = "Update Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
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
                        onClick = { onUpdateStatus("found") },
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating
                    ) {
                        Text("Tandai Ditemukan")
                    }
                }
                if (animal.category == "found") {
                    OutlinedButton(
                        onClick = { onUpdateStatus("reunited") },
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating
                    ) {
                        Text("Tandai Bertemu Pemilik")
                    }
                }
            }
        }

        if (isOwner) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Postingan")
            }
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

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Postingan") },
            text = { Text("Apakah Anda yakin ingin menghapus postingan ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetailScreenPreview() {
    val mockAnimal = Animal(
        id = "1",
        type = "Kucing",
        name = "Milo",
        age = 6,
        description = "Kucing jantan lucu, sudah divaksin",
        status = "available",
        category = "adoption",
        latitude = -6.2,
        longitude = 106.8,
        posterContact = "081234567890",
        posterId = "user1",
        posterName = "Budi",
        imageId = ""
    )
    RescuePetTheme {
        DetailContent(
            animal = mockAnimal,
            currentUserId = "user1",
            currentRole = "Donor",
            isUpdating = false,
            isDeleting = false,
            imageUrl = null,
            address = "Jakarta, Indonesia",
            context = androidx.compose.ui.platform.LocalContext.current,
            onUpdateStatus = {},
            onDelete = {},
            onEdit = {}
        )
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
            modifier = Modifier.widthIn(max = 120.dp)
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(12.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
