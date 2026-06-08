package com.yarsi.rescuepet.ui.post

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.ui.theme.RescuePetTheme
import com.yarsi.rescuepet.utils.Result
import kotlinx.coroutines.launch
import java.io.File

class PostAnimalActivity : ComponentActivity() {
    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val postState by viewModel.postState.observeAsState()
    val isLoading by remember { derivedStateOf { postState is Result.Loading } }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedType by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var contactError by remember { mutableStateOf<String?>(null) }
    var latitudeError by remember { mutableStateOf<String?>(null) }
    var longitudeError by remember { mutableStateOf<String?>(null) }

    val typeOptions = listOf("Kucing", "Anjing", "Kelinci", "Hamster", "Burung", "Lainnya")
    val categoryOptions = listOf("Adopsi", "Hilang", "Ditemukan")

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(postState) {
        when (val state = postState) {
            is Result.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Berhasil diposting") }
                kotlinx.coroutines.delay(1200)
                onSuccess()
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
                title = { Text("Posting Hewan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                    value = selectedType,
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
                                selectedType = type
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
                    value = selectedCategory,
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
                                selectedCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Hewan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() } },
                label = { Text("Usia (bulan)") },
                placeholder = { Text("Isi 0 jika tidak tahu") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val descriptionMaxLength = 500
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= descriptionMaxLength) description = it },
                label = { Text("Deskripsi") },
                supportingText = { Text("${description.length}/$descriptionMaxLength") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it; contactError = null },
                label = { Text("Kontak (No. HP / Email)") },
                isError = contactError != null,
                supportingText = contactError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it; latitudeError = null },
                    label = { Text("Latitude") },
                    isError = latitudeError != null,
                    supportingText = latitudeError?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it; longitudeError = null },
                    label = { Text("Longitude") },
                    isError = longitudeError != null,
                    supportingText = longitudeError?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
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
                    contactError = validateContact(contact)
                    latitudeError = validateLatitude(latitude)
                    longitudeError = validateLongitude(longitude)
                    if (contactError != null || latitudeError != null || longitudeError != null) {
                        return@Button
                    }
                    val animal = Animal(
                        type = selectedType.lowercase(),
                        name = name,
                        age = age.toIntOrNull() ?: 0,
                        description = description,
                        status = "available",
                        latitude = latitude.toDoubleOrNull() ?: 0.0,
                        longitude = longitude.toDoubleOrNull() ?: 0.0,
                        posterContact = contact,
                        category = when (selectedCategory) {
                            "Hilang" -> "lost"
                            "Ditemukan" -> "found"
                            else -> "adoption"
                        }
                    )
                    val uri = imageUri
                    val (imageFile, pickFailed) = if (uri != null) {
                        val file = uriToFile(context, uri)
                        file to (file == null)
                    } else {
                        null to false
                    }
                    viewModel.postAnimal(animal, imageFile, pickFailed)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && name.isNotBlank()
                        && selectedType.isNotBlank()
                        && selectedCategory.isNotBlank()
                        && age.isNotBlank()
                        && contact.isNotBlank()
            ) {
                if (isLoading) {
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

private fun validateContact(contact: String): String? {
    if (contact.length < 6) return "Kontak minimal 6 karakter"
    val isPhone = Regex("^[+]?[0-9]{8,15}$").matches(contact)
    val isEmail = Regex("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$").matches(contact)
    if (!isPhone && !isEmail) return "Masukkan nomor HP atau email yang valid"
    return null
}

private fun validateLatitude(lat: String): String? {
    if (lat.isBlank()) return null
    val value = lat.toDoubleOrNull()
    if (value == null) return "Latitude tidak valid"
    if (value < -90.0 || value > 90.0) return "Latitude harus antara -90 dan 90"
    return null
}

private fun validateLongitude(lon: String): String? {
    if (lon.isBlank()) return null
    val value = lon.toDoubleOrNull()
    if (value == null) return "Longitude tidak valid"
    if (value < -180.0 || value > 180.0) return "Longitude harus antara -180 dan 180"
    return null
}

private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, "upload_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output -> inputStream.copyTo(output) }
        file
    } catch (_: Exception) {
        null
    }
}
