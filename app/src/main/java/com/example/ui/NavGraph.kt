package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.components.StudioBottomNavBar
import com.example.ui.components.StudioTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.theme.DeepNavyBackground
import com.example.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { err ->
            snackbarHostState.showSnackbar(message = err)
            viewModel.dismissError()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavyBackground),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!uiState.isFullscreenPreview) {
                StudioTopBar(
                    selectedModel = uiState.selectedModel,
                    apiKey = uiState.apiKey,
                    isApiKeyDialogOpen = uiState.isApiKeyDialogOpen,
                    onModelSelected = viewModel::updateSelectedModel,
                    onOpenApiKeyDialog = viewModel::openApiKeyDialog,
                    onSaveApiKey = viewModel::updateApiKey,
                    onFileImported = viewModel::importCodeFromFile
                )
            }
        },
        bottomBar = {
            if (!uiState.isFullscreenPreview) {
                StudioBottomNavBar(
                    selectedTab = uiState.currentTab,
                    onTabSelected = viewModel::setCurrentTab
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepNavyBackground)
        ) {
            AnimatedContent(
                targetState = uiState.currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> ChatScreen(
                        messages = uiState.messages,
                        isLoading = uiState.isLoading,
                        onSendMessage = viewModel::sendMessage,
                        onResetChat = viewModel::resetChat,
                        onLoadCodeToEditor = { code ->
                            viewModel.updateCodeState(code)
                            viewModel.setCurrentTab(1) // Move to Editor
                        }
                    )
                    1 -> EditorScreen(
                        codeState = uiState.codeState,
                        onCodeChange = viewModel::updateCodeState,
                        onRunCode = viewModel::runCodeFromEditor
                    )
                    2 -> PreviewScreen(
                        codeState = uiState.codeState,
                        isFullscreen = uiState.isFullscreenPreview,
                        onToggleFullscreen = viewModel::toggleFullscreenPreview
                    )
                }
            }
        }
    }
}
