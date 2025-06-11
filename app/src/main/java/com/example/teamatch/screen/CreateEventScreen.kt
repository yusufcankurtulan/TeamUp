package com.example.teamatch.screen

import com.example.teamatch.R
import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.util.LocationHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.time.LocalTime
import java.util.*

@Composable
fun CreateEventScreen(navController: NavController, onBackPressed: () -> Unit, preferencesManager: PreferencesManager) {
    val context = LocalContext.current
    val pitchMap = remember { loadPitchesFromJson(context) }
    val districts = pitchMap.keys.sorted()
    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val baseColor = if (isDarkTheme) Color(0xFF74BBFB) else Color.Black


    var selectedDistrict by rememberSaveable { mutableStateOf("") }
    var selectedPitch by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf("") }
    var teamSize by rememberSaveable { mutableStateOf("") }
    var teamSizeError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        BackgroundFootballIcons(
            preferencesManager = preferencesManager
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = getTranslatedString(R.string.back),
                    tint = baseColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                getTranslatedString(R.string.create_match),
                color = baseColor,
                fontFamily = FontFamily(Font(R.font.winter)),
                fontSize = 35.sp,
                modifier = Modifier.offset(x = 25.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomDropdown(getTranslatedString(R.string.select_district), districts, selectedDistrict, {
                selectedDistrict = it
                selectedPitch = ""
            }, baseColor)

            if (selectedDistrict.isNotEmpty()) {
                val pitches = pitchMap[selectedDistrict] ?: emptyList()
                CustomDropdown(getTranslatedString(R.string.select_pitch), pitches, selectedPitch, {
                    selectedPitch = it
                }, baseColor)
            }

            CustomDatePickerField(getTranslatedString(R.string.select_date), selectedDate, { selectedDate = it }, context, baseColor)

            val currentHour = remember { LocalTime.now().hour }
            val currentMinute = remember { LocalTime.now().minute }

            val rawFirstStart = if (currentMinute > 0) currentHour + 1 else currentHour
            val firstAvailableStart = rawFirstStart.coerceAtMost(23)
            val startOptions = (firstAvailableStart..22).map { String.format("%02d", it) }

            var startTime by rememberSaveable { mutableStateOf(startOptions.firstOrNull() ?: "") }
            var endTime by rememberSaveable { mutableStateOf("") }

            val endOptions = startTime.toIntOrNull()?.let {
                ((it + 1)..23).map { it.toString() }
            } ?: emptyList()

            LaunchedEffect(startTime) {
                val start = startTime.toIntOrNull()
                if (start != null && (endTime.isEmpty() || (endTime.toIntOrNull() ?: 0) <= start)) {
                    val newEnd = (start + 1).coerceAtMost(23)
                    endTime = newEnd.toString()
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 3.dp)
                ) {
                    CustomDropdown(
                        getTranslatedString(R.string.start_time),
                        startOptions.map { "$it:00" },
                        "$startTime:00",
                        {
                            startTime = it.removeSuffix(":00")
                        },
                        baseColor
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                ) {
                    CustomDropdown(
                        getTranslatedString(R.string.end_time),
                        endOptions.map { "$it:00" },
                        if (endTime.isNotEmpty()) "$endTime:00" else "",
                        {
                            endTime = it.removeSuffix(":00")
                        },
                        baseColor
                    )
                }
            }


            CustomRoundedTextField(getTranslatedString(R.string.team_size), teamSize, {
                if (it.isEmpty() || it.toIntOrNull() != null) {
                    teamSize = it
                    teamSizeError = it.toIntOrNull()?.let { num -> num !in 3..11 } == true
                }
            }, if (teamSizeError) Color.Red else baseColor)

            if (teamSizeError) {
                Text(getTranslatedString(R.string.team_size_error), color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
            }

            Spacer(modifier = Modifier.height(5.dp))

            CustomButton(
                text = getTranslatedString(R.string.create_match),
                onClick = {
                    if (selectedPitch.isNotEmpty() && selectedDate.isNotEmpty() && startTime.isNotEmpty() && endTime.isNotEmpty() && teamSize.isNotEmpty()) {
                        LocationHelper(context).createMatchWithCurrentLocation(selectedPitch, selectedDate, startTime, endTime, teamSize) { success ->
                            val message = if (success) context.getString(R.string.match_created) else context.getString(R.string.match_failed)
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                navController.navigate("home/") { popUpTo("create_event") { inclusive = true } }
                            }
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                borderColor = baseColor,
                textColor = baseColor
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CustomRoundedTextField(label: String, value: String, onValueChange: (String) -> Unit, borderColor: Color) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        label = { Text(label, color = borderColor, fontSize = 13.sp, fontWeight = FontWeight.Normal) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = borderColor,
            focusedBorderColor = borderColor,
            unfocusedTextColor = borderColor,
            focusedTextColor = borderColor,
            disabledContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun CustomButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, borderColor: Color, textColor: Color, fontWeight: FontWeight = FontWeight.Normal, fontSize: TextUnit = 18.sp) {
    Button(
        onClick = onClick,
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(35.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = textColor)
    ) {
        Text(text, fontSize = fontSize, fontWeight = fontWeight)
    }
}

@Composable
fun CustomDatePickerField(label: String, selectedDate: String, onDateSelected: (String) -> Unit, context: Context, borderColor: Color) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val datePickerDialog = remember {
        DatePickerDialog(context, { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val formattedDate = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
            onDateSelected(formattedDate)
        }, year, month, day).apply { datePicker.minDate = calendar.timeInMillis }
    }
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = borderColor, fontSize = 13.sp) },
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = getTranslatedString(R.string.select_date), tint = borderColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = borderColor,
                focusedBorderColor = borderColor,
                unfocusedTextColor = borderColor,
                focusedTextColor = borderColor,
                disabledContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(30.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier
            .matchParentSize()
            .clickable { datePickerDialog.show() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdown(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit, borderColor: Color) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = borderColor, fontSize = 13.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = borderColor,
                focusedBorderColor = borderColor,
                unfocusedTextColor = borderColor,
                focusedTextColor = borderColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(30.dp),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .clip(RoundedCornerShape(12.dp))
                .heightIn(max = 300.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = option == selected
                DropdownMenuItem(
                    text = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(text = option, fontSize = 14.sp, color = borderColor, fontWeight = FontWeight.Normal)
                        }
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp)
                )
                if (index < options.size - 1) {
                    Divider(color = borderColor, thickness = 1.dp, modifier = Modifier
                        .width(300.dp)
                        .align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

fun loadPitchesFromJson(context: Context): Map<String, List<String>> {
    val inputStream = context.resources.openRawResource(R.raw.sahalar)
    val reader = InputStreamReader(inputStream)
    val type = object : TypeToken<Map<String, List<String>>>() {}.type
    return Gson().fromJson(reader, type)
}