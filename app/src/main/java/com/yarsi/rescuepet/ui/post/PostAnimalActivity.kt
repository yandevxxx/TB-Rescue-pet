package com.yarsi.rescuepet.ui.post

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.yarsi.rescuepet.ui.theme.RescuePetTheme
import com.yarsi.rescuepet.utils.Result
import com.yarsi.rescuepet.utils.getCurrentLocation
import com.yarsi.rescuepet.utils.uriToFile
import kotlinx.coroutines.launch

class PostAnimalActivity : ComponentActivity() {
    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animalId = intent.getStringExtra("animal_id")
        if (animalId != null) {
            viewModel.loadForEdit(animalId)
        }
        setContent {
            RescuePetTheme {
                PostAnimalScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onSuccess = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostAnimalScreen(
    viewModel: PostViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val formState by viewModel.formState.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var typeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val typeOptions = listOf("Kucing", "Anjing", "Kelinci", "Hamster", "Burung", "Lainnya")
    val categoryOptions = listOf("Adopsi", "Hilang", "Ditemukan")

    val locationPermissionGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setGettingLocation(true)
            getCurrentLocation(context, { lat, lon ->
                viewModel.onLocationResult(lat, lon)
            }, {
                viewModel.setGettingLocation(false)
            })
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val state = formState ?: PostFormState()

    LaunchedEffect(state.submitResult) {
        when (val result = state.submitResult) {
            is Result.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Berhasil diposting") }
                kotlinx.coroutines.delay(1200)
                onSuccess()
            }
            is Result.Error -> {
                scope.launch { snackbarHostState.showSnackbar(result.message) }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val formState = viewModel.formState.value
                    Text(if (formState?.isEditMode == true) "Edit Hewan" else "Posting Hewan")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jenis Hewan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    typeOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.onTypeChanged(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categoryOptions.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                viewModel.onCategoryChanged(cat)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("Nama Hewan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.age,
                onValueChange = { viewModel.onAgeChanged(it) },
                label = { Text("Usia (bulan)") },
                placeholder = { Text("Isi 0 jika tidak tahu") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Deskripsi") },
                supportingText = {
                    val len = state.description.length
                    val color = when {
                        len >= 500 -> MaterialTheme.colorScheme.error
                        len >= 400 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text("$len/500", color = color)
                },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.contact,
                onValueChange = { viewModel.onContactChanged(it) },
                label = { Text("Kontak (No. HP / Email)") },
                isError = state.contactError != null,
                supportingText = state.contactError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.latitude,
                    onValueChange = { viewModel.onLatitudeChanged(it) },
                    label = { Text("Latitude") },
                    isError = state.latitudeError != null,
                    supportingText = state.latitudeError?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.longitude,
                    onValueChange = { viewModel.onLongitudeChanged(it) },
                    label = { Text("Longitude") },
                    isError = state.longitudeError != null,
                    supportingText = state.longitudeError?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.setGettingLocation(true)
                    if (locationPermissionGranted) {
                        getCurrentLocation(context, { lat, lon ->
                            viewModel.onLocationResult(lat, lon)
                        }, {
                            viewModel.setGettingLocation(false)
                        })
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isGettingLocation
            ) {
                if (state.isGettingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Gunakan Lokasi Saya")
            }

            if (state.isGettingLocation || (state.latitude.isNotBlank() && state.longitude.isNotBlank())) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("Lokasi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(6.dp))
                        if (state.isLoadingAddress) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(6.dp))
                                Text("Memuat alamat...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else if (state.address != null) {
                            Text(state.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        } else {
                            Text("Alamat tidak tersedia", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        val lat = state.latitude.toDoubleOrNull()
                        val lng = state.longitude.toDoubleOrNull()
                        val coordText = if (lat != null && lng != null) {
                            "${"%.4f".format(java.util.Locale.US, lat)}, ${"%.4f".format(java.util.Locale.US, lng)}"
                        } else {
                            "${state.latitude}, ${state.longitude}"
                        }
                        Text(
                            text = coordText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                if (lat != null && lng != null) {
                                    val uri = "geo:$lat,$lng?q=$lat,$lng".toUri()
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") }
                ) {
                    Text("Pilih Foto")
                }
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Preview",
                        modifier = Modifier
                            .size(80.dp)
                            .width(80.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val uri = imageUri
                    val imageFile = if (uri != null) uriToFile(context, uri) else null
                    viewModel.submit(imageFile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isSubmitting && state.isFormValid
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Simpan")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PostAnimalScreenPreview() {
    RescuePetTheme {
        var typeExpanded by remember { mutableStateOf(false) }
        var categoryExpanded by remember { mutableStateOf(false) }
        val typeOptions = listOf("Kucing", "Anjing", "Kelinci", "Hamster", "Burung", "Lainnya")
        val categoryOptions = listOf("Adopsi", "Hilang", "Ditemukan")
        val state = PostFormState()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Posting Hewan") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { p ->
            Column(Modifier.fillMaxSize().padding(p).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(typeExpanded, { typeExpanded = it }) {
                    OutlinedTextField(state.type, {}, readOnly = true, label = { Text("Jenis Hewan") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                    ExposedDropdownMenu(typeExpanded, { typeExpanded = false }) { typeOptions.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { typeExpanded = false }) } }
                }
                ExposedDropdownMenuBox(categoryExpanded, { categoryExpanded = it }) {
                    OutlinedTextField(state.category, {}, readOnly = true, label = { Text("Kategori") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                    ExposedDropdownMenu(categoryExpanded, { categoryExpanded = false }) { categoryOptions.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { categoryExpanded = false }) } }
                }
                OutlinedTextField(state.name, { }, label = { Text("Nama Hewan") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.age, { }, label = { Text("Usia (bulan)") }, placeholder = { Text("Isi 0 jika tidak tahu") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.description, { }, label = { Text("Deskripsi") }, supportingText = {
                    val len = state.description.length
                    val color = when {
                        len >= 500 -> MaterialTheme.colorScheme.error
                        len >= 400 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text("$len/500", color = color)
                }, minLines = 3, maxLines = 5, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.contact, { }, label = { Text("Kontak (No. HP / Email)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(state.latitude, { }, label = { Text("Latitude") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(state.longitude, { }, label = { Text("Longitude") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Gunakan Lokasi Saya") }
                OutlinedButton(onClick = { }) { Text("Pilih Foto") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(50.dp), enabled = false) { Text("Simpan") }
            }
        }
    }
}
