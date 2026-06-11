package com.yarsi.rescuepet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.ui.auth.LoginActivity
import com.yarsi.rescuepet.ui.detail.AnimalDetailActivity
import com.yarsi.rescuepet.ui.home.HomeViewModel
import com.yarsi.rescuepet.ui.post.PostAnimalActivity
import com.yarsi.rescuepet.ui.search.SearchActivity
import com.yarsi.rescuepet.ui.theme.RescuePetTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.subscribeRealtime()
            }
        }
    }

    override fun onStop() {
        homeViewModel.unsubscribeRealtime()
        super.onStop()
    }
}

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
    val selectedType by viewModel.selectedType.observeAsState()

    DashboardContent(
        animals = animals,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        error = error,
        selectedFilter = selectedFilter,
        selectedType = selectedType,
        getImageUrl = { imageId -> 
            try { StorageRepository().getImageUrl(imageId) } catch(e: Exception) { "" }
        },
        onFilterClick = { viewModel.setFilter(it) },
        onTypeFilterClick = { viewModel.setTypeFilter(it) },
        onLoadMore = { viewModel.loadMore() },
        onAddAnimal = onAddAnimal,
        onSearchNearby = onSearchNearby,
        onAnimalClick = onAnimalClick,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    animals: List<Animal>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    error: String?,
    selectedFilter: String?,
    selectedType: String?,
    getImageUrl: (String) -> String,
    onFilterClick: (String?) -> Unit,
    onTypeFilterClick: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onAddAnimal: () -> Unit,
    onSearchNearby: () -> Unit,
    onAnimalClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val filterOptions = listOf(null to "Semua", "adoption" to "Adopsi", "lost" to "Hilang", "found" to "Ditemukan")
    val typeOptions = remember(animals) {
        if (animals.isEmpty()) emptyList() else listOf(null to "Semua") + animals.map { it.type }.distinct().sorted().map { it to it }
    }
    
    val listState = rememberLazyListState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    
    LaunchedEffect(shouldLoadMore, hasMore, isLoadingMore) {
        if (shouldLoadMore && hasMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "PawBuddy", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onSearchNearby) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAnimal,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                        label = { Text("Explore") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        label = { Text("Alerts") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profile") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Categories Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = selectedFilter == value,
                        onClick = { onFilterClick(value) },
                        label = { Text(label) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            // Pet Types Filter
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
                        onClick = { onTypeFilterClick(value) },
                        label = { Text(label) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && animals.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (error != null && animals.isEmpty()) {
                    Text(text = error, modifier = Modifier.align(Alignment.Center))
                } else if (animals.isEmpty()) {
                    Text(
                        text = "Belum ada hewan yang diposting",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(animals) { animal ->
                            AnimalCard(
                                animal = animal,
                                getImageUrl = getImageUrl,
                                onClick = { onAnimalClick(animal.id) }
                            )
                        }
                        if (isLoadingMore) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
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
fun AnimalCard(animal: Animal, getImageUrl: (String) -> String, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (animal.imageId.isNotEmpty()) getImageUrl(animal.imageId) else null,
                contentDescription = animal.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = animal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = animal.type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusTag(animal.status)
                    CategoryTag(animal.category)
                }
                if (animal.posterContact.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = maskContact(animal.posterContact),
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val label = when (status) {
        "available" -> "Tersedia"
        "adopted" -> "Teradopsi"
        "reunited" -> "Ditemukan"
        else -> status
    }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun CategoryTag(category: String) {
    val label = when (category) {
        "adoption" -> "Adopsi"
        "lost" -> "Hilang"
        "found" -> "Ditemukan"
        else -> category
    }
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
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

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    RescuePetTheme {
        DashboardContent(
            animals = listOf(
                Animal(id = "1", name = "Luna", type = "Kucing", category = "adoption", status = "available", posterContact = "08123456789"),
                Animal(id = "2", name = "Rocky", type = "Anjing", category = "lost", status = "available", posterContact = "085711223344")
            ),
            isLoading = false,
            isLoadingMore = false,
            hasMore = false,
            error = null,
            selectedFilter = null,
            selectedType = null,
            getImageUrl = { "" },
            onFilterClick = {},
            onTypeFilterClick = {},
            onLoadMore = {},
            onAddAnimal = {},
            onSearchNearby = {},
            onAnimalClick = {},
            onLogout = {}
        )
    }
}
