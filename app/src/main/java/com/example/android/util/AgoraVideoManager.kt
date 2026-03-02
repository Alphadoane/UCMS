package com.example.android.util

import android.content.Context
import android.view.SurfaceView
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manages Agora RTC Engine for real-time video.
 * Acts as a wrapper to isolate the heavy SDK logic.
 */
object AgoraVideoManager {
    
    // REPLACE WITH YOUR REAL AGORA APP ID
    private const val APP_ID = "993dc359746745f797dbf56740ff3b0f" 
    
    private var rtcEngine: RtcEngine? = null
    private var appContext: Context? = null
    
    // State to hold set of active remote UIDs
    private val _remoteUsers = MutableStateFlow<Set<Int>>(emptySet())
    val remoteUsers = _remoteUsers.asStateFlow()
    
    // Chat State
    private val _incomingMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList()) // UserID -> Message
    val incomingMessages = _incomingMessages.asStateFlow()
    private var dataStreamId: Int = -1

    // Breakout Room Command Events
    private val _commandEvents = MutableStateFlow<String?>(null)
    val commandEvents = _commandEvents.asStateFlow()

    private val eventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            _remoteUsers.value += uid
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            _remoteUsers.value -= uid
        }
        
        override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
            super.onStreamMessage(uid, streamId, data)
            if (data != null) {
                val message = String(data, Charsets.UTF_8)
                if (message.startsWith("CMD:")) {
                    _commandEvents.value = message
                } else {
                    _incomingMessages.value += (uid.toString() to message)
                }
            }
        }
    }

    fun initialize(context: Context): Boolean {
        if (APP_ID == "YOUR_AGORA_APP_ID" || APP_ID.isEmpty()) return false
        
        try {
            val config = io.agora.rtc2.RtcEngineConfig()
            config.mContext = context.applicationContext
            config.mAppId = APP_ID
            config.mEventHandler = eventHandler
            rtcEngine = RtcEngine.create(config)
            rtcEngine?.enableVideo()
            this.appContext = context.applicationContext
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun joinChannel(channelName: String, uid: Int) {
        val context = appContext ?: return
        
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var token: String? = null
            
            // Try to fetch an Agora token from the backend.
            // The project runs in App ID Only mode, so the backend returns token=null.
            // We iterate over several base URLs (ngrok → emulator → LAN) for resilience.
            val baseUrls = listOf(
                "https://entreatingly-commonable-georgann.ngrok-free.dev/api/",
                "http://10.0.2.2:8000/api/",
                "http://10.21.167.10:8000/api/"
            )
            
            try {
                val userPrefs = com.example.android.data.prefs.UserPrefs(context)
                val jwtToken = userPrefs.authToken.first()
                
                val encodedChannel = java.net.URLEncoder.encode(channelName, "UTF-8")

                for (base in baseUrls) {
                    try {
                        val urlString = "${base}virtual/agora-token?channelName=$encodedChannel&uid=$uid"
                        val url = java.net.URL(urlString)
                        val connection = url.openConnection() as java.net.HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connectTimeout = 4000
                        connection.readTimeout = 4000
                        if (!jwtToken.isNullOrBlank()) {
                            connection.setRequestProperty("Authorization", "Bearer $jwtToken")
                        }
                        
                        if (connection.responseCode == 200) {
                            val response = connection.inputStream.bufferedReader().use { it.readText() }
                            // Extract token (may be null in App ID Only mode)
                            val match = "\"token\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(response)
                            token = match?.groupValues?.get(1) // remains null if backend returns null
                            break // success — stop trying other URLs
                        }
                    } catch (_: java.io.IOException) {
                        // Try next URL
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Join channel — token is null in App ID Only mode, which Agora accepts
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                rtcEngine?.joinChannel(token, channelName, "Optional Info", uid)
            }
        }
    }
    
    fun switchChannel(newChannel: String, uid: Int) {
        rtcEngine?.leaveChannel()
        _remoteUsers.value = emptySet() // Clear remote users
        joinChannel(newChannel, uid)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    fun setupLocalVideo(view: SurfaceView) {
        val canvas = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0)
        rtcEngine?.setupLocalVideo(canvas)
        rtcEngine?.startPreview()
    }

    fun setupRemoteVideo(view: SurfaceView, uid: Int) {
        val canvas = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid)
        rtcEngine?.setupRemoteVideo(canvas)
    }

    fun muteLocalAudioStream(muted: Boolean) {
        rtcEngine?.muteLocalAudioStream(muted)
    }

    fun muteLocalVideoStream(muted: Boolean) {
        rtcEngine?.muteLocalVideoStream(muted)
        if (muted) {
            rtcEngine?.stopPreview()
        } else {
            rtcEngine?.startPreview()
        }
    }
    
    fun createVideoView(context: Context): SurfaceView {
        val view = SurfaceView(context)
        view.setZOrderMediaOverlay(true)
        return view
    }
    

    // ... video methods ...

    fun startScreenShare() {
        val parameters = io.agora.rtc2.ScreenCaptureParameters()
        parameters.captureAudio = true
        parameters.captureVideo = true
        val videoCaptureParameters = io.agora.rtc2.ScreenCaptureParameters.VideoCaptureParameters()
        parameters.videoCaptureParameters = videoCaptureParameters
        
        rtcEngine?.startScreenCapture(parameters)
    }

    fun stopScreenShare() {
        rtcEngine?.stopScreenCapture()
        // Restore camera
        rtcEngine?.startPreview()
    }
    
    fun initDataStream() {
        if (dataStreamId == -1) {
            val config = io.agora.rtc2.DataStreamConfig()
            config.ordered = true
            config.syncWithAudio = false
            dataStreamId = rtcEngine?.createDataStream(config) ?: -1
        }
    }
    
    fun sendChatMessage(message: String) {
        if (dataStreamId != -1) {
             rtcEngine?.sendStreamMessage(dataStreamId, message.toByteArray(Charsets.UTF_8))
             // Add to local list too (only for regular chats, commands are hidden)
             if (!message.startsWith("CMD:")) {
                _incomingMessages.value += ("Me" to message)
             }
        }
    }

    fun destroy() {
        RtcEngine.destroy()
        rtcEngine = null
    }
}
