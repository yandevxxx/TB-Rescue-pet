package com.yarsi.rescuepet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yarsi.rescuepet.ui.home.DashboardScreen
import com.yarsi.rescuepet.ui.home.HomeViewModel
import com.yarsi.rescuepet.ui.auth.LoginActivity
import com.yarsi.rescuepet.ui.detail.AnimalDetailActivity
import com.yarsi.rescuepet.ui.post.PostAnimalActivity
import com.yarsi.rescuepet.ui.search.SearchActivity
import com.yarsi.rescuepet.ui.theme.RescuePetTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    private val postResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        homeViewModel.loadAnimals()
    }

    private val detailResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        homeViewModel.loadAnimals()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RescuePetTheme {
                DashboardScreen(
                    viewModel = homeViewModel,
                    onAddAnimal = {
                        postResultLauncher.launch(Intent(this, PostAnimalActivity::class.java))
                    },
                    onSearchNearby = {
                        startActivity(Intent(this, SearchActivity::class.java))
                    },
                    onAnimalClick = { animalId ->
                        detailResultLauncher.launch(
                            Intent(this, AnimalDetailActivity::class.java).apply {
                                putExtra("animal_id", animalId)
                            }
                        )
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
