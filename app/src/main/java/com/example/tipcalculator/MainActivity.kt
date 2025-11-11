package com.example.tipcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tipcalculator.ui.theme.TipCalculatorTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TipCalculatorTheme {
                TipCalculatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TipCalculatorApp(){
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) {innerPadding->
        Surface(
            modifier = Modifier
                .padding(
                    innerPadding
                )
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.tertiaryContainer
                ),
            color = MaterialTheme.colorScheme.background
        ) {
            TipTimeLayout()
        }
    }
}

@Composable
fun TipTimeLayout(modifier: Modifier = Modifier){
    var amtInput by remember {  mutableStateOf("")}
    var tipPercentInput by remember {  mutableStateOf("")}
    var switchState by remember { mutableStateOf(false) }
    val amount = amtInput.toDoubleOrNull()?:0.0
    val tipPercent = tipPercentInput.toDoubleOrNull()?:15.0
    val tip = calculateTip(
        amount = amount,
        roundUp = switchState,
        tipPercent = tipPercent
    )
    Column(
        modifier=modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 36.dp)
            .verticalScroll(
                rememberScrollState()
            )
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .align(
                    alignment = Alignment.Start
                )
                .padding(
                    top = 16.dp,
                    bottom = 36.dp
                )
        )

        EditNumberField(
            modifier = modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            onValueChange = {
                amtInput = it
            },
            value = amtInput,
            label = R.string.bill_amount,
            imeAction = ImeAction.Next,
            leadingIcon = R.drawable.money,
            iconDescription = R.string.bill_icon
        )

        EditNumberField(
            modifier = modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            onValueChange = {
                tipPercentInput = it
            },
            value = tipPercentInput,
            label = R.string.tip_percentage,
            imeAction = ImeAction.Done,
            leadingIcon = R.drawable.percent,
            iconDescription = R.string.percent_icon
        )

        RoundTheTipRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 32.dp
                ),
            value = switchState,
            onCheckedChange = {switchState = it}
        )

        Text(
            text = stringResource(R.string.tip_amount, tip),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier.height(150.dp))
    }
}

@Composable
fun EditNumberField(
    modifier: Modifier= Modifier,
    value: String,
    onValueChange: (String)->Unit,
    @DrawableRes leadingIcon: Int,
    imeAction: ImeAction,
    @StringRes label: Int,
    @StringRes iconDescription: Int
){
    OutlinedTextField(
        label = {
            Text(
                stringResource(label)
            )
        },
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        singleLine = true,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = stringResource(iconDescription)
            )
        }
    )
}

@Composable
fun RoundTheTipRow(
    modifier: Modifier = Modifier,
    value: Boolean,
    onCheckedChange: (Boolean)->Unit
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.round_up_tip),
        )
        Switch(
            checked = value,
            onCheckedChange = onCheckedChange
        )
    }
}
@VisibleForTesting
internal fun calculateTip(
    amount: Double,
    tipPercent: Double,
    roundUp: Boolean
): String {
    val tip = tipPercent / 100 * amount
    if(roundUp)
        kotlin.math.ceil(tip)
    val indiaLocale = Locale("en", "IN")
    val formattedTip = NumberFormat.getCurrencyInstance(indiaLocale).format(tip)
    return formattedTip
}
/*
CenterAlignedTopAppBar: A Composable that helps us make top app bars, this one centres the title ,
the navigationIcon to left

and the actions icon to the right:
CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title(centre) = {
                    Text(
                        "Centered Top App Bar",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon(on the left) = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions(On the right) = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                }

Modifier.statusBarsPadding()->
Adds padding equal to the height of the status bar at the top of the screen.
Use Case: Ensures that your content doesn't overlap with the status bar, especially when drawing
edge-to-edge.

Modifier.safeDrawingPadding()
Applies padding to all sides of the content to avoid overlapping with system UI elements like the
status bar, navigation bar, and display cutouts.
Use Case: Ensures that your content remains within the "safe" area, avoiding any system UI elements
that might obscure it.

<string name="tip_amount">Tip Amount: %s</string>
In this when we call stringResource(R.string.tip_amount, "₹0.00"), the "₹0.00" gets placed in the
placeholder %s
 */


