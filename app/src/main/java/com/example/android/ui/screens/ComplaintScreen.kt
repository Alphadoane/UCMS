package com.example.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.data.repository.SupportRepository
import com.example.android.data.repository.AuthRepository
import com.example.android.data.model.Ticket
import com.example.android.data.model.TicketMessage
import kotlinx.coroutines.launch

@Composable
fun ComplaintScreen() {
    val context = LocalContext.current
    val repository = remember { SupportRepository(context) }
    var currentScreen by remember { mutableStateOf("list") }
    var selectedTicketId by remember { mutableStateOf<String?>(null) }
    
    // User role check (simplified, ideally passed down)
    val authRepo = remember { AuthRepository(context) }
    // We'd fetch this properly, but for now we rely on backend logic for what tickets we receive
    
    when (currentScreen) {
        "list" -> {
            ComplaintListScreen(
                repository = repository,
                onTicketClick = { id -> 
                    selectedTicketId = id
                    currentScreen = "chat"
                }
            )
        }
        "chat" -> {
            if (selectedTicketId != null) {
                ComplaintChatScreen(
                    ticketId = selectedTicketId!!,
                    repository = repository,
                    onBack = { 
                        selectedTicketId = null
                        currentScreen = "list" 
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintListScreen(
    repository: SupportRepository,
    onTicketClick: (String) -> Unit
) {
    var complaints by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Refresh Logic
    val refresh = {
        scope.launch {
            isLoading = true
            complaints = repository.getSupportTickets(null)
            isLoading = false
        }
    }
    
    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = Color(0xFF1D3762)) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Support Tickets", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (complaints.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No tickets found.") }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(complaints) { ticket ->
                        TicketItem(ticket, onClick = { onTicketClick(ticket["id"] as String) })
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateTicketDialog(repository = repository, onDismiss = { showCreateDialog = false }, onSuccess = { refresh() })
    }
}

@Composable
fun TicketItem(ticket: Map<String, Any>, onClick: () -> Unit) {
    val status = ticket["status"] as String
    val color = when(status.lowercase()) {
        "resolved" -> Color(0xFF4CAF50)
        "open" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ticket["title"] as String, fontWeight = FontWeight.Bold)
                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(status.uppercase(), color = color, modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(ticket["description"] as String, maxLines = 1, color = Color.Gray)
            Text((ticket["created_at"] as String).take(10), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintChatScreen(
    ticketId: String,
    repository: SupportRepository,
    onBack: () -> Unit
) {
    var ticket by remember { mutableStateOf<Ticket?>(null) }
    var messages by remember { mutableStateOf<List<TicketMessage>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Polling or refresh
    val refresh = {
        scope.launch {
            val result = repository.getTicketDetails(ticketId)
            if (result.isSuccess) {
                val data = result.getOrNull()!!
                ticket = data.first
                messages = data.second
            }
        }
    }
    
    LaunchedEffect(ticketId) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(ticket?.title ?: "Loading...", style = MaterialTheme.typography.titleMedium)
                        Text(ticket?.status ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    // Admin Status Actions (Simplified: Just Toggle Resolved/Open)
                    if (ticket != null) {
                        if (ticket!!.status == "open") {
                            TextButton(onClick = {
                                scope.launch {
                                    repository.updateTicketStatus(ticketId, "resolved")
                                    refresh()
                                }
                            }) { Text("Resolve") }
                        } else {
                            TextButton(onClick = {
                                scope.launch {
                                    repository.updateTicketStatus(ticketId, "open")
                                    refresh()
                                }
                            }) { Text("Reopen") }
                        }
                    }
                }
            )
        },
        bottomBar = {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                IconButton(
                    onClick = {
                        if (newMessage.isNotBlank()) {
                            isSending = true
                            scope.launch {
                                repository.postMessage(ticketId, newMessage)
                                newMessage = ""
                                isSending = false
                                refresh()
                            }
                        }
                    },
                    enabled = newMessage.isNotBlank() && !isSending
                ) {
                    if (isSending) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Icon(Icons.AutoMirrored.Filled.Send, null)
                }
            }
        }
    ) { padding ->
        LazyColumn(
             modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
             verticalArrangement = Arrangement.spacedBy(16.dp),
             reverseLayout = true // Chat style
        ) {
            items(messages.reversed()) { msg ->
                ChatBubble(msg)
            }
            item { 
                if (ticket != null) {
                    Text(
                        "Ticket Created: ${ticket!!.description}", 
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: TicketMessage) {
    val isMe = !msg.is_admin // Assumption: If I am student, admin is 'other'. If I am admin, this logic needs work.
    // Ideally we key off 'currentUserId'. For now, assuming distinct separation.
    // Actually, backend 'is_admin' field tells us who sent it.
    // If we are logged in as Admin, 'is_admin=true' means "Me".
    // If we are logged in as Student, 'is_admin=true' means "Other".
    // This is tricky without knowing current user role in UI.
    // Let's rely on Sender Name vs "Me"? Or just right/left alignment based on context.
    
    // Better Approach: Just show name.
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.is_admin) Alignment.Start else Alignment.End // Admin left, Student right? Or generic.
    ) {
         Surface(
             color = if (msg.is_admin) Color(0xFFE0E0E0) else Color(0xFFBBDEFB),
             shape = RoundedCornerShape(8.dp)
         ) {
             Column(modifier = Modifier.padding(8.dp)) {
                 Text(msg.sender_name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                 Text(msg.message)
                 Text(msg.created_at.take(16).replace("T", " "), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
             }
         }
    }
}

@Composable
fun CreateTicketDialog(
    repository: SupportRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Academic") }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Ticket") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                // Category simplified
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    repository.createSupportTicket(title, desc, cat, "medium")
                    onSuccess()
                    onDismiss()
                }
            }) { Text("Submit") }
        }
    )
}
