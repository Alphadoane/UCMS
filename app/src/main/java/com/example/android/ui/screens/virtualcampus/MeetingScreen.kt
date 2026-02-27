package com.example.android.ui.screens.virtualcampus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.delay

@Composable
fun MeetingScreen(
    meetingId: String,
    onLeave: () -> Unit,
    isLecturer: Boolean = true, // Default to true for testing, or pass from nav
    repository: com.example.android.data.repository.VirtualCampusRepository = remember { com.example.android.data.repository.VirtualCampusRepository() }
) {
    val context = LocalContext.current
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }
    val micPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // Agora State
    val remoteUsers by com.example.android.util.AgoraVideoManager.remoteUsers.collectAsState()
    var isMuted by remember { mutableStateOf(false) }
    var isVideoDisabled by remember { mutableStateOf(false) }
    val myAgoraUid = remember { (0..100000).random() }
    
    // Breakout Room State
    val commandEvent by com.example.android.util.AgoraVideoManager.commandEvents.collectAsState()
    var currentRoomId by remember { mutableStateOf(meetingId) }
    var showBreakoutDialog by remember { mutableStateOf(false) }
    var createdRooms by remember { mutableStateOf<Map<String, List<Int>>>(emptyMap()) } // RoomName -> List of UIDs
    var isInBreakoutMode by remember { mutableStateOf(false) }

    // Chat State
    var showChat by remember { mutableStateOf(false) }
    var chatMessage by remember { mutableStateOf("") }
    val messages by com.example.android.util.AgoraVideoManager.incomingMessages.collectAsState()

    DisposableEffect(Unit) {
        val initialized = com.example.android.util.AgoraVideoManager.initialize(context)
        if (initialized) {
            com.example.android.util.AgoraVideoManager.joinChannel(meetingId, myAgoraUid)
            com.example.android.util.AgoraVideoManager.initDataStream() // Init Chat
        }
        onDispose {
            com.example.android.util.AgoraVideoManager.leaveChannel()
            com.example.android.util.AgoraVideoManager.destroy()
        }
    }
    
    // Command Listener
    LaunchedEffect(commandEvent) {
        commandEvent?.let { cmd ->
            if (cmd.startsWith("CMD:BREAKOUT_START:")) {
                // Parse: CMD:BREAKOUT_START:{ "Room 1": [123, 456], "Room 2": [789] }
                // Simplified parsing for demo (assumes simple string format or we use specific delimiter)
                // Real app should use JSON parsing
                try {
                    val payload = cmd.removePrefix("CMD:BREAKOUT_START:")
                    // Mock parsing: We expect user to just know or we iterate. 
                    // For this demo, let's assume payload IS the room name if simplifed, OR we parse a "uid:room" map.
                    // Let's go with a simple "YourRoomName|uid1,uid2,uid3;NextRoom|uid4..." format for manual parsing without heavier JSON lib if needed.
                    // But actually, let's simpler: The command contains "UID:ROOM_NAME" pairs.
                    
                    val parts = payload.split(";")
                    var myAssignedRoom: String? = null
                    
                    parts.forEach { part ->
                        val (uidStr, roomName) = part.split(":")
                        if (uidStr.toIntOrNull() == myAgoraUid) {
                            myAssignedRoom = roomName
                        }
                    }
                    
                    if (myAssignedRoom != null && myAssignedRoom != currentRoomId) {
                        currentRoomId = myAssignedRoom!!
                        com.example.android.util.AgoraVideoManager.switchChannel(currentRoomId, myAgoraUid)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (cmd == "CMD:BREAKOUT_END") {
                if (currentRoomId != meetingId) {
                    currentRoomId = meetingId
                    com.example.android.util.AgoraVideoManager.switchChannel(currentRoomId, myAgoraUid)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Grid Layout for Participants
        Column(modifier = Modifier.fillMaxSize()) {
            // Room Indicator
            Text(
                text = "Room: $currentRoomId", 
                color = Color.White, 
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
            )

            val allParticipants = remember(remoteUsers) {
                val list = mutableListOf<Participant>()
                // Add Remote Users First (Presumed Presenter/Host is usually the first to join or we prioritize them)
                remoteUsers.forEach { uid ->
                    list.add(Participant(uid.toString(), "User $uid", true, Color.DarkGray, isMe = false, agoraUid = uid))
                }
                // Add Local User Last
                list.add(Participant("me", "Me", !isVideoDisabled, Color.Gray, isMe = true, agoraUid = 0))
                list
            }

            Column(modifier = Modifier.weight(1f)) {
                if (allParticipants.isEmpty()) {
                    // Start/Loading state
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Waiting...", color = Color.White)
                    }
                } else if (allParticipants.size == 1) {
                    // Single User - Full Screen
                    ParticipantTile(allParticipants.first(), !isVideoDisabled)
                } else {
                    // Multiple Users - Primary (3/4) and Secondary (1/4)
                    val primaryUser = allParticipants.first()
                    val secondaryUsers = allParticipants.drop(1)
                    
                    // Primary View (3/4 height)
                    Box(modifier = Modifier.weight(0.75f).fillMaxWidth()) {
                        ParticipantTile(primaryUser, !isVideoDisabled)
                        
                        // "Presenter" Label overlay?
                        Text(
                            text = "Presenter",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Red).padding(horizontal = 4.dp)
                        )
                    }
                    
                    // Secondary Strip (1/4 height)
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.weight(0.25f).fillMaxWidth().background(Color.DarkGray)
                    ) {
                        items(secondaryUsers) { p ->
                            Box(modifier = Modifier.width(120.dp).fillMaxHeight().padding(2.dp)) {
                                ParticipantTile(p, !isVideoDisabled)
                            }
                        }
                    }
                }
            }
            
            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute
                 IconButton(
                    onClick = { 
                        isMuted = !isMuted
                        com.example.android.util.AgoraVideoManager.muteLocalAudioStream(isMuted)
                    },
                    modifier = Modifier.background(if (isMuted) Color.White else Color.Gray, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, 
                        contentDescription = "Mute",
                        tint = if (isMuted) Color.Black else Color.White
                    )
                }
                
                // End Call
                IconButton(
                    onClick = { onLeave() },
                    modifier = Modifier.background(Color.Red, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White)
                }

                // Video
                 IconButton(
                    onClick = { 
                        isVideoDisabled = !isVideoDisabled
                        com.example.android.util.AgoraVideoManager.muteLocalVideoStream(isVideoDisabled)
                     },
                    modifier = Modifier.background(if (isVideoDisabled) Color.White else Color.Gray, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        if (isVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam, 
                        contentDescription = "Video",
                        tint = if (isVideoDisabled) Color.Black else Color.White
                    )
                }
                
                // Screen Share
                IconButton(
                    onClick = { 
                         com.example.android.util.AgoraVideoManager.startScreenShare()
                    },
                    modifier = Modifier.background(Color.Gray, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.ScreenShare, contentDescription = "Share Screen", tint = Color.White)
                }
                
                // Chat
                IconButton(
                    onClick = { showChat = !showChat },
                    modifier = Modifier.background(if (showChat) Color.White else Color.Gray, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = if (showChat) Color.Black else Color.White)
                }

                // Breakout Rooms (Lecturer Only)
                if (isLecturer && !isInBreakoutMode) {
                     IconButton(
                        onClick = { showBreakoutDialog = true },
                        modifier = Modifier.background(Color.Blue, androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.GroupWork, contentDescription = "Breakout", tint = Color.White)
                    }
                } else if (isLecturer && isInBreakoutMode) {
                     IconButton(
                        onClick = { 
                            // End Breakout
                            com.example.android.util.AgoraVideoManager.sendChatMessage("CMD:BREAKOUT_END")
                            isInBreakoutMode = false
                            currentRoomId = meetingId
                            com.example.android.util.AgoraVideoManager.switchChannel(currentRoomId, myAgoraUid)
                        },
                        modifier = Modifier.background(Color.Red, androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.GroupOff, contentDescription = "End Breakout", tint = Color.White)
                    }
                }
            }
        }
        
        // Chat Overlay
        if (showChat) {
             Box(
                 modifier = Modifier
                     .align(Alignment.BottomEnd)
                     .padding(bottom = 80.dp, end = 16.dp)
                     .size(width = 300.dp, height = 400.dp)
                     .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
             ) {
                 Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                     Text("Chat", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                     
                     androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
                         items(messages) { (sender, msg) ->
                             Text(text = "$sender: $msg", modifier = Modifier.padding(4.dp))
                         }
                     }
                     
                     Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                         androidx.compose.material3.TextField(
                             value = chatMessage,
                             onValueChange = { chatMessage = it },
                             modifier = Modifier.weight(1f),
                             placeholder = { Text("Message...") }
                         )
                         IconButton(onClick = {
                             if (chatMessage.isNotBlank()) {
                                 com.example.android.util.AgoraVideoManager.sendChatMessage(chatMessage)
                                 chatMessage = ""
                             }
                         }) {
                             Icon(Icons.Default.Send, contentDescription = "Send")
                         }
                     }
                 }
             }
        }

        // Breakout Dialog
        if (showBreakoutDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showBreakoutDialog = false },
                title = { Text("Create Breakout Rooms") },
                text = {
                    Column {
                         Text("Split participants into rooms of max 10 students?")
                         // In a real app, logic to choose N rooms
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = {
                        showBreakoutDialog = false
                        isInBreakoutMode = true
                        
                        // Valid UIDs are in remoteUsers + potentially others we track.
                        // For demo, we just assign currently visible remote users.
                        val users = remoteUsers.toList()
                        
                        // Chunk users into groups of 10
                        val chunks = users.chunked(10)
                        
                        // Build Command string: "UID:ROOM_NAME;UID:ROOM_NAME..."
                        val sb = StringBuilder()
                        
                        chunks.forEachIndexed { index, roomUsers ->
                            val roomName = "Breakout ${index + 1}"
                            roomUsers.forEach { uid ->
                                sb.append("$uid:$roomName;")
                            }
                        }
                        
                        // Lecturer switches to the last room or first room. 
                        // Let's go with "Breakout 1" if exists, or stay.
                        val myStartRoom = if (chunks.isNotEmpty()) "Breakout 1" else meetingId
                        
                        com.example.android.util.AgoraVideoManager.sendChatMessage("CMD:BREAKOUT_START:${sb.toString()}")
                        
                        // Lecturer switches locally
                        if (myStartRoom != meetingId) {
                            currentRoomId = myStartRoom
                            com.example.android.util.AgoraVideoManager.switchChannel(currentRoomId, myAgoraUid)
                        }
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.Button(onClick = { showBreakoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ParticipantTile(p: Participant, isMyVideoOn: Boolean) {
    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        if (p.isMe) {
            if (isMyVideoOn) {
                AndroidView(
                    factory = { ctx ->
                        val view = com.example.android.util.AgoraVideoManager.createVideoView(ctx)
                        com.example.android.util.AgoraVideoManager.setupLocalVideo(view)
                        view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                 Placeholder(p.name)
            }
        } else {
             if (p.agoraUid != null) {
                 AndroidView(
                     factory = { ctx ->
                         val view = com.example.android.util.AgoraVideoManager.createVideoView(ctx)
                         com.example.android.util.AgoraVideoManager.setupRemoteVideo(view, p.agoraUid)
                         view
                     },
                     modifier = Modifier.fillMaxSize()
                 )
             } else {
                 Placeholder(p.name)
            }
        }
        
        Text(
            text = p.name,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).background(Color.Black.copy(alpha = 0.5f)).padding(4.dp)
        )
    }
}

@Composable
fun Placeholder(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
    }
}

data class Participant(
    val id: String, 
    val name: String, 
    val isVideoOn: Boolean, 
    val color: Color, 
    val isMe: Boolean = false,
    val agoraUid: Int? = null
)

// Enum for Connection State
enum class ConnectionState {
    CONNECTING, WAITING_FOR_HOST, CONNECTED, DISCONNECTED
}

