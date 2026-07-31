package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WhitelistedDeviceEntity
import com.example.ui.TrackerViewModel
import com.example.ui.theme.*

@Composable
fun WhitelistScreen(
    viewModel: TrackerViewModel
) {
    val whitelist by viewModel.whitelistedDevices.collectAsState()
    var inputMac by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Text(
            text = "Trusted Devices Whitelist",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelPurplePrimary
        )
        Text(
            text = "Whitelisted devices (e.g. smartwatch, earbuds) will never trigger stalker alerts.",
            fontSize = 12.sp,
            color = SentinelTextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Whitelist Entry Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SentinelCardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ADD TRUSTED DEVICE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentinelTextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Device Name (e.g. My Wireless Buds)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("whitelist_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SentinelPurplePrimary,
                        unfocusedBorderColor = SentinelTextMuted,
                        focusedLabelColor = SentinelPurplePrimary,
                        unfocusedLabelColor = SentinelTextMuted,
                        focusedTextColor = SentinelTextPrimary,
                        unfocusedTextColor = SentinelTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputMac,
                    onValueChange = { inputMac = it },
                    label = { Text("Bluetooth MAC Address (e.g. AA:BB:CC:11:22:33)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("whitelist_mac_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SentinelPurplePrimary,
                        unfocusedBorderColor = SentinelTextMuted,
                        focusedLabelColor = SentinelPurplePrimary,
                        unfocusedLabelColor = SentinelTextMuted,
                        focusedTextColor = SentinelTextPrimary,
                        unfocusedTextColor = SentinelTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (inputMac.isNotBlank()) {
                            viewModel.whitelistDevice(
                                inputMac.trim(),
                                if (inputName.isNotBlank()) inputName.trim() else "Trusted Device ($inputMac)"
                            )
                            inputMac = ""
                            inputName = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_whitelist_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SentinelPurplePrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = androidx.compose.ui.graphics.Color.Black)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("ADD TO TRUSTED LIST", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = androidx.compose.ui.graphics.Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Whitelisted Devices Header
        Text(
            text = "TRUSTED DEVICES (${whitelist.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelTextMuted,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (whitelist.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No trusted devices added yet.\nAdd your own headphones, watch, or laptop above.",
                    fontSize = 12.sp,
                    color = SentinelTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(whitelist, key = { it.macAddress }) { device ->
                    WhitelistedDeviceCard(
                        device = device,
                        onRemove = { viewModel.removeFromWhitelist(device.macAddress) }
                    )
                }
            }
        }
    }
}

@Composable
fun WhitelistedDeviceCard(
    device: WhitelistedDeviceEntity,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("whitelist_card_${device.macAddress}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelCardBg)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SentinelSuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Trusted",
                        tint = SentinelSuccessGreen
                    )
                }

                Column {
                    Text(
                        text = device.deviceName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SentinelTextPrimary
                    )
                    Text(
                        text = "MAC: ${device.macAddress}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SentinelTextMuted
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.testTag("remove_whitelist_${device.macAddress}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = SentinelAlertText
                )
            }
        }
    }
}
