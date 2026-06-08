package com.yarsi.rescuepet.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.repository.AnimalRepository
import com.yarsi.rescuepet.utils.Result

class SearchViewModel : ViewModel() {
    private val repository = AnimalRepository()

    private val _nearbyAnimals = MutableLiveData<List<AnimalWithDistance>>()
    val nearbyAnimals: LiveData<List<AnimalWithDistance>> = _nearbyAnimals

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _userLocation = MutableLiveData<Pair<Double, Double>?>()
    val userLocation: LiveData<Pair<Double, Double>?> = _userLocation

    fun onLocationError(message: String) {
        _error.value = message
    }

    fun onLocationUnavailable() {
        _error.value = "Lokasi tidak tersedia, nyalakan GPS"
    }

    fun onLocationFailure() {
        _error.value = "Gagal mendapatkan lokasi"
    }

    fun searchNearby(userLat: Double, userLon: Double) {
        _userLocation.value = Pair(userLat, userLon)
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.searchNearby(userLat, userLon)) {
                is Result.Success -> {
                    val filtered = filterByDistance(result.data, userLat, userLon, 10.0)
                    _nearbyAnimals.value = filtered
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    private fun filterByDistance(
        animals: List<Animal>,
        userLat: Double,
        userLon: Double,
        radiusKm: Double = 10.0
    ): List<AnimalWithDistance> {
        return animals
            .filter { it.latitude != 0.0 && it.longitude != 0.0 }
            .map { animal ->
                val distance = calculateDistance(userLat, userLon, animal.latitude, animal.longitude)
                AnimalWithDistance(animal, distance)
            }
            .filter { it.distanceKm <= radiusKm }
            .sortedBy { it.distanceKm }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}

data class AnimalWithDistance(
    val animal: Animal,
    val distanceKm: Double
)
