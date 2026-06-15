package com.yarsi.rescuepet.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.yarsi.rescuepet.data.remote.AppwriteClient
import com.yarsi.rescuepet.ui.auth.AuthViewModel
import com.yarsi.rescuepet.ui.auth.RegisterScreen
import com.yarsi.rescuepet.ui.theme.RescuePetTheme

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    AppwriteClient.initialize(LocalContext.current)
    RescuePetTheme {
        RegisterScreen(
            viewModel = AuthViewModel(),
            onRegisterSuccess = {},
            onNavigateToLogin = {}
        )
    }
}
