package com.example

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    
    // Core state variables (delegated to Compose via State or live update patterns)
    private var isRecognizerInitialized = false
    private var isTtsInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize SpeechRecognizer
        try {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                isRecognizerInitialized = true
            } else {
                Log.w(TAG, "Speech recognition is not available on this device")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SpeechRecognizer: ${e.message}")
        }

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                textToSpeech?.language = Locale.ENGLISH // Default, will switch dynamically
                Log.d(TAG, "TextToSpeech initialized successfully")
            } else {
                Log.e(TAG, "Failed to initialize TextToSpeech")
            }
        }

        setContent {
            MyApplicationTheme {
                JarvisDashboard(
                    speechRecognizer = speechRecognizer,
                    textToSpeech = textToSpeech,
                    isRecognizerReady = isRecognizerInitialized,
                    isTtsReady = isTtsInitialized,
                    onExecuteAction = { action, query -> executeSystemAction(action, query) }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    /**
     * Executes appropriate Android system intents based on commands parsed by Jarvis.
     */
    private fun executeSystemAction(action: String, query: String?) {
        runOnUiThread {
            try {
                when (action) {
                    "OPEN_YOUTUBE" -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                            setPackage("com.google.android.youtube")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        } else {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }
                        showToast("Opening YouTube, sir.")
                    }
                    "OPEN_GOOGLE" -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        showToast("Opening Google, sir.")
                    }
                    "OPEN_CAMERA" -> {
                        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        showToast("Opening Camera, sir.")
                    }
                    "OPEN_SETTINGS" -> {
                        val intent = Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        showToast("Opening Settings, sir.")
                    }
                    "OPEN_WIFI" -> {
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        showToast("Opening Wi-Fi settings, sir.")
                    }
                    "OPEN_BLUETOOTH" -> {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        showToast("Opening Bluetooth settings, sir.")
                    }
                    "OPEN_VOLUME" -> {
                        val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        showToast("Opening Sound settings, sir.")
                    }
                    "GOOGLE_SEARCH" -> {
                        val searchQuery = query ?: "cats"
                        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                            putExtra(SearchManager.QUERY, searchQuery)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        showToast("Searching Google for '$searchQuery', sir.")
                    }
                    else -> {
                        Log.d(TAG, "No executable intent for action: $action")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute intent action $action: ${e.message}")
                showToast("Cannot open that settings panel on this device, sir.")
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Main JARVIS Composable Dashboard. Handles all state, speech callback, layout, and visual transitions.
 */
@Composable
fun JarvisDashboard(
    speechRecognizer: SpeechRecognizer?,
    textToSpeech: TextToSpeech?,
    isRecognizerReady: Boolean,
    isTtsReady: Boolean,
    onExecuteAction: (String, String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Core states
    var status by remember { mutableStateOf("Ready") } // "Ready", "Listening", "Thinking", "Speaking", "Permission Denied"
    var queryText by remember { mutableStateOf("") }
    var userSpeechInput by remember { mutableStateOf("") }
    var rmsLevel by remember { mutableFloatStateOf(0.1f) }
    
    // Conversation history
    val messages = remember { mutableStateListOf<JarvisMessage>() }
    val listState = rememberLazyListState()
    
    // Language Configuration
    val languageOptions = listOf(
        LanguageConfig("Auto (Multilingual)", "en-IN"),
        LanguageConfig("English", "en-US"),
        LanguageConfig("Hindi (हिन्दी)", "hi-IN"),
        LanguageConfig("Nepali (नेपाली)", "ne-NP"),
        LanguageConfig("Urdu (اردو)", "ur-PK"),
        LanguageConfig("Bengali (বাংলা)", "bn-IN"),
        LanguageConfig("Tamil (தமிழ்)", "ta-IN"),
        LanguageConfig("Telugu (తెలుగు)", "te-IN"),
        LanguageConfig("Marathi (मराठी)", "mr-IN"),
        LanguageConfig("Gujarati (ગુજરાતી)", "gu-IN"),
        LanguageConfig("Punjabi (ਪੰਜਾਬੀ)", "pa-IN")
    )
    var selectedLanguageIndex by remember { mutableIntStateOf(0) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

    // TTS completion listener
    LaunchedEffect(isTtsReady) {
        if (isTtsReady && textToSpeech != null) {
            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    status = "Speaking"
                }

                override fun onDone(utteranceId: String?) {
                    status = "Ready"
                }

                override fun onError(utteranceId: String?) {
                    status = "Ready"
                }
            })
        }
    }

    // Scroll to bottom when message size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for Recording Audio
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            status = "Listening"
            startListeningFlow(
                context = context,
                speechRecognizer = speechRecognizer,
                langCode = languageOptions[selectedLanguageIndex].code,
                onPartialResults = { partial -> userSpeechInput = partial },
                onFinalResults = { finalTranscript ->
                    userSpeechInput = ""
                    if (finalTranscript.trim().isNotEmpty()) {
                        messages.add(JarvisMessage(finalTranscript, isUser = true))
                        scope.launch {
                            status = "Thinking"
                            val apiKey = BuildConfig.GEMINI_API_KEY
                            val answer = JarvisBrain.think(apiKey, finalTranscript, messages)
                            messages.add(JarvisMessage(answer.reply, isUser = false))
                            
                            // Trigger action if detected
                            answer.action?.let { act ->
                                delay(800) // Small cinematic delay for voice responses
                                onExecuteAction(act, answer.actionQuery)
                            }

                            // Trigger speech response
                            speakResponse(textToSpeech, answer.reply, answer.detectedLanguage)
                        }
                    } else {
                        status = "Ready"
                    }
                },
                onRmsChanged = { rms -> rmsLevel = rms },
                onError = { errorMsg ->
                    Log.e("JarvisDashboard", "Speech error: $errorMsg")
                    status = "Ready"
                    rmsLevel = 0.1f
                }
            )
        } else {
            status = "Permission Denied"
            Toast.makeText(context, "Microphone access is required for JARVIS Voice commands", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JarvisDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(JarvisDarkBg, JarvisDarkBg, JarvisSurface)
                    )
                )
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "JARVIS SYSTEM",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = JarvisPrimaryNeon,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Stark Industries OS v4.1",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisTextSecondary
                    )
                }

                // Language Selector Chip
                Box {
                    AssistChip(
                        onClick = { showLanguageDropdown = true },
                        label = {
                            Text(
                                text = languageOptions[selectedLanguageIndex].displayName,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisAccentGlow
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = JarvisPrimaryNeon,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = JarvisCardBg,
                            labelColor = JarvisTextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
                    )

                    DropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false },
                        modifier = Modifier
                            .background(JarvisSurface)
                            .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                    ) {
                        languageOptions.forEachIndexed { idx, config ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = config.displayName,
                                        fontFamily = FontFamily.Monospace,
                                        color = JarvisTextPrimary,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    selectedLanguageIndex = idx
                                    showLanguageDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Divider Line
            Divider(
                color = JarvisBorder.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // Conversation Chat Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit, // Stark / Jarvis reactor style icon
                            contentDescription = "Core Reactor",
                            tint = JarvisPrimaryNeon.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(72.dp)
                                .scale(1.1f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Core Interface Active",
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Say \"YouTube kholo\", \"Open Camera\", \"Google par search karo movies\", or ask me anything. I understand Hindi, English, Punjabi, Bengali, Tamil, Telugu, and Hinglish commands.",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            ChatBubble(message = message)
                        }
                    }
                }
            }

            // Live Speech transcript display
            if (status == "Listening" && userSpeechInput.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisCardBg.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisPrimaryNeon.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = "Listening Icon",
                            tint = JarvisAccentGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userSpeechInput,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = JarvisTextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Waveform and Status Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // JARVIS Status
                Text(
                    text = when (status) {
                        "Listening" -> "• DETECTING SPEECH..."
                        "Thinking" -> "• PROCESSING INPUT..."
                        "Speaking" -> "• TRANSMITTING RESPONSE..."
                        "Permission Denied" -> "• ERROR: MIC UNLOCK NEEDED"
                        else -> "• SYSTEM ACTIVE"
                    },
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when (status) {
                        "Listening" -> JarvisAccentGlow
                        "Thinking" -> JarvisPrimaryNeon
                        "Speaking" -> JarvisSecondaryNeon
                        else -> JarvisTextSecondary
                    },
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Waveform Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (status == "Listening" || status == "Speaking") {
                        JarvisWaveform(rmsLevel = rmsLevel, isSpeaking = status == "Speaking")
                    } else {
                        // Inactive glowing horizontal dashboard index line
                        Canvas(
                            modifier = Modifier
                                .width(120.dp)
                                .height(2.dp)
                        ) {
                            drawLine(
                                color = JarvisBorder,
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
            }

            // Input and Controls Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(bottom = 28.dp, top = 20.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Manual TextInput Fallback (Very robust for testing & emulators)
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = {
                            Text(
                                text = "Ask JARVIS or type commands...",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextSecondary
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisPrimaryNeon,
                            unfocusedBorderColor = JarvisBorder,
                            focusedContainerColor = JarvisDarkBg,
                            unfocusedContainerColor = JarvisDarkBg,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (queryText.isNotEmpty()) {
                                IconButton(onClick = { queryText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = JarvisTextSecondary
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text Submit Button
                    IconButton(
                        onClick = {
                            if (queryText.trim().isNotEmpty()) {
                                val userQuery = queryText
                                queryText = ""
                                messages.add(JarvisMessage(userQuery, isUser = true))
                                scope.launch {
                                    status = "Thinking"
                                    val apiKey = BuildConfig.GEMINI_API_KEY
                                    val answer = JarvisBrain.think(apiKey, userQuery, messages)
                                    messages.add(JarvisMessage(answer.reply, isUser = false))
                                    
                                    // Trigger action if detected
                                    answer.action?.let { act ->
                                        delay(800)
                                        onExecuteAction(act, answer.actionQuery)
                                    }

                                    // Trigger speech response
                                    speakResponse(textToSpeech, answer.reply, answer.detectedLanguage)
                                }
                            }
                        },
                        enabled = queryText.trim().isNotEmpty() || status == "Thinking",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (queryText.isNotEmpty()) JarvisPrimaryNeon else JarvisCardBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (queryText.isNotEmpty()) JarvisDarkBg else JarvisTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Microphone action button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = if (status == "Listening") 1.25f else 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    val glowColor = if (status == "Listening") JarvisAccentGlow else JarvisPrimaryNeon

                    // Animated Mic circle
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        glowColor.copy(alpha = 0.4f),
                                        glowColor.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(2.dp, glowColor, CircleShape)
                            .clickable {
                                if (status == "Listening") {
                                    speechRecognizer?.stopListening()
                                    status = "Ready"
                                    rmsLevel = 0.1f
                                } else {
                                    // Request microphone permission runtime flow
                                    val micPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                    if (micPermission == PackageManager.PERMISSION_GRANTED) {
                                        // Trigger speech recognizer directly
                                        status = "Listening"
                                        startListeningFlow(
                                            context = context,
                                            speechRecognizer = speechRecognizer,
                                            langCode = languageOptions[selectedLanguageIndex].code,
                                            onPartialResults = { partial -> userSpeechInput = partial },
                                            onFinalResults = { finalTranscript ->
                                                userSpeechInput = ""
                                                if (finalTranscript.trim().isNotEmpty()) {
                                                    messages.add(JarvisMessage(finalTranscript, isUser = true))
                                                    scope.launch {
                                                        status = "Thinking"
                                                        val apiKey = BuildConfig.GEMINI_API_KEY
                                                        val answer = JarvisBrain.think(apiKey, finalTranscript, messages)
                                                        messages.add(JarvisMessage(answer.reply, isUser = false))
                                                        
                                                        // Trigger action if detected
                                                        answer.action?.let { act ->
                                                            delay(800)
                                                            onExecuteAction(act, answer.actionQuery)
                                                        }

                                                        // Trigger speech response
                                                        speakResponse(textToSpeech, answer.reply, answer.detectedLanguage)
                                                    }
                                                } else {
                                                    status = "Ready"
                                                }
                                            },
                                            onRmsChanged = { rms -> rmsLevel = rms },
                                            onError = { errorMsg ->
                                                Log.e("JarvisDashboard", "Speech error: $errorMsg")
                                                status = "Ready"
                                                rmsLevel = 0.1f
                                            }
                                        )
                                    } else {
                                        // Trigger Runtime Request
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (status == "Listening") Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Microphone Trigger Button",
                            tint = JarvisTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (status == "Listening") "TAP TO SILENCE" else "TAP MIC TO SPEAK",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * Beautiful styled chat bubble with hologram-card layout.
 */
@Composable
fun ChatBubble(message: JarvisMessage) {
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val glowColor = if (message.isUser) JarvisSecondaryNeon else JarvisPrimaryNeon

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(1.dp, glowColor.copy(alpha = 0.3f), bubbleShape),
            shape = bubbleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) JarvisCardBg.copy(alpha = 0.6f) else JarvisSurface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // Sender label
                Text(
                    text = if (message.isUser) "USER" else "JARVIS",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = glowColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Text text
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * Live audio recording RMS-driven horizontal waveform visualizer.
 */
@Composable
fun JarvisWaveform(rmsLevel: Float, isSpeaking: Boolean) {
    val count = 20
    val infiniteTransition = rememberInfiniteTransition()
    
    // Create random background modulations if we are speaking
    val animFactor by if (isSpeaking) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val gap = 12f
        val barWidth = 6f
        val totalBarWidth = barWidth + gap
        val startX = (width - (count * totalBarWidth - gap)) / 2f

        for (i in 0 until count) {
            // Modulate heights beautifully
            val distanceFromCenter = Math.abs(i - count / 2f) / (count / 2f)
            val centerMultiplier = (1.0f - distanceFromCenter).coerceIn(0.2f, 1.0f)
            
            val amplitude = if (isSpeaking) {
                (centerMultiplier * 20f * animFactor + (i % 3) * 3f)
            } else {
                (rmsLevel * 32f * centerMultiplier + (i % 2) * 2f).coerceIn(4f, 32f)
            }

            val x = startX + i * totalBarWidth
            drawRoundRect(
                color = if (isSpeaking) JarvisSecondaryNeon else JarvisAccentGlow,
                topLeft = Offset(x, midY - amplitude / 2f),
                size = androidx.compose.ui.geometry.Size(barWidth, amplitude),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}

/**
 * Encapsulates language configurations.
 */
data class LanguageConfig(
    val displayName: String,
    val code: String
)

/**
 * Triggers speaking output for replies, auto-configuring local speech engines.
 */
fun speakResponse(tts: TextToSpeech?, text: String, langCode: String) {
    if (tts == null) return
    
    val cleanText = text.replace(Regex("[*#_`{}\\[\\]]"), "") // clean markdowns for perfect reading
    val locale = when {
        langCode.contains("hi") || langCode.contains("Hinglish") -> Locale("hi", "IN")
        langCode.contains("ne") -> Locale("ne", "NP")
        langCode.contains("ur") -> Locale("ur", "PK")
        langCode.contains("bn") -> Locale("bn", "IN")
        langCode.contains("ta") -> Locale("ta", "IN")
        langCode.contains("te") -> Locale("te", "IN")
        langCode.contains("mr") -> Locale("mr", "IN")
        langCode.contains("gu") -> Locale("gu", "IN")
        langCode.contains("pa") -> Locale("pa", "IN")
        else -> Locale.ENGLISH
    }

    try {
        tts.language = locale
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_REPLY")
        }
        tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_REPLY")
    } catch (e: Exception) {
        Log.e("JarvisDashboard", "Failed to speak: ${e.message}")
    }
}

/**
 * Initiates speech recognition request binding.
 */
fun startListeningFlow(
    context: Context,
    speechRecognizer: SpeechRecognizer?,
    langCode: String,
    onPartialResults: (String) -> Unit,
    onFinalResults: (String) -> Unit,
    onRmsChanged: (Float) -> Unit,
    onError: (String) -> Unit
) {
    if (speechRecognizer == null) {
        onError("Speech Recognizer is not available on this device")
        return
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langCode)
        putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf(langCode, "en-IN", "hi-IN"))
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onPartialResults("Listening...")
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {
            // Map decibels to normalized float range
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
            onRmsChanged(normalized)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            val errMsg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions missing"
                SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Speech recognition failed ($error)"
            }
            onError(errMsg)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val transcript = matches?.firstOrNull() ?: ""
            onFinalResults(transcript)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val transcript = matches?.firstOrNull() ?: ""
            if (transcript.isNotEmpty()) {
                onPartialResults(transcript)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    try {
        speechRecognizer.startListening(intent)
    } catch (e: Exception) {
        onError("Failed to start voice listener: ${e.message}")
    }
}

// Keeping the Greeting contract so that GreetingScreenshotTest.kt compiles successfully!
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
