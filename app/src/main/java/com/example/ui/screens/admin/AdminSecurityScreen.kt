package com.example.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSecurityScreen(
    onChangePin: (currentPin: String, newPin: String, confirmPin: String) -> Result<Unit>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var showCurrentPin by remember { mutableStateOf(false) }
    var showNewPin by remember { mutableStateOf(false) }
    var showConfirmPin by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "एडमिन सुरक्षा व पिन प्रबंधन",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Admin Security & PIN Management",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("admin_security_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = OliveContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = OliveTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "सुरक्षित पिन नीति (Security Guidelines)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnOliveContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• पिन 4 से 6 अंकों (0-9) का होना आवश्यक है\n• पिन साल्टेड SHA-256 हैश द्वारा सुरक्षित रूप से एन्क्रिप्टेड है\n• किसी के साथ अपना एडमिन पिन साझा न करें",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Success Message Banner
            if (successMessage != null) {
                item {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = successMessage ?: "",
                                color = Color(0xFF1B5E20),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Error Message Banner
            if (errorMessage != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // PIN Change Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "एडमिन पिन बदलें (Change Admin PIN)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // 1. Current PIN
                        OutlinedTextField(
                            value = currentPin,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                    currentPin = it
                                    errorMessage = null
                                    successMessage = null
                                }
                            },
                            label = { Text("वर्तमान पिन (Current PIN) *") },
                            placeholder = { Text("वर्तमान 4-6 अंकों का पिन") },
                            leadingIcon = {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = OliveTertiary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPin = !showCurrentPin }) {
                                    Icon(
                                        imageVector = if (showCurrentPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            },
                            visualTransformation = if (showCurrentPin) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("current_pin_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 2. New PIN
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                    newPin = it
                                    errorMessage = null
                                    successMessage = null
                                }
                            },
                            label = { Text("नया पिन (New PIN - 4 to 6 digits) *") },
                            placeholder = { Text("नया सुरक्षित पिन") },
                            leadingIcon = {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = SaffronPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showNewPin = !showNewPin }) {
                                    Icon(
                                        imageVector = if (showNewPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            },
                            visualTransformation = if (showNewPin) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_pin_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 3. Confirm New PIN
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                    confirmPin = it
                                    errorMessage = null
                                    successMessage = null
                                }
                            },
                            label = { Text("नए पिन की पुष्टि करें (Confirm New PIN) *") },
                            placeholder = { Text("नया पिन पुनः दर्ज करें") },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = SaffronDark)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPin = !showConfirmPin }) {
                                    Icon(
                                        imageVector = if (showConfirmPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            },
                            visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val result = onChangePin(currentPin, newPin, confirmPin)
                                    result.onSuccess {
                                        successMessage = "एडमिन पिन सफलतापूर्वक अपडेट कर दिया गया है!"
                                        currentPin = ""
                                        newPin = ""
                                        confirmPin = ""
                                    }.onFailure { error ->
                                        errorMessage = error.message ?: "पिन अपडेट करने में विफल।"
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_pin_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                val result = onChangePin(currentPin, newPin, confirmPin)
                                result.onSuccess {
                                    successMessage = "एडमिन पिन सफलतापूर्वक अपडेट कर दिया गया है!"
                                    errorMessage = null
                                    currentPin = ""
                                    newPin = ""
                                    confirmPin = ""
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "पिन अपडेट करने में विफल।"
                                    successMessage = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("change_pin_button")
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("पिन अपडेट करें (Save New PIN)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
