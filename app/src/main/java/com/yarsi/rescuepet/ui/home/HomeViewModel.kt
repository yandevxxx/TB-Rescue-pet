package com.yarsi.rescuepet.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.appwrite.models.RealtimeSubscription
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.data.repository.AnimalRepository
import com.yarsi.rescuepet.data.repository.AuthRepository
import com.yarsi.rescuepet.data.repository.StorageRepository
import com.yarsi.rescuepet.utils.Constants
import com.yarsi.rescuepet.utils.Result

class HomeViewModel : ViewModel() {
    private val repository = AnimalRepository()
    private val storageRepo = StorageRepository()
    private var realtimeSubscription: RealtimeSubscription? = null
    private var currentCategory: String? = null
    private var debounceJob: Job? = null
    private var lastDocId: String? = null
    private var loadGeneration = 0L

    private val _animals = MutableLiveData<List<Animal>>()
    val animals: LiveData<List<Animal>> = _animals

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedFilter = MutableLiveData<String?>(null)
    val selectedFilter: LiveData<String?> = _selectedFilter

    private val _selectedType = MutableLiveData<String?>(null)
    val selectedType: LiveData<String?> = _selectedType

    private val _currentRole = MutableLiveData<String?>(null)
    val currentRole: LiveData<String?> = _currentRole

    private var allAnimals: List<Animal> = emptyList()

    companion object {
        private const val PAGE_SIZE = 20
    }

    fun loadRole() {
        viewModelScope.launch {
            _currentRole.value = AuthRepository().getRole()
        }
    }

    fun loadAnimals(category: String? = null) {
        loadGeneration++
        val gen = loadGeneration
        currentCategory = category
        lastDocId = null
        allAnimals = emptyList()
        _animals.value = emptyList()
        _hasMore.value = false
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.getAnimalsPaged(category, PAGE_SIZE, null)) {
                is Result.Success -> {
                    if (gen != loadGeneration) return@launch
                    val (data, lastId) = result.data
                    allAnimals = data
                    lastDocId = lastId
                    _hasMore.value = lastId != null
                    applyFilters()
                    _error.value = null
                }
                is Result.Error -> {
                    if (gen != loadGeneration) return@launch
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value == true || _hasMore.value != true) return
        val gen = loadGeneration
        _isLoadingMore.value = true
        viewModelScope.launch {
            when (val result = repository.getAnimalsPaged(currentCategory, PAGE_SIZE, lastDocId)) {
                is Result.Success -> {
                    if (gen != loadGeneration) return@launch
                    val (data, lastId) = result.data
                    allAnimals = allAnimals + data
                    lastDocId = lastId
                    _hasMore.value = lastId != null
                    applyFilters()
                }
                is Result.Error -> {
                    if (gen != loadGeneration) return@launch
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoadingMore.value = false
        }
    }

    private fun applyFilters() {
        val filtered = if (_selectedType.value != null) {
            allAnimals.filter { it.type == _selectedType.value }
        } else {
            allAnimals
        }
        _animals.value = filtered
    }

    fun setTypeFilter(type: String?) {
        _selectedType.value = type
        applyFilters()
    }

    fun subscribeRealtime() {
        unsubscribeRealtime()
        val realtime = AppwriteClient.getInstance().getRealtime()
        val channel = "databases.${Constants.DATABASE_ID}.collections.${Constants.COLLECTION_ANIMALS}.documents"
        realtimeSubscription = realtime.subscribe(channel) {
            debounceJob?.cancel()
            debounceJob = viewModelScope.launch {
                delay(500)
                loadAnimals(currentCategory)
            }
        }
    }

    fun unsubscribeRealtime() {
        realtimeSubscription?.close()
        realtimeSubscription = null
    }

    fun setFilter(category: String?) {
        _selectedFilter.value = category
        _selectedType.value = null
        loadAnimals(category)
    }

    fun getImageUrl(imageId: String): String = storageRepo.getImageUrl(imageId)

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            AuthRepository().logout()
            onDone()
        }
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeRealtime()
    }
}
