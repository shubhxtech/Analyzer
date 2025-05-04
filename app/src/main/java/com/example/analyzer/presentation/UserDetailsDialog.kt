package com.example.analyzer.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.analyzer.remote.TongueAnalysisResponse
import com.example.analyzer.remote.UserProfileViewModel
import com.example.analyzer.remote.roomdatabase.UserProfilesRepository


data class UserProfile(
    val name: String,
    val age: String,
    val gender: String,
    val tongueAnalysisHistory: MutableMap<String, TongueAnalysisResponse> = mutableMapOf()
)




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    userProfilesRepository: UserProfilesRepository,
    viewModel : UserProfileViewModel
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")

    // State for showing/hiding recent profiles
    var showRecentProfiles by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            if (showRecentProfiles) {
                RecentProfilesView(
                    profiles = viewModel.profileList,
                    onProfileSelected = { profile ->
                        name = profile.name
                        age = profile.age
                        gender = profile.gender
                        showRecentProfiles = false
                    },
                    onBack = { showRecentProfiles = false }
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "User Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Age field
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gender dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.2f
                                ),
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        expanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
//                        HorizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        PrimaryButton(
                            onClick = {
                                if (name.isNotBlank() && age.isNotBlank() && gender.isNotBlank()) {
                                    onConfirm(name, age, gender)
                                }

                            },
                            text = "Continue"
                        )


                    Spacer(modifier = Modifier.height(8.dp))

                        // Select Recent Button
                        if (viewModel.profileList.isNotEmpty()) {
                            SecondaryButton(
                                text = "Select Recent",
                                onClick = { showRecentProfiles = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentProfilesView(
    profiles: List<UserProfile>,
    onProfileSelected: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                // You'd normally use an icon here, but since we're keeping it
                // minimal with no icon dependencies, we'll use text
                Text("<", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Recent Profiles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            items(profiles) { profile ->
                ProfileItem(
                    profile = profile,
                    onClick = { onProfileSelected(profile) }
                )

                if (profile != profiles.last()) {
                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileItem(
    profile: UserProfile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${profile.age} years • ${profile.gender}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
