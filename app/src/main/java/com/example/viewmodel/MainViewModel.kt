package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApiService
import com.example.data.DataStoreManager
import com.example.model.ChatCompletionRequest
import com.example.model.ChatMessage
import com.example.model.ChatMessageDto
import com.example.model.Sender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val apiKey: String = "",
    val selectedModel: String = "deepseek-v4-flash",
    val codeState: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val currentTab: Int = 0, // 0 = Chat, 1 = Editor, 2 = Preview
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFullscreenPreview: Boolean = false,
    val isApiKeyDialogOpen: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)
    private val apiService = ApiService.create()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val systemPrompt = ChatMessageDto(
        role = "system",
        content = """You are DeepSeek Web Studio AI, a world-class front-end engineer and creative Web developer. 
You communicate naturally in English or Bengali according to user queries.
When user requests a web page, component, app, interactive tool, or game:
1. Provide a brief friendly explanation in English or Bengali.
2. ALWAYS generate the full, self-contained single-file HTML code with CSS embedded in <style> and JavaScript in <script> tags.
3. CRITICAL: Wrap the full HTML code strictly inside ```html ... ``` markdown code blocks so the app can auto-parse and live-preview it instantly."""
    )

    init {
        viewModelScope.launch {
            val savedKey = dataStoreManager.apiKeyFlow.first()
            val savedModel = dataStoreManager.selectedModelFlow.first()
            val savedCode = dataStoreManager.codeStateFlow.first()

            val initialWelcomeMsg = ChatMessage(
                sender = Sender.AI,
                content = "👋 **Welcome to DeepSeek AI Web Code Studio!**\n\nI can generate interactive HTML/CSS/JS single-file web apps, landing pages, and games. Ask me anything in English or Bengali!\n\n*Tip: Set your API Key in the top right key icon if required.*"
            )

            _uiState.update {
                it.copy(
                    apiKey = savedKey,
                    selectedModel = savedModel,
                    codeState = savedCode,
                    messages = listOf(initialWelcomeMsg)
                )
            }
        }
    }

    fun updateApiKey(newKey: String) {
        viewModelScope.launch {
            dataStoreManager.saveApiKey(newKey)
            _uiState.update { it.copy(apiKey = newKey.trim(), isApiKeyDialogOpen = false) }
        }
    }

    fun openApiKeyDialog(open: Boolean) {
        _uiState.update { it.copy(isApiKeyDialogOpen = open) }
    }

    fun updateSelectedModel(model: String) {
        viewModelScope.launch {
            dataStoreManager.saveSelectedModel(model)
            _uiState.update { it.copy(selectedModel = model) }
        }
    }

    fun setCurrentTab(tabIndex: Int) {
        _uiState.update { it.copy(currentTab = tabIndex) }
    }

    fun updateCodeState(newCode: String) {
        viewModelScope.launch {
            dataStoreManager.saveCodeState(newCode)
            _uiState.update { it.copy(codeState = newCode) }
        }
    }

    fun runCodeFromEditor() {
        // Run code moves directly to preview tab
        _uiState.update { it.copy(currentTab = 2) }
    }

    fun resetChat() {
        val welcomeMsg = ChatMessage(
            sender = Sender.AI,
            content = "✨ **Memory Cleared!** Starting a fresh new chat session. What web app or code would you like to build today?"
        )
        _uiState.update {
            it.copy(
                messages = listOf(welcomeMsg),
                errorMessage = null
            )
        }
    }

    fun importCodeFromFile(fileName: String, content: String) {
        updateCodeState(content)
        val systemNotice = ChatMessage(
            sender = Sender.SYSTEM,
            content = "📁 Imported **$fileName** into Editor and loaded into Live Canvas Preview!"
        )
        _uiState.update {
            it.copy(
                messages = it.messages + systemNotice,
                currentTab = 2 // Auto navigate to Preview Tab
            )
        }
    }

    fun toggleFullscreenPreview() {
        _uiState.update { it.copy(isFullscreenPreview = !it.isFullscreenPreview) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        val userMsg = ChatMessage(sender = Sender.USER, content = trimmed)
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val currentMessages = _uiState.value.messages
                val dtos = mutableListOf<ChatMessageDto>()
                dtos.add(systemPrompt)

                // Build context from recent message history (up to last 10 messages)
                currentMessages.takeLast(10).forEach { msg ->
                    when (msg.sender) {
                        Sender.USER -> dtos.add(ChatMessageDto("user", msg.content))
                        Sender.AI -> dtos.add(ChatMessageDto("assistant", msg.content))
                        Sender.SYSTEM -> {}
                    }
                }

                val request = ChatCompletionRequest(
                    model = _uiState.value.selectedModel,
                    messages = dtos,
                    temperature = 0.7,
                    maxTokens = 4096,
                    stream = false
                )

                val rawApiKey = _uiState.value.apiKey
                val authHeader = if (rawApiKey.isNotBlank()) {
                    if (rawApiKey.startsWith("Bearer ")) rawApiKey else "Bearer $rawApiKey"
                } else {
                    "Bearer deepseek-default-key"
                }

                val response = apiService.getChatCompletion(
                    authHeader = authHeader,
                    request = request
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val aiResponseText = responseBody?.choices?.firstOrNull()?.message?.content
                        ?: "No text received from DeepSeek AI."

                    // Regex extract HTML code
                    val extractedHtml = extractCodeBlock(aiResponseText)

                    val aiMsg = ChatMessage(
                        sender = Sender.AI,
                        content = aiResponseText,
                        extractedCode = extractedHtml
                    )

                    var autoSwitchedToPreview = false
                    if (extractedHtml != null && extractedHtml.isNotBlank()) {
                        updateCodeState(extractedHtml)
                        autoSwitchedToPreview = true
                    }

                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + aiMsg,
                            isLoading = false,
                            currentTab = if (autoSwitchedToPreview) 2 else state.currentTab
                        )
                    }

                } else {
                    val errorStr = "API Error ${response.code()}: ${response.errorBody()?.string() ?: response.message()}"
                    val errorMsg = ChatMessage(
                        sender = Sender.AI,
                        content = "❌ **Request Failed**\n$errorStr\n\n*Please verify your API Key and network connection.*",
                        isError = true
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + errorMsg,
                            isLoading = false,
                            errorMessage = errorStr
                        )
                    }
                }

            } catch (e: Exception) {
                val exceptionStr = e.localizedMessage ?: "Network connection error"
                val errorMsg = ChatMessage(
                    sender = Sender.AI,
                    content = "⚡ **Connection Error**\n$exceptionStr\n\n*Check network connection or API URL availability.*",
                    isError = true
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + errorMsg,
                        isLoading = false,
                        errorMessage = exceptionStr
                    )
                }
            }
        }
    }

    private fun extractCodeBlock(response: String): String? {
        // Regex for ```html ... ```
        val htmlFenceRegex = """```html\s*([\s\S]*?)\s*```""".toRegex(RegexOption.IGNORE_CASE)
        val match = htmlFenceRegex.find(response)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        // Generic ``` ... ``` if contains <html> or <!DOCTYPE html>
        val genericFenceRegex = """```\s*([\s\S]*?)\s*```""".toRegex()
        val genericMatches = genericFenceRegex.findAll(response)
        for (gMatch in genericMatches) {
            val code = gMatch.groupValues[1].trim()
            if (code.contains("<html", ignoreCase = true) || code.contains("<!DOCTYPE", ignoreCase = true)) {
                return code
            }
        }

        // Fallback check if response itself contains <html>...</html>
        if (response.contains("<html", ignoreCase = true) && response.contains("</html>", ignoreCase = true)) {
            val startIdx = response.indexOf("<html", ignoreCase = true)
            val endIdx = response.lastIndexOf("</html>", ignoreCase = true) + 7
            if (startIdx >= 0 && endIdx > startIdx) {
                return response.substring(startIdx, endIdx).trim()
            }
        }

        return null
    }
}
