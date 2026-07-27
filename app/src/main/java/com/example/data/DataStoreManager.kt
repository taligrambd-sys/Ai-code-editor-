package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "deepseek_studio_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val CODE_STATE = stringPreferencesKey("code_state")
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }

    val selectedModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL] ?: "deepseek-v4-flash"
    }

    val codeStateFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CODE_STATE] ?: DEFAULT_HTML_TEMPLATE
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key.trim()
        }
    }

    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL] = model
        }
    }

    suspend fun saveCodeState(code: String) {
        context.dataStore.edit { preferences ->
            preferences[CODE_STATE] = code
        }
    }
}

const val DEFAULT_HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DeepSeek AI Canvas</title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
        }
        body {
            background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%);
            color: #f8fafc;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .card {
            background: rgba(30, 41, 59, 0.7);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 20px;
            padding: 32px;
            max-width: 440px;
            width: 100%;
            text-align: center;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
        }
        .badge {
            display: inline-block;
            background: linear-gradient(90deg, #00f2fe, #4facfe);
            color: #0f172a;
            font-size: 12px;
            font-weight: 700;
            padding: 6px 14px;
            border-radius: 20px;
            margin-bottom: 16px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        h1 {
            font-size: 26px;
            font-weight: 700;
            margin-bottom: 12px;
            background: linear-gradient(90deg, #ffffff, #93c5fd);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        p {
            color: #94a3b8;
            font-size: 15px;
            line-height: 1.6;
            margin-bottom: 24px;
        }
        .btn {
            background: linear-gradient(90deg, #00f2fe, #4facfe);
            color: #0f172a;
            border: none;
            padding: 14px 28px;
            border-radius: 12px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
            box-shadow: 0 4px 14px rgba(0, 242, 254, 0.3);
        }
        .btn:active {
            transform: scale(0.96);
        }
        .counter {
            font-size: 32px;
            font-weight: 800;
            color: #38bdf8;
            margin: 16px 0;
        }
    </style>
</head>
<body>
    <div class="card">
        <span class="badge">Live Web Canvas</span>
        <h1>DeepSeek AI Studio</h1>
        <p>Your AI generated web app will appear here instantly! Ask AI in the Chat tab to build apps, games, or interactive widgets.</p>
        <div class="counter" id="clickCount">0</div>
        <button class="btn" onclick="handleClick()">Tap Interactive Demo</button>
    </div>

    <script>
        let count = 0;
        function handleClick() {
            count++;
            document.getElementById('clickCount').innerText = count + ' Taps';
        }
    </script>
</body>
</html>"""
