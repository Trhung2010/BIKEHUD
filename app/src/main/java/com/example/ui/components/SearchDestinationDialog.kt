package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.HudColorTheme
import com.example.util.GoogleMapsHelper

@Composable
fun SearchDestinationDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    colorTheme: HudColorTheme,
    onSelectCustomDestination: (String) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var queryText by remember { mutableStateOf("") }

    val quickDestinations = listOf(
        Pair("Sân bay Tân Sơn Nhất", "Ga Quốc tế / Quốc nội, Tân Bình, TP.HCM"),
        Pair("Chợ Bến Thành", "Đường Lê Lợi, Bến Thành, Quận 1"),
        Pair("Phố Đi Bộ Nguyễn Huệ", "Quận 1, TP. Hồ Chí Minh"),
        Pair("Hồ Gươm (Hồ Hoàn Kiếm)", "Hoàn Kiếm, Hà Nội"),
        Pair("Cầu Rồng Đà Nẵng", "Đường Nguyễn Văn Linh, Đà Nẵng"),
        Pair("Quảng Trường Lâm Viên", "Đà Lạt, Lâm Đồng"),
        Pair("Ngọn Hải Đăng Vũng Tàu", "Núi Nhỏ, TP. Vũng Tàu")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, colorTheme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("search_dest_dialog"),
            color = Color(0xFF0C101C)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A73E8).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tìm Kiếm Google Maps",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Field
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("Nhập địa chỉ hoặc tên địa điểm...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_dest_text_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = colorTheme.primaryColor,
                        unfocusedBorderColor = Color(0xFF334155),
                        cursorColor = colorTheme.primaryColor
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            if (queryText.isNotBlank()) {
                                onSelectCustomDestination(queryText.trim())
                                onDismiss()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons: Navigate HUD or Launch Google Maps directly
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                onSelectCustomDestination(queryText.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = colorTheme.primaryColor.copy(alpha = 0.25f),
                            contentColor = colorTheme.primaryColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dẫn Đường HUD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    ElevatedButton(
                        onClick = {
                            val target = if (queryText.isNotBlank()) queryText.trim() else "Sân bay Tân Sơn Nhất"
                            GoogleMapsHelper.startGoogleMapsNavigation(context, target, isTwoWheeler = true)
                            onDismiss()
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF1A73E8),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mở Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ĐỊA ĐIỂM XE MÁY PHỔ BIẾN",
                    color = colorTheme.primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(quickDestinations) { (place, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF131A29))
                                .clickable {
                                    onSelectCustomDestination(place)
                                    onDismiss()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = desc,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
