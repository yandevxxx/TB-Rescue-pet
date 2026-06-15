package com.yarsi.rescuepet.ui.preview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.ui.post.PostAnimalScreen
import com.yarsi.rescuepet.ui.post.PostViewModel
import com.yarsi.rescuepet.ui.theme.RescuePetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PostAnimalScreenPreview() {
    AppwriteClient.initialize(LocalContext.current)
    RescuePetTheme {
        PostAnimalScreen(
            viewModel = PostViewModel(),
            onBack = {},
            onSuccess = {}
        )
    }
}
