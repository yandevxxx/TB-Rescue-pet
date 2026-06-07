package com.yarsi.rescuepet.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.yarsi.rescuepet.data.model.Animal
import com.yarsi.rescuepet.data.repository.AnimalRepository
import com.yarsi.rescuepet.utils.Result

class SearchViewModel : ViewModel() {
    private val repository = AnimalRepository()

    val nearbyAnimals = MutableLiveData<List<AnimalWithDistance>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    val userLocation = MutableLiveData<Pair<Double, Double>?>()

    fun searchNearby(userLat: Double, userLon: Double) {
        userLocation.value = Pair(userLat, userLon)
        isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.getAnimals()) {
                is Result.Success -> {
                    val filtered = filterByDistance(result.data, userLat, userLon, 10.0)
                    nearbyAnimals.value = filtered
                    error.value = null
                }
                is Result.Error -> {
                    error.value = result.message
                }
                else -> {}
            }
            isLoading.value = false
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
