package com.habitstreak.app.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.habitstreak.app.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) : PurchasesUpdatedListener {

    private val TAG = "BillingManager"
    private val PRO_SKU = "habit_streak_pro"

    private var billingClient: BillingClient? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    sealed class PurchaseState {
        object Idle : PurchaseState()
        object Loading : PurchaseState()
        object Success : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        connectToBilling()
    }

    private fun connectToBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    // Query purchases to verify pro status
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
                // Try to reconnect
                connectToBilling()
            }
        })
    }

    suspend fun launchPurchaseFlow(activity: Activity): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                _purchaseState.value = PurchaseState.Loading

                // Query product details
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_SKU)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )

                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

                val productDetailsResult = billingClient?.queryProductDetails(params)

                if (productDetailsResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productDetails = productDetailsResult.productDetailsList?.firstOrNull()

                    if (productDetails != null) {
                        val flowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build()
                                )
                            )
                            .build()

                        val billingResult = billingClient?.launchBillingFlow(activity, flowParams)
                        billingResult?.responseCode == BillingClient.BillingResponseCode.OK
                    } else {
                        _purchaseState.value = PurchaseState.Error("Product not found")
                        false
                    }
                } else {
                    _purchaseState.value = PurchaseState.Error("Failed to load product details")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching purchase flow", e)
                _purchaseState.value = PurchaseState.Error(e.message ?: "Unknown error")
                false
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled purchase")
                _purchaseState.value = PurchaseState.Idle
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned")
                _purchaseState.value = PurchaseState.Success
                scope.launch {
                    preferencesManager.setProVersion(true)
                }
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                scope.launch {
                    preferencesManager.setProVersion(true)
                    _purchaseState.value = PurchaseState.Success
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseState.value = PurchaseState.Loading
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                scope.launch {
                    preferencesManager.setProVersion(true)
                    _purchaseState.value = PurchaseState.Success
                }
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                _purchaseState.value = PurchaseState.Error("Failed to complete purchase")
            }
        }
    }

    fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPro = purchases.any { purchase ->
                    purchase.products.contains(PRO_SKU) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                scope.launch {
                    preferencesManager.setProVersion(hasPro)
                    if (hasPro) {
                        _purchaseState.value = PurchaseState.Success
                    }
                }
            }
        }
    }

    fun cleanup() {
        billingClient?.endConnection()
        billingClient = null
    }
}
