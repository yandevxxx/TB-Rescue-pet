package com.yarsi.rescuepet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.ui.home.HomeViewModel
import com.yarsi.rescuepet.ui.post.PostAnimalActivity
import com.yarsi.rescuepet.ui.detail.AnimalDetailActivity
import com.yarsi.rescuepet.ui.auth.LoginActivity
import com.yarsi.rescuepet.ui.search.SearchActivity
import com.yarsi.rescuepet.ui.theme.RescuePetTheme

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.subscribeRealtime()
        enableEdgeToEdge()
        setContent {
            RescuePetTheme {
                DashboardScreen(
                    viewModel = homeViewModel,
                    onAddAnimal = {
                        startActivity(Intent(this, PostAnimalActivity::class.java))
                    },
                    onSearchNearby = {
                        startActivity(Intent(this, SearchActivity::class.java))
                    },
                    onAnimalClick = { animalId ->
                        Intent(this, AnimalDetailActivity::class.java).apply {
                            putExtra("animal_id", animalId)
                            startActivity(this)
                        }
                    },
                    onLogout = {
                        homeViewModel.logout {
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            })
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HomeViewModel,
    onAddAnimal: () -> Unit,
    onSearchNearby: () -> Unit,
    onAnimalClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val animals by viewModel.animals.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val isLoadingMore by viewModel.isLoadingMore.observeAsState(false)
    val hasMore by viewModel.hasMore.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val selectedFilter by viewModel.selectedFilter.observeAsState()

    val filterOptions = listOf(null to "Semua", "adoption" to "Adopsi", "lost" to "Hilang", "found" to "Ditemukan")
    val selectedType by viewModel.selectedType.observeAsState()
    val typeOptions = remember(animals) {
        if (animals.isEmpty()) emptyList() else listOf(null to "Semua Jenis") + animals.map { it.type }.distinct().sorted().map { it to it }
    }
    val storageRepo = remember { StorageRepository() }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore, hasMore, isLoadingMore) {
        if (shouldLoadMore && hasMore && !isLoadingMore) {
            viewModel.loadMore()
        }
    }

    LaunchedEffect(Unit) {
        if (animals.isEmpty() && !isLoading) {
            viewModel.loadAnimals()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RescuePet") },
                actions = {
                    IconButton(onClick = onSearchNearby) {
                        Icon(Icons.Default.Search, contentDescription = "Cari Terdekat")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAnimal) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Hewan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = selectedFilter == value,
                        onClick = { viewModel.setFilter(value) },
                        label = { Text(label) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                typeOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = selectedType == value,
                        onClick = { viewModel.setTypeFilter(value) },
                        label = { Text(label) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && animals.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (error != null && animals.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = error ?: "Terjadi kesalahan",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadAnimals() }) {
                            Text("Coba Lagi")
                        }
                    }
                } else if (animals.isEmpty()) {
                    Text(
                        text = "Belum ada hewan yang diposting",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(animals, key = { it.id }) { animal ->
                            AnimalCard(
                                animal = animal,
                                storageRepo = storageRepo,
                                onClick = { onAnimalClick(animal.id) }
                            )
                        }
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalCard(animal: Animal, storageRepo: StorageRepository, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (animal.imageId.isNotEmpty()) {
                    storageRepo.getImageUrl(animal.imageId)
                } else {
                    null
                },
                contentDescription = animal.name,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = animal.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(animal.status)
                    CategoryChip(animal.category)
                }
                if (animal.posterName.isNotEmpty()) {
                    Text(
                        text = "oleh: ${animal.posterName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (animal.description.isNotEmpty()) {
                    Text(
                        text = animal.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (animal.posterContact.isNotEmpty()) {
                    Text(
                        text = "Kontak: ${maskContact(animal.posterContact)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val label = when (status) {
        "available" -> "Tersedia"
        "adopted" -> "Teradopsi"
        "found" -> "Sudah Ditemukan"
        "reunited" -> "Sudah Ditemukan"
        else -> status
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun CategoryChip(category: String) {
    val label = when (category) {
        "adoption" -> "Adopsi"
        "lost" -> "Hilang"
        "found" -> "Ditemukan"
        else -> category
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

private fun maskContact(contact: String): String {
    if (contact.length <= 4) return contact
    val first = contact.take(4)
    val last = if (contact.length >= 8) contact.takeLast(4) else ""
    val mid = contact.length - 4 - last.length
    return first + "*".repeat(mid.coerceAtLeast(0)) + last
}
