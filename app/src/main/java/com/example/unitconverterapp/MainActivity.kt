// This is the main package of our app
package com.example.unitconverterapp

// Importing necessary Android and Jetpack Compose libraries
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unitconverterapp.ui.theme.UnitConverterAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

// This is the main Activity where everything starts
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Makes the app go edge to edge on the screen
        setContent {
            // Using our custom theme
            UnitConverterAppTheme {
                // Scaffold gives us structure and padding
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConverterApp(modifier = Modifier.padding(innerPadding)) // Launch main UI
                }
            }
        }
    }
}

// This is the main UI Composable for our app
@Composable
fun ConverterApp(modifier: Modifier = Modifier) {
    // State variables to remember user input and selected options
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("Fläche → Fußballfelder") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedTimeUnit by remember { mutableStateOf("Minuten") }
    var birthDate by remember { mutableStateOf("") }

    // All conversion options the user can choose from
    val options = listOf(
        "Fläche → Fußballfelder",
        "Alter → Minuten",
        "Geld → Zeit",
        "Datum → Zeitraum",
        "Buchstabe → Binär",
        "Buchstabe → Morse"
    )

    // Layout for the app screen
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dropdown to select conversion type
        DropdownMenuBox(
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = { selectedOption = it }
        )

        // Depending on the selected conversion, show the appropriate inputs
        when (selectedOption) {
            "Datum → Zeitraum" -> {
                // Show date pickers for start and end date
                DateInputField("Startdatum", startDate) { startDate = it }
                DateInputField("Enddatum", endDate) { endDate = it }

                // Dropdown to choose time unit (minutes, days, years)
                DropdownMenuBox(
                    options = listOf("Minuten", "Tage", "Jahre"),
                    selectedOption = selectedTimeUnit,
                    onOptionSelected = { selectedTimeUnit = it }
                )
            }

            "Alter → Minuten" -> {
                // Input field for birthdate
                DateInputField("Geburtsdatum", birthDate) { birthDate = it }
            }

            "Buchstabe → Binär" -> {
                // Input for a single character
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Buchstabe") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            "Buchstabe → Morse" -> {
                // Input for Morse code conversion
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Buchstabe") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                // Default input field for area and money
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Eingabe") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Convert button
        Button(
            onClick = {
                // When clicked, convert based on selected option
                result = when (selectedOption) {
                    "Fläche → Fußballfelder" -> convertArea(input)
                    "Alter → Minuten" -> convertAgeToMinutes(birthDate)
                    "Geld → Zeit" -> convertMoneyToTime(input)
                    "Datum → Zeitraum" -> convertDateRange(startDate, endDate, selectedTimeUnit)
                    "Buchstabe → Binär" -> convertLetterToBinary(input)
                    "Buchstabe → Morse" -> convertLetterToMorse(input)
                    else -> "Ungültige Auswahl." // fallback
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Umrechnen") // Button label
        }

        // Show result text
        Text(text = result)
    }
}

// A reusable dropdown menu composable
@Composable
fun DropdownMenuBox(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Show selected value as disabled text field
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Auswahl") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false
        )

        // Dropdown list when clicked
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Date input field with a date picker dialog
@Composable
fun DateInputField(label: String, value: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxWidth().clickable {
        val today = Calendar.getInstance()
        // Opens a DatePickerDialog when clicked
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(formattedDate)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ).show()
    }) {
        // Show selected date in disabled text field
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}

// ---------------------------
// Conversion Functions Below
// ---------------------------

// Converts area in m² to soccer fields
fun convertArea(input: String): String = try {
    val sqm = input.replace(",", ".").toDouble()
    val soccerField = 7140.0
    val fields = sqm / soccerField
    "Das entspricht ca. %.2f Fußballfeldern.".format(fields)
} catch (e: Exception) {
    "Bitte gib eine gültige Fläche in m² ein."
}

// Converts age from birthdate to total minutes
fun convertAgeToMinutes(input: String): String = try {
    val birthDate = LocalDate.parse(input)
    val now = LocalDate.now()
    val minutes = ChronoUnit.MINUTES.between(birthDate.atStartOfDay(), now.atStartOfDay())
    "Das entspricht ca. %,d Minuten.".format(minutes)
} catch (e: Exception) {
    "Bitte gib ein gültiges Geburtsdatum ein (z. B. 2000-01-01)."
}

// Converts money (as seconds) into days and years
fun convertMoneyToTime(input: String): String = try {
    val money = input.replace(".", "").replace(",", ".").toDouble()
    val seconds = money
    val days = seconds / 86400
    val years = seconds / (86400 * 365.25)
    "Das entspricht ca. %.2f Tagen oder %.2f Jahren.".format(days, years)
} catch (e: Exception) {
    "Bitte gib einen gültigen Geldbetrag ein."
}

// Calculates the difference between two dates in minutes, days, or years
fun convertDateRange(startDate: String, endDate: String, timeUnit: String): String {
    return try {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        val daysBetween = ChronoUnit.DAYS.between(start, end)
        val yearsBetween = daysBetween / 365.25
        val minutesBetween = daysBetween * 1440

        when (timeUnit) {
            "Minuten" -> "Das entspricht ca. $minutesBetween Minuten."
            "Tage" -> "Das entspricht ca. $daysBetween Tagen."
            "Jahre" -> "Das entspricht ca. %.2f Jahren.".format(yearsBetween)
            else -> "Ungültige Zeiteinheit."
        }
    } catch (e: Exception) {
        "Bitte gib gültige Daten im Format YYYY-MM-DD ein."
    }
}

// Converts a letter into binary
fun convertLetterToBinary(input: String): String {
    if (input.length == 1) {
        return "Binär: " + Integer.toBinaryString(input[0].code)
    }
    return "Bitte gib nur einen Buchstaben ein."
}

// Converts a letter into Morse code
fun convertLetterToMorse(input: String): String {
    val morseCode = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..", '1' to ".----", '2' to "..---", '3' to "...--",
        '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
        '8' to "---..", '9' to "----.", '0' to "-----"
    )
    val letter = input.uppercase()
    val morse = letter.map { morseCode[it] ?: "?" }.joinToString("   ")
    return "Morsecode: $morse"
}
