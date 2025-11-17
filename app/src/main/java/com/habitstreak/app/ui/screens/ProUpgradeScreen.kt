package com.habitstreak.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitstreak.app.billing.BillingManager
import com.habitstreak.app.data.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProUpgradeScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    val billingManager = remember { BillingManager(context, preferencesManager) }
    val snackbarHostState = remember { SnackbarHostState() }

    val purchaseState by billingManager.purchaseState.collectAsState()

    // Handle purchase state changes
    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is BillingManager.PurchaseState.Success -> {
                snackbarHostState.showSnackbar("Pro unlocked! Thank you for your support!")
                onNavigateBack()
            }
            is BillingManager.PurchaseState.Error -> {
                val error = (purchaseState as BillingManager.PurchaseState.Error).message
                snackbarHostState.showSnackbar("Purchase failed: $error")
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            billingManager.cleanup()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade to Pro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Pro icon
            Text("⭐", fontSize = 72.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Unlock Pro Features",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Features list
            ProFeature("Unlimited habits")
            Spacer(modifier = Modifier.height(16.dp))
            ProFeature("Custom themes & colors")
            Spacer(modifier = Modifier.height(16.dp))
            ProFeature("Advanced statistics")
            Spacer(modifier = Modifier.height(16.dp))
            ProFeature("Cloud backup & sync")
            Spacer(modifier = Modifier.height(16.dp))
            ProFeature("Priority support")

            Spacer(modifier = Modifier.weight(1f))

            // Price
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "One-time purchase",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        "$1.99",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Purchase button
            Button(
                onClick = {
                    scope.launch {
                        val activity = context as? ComponentActivity
                        if (activity != null) {
                            billingManager.launchPurchaseFlow(activity)
                        } else {
                            snackbarHostState.showSnackbar("Unable to launch purchase flow")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = purchaseState !is BillingManager.PurchaseState.Loading
            ) {
                if (purchaseState is BillingManager.PurchaseState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Unlock Pro", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    billingManager.queryPurchases()
                }
            ) {
                Text("Restore Purchase")
            }
        }
    }
}

@Composable
fun ProFeature(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
