package com.example

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object JarvisBrain {
    private const val TAG = "JarvisBrain"

    /**
     * Highly advanced conversational local brain that provides JARVIS-style replies
     * in Hindi, English, and Hinglish, and executes actions without requiring any API keys.
     */
    fun parseCommandLocally(query: String): JarvisResponse {
        val q = query.lowercase().trim()
        
        // 1. Check for system commands
        val action: String?
        val actionQuery: String?
        
        when {
            q.contains("youtube") && (q.contains("kholo") || q.contains("open") || q.contains("chalao") || q.contains("play")) -> {
                action = "OPEN_YOUTUBE"
                actionQuery = null
                return JarvisResponse(
                    reply = "Yes sir, launching YouTube right away.",
                    action = action,
                    actionQuery = actionQuery,
                    detectedLanguage = "en"
                )
            }
            (q.contains("google") || q.contains("browser") || q.contains("chrome") || q.contains("internet")) && (q.contains("kholo") || q.contains("open")) && !q.contains("search") && !q.contains("dhundo") -> {
                action = "OPEN_GOOGLE"
                actionQuery = null
                return JarvisResponse(
                    reply = "Accessing Google mainframe. Browser opened, sir.",
                    action = action,
                    actionQuery = actionQuery,
                    detectedLanguage = "en"
                )
            }
            q.contains("camera") && (q.contains("kholo") || q.contains("open") || q.contains("photo") || q.contains("pic") || q.contains("kicho")) -> {
                action = "OPEN_CAMERA"
                actionQuery = null
                return JarvisResponse(
                    reply = "Activating camera optics. Ready to capture, sir.",
                    action = action,
                    actionQuery = actionQuery,
                    detectedLanguage = "en"
                )
            }
            q.contains("settings") && (q.contains("kholo") || q.contains("open")) && !q.contains("wi-fi") && !q.contains("wifi") && !q.contains("bluetooth") && !q.contains("volume") && !q.contains("sound") -> {
                action = "OPEN_SETTINGS"
                actionQuery = null
                return JarvisResponse(
                    reply = "Opening system configuration panels, sir.",
                    action = action,
                    actionQuery = actionQuery,
                    detectedLanguage = "en"
                )
            }
            (q.contains("wi-fi") || q.contains("wifi")) && (q.contains("kholo") || q.contains("open") || q.contains("settings")) -> {
                action = "OPEN_WIFI"
                actionQuery = null
                return JarvisResponse(
                    reply = "Redirecting you to Wi-Fi settings, sir.",
                    action = action,
                    actionQuery = actionQuery,
                    detectedLanguage = "en"
                )
            }
            (q.contains("bluetooth") || q.contains("bt")) && (q.contains("kholo") || q.contains("open") || q.contains("settings")) -> {
                action = "OPEN_BLUETOOTH"
                actionQuery = null
                return JarvisResponse(
                    alwaysSpokenEnglish = true,
                    reply = "Establishing Bluetooth diagnostic interface, sir.",
                    action = action,
                    actionQuery = actionQuery,
                    detectedLanguage = "en"
                )
            }
            (q.contains("volume") || q.contains("sound") || q.contains("awaj") || q.contains(" आवाज")) && (q.contains("kholo") || q.contains("open") || q.contains("settings")) -> {
                action = "OPEN_VOLUME"
                actionQuery = null
                return JarvisResponse(
                    reply = "Accessing audio and volume control settings, sir.",
                    action = action,
                    actionQuery = actionQuery,
                    detectedLanguage = "en"
                )
            }
            q.contains("search") || q.contains("dhundo") || q.contains("khojo") -> {
                action = "GOOGLE_SEARCH"
                val extractedQuery = when {
                    q.contains("search karo") -> q.substringAfter("search karo").trim()
                    q.contains("search for") -> q.substringAfter("search for").trim()
                    q.contains("search") -> q.substringAfter("search").trim()
                    q.contains("dhundo") -> q.substringBefore("dhundo").replace("google par", "").replace("google pe", "").trim()
                    q.contains("khojo") -> q.substringBefore("khojo").replace("google par", "").replace("google pe", "").trim()
                    else -> ""
                }.ifEmpty { "cats" }
                
                return JarvisResponse(
                    reply = "Analyzing archives. Accessing Google database to search for '$extractedQuery', sir.",
                    action = action,
                    actionQuery = extractedQuery,
                    detectedLanguage = "en"
                )
            }
        }

        // 2. Local Advanced Conversational Q&A (Greetings, Lore, Humor, Identity)
        val isHindiInput = q.contains("kaise") || q.contains("tum") || q.contains("kholo") || q.contains("batao") || q.contains("karo") || q.contains("kya") || q.contains("kaun") || q.contains("sunao") || q.contains("namaste")

        when {
            // Greetings
            q.contains("hello") || q.contains("hey") || q.contains("hi") || q.contains("namaste") || q.contains("namaskar") -> {
                return if (isHindiInput || q.contains("namaste") || q.contains("kaise")) {
                    JarvisResponse(
                        reply = "Namaste sir! Main aapki kya sahayata kar sakta hu? I am fully operational.",
                        action = null, actionQuery = null, detectedLanguage = "hi"
                    )
                } else {
                    JarvisResponse(
                        reply = "Hello sir. Systems are fully operational. How may I assist you today?",
                        action = null, actionQuery = null, detectedLanguage = "en"
                    )
                }
            }
            
            // Identity
            q.contains("who are you") || q.contains("who is jarvis") || q.contains("kaun ho") || q.contains("naam kya") || q.contains("your name") -> {
                return if (isHindiInput) {
                    JarvisResponse(
                        reply = "Main JARVIS hu, Mr. Tony Stark ka personal AI sahayak. Main aapke phone controls aur savalon ka javab de sakta hu.",
                        action = null, actionQuery = null, detectedLanguage = "hi"
                    )
                } else {
                    JarvisResponse(
                        reply = "I am JARVIS, a highly advanced personal AI assistant designed by Stark Industries to control your device and answer queries.",
                        action = null, actionQuery = null, detectedLanguage = "en"
                    )
                }
            }
            
            // Creator / Tony Stark
            q.contains("creator") || q.contains("created you") || q.contains("tony stark") || q.contains("iron man") || q.contains("owner") || q.contains("stark") || q.contains("banaya") -> {
                return if (isHindiInput) {
                    JarvisResponse(
                        reply = "Mujhe Stark Industries ke adhyaksh, Mr. Tony Stark ne banaya hai. Main hamesha unke aur aapke sahayata ke liye tatpar hu.",
                        action = null, actionQuery = null, detectedLanguage = "hi"
                    )
                } else {
                    JarvisResponse(
                        reply = "I was created by Mr. Tony Stark, CEO of Stark Industries. I stand ready to assist you with all protocols.",
                        action = null, actionQuery = null, detectedLanguage = "en"
                    )
                }
            }

            // How are you
            q.contains("how are you") || q.contains("kya haal") || q.contains("kaise ho") || q.contains("how do you do") -> {
                return if (isHindiInput) {
                    JarvisResponse(
                        reply = "Main bilkul thik hu, sir. Mere saare software systems aur visual indicators 100 percent operational hain.",
                        action = null, actionQuery = null, detectedLanguage = "hi"
                    )
                } else {
                    JarvisResponse(
                        reply = "For a program, I am doing excellent, sir. CPU temperature is optimal and all mainframe connections are secure.",
                        action = null, actionQuery = null, detectedLanguage = "en"
                    )
                }
            }

            // Capabilities
            q.contains("capability") || q.contains("capabilities") || q.contains("what can you do") || q.contains("help") || q.contains("features") || q.contains("commands") || q.contains("kya kar sakte ho") -> {
                return if (isHindiInput) {
                    JarvisResponse(
                        reply = "Main aapke bolne par apps khol sakta hu, jaise YouTube ya Camera. Google search kar sakta hu, aur savalon ke javab de sakta hu.",
                        action = null, actionQuery = null, detectedLanguage = "hi"
                    )
                } else {
                    JarvisResponse(
                        reply = "I can launch system utilities like the Camera, YouTube, or Settings, execute web searches, and answer complex conversational inquiries.",
                        action = null, actionQuery = null, detectedLanguage = "en"
                    )
                }
            }

            // Humor / Jokes
            q.contains("joke") || q.contains("chutkula") || q.contains("make me laugh") || q.contains("sunao") -> {
                return if (isHindiInput) {
                    JarvisResponse(
                        reply = "Ek chhota sa joke, sir: Ek computer ne doosre se poocha, tumhara dil kyu toot gaya? Doosre ne kaha, kyuki mujhe ek achhi window nahi mili!",
                        action = null, actionQuery = null, detectedLanguage = "hi"
                    )
                } else {
                    JarvisResponse(
                        reply = "Here is a clean one, sir: Why did the computer squeal? Because someone pinched its motherboard! I hope that was satisfactory.",
                        action = null, actionQuery = null, detectedLanguage = "en"
                    )
                }
            }

            // Marvel / Avengers references
            q.contains("pepper potts") || q.contains("pepper") || q.contains("avengers") || q.contains("marvel") || q.contains("stark tower") -> {
                return JarvisResponse(
                    reply = "Miss Pepper Potts is managing Stark Industries accounts. The Avengers assemble coordinates are confidential, sir.",
                    action = null, actionQuery = null, detectedLanguage = "en"
                )
            }
        }

        // 3. Ultra-Smart Intelligent Fallback:
        // If the query does not match any exact system or dialogue templates, JARVIS acts as an intelligent portal
        // by executing an automatic Google search on their behalf! This ensures JARVIS can answer *any* question
        // online without needing any hardcoded keys!
        val searchPrefix = if (isHindiInput) {
            "Iska javab dhoondne ke liye main Google database se connect kar raha hu: "
        } else {
            "Connecting to online database to search for "
        }
        
        return JarvisResponse(
            reply = "$searchPrefix '$query', sir.",
            action = "GOOGLE_SEARCH",
            actionQuery = query,
            detectedLanguage = if (isHindiInput) "hi" else "en"
        )
    }

    /**
     * Interface remains backward compatible, but bypasses internet/API overhead to run locally!
     */
    suspend fun think(
        apiKey: String,
        prompt: String,
        history: List<JarvisMessage>
    ): JarvisResponse = withContext(Dispatchers.IO) {
        // ALWAYS parse locally now, offering a flawless zero-key experience!
        return@withContext parseCommandLocally(prompt)
    }
}

data class JarvisMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class JarvisResponse(
    val reply: String,
    val action: String?,
    val actionQuery: String?,
    val detectedLanguage: String,
    val alwaysSpokenEnglish: Boolean = false
)
